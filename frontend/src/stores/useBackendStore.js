/**
 * useBackendStore
 *
 * This Pinia store manages the connection details and readiness status of the
 * backend service. It is responsible for retrieving the backend's port and
 * authentication token, primarily through Electron's IPC mechanism.
 *
 * State:
 * - `port`: A reactive reference to the backend server's port number.
 * - `token`: A reactive reference to the authentication token required for
 *   backend API calls.
 *
 * Getters:
 * - `ready`: A computed property that returns `true` if both the `port` and
 *   `token` have been successfully retrieved, indicating the backend is ready
 *   for communication.
 *
 * Actions:
 * - `init()`: Asynchronously initializes the store by attempting to fetch
 *   the backend port and token from the Electron main process. It logs an
 *   error if `window.electronAPI` is not available.
 */
import {defineStore} from 'pinia'
import {ref, computed} from 'vue'

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

    return {port, token, ready, init}
})
