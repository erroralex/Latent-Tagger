/**
 * useModelStore
 *
 * This Pinia store manages the state and actions related to the ONNX model
 * used for tag translation. It tracks the model's readiness, download status,
 * and progress. It also handles initiating model downloads and polling the
 * backend for status updates.
 *
 * State:
 * - `isModelReady`: A reactive boolean indicating whether the model files are
 *   downloaded and ready for use.
 * - `isDownloading`: A reactive boolean indicating if a model download is
 *   currently in progress.
 * - `downloadProgress`: A reactive number representing the current download
 *   progress (0-100).
 * - `pollingInterval`: A private variable to hold the interval ID for status polling.
 *
 * Actions:
 * - `checkStatus()`: Asynchronously fetches the current model status from the
 *   backend API and updates the store's state. It also manages starting or
 *   stopping the polling mechanism based on the download status.
 * - `downloadModel()`: Asynchronously initiates the model download process via
 *   the backend API and starts status polling.
 * - `startPolling()`: Starts a recurring interval to call `checkStatus()`
 *   periodically.
 * - `stopPolling()`: Clears the polling interval, stopping status updates.
 */
import {defineStore} from 'pinia';
import {ref} from 'vue';
import {getModelStatus, downloadModel as apiDownloadModel} from '../api/backendApi';

export const useModelStore = defineStore('model', () => {
    const isModelReady = ref(false);
    const isDownloading = ref(false);
    const downloadProgress = ref(0);
    let pollingInterval = null;

    async function checkStatus() {
        try {
            const status = await getModelStatus();
            isModelReady.value = status.ready;
            isDownloading.value = status.downloading;
            downloadProgress.value = status.progress;

            if (isDownloading.value && !pollingInterval) {
                startPolling();
            } else if (!isDownloading.value && pollingInterval) {
                stopPolling();
            }
        } catch (error) {
            console.error('Failed to get model status:', error);
            stopPolling();
        }
    }

    async function downloadModel() {
        try {
            await apiDownloadModel();
            isDownloading.value = true;
            startPolling();
        } catch (error) {
            console.error('Failed to start model download:', error);
        }
    }

    function startPolling() {
        if (pollingInterval) return;
        pollingInterval = setInterval(checkStatus, 1500);
    }

    function stopPolling() {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null;
        }
    }

    return {
        isModelReady,
        isDownloading,
        downloadProgress,
        checkStatus,
        downloadModel,
        stopPolling,
    };
});
