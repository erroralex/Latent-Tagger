<template>
  <div class="layout-wrapper">
    <header class="app-header glass-header drag-region">

      <div class="header-left no-drag">
        <span class="app-title text-gradient">Latent Tagger</span>
      </div>

      <div class="header-center"></div>

      <div class="header-right no-drag">
        <div class="window-controls">
          <button class="win-btn" @click="minimizeWindow" title="Minimize">&#8211;</button>
          <button class="win-btn" @click="maximizeWindow" title="Maximize">&#9723;</button>
          <button class="win-btn close" @click="closeWindow" title="Close">&#10005;</button>
        </div>
      </div>

    </header>

    <main class="app-body">
      <div v-if="backendStore.ready" class="app-main">
        <router-view />
      </div>
      <div v-else class="loading-screen">
        Connecting to backend...
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useTheme } from './composables/useTheme';
import { useBackendStore } from './stores/useBackendStore';

const { currentTheme, applyTheme } = useTheme();
const backendStore = useBackendStore();

const minimizeWindow = () => window.windowAPI?.minimize();
const maximizeWindow = () => window.windowAPI?.maximize();
const closeWindow = () => window.windowAPI?.close();

onMounted(async () => {
  applyTheme(currentTheme.value);
  await backendStore.init();
});
</script>

<style>
.loading-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 1.2em;
  color: var(--text-primary);
  background: var(--bg-app);
}
</style>