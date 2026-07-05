import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import HealthTile from '../../src/components/HealthTile.vue'

// Vuestic components are globally registered in the app; stub the ones we assert on
// so we can inspect the props/slots HealthTile hands them.
const stubs = {
  'va-card': { template: '<div class="va-card"><slot /></div>' },
  'va-card-title': { template: '<div class="va-card-title"><slot /></div>' },
  'va-card-content': { template: '<div class="va-card-content"><slot /></div>' },
  'va-icon': { props: ['name', 'color'], template: '<i class="va-icon" :data-name="name" :data-color="color" />' },
  'va-chip': { template: '<span class="va-chip"><slot /></span>' }
}

function mountTile(panel) {
  return mount(HealthTile, { props: { panel }, global: { stubs } })
}

describe('HealthTile status mapping', () => {
  const cases = [
    ['OK', 'check_circle', 'success'],
    ['WARNING', 'warning', 'warning'],
    ['ERROR', 'cancel', 'danger'],
    ['UNKNOWN', 'help', 'secondary']
  ]

  cases.forEach(([status, icon, color]) => {
    it(`maps ${status} to icon=${icon} color=${color}`, () => {
      const wrapper = mountTile({ key: 'k', title: 'T', status })
      const iconEl = wrapper.find('.va-icon')
      expect(iconEl.attributes('data-name')).toBe(icon)
      expect(iconEl.attributes('data-color')).toBe(color)
    })
  })

  it('falls back to UNKNOWN styling for an unrecognized status', () => {
    const wrapper = mountTile({ key: 'k', title: 'T', status: 'BOGUS' })
    const iconEl = wrapper.find('.va-icon')
    expect(iconEl.attributes('data-name')).toBe('help')
    expect(iconEl.attributes('data-color')).toBe('secondary')
  })
})

describe('HealthTile rendering', () => {
  it('renders the panel title and headline', () => {
    const wrapper = mountTile({ key: 'k', title: 'Test Suites', status: 'OK', headline: '5/5 passing' })
    expect(wrapper.text()).toContain('Test Suites')
    expect(wrapper.text()).toContain('5/5 passing')
  })

  it('shows a LIVE chip for realtime panels', () => {
    const wrapper = mountTile({ key: 'k', title: 'T', status: 'OK', mode: 'realtime' })
    expect(wrapper.find('.va-chip').exists()).toBe(true)
    expect(wrapper.find('.va-chip').text()).toBe('LIVE')
  })

  it('hides the LIVE chip for historical panels', () => {
    const wrapper = mountTile({ key: 'k', title: 'T', status: 'OK', mode: 'historical' })
    expect(wrapper.find('.va-chip').exists()).toBe(false)
  })

  it('labels the timestamp "as of" for historical panels', () => {
    const wrapper = mountTile({ key: 'k', title: 'T', status: 'OK', mode: 'historical', lastUpdated: '2026-07-04T12:00:00Z' })
    expect(wrapper.find('.updated').text()).toContain('as of')
  })

  it('labels the timestamp "updated" for realtime panels', () => {
    const wrapper = mountTile({ key: 'k', title: 'T', status: 'OK', mode: 'realtime', lastUpdated: '2026-07-04T12:00:00Z' })
    expect(wrapper.find('.updated').text()).toContain('updated')
  })

  it('omits the timestamp line when lastUpdated is missing', () => {
    const wrapper = mountTile({ key: 'k', title: 'T', status: 'OK' })
    expect(wrapper.find('.updated').exists()).toBe(false)
  })
})

describe('HealthTile interaction', () => {
  it('emits select with the panel when clicked', async () => {
    const panel = { key: 'k', title: 'T', status: 'OK' }
    const wrapper = mountTile(panel)

    await wrapper.find('.va-card').trigger('click')

    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')[0][0]).toEqual(panel)
  })
})
