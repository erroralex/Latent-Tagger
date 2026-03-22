<template>
  <div class="main-view">
    <div class="main-content">
      <TranslateInput />
      <PresetBar />
      <TagSearchPanel />
      <PromptEditor />
      <ImplicationHints v-if="lastAddedTag" :tag-name="lastAddedTag.name" />
    </div>
    <div class="sidebar">
      <HistoryPanel />
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useBackendStore } from '../stores/useBackendStore'
import { useHistoryStore } from '../stores/useHistoryStore'
import { usePromptStore } from '../stores/usePromptStore'
import TranslateInput from '../components/TranslateInput.vue'
import PresetBar from '../components/PresetBar.vue'
import TagSearchPanel from '../components/TagSearchPanel.vue'
import PromptEditor from '../components/PromptEditor.vue'
import ImplicationHints from '../components/ImplicationHints.vue'
import HistoryPanel from '../components/HistoryPanel.vue'

const backendStore = useBackendStore()
const historyStore = useHistoryStore()
const promptStore = usePromptStore()

const lastAddedTag = computed(() => promptStore.tags.length > 0 ? promptStore.tags[promptStore.tags.length - 1] : null)

onMounted(async () => {
  await backendStore.init()
  if (backendStore.ready) {
    historyStore.load()
  }
})
</script>

<style scoped>
.main-view {
  display: grid;
  grid-template-columns: 3fr 1fr;
  height: calc(100vh - 32px); /* Adjust for title bar */
}
.main-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sidebar {
  height: 100%;
}
</style>
