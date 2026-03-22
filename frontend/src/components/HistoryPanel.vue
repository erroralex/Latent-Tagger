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
import { onMounted } from 'vue'
import { useHistoryStore } from '../stores/useHistoryStore'
import { usePromptStore } from '../stores/usePromptStore'

const historyStore = useHistoryStore()
const promptStore = usePromptStore()

onMounted(() => {
  historyStore.load()
})

const loadEntry = (entry) => {
  promptStore.setFromTranslation(entry.tags.map(name => ({ name, valid: null, postCount: 0 })))
}
</script>

<style scoped>
.history-panel {
  padding: 8px;
  border-left: 1px solid #555;
  height: 100%;
}
ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
li {
  margin-bottom: 12px;
}
.entry-nl {
  font-style: italic;
  color: #aaa;
}
</style>
