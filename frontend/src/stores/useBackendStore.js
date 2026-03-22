import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useBackendStore = defineStore('backend', () => {
  const port = ref(null)
  const token = ref(null)
  const ready = computed(() => port.value !== null && token.value !== null)

  async function init() {
    if (window.electronAPI) {
        const result = await window.electronAPI.getBackendPort()
        port.value = result.port
        token.value = result.token
    } else {
        console.error('electronAPI not found. Are you running in Electron?')
    }
  }

  return { port, token, ready, init }
})
