<template>
  <div class="preset-bar">
    <button
      v-for="preset in presets"
      :key="preset.id"
      :class="{ active: translateStore.selectedModel === preset.id }"
      @click="applyPreset(preset)"
    >
      {{ preset.label }}
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPresets } from '../api/backendApi'
import { usePromptStore } from '../stores/usePromptStore'
import { useTranslateStore } from '../stores/useTranslateStore'

const presets = ref([])
const promptStore = usePromptStore()
const translateStore = useTranslateStore()

onMounted(async () => {
  try {
    presets.value = await getPresets()
  } catch (error) {
    console.error('Failed to load presets:', error)
  }
})

const applyPreset = (preset) => {
  translateStore.selectedModel = preset.id
  preset.qualityTags.forEach(name => {
    promptStore.insertTag({ name, valid: null, postCount: 0 })
  })
}
</script>

<style scoped>
.preset-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
button.active {
  background-color: #4caf50;
}
</style>
