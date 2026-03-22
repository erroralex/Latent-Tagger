<template>
  <div class="settings-view">
    <h2>Settings</h2>
    <div class="setting">
      <label for="apiKey">API Key</label>
      <input type="password" id="apiKey" v-model="apiKey" />
    </div>
    <div class="setting">
      <label for="modelFamily">Model Family</label>
      <select id="modelFamily" v-model="translateStore.selectedModel">
        <option value="generic">Generic (SDXL / SD1.5)</option>
        <option value="pony">Pony Diffusion</option>
        <option value="illustrious">Illustrious / NoobAI</option>
      </select>
    </div>
    <div class="setting">
      <label>
        <input type="checkbox" v-model="lowVramMode" />
        Low VRAM Mode
      </label>
    </div>
    <button @click="$router.push('/')">Back</button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useTranslateStore } from '../stores/useTranslateStore'

const translateStore = useTranslateStore()

const apiKey = ref(localStorage.getItem('apiKey') || '')
const lowVramMode = ref(localStorage.getItem('lowVramMode') === 'true')

watch(apiKey, (newVal) => localStorage.setItem('apiKey', newVal))
watch(lowVramMode, (newVal) => localStorage.setItem('lowVramMode', newVal))
</script>

<style scoped>
.settings-view {
  padding: 16px;
}
.setting {
  margin-bottom: 16px;
}
label {
  display: block;
  margin-bottom: 4px;
}
</style>
