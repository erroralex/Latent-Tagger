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
  padding: 8px;
  border-left: 1px solid var(--color-border);
  height: 100%;
}

.history-panel ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.history-panel li {
  margin-bottom: 12px;
}

.history-panel .entry-nl {
  font-style: italic;
  color: var(--color-text-secondary);
}
</style>
