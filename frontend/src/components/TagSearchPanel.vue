<template>
  <div class="search-panel">
    <input
        type="text"
        v-model="query"
        @input="debouncedSearch"
        @keydown.esc="showResults = false"
        @blur="onBlur"
        placeholder="Search for tags..."
    />
    <div v-if="showResults && tagStore.searchResults.length > 0" class="results-dropdown">
      <TagChip
          v-for="result in tagStore.searchResults"
          :key="result.name"
          :tag="result"
          :removable="false"
          @click="selectTag(result)"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * TagSearchPanel.vue
 *
 * This component provides a search interface for finding and adding tags to the prompt.
 * Users can type into an input field, and the component will display a dropdown of
 * matching tags fetched from the backend.
 *
 * Search queries are debounced to prevent excessive API calls. When a tag is selected
 * from the results, it is added to the current prompt.
 *
 * State:
 * - `query`: A reactive reference to the current search input value.
 * - `showResults`: A reactive boolean controlling the visibility of the search results dropdown.
 *
 * Actions:
 * - `debouncedSearch`: A debounced function that triggers a tag search via `useTagStore`
 *   after a short delay, updating `searchResults`.
 * - `selectTag(tag)`: Adds the selected tag to the `usePromptStore` and clears the search.
 * - `onBlur`: Hides the search results dropdown after a short delay when the input loses focus.
 *
 * @uses useTagStore For performing tag searches and accessing search results.
 * @uses usePromptStore For adding selected tags to the prompt.
 * @uses TagChip For displaying individual search results.
 */
import {ref} from 'vue'
import {useTagStore} from '../stores/useTagStore'
import {usePromptStore} from '../stores/usePromptStore'
import TagChip from './TagChip.vue'
import {debounce} from 'lodash-es'

const query = ref('')
const showResults = ref(false)
const tagStore = useTagStore()
const promptStore = usePromptStore()

const debouncedSearch = debounce(() => {
  tagStore.search(query.value)
  showResults.value = true
}, 150)

const selectTag = (tag) => {
  promptStore.insertTag(tag)
  query.value = ''
  showResults.value = false
}

const onBlur = () => {
  setTimeout(() => {
    showResults.value = false
  }, 150)
}
</script>

<style scoped>
.search-panel {
  position: relative;
}

input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  background: var(--bg-input);
  border: 1px solid var(--border-input);
  color: var(--text-primary);
  border-radius: var(--border-radius-md, 8px);
}

input::placeholder {
  color: var(--text-secondary);
}

.results-dropdown {
  position: absolute;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;

  /* Glassmorphism Implementation */
  background: var(--bg-panel);
  backdrop-filter: var(--glass-blur, blur(10px));
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-panel);
  border-radius: var(--border-radius-md, 8px);
  margin-top: 4px;
  padding: 4px;
}
</style>