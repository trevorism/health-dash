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
    const headers = wrapper.findAll('th .col-label').map((label) => label.text())
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

  function columnValues(wrapper, index) {
    return wrapper.findAll('tbody tr').map((tr) => tr.findAll('td')[index].text())
  }

  const namedRows = {
    headline: 'h',
    details: { suites: [{ name: 'b' }, { name: 'c' }, { name: 'a' }] }
  }

  it('leaves rows in source order until a header is clicked', () => {
    const wrapper = mountDetail(namedRows)
    expect(columnValues(wrapper, 0)).toEqual(['b', 'c', 'a'])
  })

  it('sorts ascending on the first header click', async () => {
    const wrapper = mountDetail(namedRows)
    await wrapper.find('th').trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['a', 'b', 'c'])
  })

  it('sorts descending on the second header click', async () => {
    const wrapper = mountDetail(namedRows)
    await wrapper.find('th').trigger('click')
    await wrapper.find('th').trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['c', 'b', 'a'])
  })

  it('restores the source order on the third header click', async () => {
    const wrapper = mountDetail(namedRows)
    await wrapper.find('th').trigger('click')
    await wrapper.find('th').trigger('click')
    await wrapper.find('th').trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['b', 'c', 'a'])
  })

  it('reports the sort direction through aria-sort', async () => {
    const wrapper = mountDetail(namedRows)
    const header = wrapper.find('th')
    expect(header.attributes('aria-sort')).toBe('none')
    await header.trigger('click')
    expect(header.attributes('aria-sort')).toBe('ascending')
    await header.trigger('click')
    expect(header.attributes('aria-sort')).toBe('descending')
  })

  it('sorts by keyboard activation of a header', async () => {
    const wrapper = mountDetail(namedRows)
    await wrapper.find('th').trigger('keydown.enter')
    expect(columnValues(wrapper, 0)).toEqual(['a', 'b', 'c'])
  })

  it('starts ascending when switching to a different column', async () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ a: 2, b: 'z' }, { a: 1, b: 'y' }] }
    })
    const headers = wrapper.findAll('th')
    await headers[0].trigger('click')
    await headers[0].trigger('click')
    await headers[1].trigger('click')
    expect(headers[0].attributes('aria-sort')).toBe('none')
    expect(headers[1].attributes('aria-sort')).toBe('ascending')
    expect(columnValues(wrapper, 1)).toEqual(['y', 'z'])
  })

  it('sorts numbers numerically rather than lexically', async () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ n: 10 }, { n: 9 }, { n: 100 }] }
    })
    await wrapper.find('th').trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['9', '10', '100'])
  })

  it('sorts booleans with false before true', async () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ ok: true }, { ok: false }, { ok: true }] }
    })
    await wrapper.find('th').trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['false', 'true', 'true'])
  })

  it('keeps missing values last in both directions', async () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: { rows: [{ a: 'b' }, { z: 1 }, { a: 'a' }] }
    })
    const header = wrapper.findAll('th')[0]
    await header.trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['a', 'b', ''])
    await header.trigger('click')
    expect(columnValues(wrapper, 0)).toEqual(['b', 'a', ''])
  })

  it('sorts each section table independently', async () => {
    const wrapper = mountDetail({
      headline: 'h',
      details: {
        first: [{ v: 'b' }, { v: 'a' }],
        second: [{ v: 'd' }, { v: 'c' }]
      }
    })
    const tables = wrapper.findAll('table.detail-table')
    await tables[0].find('th').trigger('click')
    expect(tables[0].findAll('tbody td').map((td) => td.text())).toEqual(['a', 'b'])
    expect(tables[1].findAll('tbody td').map((td) => td.text())).toEqual(['d', 'c'])
  })

  it('clears sorting when the panel changes', async () => {
    const wrapper = mountDetail(namedRows)
    await wrapper.find('th').trigger('click')
    await wrapper.setProps({
      panel: { headline: 'h2', details: { suites: [{ name: 'y' }, { name: 'x' }] } }
    })
    expect(columnValues(wrapper, 0)).toEqual(['y', 'x'])
    expect(wrapper.find('th').attributes('aria-sort')).toBe('none')
  })
})
