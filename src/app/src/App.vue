<script setup>
import MenuBar from '@trevorism/ui-header-bar'
import HealthTile from './components/HealthTile.vue'
</script>

<template>
  <menu-bar></menu-bar>
  <div class="p-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    <health-tile endpoint="api/health/testsuite"></health-tile>
  </div>
</template>

<style scoped></style>
