import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PanelDetail from '../../src/components/PanelDetail.vue'

const stubs = {
  'va-chip': { template: '<span class="va-chip"><slot /></span>' }
}

function mountDetail(panel) {
  return mount(PanelDetail, { props: { panel }, global: { stubs } })
}

describe('PanelDetail', () => {
  it('renders the headline', () => {
    const wrapper = mountDetail({ headline: 'all good', details: {} })
    expect(wrapper.find('.headline').text()).toBe('all good')
  })

  it('shows a placeholder when there are no detail sections', () => {
    const wrapper = mountDetail({ headline: 'h', details: {} })
    expect(wrapper.find('.empty').text()).toBe('No additional detail.')
  })

  it('treats a missing details map as empty', () => {
    const wrapper = mountDetail({ headline: 'h' })
    expect(wrapper.find('.empty').text()).toBe('No additional detail.')
  })

  it('renders a section title from each details key', () => {
    const wrapper = mountDetail({ headline: 'h', details: { suites: [{ name: 'a' }] } })
    expect(wrapper.find('.section-title').text()).toBe('suites')
  })

  it('renders arrays of objects as a table', () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { suites: [{ name: 'a', ok: true }, { name: 'b', ok: false }] }
    })
    const table = wrapper.find('table.detail-table')
    expect(table.exists()).toBe(true)
    expect(table.findAll('tbody tr')).toHaveLength(2)
  })

  it('builds table columns from the union of keys across rows', () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ a: 1 }, { b: 2 }, { a: 3, c: 4 }] }
    })
    const headers = wrapper.findAll('th').map((th) => th.text())
    expect(headers).toEqual(['a', 'b', 'c'])
  })

  it('stringifies object-valued cells', () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ meta: { x: 1 } }] }
    })
    expect(wrapper.find('tbody td').text()).toBe('{"x":1}')
  })

  it('renders blank for null/undefined cells', () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ a: null }] }
    })
    expect(wrapper.find('tbody td').text()).toBe('')
  })

  it('renders a primitive list as chips', () => {
    const wrapper = mountDetail({ headline: 'h', details: { unrouted: ['trade', 'login'] } })
    const chips = wrapper.findAll('.va-chip')
    expect(chips).toHaveLength(2)
    expect(chips.map((c) => c.text())).toEqual(['trade', 'login'])
  })

  it('shows "none" for an empty primitive list', () => {
    const wrapper = mountDetail({ headline: 'h', details: { unrouted: [] } })
    expect(wrapper.findAll('.va-chip')).toHaveLength(0)
    expect(wrapper.find('.section .empty').text()).toBe('none')
  })

  it('renders a scalar detail value directly', () => {
    const wrapper = mountDetail({ headline: 'h', details: { count: 42 } })
    const section = wrapper.find('.section')
    expect(section.text()).toContain('42')
  })
})
