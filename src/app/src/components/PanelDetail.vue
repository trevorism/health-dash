<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  panel: { type: Object, required: true }
})

const sortState = reactive({})

watch(
  () => props.panel,
  () => {
    for (const key of Object.keys(sortState)) delete sortState[key]
  }
)

// details is a map of section-name -> value; render each section generically.
const sections = computed(() => Object.entries(props.panel.details || {}))

function isObjectRows(value) {
  return Array.isArray(value) && value.length > 0 && typeof value[0] === 'object' && value[0] !== null
}

function isPrimitiveList(value) {
  return Array.isArray(value) && (value.length === 0 || typeof value[0] !== 'object')
}

function columns(rows) {
  const keys = []
  for (const row of rows) {
    for (const key of Object.keys(row)) {
      if (!keys.includes(key)) keys.push(key)
    }
  }
  return keys
}

function cell(value) {
  if (value === null || value === undefined) return ''
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function toggleSort(section, column) {
  const current = sortState[section]
  if (!current || current.column !== column) {
    sortState[section] = { column, direction: 'asc' }
  } else if (current.direction === 'asc') {
    sortState[section] = { column, direction: 'desc' }
  } else {
    delete sortState[section]
  }
}

function sortDirection(section, column) {
  const current = sortState[section]
  return current && current.column === column ? current.direction : null
}

function ariaSort(section, column) {
  const direction = sortDirection(section, column)
  if (direction === 'asc') return 'ascending'
  if (direction === 'desc') return 'descending'
  return 'none'
}

function sortRank(value) {
  if (value === null || value === undefined || value === '') return 2
  return 1
}

function compareValues(left, right) {
  if (typeof left === 'boolean' || typeof right === 'boolean') {
    return Number(left) - Number(right)
  }

  const leftNumber = Number(left)
  const rightNumber = Number(right)
  if (!Number.isNaN(leftNumber) && !Number.isNaN(rightNumber)) {
    return leftNumber - rightNumber
  }

  return cell(left).localeCompare(cell(right), undefined, { numeric: true, sensitivity: 'base' })
}

function sortedRows(section, rows) {
  const current = sortState[section]
  if (!current) return rows

  const factor = current.direction === 'desc' ? -1 : 1
  return [...rows].sort((a, b) => {
    const left = a[current.column]
    const right = b[current.column]
    const rankDifference = sortRank(left) - sortRank(right)
    if (rankDifference !== 0) return rankDifference
    if (sortRank(left) === 2) return 0
    return factor * compareValues(left, right)
  })
}
</script>

<template>
  <div class="detail">
    <p class="headline">{{ panel.headline }}</p>

    <div v-if="sections.length === 0" class="empty">No additional detail.</div>

    <div v-for="[name, value] in sections" :key="name" class="section">
      <h3 class="section-title">{{ name }}</h3>

      <table v-if="isObjectRows(value)" class="detail-table">
        <thead>
          <tr>
            <th
              v-for="col in columns(value)"
              :key="col"
              class="sortable"
              :aria-sort="ariaSort(name, col)"
              tabindex="0"
              @click="toggleSort(name, col)"
              @keydown.enter.prevent="toggleSort(name, col)"
              @keydown.space.prevent="toggleSort(name, col)"
            >
              <span class="col-label">{{ col }}</span>
              <span class="sort-indicator" :class="{ active: sortDirection(name, col) }">
                {{ sortDirection(name, col) === 'desc' ? '▼' : '▲' }}
              </span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, i) in sortedRows(name, value)" :key="i">
            <td v-for="col in columns(value)" :key="col">{{ cell(row[col]) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-else-if="isPrimitiveList(value)" class="chips">
        <va-chip v-for="(item, i) in value" :key="i" size="small" outline>{{ item }}</va-chip>
        <span v-if="value.length === 0" class="empty">none</span>
      </div>

      <div v-else>{{ cell(value) }}</div>
    </div>
  </div>
</template>

<style scoped>
.headline {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}
.section {
  margin-bottom: 1.5rem;
}
.section-title {
  font-weight: 600;
  text-transform: capitalize;
  margin-bottom: 0.5rem;
}
.detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}
.detail-table th,
.detail-table td {
  border: 1px solid var(--va-background-border);
  padding: 0.35rem 0.5rem;
  text-align: left;
  vertical-align: top;
}
.detail-table th {
  background: var(--va-background-secondary);
}
.detail-table th.sortable {
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
}
.detail-table th.sortable:hover {
  background: var(--va-background-element);
}
.sort-indicator {
  font-size: 0.7em;
  margin-left: 0.25rem;
  opacity: 0;
}
.detail-table th.sortable:hover .sort-indicator {
  opacity: 0.4;
}
.sort-indicator.active {
  opacity: 1;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}
.empty {
  color: var(--va-secondary);
}
</style>
