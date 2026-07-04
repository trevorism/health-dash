<script setup>
import MenuBar from '@trevorism/ui-header-bar'
import HealthTile from './components/HealthTile.vue'
import PanelDetail from './components/PanelDetail.vue'
import axios from 'axios'
import { onMounted, onUnmounted, ref } from 'vue'
import { useCookies } from 'vue3-cookies'

// Poll cadence for the glance view. The backend already holds current snapshots
// (push providers via webhook, polling providers on read), so this just re-reads.
const POLL_INTERVAL_MS = 15 * 1000

const { cookies } = useCookies()
const authenticated = ref(!!cookies.get('user_name'))
const panels = ref([])
const selectedPanel = ref(null)
const showDetail = ref(false)
let pollTimer = null

function openDetail(panel) {
  selectedPanel.value = panel
  showDetail.value = true
}

async function loadHealth() {
  try {
    const { data } = await axios.get('api/health')
    panels.value = data
  } catch (e) {
    // Leave the last-known panels in place on a transient error.
  }
}

onMounted(() => {
  if (!authenticated.value) return
  loadHealth()
  pollTimer = setInterval(loadHealth, POLL_INTERVAL_MS)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <menu-bar></menu-bar>
  <div class="p-4">
    <div class="mb-6">
      <h1 class="text-2xl font-bold">System Health</h1>
      <p class="meta">At-a-glance status across the platform's services.</p>
    </div>

    <div v-if="authenticated" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <health-tile
        v-for="panel in panels"
        :key="panel.key"
        :panel="panel"
        @select="openDetail"
      ></health-tile>
    </div>
    <div v-else class="empty-state">Please log in to view system health.</div>

    <va-modal v-model="showDetail" size="large" hide-default-actions close-button>
      <template #header>
        <h2 class="text-xl font-bold">{{ selectedPanel?.title }}</h2>
      </template>
      <panel-detail v-if="selectedPanel" :panel="selectedPanel"></panel-detail>
    </va-modal>
  </div>
</template>

<style scoped>
.meta {
  color: var(--va-secondary);
}
.empty-state {
  padding: 2rem 0;
  color: var(--va-secondary);
}
</style>
