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
import { ref } from 'vue'
import { useTagStore } from '../stores/useTagStore'
import { usePromptStore } from '../stores/usePromptStore'
import TagChip from './TagChip.vue'
import { debounce } from 'lodash-es'

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
}
.results-dropdown {
  position: absolute;
  background: #333;
  border: 1px solid #555;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}
</style>
