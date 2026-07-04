import axios from 'axios'
import { isLoggedIn } from './auth'

// Session tokens live 15 minutes; refresh a couple of minutes early.
const PROACTIVE_INTERVAL_MS = 13 * 60 * 1000
const REFRESH_URL = '/api/refresh/'

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
        window.location.assign('/')
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
