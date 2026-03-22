<template>
  <TitleBar />
  <main>
    <div v-if="backendStore.ready">
      <router-view />
    </div>
    <div v-else class="loading-screen">
      Connecting to backend...
    </div>
  </main>
</template>

<script setup>
import { onMounted } from 'vue'
import { useBackendStore } from './stores/useBackendStore'
import TitleBar from './components/TitleBar.vue'

const backendStore = useBackendStore()

onMounted(() => {
  backendStore.init()
})
</script>

<style>
/* Global styles */
body {
  margin: 0;
  background-color: #1e1e1e;
  color: #d4d4d4;
  font-family: sans-serif;
}
button {
  background-color: #3c3c3c;
  color: #d4d4d4;
  border: 1px solid #555;
  padding: 8px 12px;
  cursor: pointer;
}
button:hover {
  background-color: #4c4c4c;
}
input, textarea, select {
  background-color: #3c3c3c;
  color: #d4d4d4;
  border: 1px solid #555;
  padding: 8px;
}
.loading-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 32px);
  font-size: 1.2em;
}
</style>
