<template>
  <div class="translate-input">
    <textarea v-model="translateStore.nlInput" placeholder="Enter natural language prompt..."></textarea>
    <button @click="translateStore.translate()" :disabled="translateStore.isLoading">
      <span v-if="translateStore.isLoading" class="spinner"></span>
      <span v-else>Translate</span>
    </button>
    <div v-if="translateStore.warning" class="warning">{{ translateStore.warning }}</div>
  </div>
</template>

<script setup>
import { useTranslateStore } from '../stores/useTranslateStore'
const translateStore = useTranslateStore()
</script>

<style scoped>
.translate-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
textarea {
  height: 60px;
}
.warning {
  color: #ff9800;
}
.spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
