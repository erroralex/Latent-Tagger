/**
 * useHistoryStore
 *
 * This Pinia store manages the application's translation history.
 * It provides functionalities to load existing history entries from the backend
 * and to save new translation results.
 *
 * State:
 * - `entries`: A reactive reference to an array of history entry objects.
 *
 * Actions:
 * - `load()`: Asynchronously fetches the translation history from the backend API
 *   and updates the `entries` state.
 * - `save(tags, nlInput, model)`: Asynchronously saves a new history entry to the
 *   backend. After a successful save, it reloads the history to reflect the changes.
 */
import {defineStore} from 'pinia'
import {ref} from 'vue'
import {getHistory, saveHistory} from '../api/backendApi'

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
            await load()
        } catch (error) {
            console.error('Failed to save history:', error)
        }
    }

    return {entries, load, save}
})
