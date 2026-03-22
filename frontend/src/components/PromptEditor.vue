<template>
  <div class="prompt-editor">
    <div class="tags-container">
      <TagChip
        v-for="tag in promptStore.tags"
        :key="tag.name"
        :tag="tag"
        @remove="promptStore.removeTag"
      />
    </div>
    <textarea :value="promptStore.asString" readonly></textarea>
    <div class="actions">
      <button @click="copy">Copy</button>
      <button @click="promptStore.clearAll()">Clear all</button>
    </div>
  </div>
</template>

<script setup>
import { usePromptStore } from '../stores/usePromptStore'
import TagChip from './TagChip.vue'

const promptStore = usePromptStore()

const copy = () => {
  navigator.clipboard.writeText(promptStore.asString)
}
</script>

<style scoped>
.prompt-editor {
  display: flex;
  flex-direction: column;
}
.tags-container {
  display: flex;
  flex-wrap: wrap;
  min-height: 100px;
  border: 1px solid #555;
  padding: 4px;
}
textarea {
  margin-top: 8px;
  height: 80px;
  resize: vertical;
}
.actions {
  margin-top: 8px;
}
</style>
