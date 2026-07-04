<script setup>
import MenuBar from '@trevorism/ui-header-bar'
import HealthTile from './components/HealthTile.vue'
import { ref } from 'vue'
import { useCookies } from 'vue3-cookies'

const { cookies } = useCookies()
const authenticated = ref(!!cookies.get('user_name'))
</script>

<template>
  <menu-bar></menu-bar>
  <div class="p-4">
    <div class="mb-6">
      <h1 class="text-2xl font-bold">System Health</h1>
      <p class="meta">At-a-glance status across the platform's services.</p>
    </div>

    <div v-if="authenticated" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <health-tile endpoint="api/health/testsuite"></health-tile>
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
