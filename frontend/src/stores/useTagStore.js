import { defineStore } from 'pinia'
import { ref } from 'vue'
import { searchTags } from '../api/backendApi'

export const useTagStore = defineStore('tag', () => {
  const searchResults = ref([])
  const isSearching = ref(false)

  async function search(q) {
    if (!q) {
      searchResults.value = []
      return
    }
    isSearching.value = true
    try {
      searchResults.value = await searchTags(q)
    } catch (error) {
      console.error('Tag search failed:', error)
      searchResults.value = []
    } finally {
      isSearching.value = false
    }
  }

  return { searchResults, isSearching, search }
})
