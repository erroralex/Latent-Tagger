import { defineStore } from 'pinia'
import { ref } from 'vue'
import { translate as apiTranslate } from '../api/backendApi'
import { usePromptStore } from './usePromptStore'

export const useTranslateStore = defineStore('translate', () => {
  const nlInput = ref('')
  const isLoading = ref(false)
  const warning = ref(null)
  const selectedModel = ref('generic')

  async function translate() {
    isLoading.value = true
    warning.value = null
    const promptStore = usePromptStore()
    try {
      const result = await apiTranslate(nlInput.value, selectedModel.value)
      promptStore.setFromTranslation(result.tags.map(name => ({ name, valid: true, postCount: 0 }))) // Assuming translated tags are valid
      warning.value = result.warning
    } catch (error) {
      console.error('Translation failed:', error)
      warning.value = 'Translation failed.'
    } finally {
      isLoading.value = false
    }
  }

  return { nlInput, isLoading, warning, selectedModel, translate }
})
