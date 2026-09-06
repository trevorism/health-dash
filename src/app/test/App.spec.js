import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import App from '../src/App.vue'

const auth = vi.hoisted(() => ({ session: null, login: vi.fn() }))

vi.mock('@trevorism/ui-auth', async () => {
  const { reactive, computed } = await import('vue')
  auth.session = reactive({ authenticated: false, loading: false })
  return {
    useAuth: () => ({
      user: computed(() => (auth.session.authenticated ? { username: 'tester' } : null)),
      isAuthenticated: computed(() => auth.session.authenticated),
      isAdmin: computed(() => false),
      loading: computed(() => auth.session.loading),
      ready: Promise.resolve(),
      login: auth.login,
      logout: vi.fn()
    })
  }
})

vi.mock('@trevorism/ui-header-bar', () => ({
  MenuBar: { name: 'MenuBar', template: '<nav class="menu-bar" />' }
}))

const get = vi.hoisted(() => vi.fn())
vi.mock('axios', () => ({ default: { get } }))

const stubs = {
  'va-progress-circle': { template: '<span class="spinner" />' },
  'va-button': { template: '<button><slot /></button>' },
  'va-modal': { template: '<div class="va-modal"><slot /></div>' },
  'health-tile': { props: ['panel'], template: '<div class="tile">{{ panel.key }}</div>' },
  'panel-detail': { template: '<div />' }
}

const POLL_INTERVAL_MS = 15 * 1000

// Every App watches the same module-level session, so one left mounted keeps
// polling into the next test.
const mounted = []

function mountApp() {
  const wrapper = mount(App, { global: { stubs } })
  mounted.push(wrapper)
  return wrapper
}

function signIn() {
  auth.session.authenticated = true
}

describe('App', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    get.mockReset()
    get.mockResolvedValue({ data: [{ key: 'data' }, { key: 'auth' }] })
    auth.login.mockClear()
    auth.session.authenticated = false
    auth.session.loading = false
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop().unmount()
    }
    vi.useRealTimers()
  })

  it('waits for the session before deciding you are signed out', () => {
    auth.session.loading = true

    const wrapper = mountApp()

    expect(wrapper.text()).toContain('Checking your session')
    expect(wrapper.text()).not.toContain('Please log in')
  })

  it('offers a sign in to an anonymous visitor and asks for no health data', () => {
    const wrapper = mountApp()

    expect(wrapper.text()).toContain('Please log in')
    expect(get).not.toHaveBeenCalled()
  })

  it('starts the login handoff from the sign in button', async () => {
    const wrapper = mountApp()

    await wrapper.find('button').trigger('click')

    expect(auth.login).toHaveBeenCalledTimes(1)
  })

  it('reads health from an absolute path so the interceptor recognises it', async () => {
    signIn()
    mountApp()
    await flushPromises()

    expect(get).toHaveBeenCalledWith('/api/health')
  })

  it('renders a tile per panel once signed in', async () => {
    signIn()
    const wrapper = mountApp()
    await flushPromises()

    expect(wrapper.findAll('.tile')).toHaveLength(2)
  })

  it('starts polling when a session arrives, without a reload', async () => {
    const wrapper = mountApp()
    expect(get).not.toHaveBeenCalled()

    signIn()
    await flushPromises()

    expect(get).toHaveBeenCalledTimes(1)
    expect(wrapper.findAll('.tile')).toHaveLength(2)
  })

  it('keeps polling on the interval', async () => {
    signIn()
    mountApp()
    await flushPromises()
    expect(get).toHaveBeenCalledTimes(1)

    await vi.advanceTimersByTimeAsync(POLL_INTERVAL_MS)

    expect(get).toHaveBeenCalledTimes(2)
  })

  it('stops polling and clears the board when the session ends', async () => {
    signIn()
    const wrapper = mountApp()
    await flushPromises()
    const callsWhileSignedIn = get.mock.calls.length

    auth.session.authenticated = false
    await flushPromises()

    expect(wrapper.findAll('.tile')).toHaveLength(0)
    expect(wrapper.text()).toContain('Please log in')

    await vi.advanceTimersByTimeAsync(POLL_INTERVAL_MS * 3)
    expect(get).toHaveBeenCalledTimes(callsWhileSignedIn)
  })

  it('keeps the last known panels when a poll fails', async () => {
    signIn()
    const wrapper = mountApp()
    await flushPromises()

    get.mockRejectedValueOnce(new Error('boom'))
    await vi.advanceTimersByTimeAsync(POLL_INTERVAL_MS)

    expect(wrapper.findAll('.tile')).toHaveLength(2)
  })

  it('stops polling when the app goes away', async () => {
    signIn()
    const wrapper = mountApp()
    await flushPromises()
    const callsWhileMounted = get.mock.calls.length

    wrapper.unmount()
    await vi.advanceTimersByTimeAsync(POLL_INTERVAL_MS * 3)

    expect(get).toHaveBeenCalledTimes(callsWhileMounted)
  })
})
