<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// endpoint is the /api/health/... path for one provider (e.g. 'api/health/testsuite')
const props = defineProps({
  endpoint: { type: String, required: true }
})

const panel = ref(null)
const loading = ref(true)
const failed = ref(false)

const STATUS = {
  OK: { icon: 'check_circle', color: 'success' },
  WARN: { icon: 'warning', color: 'warning' },
  ERROR: { icon: 'cancel', color: 'danger' },
  UNKNOWN: { icon: 'help', color: 'secondary' }
}

const meta = computed(() => STATUS[panel.value?.status] || STATUS.UNKNOWN)

onMounted(async () => {
  try {
    const { data } = await axios.get(props.endpoint)
    panel.value = data
  } catch (e) {
    failed.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <va-card class="health-tile">
    <va-card-title>
      <va-icon :name="meta.icon" :color="meta.color" size="large" class="mr-2" />
      {{ panel?.title || 'Loading…' }}
    </va-card-title>
    <va-card-content>
      <span v-if="loading">Loading…</span>
      <span v-else-if="failed">Unavailable</span>
      <span v-else class="text-lg">{{ panel.headline }}</span>
    </va-card-content>
  </va-card>
</template>

<style scoped>
.health-tile {
  border: 1px solid var(--va-background-border);
}
</style>
