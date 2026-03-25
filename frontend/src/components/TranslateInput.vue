<template>
  <div class="translate-container">
    <LlmDownloadPrompt v-if="!modelStore.isModelReady"/>
    <div v-else class="translate-input">
      <textarea v-model="translateStore.nlInput" placeholder="Enter natural language prompt..."></textarea>
      <button @click="translateStore.translate()" :disabled="translateStore.isLoading">
        <span v-if="translateStore.isLoading" class="spinner"></span>
        <span v-else>Translate</span>
      </button>
      <div v-if="translateStore.warning" class="warning">{{ translateStore.warning }}</div>
    </div>
  </div>
</template>

<script setup>
/**
 * TranslateInput.vue
 *
 * This component provides the user interface for entering natural language prompts
 * and initiating the translation process into tags. It dynamically displays
 * a download prompt for the LLM if the model is not yet ready.
 *
 * Once the model is ready, it presents a textarea for input, a translate button,
 * and displays any warnings or loading indicators during the translation process.
 *
 * @uses useTranslateStore For managing the natural language input, translation
 *   logic, loading state, and warnings.
 * @uses useModelStore For checking the LLM's readiness and managing its download status.
 * @uses LlmDownloadPrompt For displaying the model download interface.
 */
import {onMounted, onUnmounted} from 'vue';
import {useTranslateStore} from '../stores/useTranslateStore';
import {useModelStore} from '../stores/useModelStore';
import LlmDownloadPrompt from './LlmDownloadPrompt.vue';

const translateStore = useTranslateStore();
const modelStore = useModelStore();

onMounted(() => {
  modelStore.checkStatus();
});

onUnmounted(() => {
  modelStore.stopPolling();
});
</script>

<style scoped>
.translate-input {
  display: flex;
  flex-direction: column;
  gap: 8px;

  /* Container Styling */
  padding: 16px;
  background: var(--bg-panel);
  backdrop-filter: var(--glass-blur, blur(10px));
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-panel);
  border-radius: var(--border-radius-md, 8px);
}

.translate-input textarea {
  height: 60px;
  background: var(--bg-input);
  border: 1px solid var(--border-input);
  color: var(--text-primary);
  border-radius: 4px;
  padding: 8px;
  font-family: inherit;
  resize: vertical;
}

.translate-input textarea::placeholder {
  color: var(--text-secondary);
}

.translate-input button {
  background: var(--accent-primary, var(--btn-fill));
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.translate-input button:hover:not(:disabled) {
  background: var(--grad-hover, var(--accent-primary));
}

.translate-input button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.translate-input .warning {
  color: var(--status-warning);
  font-size: 13px;
  margin-top: 4px;
}

.translate-input .spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>