<template>
  <div class="glass-prompt">
    <div class="prompt-content">
      <div v-if="!modelStore.isDownloading">
        <div class="icon">⚠️</div>
        <h3>Local LLM Required</h3>
        <p>
          Offline translation requires a local AI model (~2.4 GB).
          The download will be saved to your user directory.
        </p>
        <div class="button-group">
          <button @click="modelStore.downloadModel">Download Model</button>
          <button @click="openModelLocation" class="secondary-btn">Open Model Location</button>
        </div>
      </div>
      <div v-else>
        <h3>Downloading Model...</h3>
        <div class="progress-bar-container">
          <div class="progress-bar" :style="{ width: modelStore.downloadProgress + '%' }"></div>
        </div>
        <p>{{ modelStore.downloadProgress }}%</p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * LlmDownloadPrompt.vue
 *
 * This component displays a prompt to the user regarding the download status
 * of the local Large Language Model (LLM). It conditionally renders two states:
 *
 * 1. **Download Required**: If the model is not currently downloading, it informs
 *    the user that a local LLM is needed for offline translation, provides
 *    information about the file size, and offers a button to initiate the download.
 * 2. **Downloading**: If the model is actively downloading, it displays a progress bar
 *    and the current download percentage.
 *
 * This component relies on the `useModelStore` to get and update the model's
 * download status and progress.
 *
 * @uses useModelStore For accessing and controlling the LLM download state.
 */
import { useModelStore } from '../stores/useModelStore';
import { getModelPath } from '../api/backendApi';

const modelStore = useModelStore();

const openModelLocation = async () => {
  try {
    const path = await getModelPath();
    window.electronAPI.openPath(path);
  } catch (error) {
    console.error('Failed to get model path:', error);
  }
};
</script>

<style scoped>
.glass-prompt {
  background: var(--color-glass-background);
  backdrop-filter: blur(12px);
  border: 1px solid var(--color-glass-border);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
}

.glass-prompt .prompt-content .icon {
  font-size: 2.5em;
  margin-bottom: 16px;
}

.glass-prompt .progress-bar-container {
  width: 100%;
  height: 8px;
  background: var(--color-progress-background);
  border-radius: 4px;
  margin: 16px 0;
}

.glass-prompt .progress-bar {
  height: 100%;
  background: var(--color-primary);
  border-radius: 4px;
  transition: width 0.3s ease-in-out;
}

.button-group {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>
