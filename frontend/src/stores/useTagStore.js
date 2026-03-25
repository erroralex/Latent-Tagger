/**
 * useTagStore
 *
 * This Pinia store manages the state and logic related to tag searching.
 * It provides functionalities to search for tags using a backend API
 * and maintains the search results and a loading indicator.
 *
 * State:
 * - `searchResults`: A reactive reference to an array of tag search results.
 * - `isSearching`: A reactive boolean indicating whether a tag search is currently in progress.
 *
 * Actions:
 * - `search(q)`: Initiates a tag search with the given query string `q`.
 *   It updates `searchResults` with the fetched data and sets `isSearching` accordingly.
 *   If the query is empty, it clears the search results.
 */
import {defineStore} from 'pinia'
import {ref} from 'vue'
import {searchTags} from '../api/backendApi'

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

    return {searchResults, isSearching, search}
})
