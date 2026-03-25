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
/**
 * MainView.vue
 *
 * This component serves as the primary layout for the Latent Tagger application.
 * It orchestrates the display and interaction of various sub-components related
 * to tag translation, prompt editing, tag searching, and history management.
 *
 * The layout is divided into a main content area and a sidebar.
 * The main content area houses:
 * - TranslateInput: For natural language input and translation.
 * - PresetBar: For quick access to predefined tag presets.
 * - TagSearchPanel: For searching and adding tags to the prompt.
 * - PromptEditor: For displaying and manually editing the current tag prompt.
 * - ImplicationHints: Displays suggested tags based on the last added tag.
 *
 * The sidebar contains:
 * - HistoryPanel: To view and manage past translation requests.
 *
 * This component utilizes several Vuex stores for state management:
 * - useBackendStore: Manages the connection and status of the backend service.
 * - useHistoryStore: Manages the history of translated prompts.
 * - usePromptStore: Manages the current prompt's tags and related logic.
 *
 * On component mount, it initializes the backend store and, if the backend is ready,
 * loads the history from the history store. It also computes the last added tag
 * to dynamically display implication hints.
 */
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