<script setup>
import MenuBar from '@trevorism/ui-header-bar'
import HealthTile from './components/HealthTile.vue'
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useCookies } from 'vue3-cookies'

const { cookies } = useCookies()
const authenticated = ref(!!cookies.get('user_name'))
const panels = ref([])

onMounted(async () => {
  if (!authenticated.value) return
  try {
    const { data } = await axios.get('api/health')
    panels.value = data
  } catch (e) {
    panels.value = []
  }
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
      <health-tile v-for="panel in panels" :key="panel.key" :panel="panel"></health-tile>
    </div>
    <div v-else class="empty-state">Please log in to view system health.</div>
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
