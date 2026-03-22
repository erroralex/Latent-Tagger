import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHistory, saveHistory } from '../api/backendApi'

export const useHistoryStore = defineStore('history', () => {
  const entries = ref([])

  async function load() {
    try {
      entries.value = await getHistory()
    } catch (error) {
      console.error('Failed to load history:', error)
    }
  }

  async function save(tags, nlInput, model) {
    try {
      await saveHistory(tags, nlInput, model)
      await load() // Refresh history
    } catch (error) {
      console.error('Failed to save history:', error)
    }
  }

  return { entries, load, save }
})
