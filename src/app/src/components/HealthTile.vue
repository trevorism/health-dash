<script setup>
import { computed } from 'vue'

// panel is one HealthPanel from GET /api/health:
// { key, title, status, headline, mode, lastUpdated }
const props = defineProps({
  panel: { type: Object, required: true }
})

const emit = defineEmits(['select'])

const STATUS = {
  OK: { icon: 'check_circle', color: 'success' },
  WARNING: { icon: 'warning', color: 'warning' },
  ERROR: { icon: 'cancel', color: 'danger' },
  UNKNOWN: { icon: 'help', color: 'secondary' }
}

const meta = computed(() => STATUS[props.panel.status] || STATUS.UNKNOWN)
const isRealtime = computed(() => props.panel.mode === 'realtime')
const updated = computed(() =>
  props.panel.lastUpdated ? new Date(props.panel.lastUpdated).toLocaleTimeString() : ''
)
</script>

<template>
  <va-card class="health-tile" @click="emit('select', panel)">
    <va-card-title>
      <va-icon :name="meta.icon" :color="meta.color" size="large" class="mr-2" />
      {{ panel.title }}
      <va-chip v-if="isRealtime" size="small" color="info" class="ml-auto">LIVE</va-chip>
    </va-card-title>
    <va-card-content>
      <span class="text-lg">{{ panel.headline }}</span>
      <div v-if="updated" class="updated">
        {{ isRealtime ? 'updated' : 'as of' }} {{ updated }}
      </div>
    </va-card-content>
  </va-card>
</template>

<style scoped>
.health-tile {
  border: 1px solid var(--va-background-border);
  cursor: pointer;
}
.health-tile:hover {
  border-color: var(--va-primary);
}
.updated {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: var(--va-secondary);
}
</style>
