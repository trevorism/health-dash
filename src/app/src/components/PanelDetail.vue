<script setup>
import { computed } from 'vue'

const props = defineProps({
  panel: { type: Object, required: true }
})

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
            <th v-for="col in columns(value)" :key="col">{{ col }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, i) in value" :key="i">
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
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}
.empty {
  color: var(--va-secondary);
}
</style>
