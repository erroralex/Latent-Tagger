<template>
  <div class="history-panel">
    <h3>History</h3>
    <div v-if="historyStore.entries.length === 0">No history yet.</div>
    <ul v-else>
      <li v-for="entry in historyStore.entries" :key="entry.id">
        <div class="entry-tags">{{ entry.tags.join(', ').substring(0, 60) }}...</div>
        <div v-if="entry.nlInput" class="entry-nl">{{ entry.nlInput }}</div>
        <button @click="loadEntry(entry)">Load</button>
      </li>
    </ul>
  </div>
</template>

<script setup>
/**
 * HistoryPanel.vue
 *
 * This component displays a panel showing the user's translation history.
 * It fetches history entries from the `useHistoryStore` and allows users
 * to load a previous history entry back into the prompt editor.
 *
 * Each history entry shows a truncated list of its tags and, if available,
 * the original natural language input. A "Load" button is provided to
 * re-apply the tags of a selected history entry.
 *
 * @uses useHistoryStore For fetching and managing translation history.
 * @uses usePromptStore For loading selected history tags into the prompt editor.
 */
import {onMounted} from 'vue'
import {useHistoryStore} from '../stores/useHistoryStore'
import {usePromptStore} from '../stores/usePromptStore'

const historyStore = useHistoryStore()
const promptStore = usePromptStore()

onMounted(() => {
  historyStore.load()
})

const loadEntry = (entry) => {
  promptStore.setFromTranslation(entry.tags.map(name => ({name, valid: null, postCount: 0})))
}
</script>

<style scoped>
.history-panel {
  padding: 16px;
  height: 100%;

  /* Glassmorphism Implementation */
  background: var(--bg-panel);
  backdrop-filter: var(--glass-blur, blur(10px));
  border-left: 1px solid var(--border-light);
  box-shadow: var(--shadow-panel);
}

.history-panel h3 {
  color: var(--text-primary);
  margin-top: 0;
  margin-bottom: 16px;
}

.history-panel ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.history-panel li {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: var(--border-radius-md, 8px);
  background: var(--bg-input);
  border: 1px solid var(--border-input);
}

.history-panel .entry-tags {
  color: var(--text-primary);
  font-size: 14px;
  margin-bottom: 4px;
}

.history-panel .entry-nl {
  font-style: italic;
  color: var(--text-secondary);
  font-size: 12px;
  margin-bottom: 8px;
}

.history-panel button {
  background: var(--accent-primary, var(--btn-fill));
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.history-panel button:hover {
  background: var(--grad-hover, var(--accent-primary));
}
</style>