import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const usePromptStore = defineStore('prompt', () => {
  const tags = ref([])
  const rawText = computed(() => tags.value.map(t => t.name).join(', '))

  function insertTag(tagResult) {
    if (!tags.value.some(t => t.name === tagResult.name)) {
      tags.value.push(tagResult)
    }
  }

  function removeTag(name) {
    tags.value = tags.value.filter(t => t.name !== name)
  }

  function clearAll() {
    tags.value = []
  }

  function setFromTranslation(tagResultArray) {
    tags.value = tagResultArray
  }

  return { tags, rawText, asString: rawText, insertTag, removeTag, clearAll, setFromTranslation }
})
