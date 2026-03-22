<template>
  <div class="implication-hints" v-if="implications.length > 0">
    <strong>Implications for {{ tagName }}:</strong>
    <div class="hints-container">
      <span v-for="imp in implications" :key="imp" @click="addTag(imp)" class="hint">
        {{ imp }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getImplications } from '../api/backendApi'
import { usePromptStore } from '../stores/usePromptStore'

const props = defineProps({
  tagName: { type: String, required: true }
})

const implications = ref([])
const promptStore = usePromptStore()

watch(() => props.tagName, async (newTagName) => {
  if (newTagName) {
    try {
      implications.value = await getImplications(newTagName)
    } catch (error) {
      console.error('Failed to get implications:', error)
      implications.value = []
    }
  } else {
    implications.value = []
  }
}, { immediate: true })

const addTag = (name) => {
  promptStore.insertTag({ name, valid: null, postCount: 0 })
}
</script>

<style scoped>
.implication-hints {
  margin-top: 16px;
}
.hints-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}
.hint {
  background: #444;
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
}
.hint:hover {
  background: #555;
}
</style>
