import axios from 'axios'
import { isLoggedIn } from './auth'

// Session tokens live 15 minutes; refresh a couple of minutes early.
const PROACTIVE_INTERVAL_MS = 13 * 60 * 1000
const REFRESH_URL = '/api/refresh/'
const LOGIN_URL = 'https://login.auth.trevorism.com'

// Single-flight guard so concurrent 401s share one in-flight refresh.
let refreshPromise = null
function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = axios.post(REFRESH_URL).finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

// When a refresh can't recover the session, the user must re-authenticate.
// Reloading the dashboard would just 401 again (an infinite refresh loop), so
// drop the stale identity cookie and hand off to the central login page, which
// returns here once authenticated. The guard keeps concurrent 401s from each
// firing a navigation.
let redirecting = false
function redirectToLogin() {
  if (redirecting) return
  redirecting = true
  // user_name is set on .trevorism.com in prod; also clear the host-only
  // variant so local dev doesn't loop.
  document.cookie = 'user_name=; Max-Age=0; path=/; domain=.trevorism.com'
  document.cookie = 'user_name=; Max-Age=0; path=/'
  window.location.assign(`${LOGIN_URL}?return_url=${encodeURIComponent(window.location.href)}`)
}

export function installAuthRefresh() {
  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const { response, config } = error
      const retryable =
        response?.status === 401 && config && !config._retried && !config.url?.includes('/api/refresh')
      if (!retryable) return Promise.reject(error)

      config._retried = true
      try {
        await refreshSession()
      } catch {
        redirectToLogin()
        return Promise.reject(error)
      }
      return axios(config)
    }
  )
}

// Renew the session on a timer while logged in; the 401 interceptor is the net.
export function startProactiveRefresh() {
  setInterval(() => {
    if (isLoggedIn()) refreshSession().catch(() => {})
  }, PROACTIVE_INTERVAL_MS)
}
