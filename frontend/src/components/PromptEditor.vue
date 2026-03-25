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
  padding: 8px;

  /* Glassmorphism Implementation */
  background: var(--bg-panel);
  backdrop-filter: var(--glass-blur, blur(10px));
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-panel);
  border-radius: var(--border-radius-md, 8px);
}

textarea {
  margin-top: 8px;
  height: 80px;
  resize: vertical;
  background: var(--bg-input);
  border: 1px solid var(--border-input);
  color: var(--text-primary);
  border-radius: var(--border-radius-md, 8px);
  padding: 8px;
}

.actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.actions button {
  background: var(--accent-primary, var(--btn-fill));
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.actions button:hover {
  background: var(--grad-hover, var(--accent-primary));
}
</style>