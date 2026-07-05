import { describe, it, expect, beforeEach, vi } from 'vitest'

// axios is both callable (axios(config) retries the request) and has .post / .interceptors.
vi.mock('axios', () => {
  const axiosFn = vi.fn()
  axiosFn.post = vi.fn()
  axiosFn.interceptors = { response: { use: vi.fn() } }
  return { default: axiosFn }
})

import axios from 'axios'
import { installAuthRefresh, startProactiveRefresh } from '../../src/utils/authRefresh'

const REFRESH_URL = '/api/refresh/'

function clearCookies() {
  document.cookie.split(';').forEach((c) => {
    const name = c.split('=')[0].trim()
    if (name) document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT`
  })
}

// Install the interceptor and return the onRejected handler it registered.
function getRejectionHandler() {
  installAuthRefresh()
  return axios.interceptors.response.use.mock.calls.at(-1)[1]
}

describe('installAuthRefresh', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    clearCookies()
    // authRefresh redirects via window.location.assign on refresh failure.
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { assign: vi.fn(), href: 'http://localhost/' }
    })
  })

  it('refreshes the session and retries the original request on a 401', async () => {
    axios.post.mockResolvedValue({})
    axios.mockResolvedValue('RETRIED')
    const onRejected = getRejectionHandler()
    const error = { response: { status: 401 }, config: { url: '/api/health' } }

    const result = await onRejected(error)

    expect(axios.post).toHaveBeenCalledWith(REFRESH_URL)
    expect(axios).toHaveBeenCalledWith(error.config)
    expect(error.config._retried).toBe(true)
    expect(result).toBe('RETRIED')
  })

  it('does not refresh on non-401 errors', async () => {
    const onRejected = getRejectionHandler()
    const error = { response: { status: 500 }, config: { url: '/api/health' } }

    await expect(onRejected(error)).rejects.toBe(error)
    expect(axios.post).not.toHaveBeenCalled()
  })

  it('does not retry a request that was already retried', async () => {
    const onRejected = getRejectionHandler()
    const error = { response: { status: 401 }, config: { url: '/api/health', _retried: true } }

    await expect(onRejected(error)).rejects.toBe(error)
    expect(axios.post).not.toHaveBeenCalled()
  })

  it('does not attempt to refresh a failed refresh call itself', async () => {
    const onRejected = getRejectionHandler()
    const error = { response: { status: 401 }, config: { url: '/api/refresh/' } }

    await expect(onRejected(error)).rejects.toBe(error)
    expect(axios.post).not.toHaveBeenCalled()
  })

  it('redirects home when the refresh itself fails', async () => {
    axios.post.mockRejectedValue(new Error('refresh boom'))
    const onRejected = getRejectionHandler()
    const error = { response: { status: 401 }, config: { url: '/api/health' } }

    await expect(onRejected(error)).rejects.toBe(error)
    expect(window.location.assign).toHaveBeenCalledWith('/')
  })

  it('shares a single in-flight refresh across concurrent 401s', async () => {
    let resolvePost
    axios.post.mockReturnValue(new Promise((resolve) => (resolvePost = resolve)))
    axios.mockResolvedValue('ok')
    const onRejected = getRejectionHandler()

    const first = onRejected({ response: { status: 401 }, config: { url: '/api/a' } })
    const second = onRejected({ response: { status: 401 }, config: { url: '/api/b' } })
    resolvePost({})
    await Promise.all([first, second])

    expect(axios.post).toHaveBeenCalledTimes(1)
  })
})

describe('startProactiveRefresh', () => {
  beforeEach(() => {
    vi.resetAllMocks()
    clearCookies()
  })

  it('refreshes on the interval while logged in', async () => {
    vi.useFakeTimers()
    try {
      document.cookie = 'user_name=erin'
      axios.post.mockResolvedValue({})

      startProactiveRefresh()
      await vi.advanceTimersByTimeAsync(13 * 60 * 1000)

      expect(axios.post).toHaveBeenCalledWith(REFRESH_URL)
    } finally {
      vi.useRealTimers()
    }
  })

  it('does not refresh when logged out', async () => {
    vi.useFakeTimers()
    try {
      axios.post.mockResolvedValue({})

      startProactiveRefresh()
      await vi.advanceTimersByTimeAsync(13 * 60 * 1000)

      expect(axios.post).not.toHaveBeenCalled()
    } finally {
      vi.useRealTimers()
    }
  })
})
