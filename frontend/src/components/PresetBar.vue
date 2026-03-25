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
/**
 * PresetBar.vue
 *
 * This component displays a bar of predefined tag presets. Each preset is represented
 * by a button. When a preset button is clicked, it applies the associated settings,
 * such as selecting a model and inserting specific quality tags into the prompt.
 *
 * The presets are loaded asynchronously from the backend API on component mount.
 * The currently active preset (based on the selected model) is highlighted.
 *
 * State:
 * - `presets`: A reactive reference to an array of preset objects loaded from the backend.
 *
 * Actions:
 * - `applyPreset(preset)`: Sets the `selectedModel` in `useTranslateStore` to the
 *   preset's ID and inserts the preset's `qualityTags` into the `usePromptStore`.
 *
 * @uses usePromptStore For inserting tags into the current prompt.
 * @uses useTranslateStore For setting the selected translation model.
 */
import {ref, onMounted} from 'vue'
import {getPresets} from '../api/backendApi'
import {usePromptStore} from '../stores/usePromptStore'
import {useTranslateStore} from '../stores/useTranslateStore'

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
    promptStore.insertTag({name, valid: null, postCount: 0})
  })
}
</script>

<style scoped>
.preset-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.preset-bar button {
  background: var(--bg-input);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  padding: 6px 12px;
  border-radius: var(--border-radius-md, 8px);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.preset-bar button:hover {
  background: var(--bg-panel);
}

.preset-bar button.active {
  background: var(--accent-primary, var(--btn-fill));
  border-color: var(--accent-primary, var(--border-light));
  color: var(--text-primary);
}
</style>