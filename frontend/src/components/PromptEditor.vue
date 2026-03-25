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
/**
 * PromptEditor.vue
 *
 * This component provides an interface for viewing and managing the current
 * set of tags that form the prompt. It displays tags as interactive `TagChip`
 * components, allowing individual tags to be removed.
 *
 * The component also features a read-only textarea that shows the combined
 * prompt string, and buttons to copy this string to the clipboard or clear
 * all tags from the prompt.
 *
 * @uses usePromptStore For accessing and modifying the current prompt's tags.
 * @uses TagChip For displaying individual tags.
 */
import {usePromptStore} from '../stores/usePromptStore'
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
  border: 1px solid var(--color-border);
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
