/**
 * useTranslateStore
 *
 * This Pinia store manages the state and logic for natural language to tag translation.
 * It handles user input, communicates with the backend translation API, and updates
 * the prompt store with the translated tags. It also manages loading states and
 * displays warnings or errors related to the translation process.
 *
 * State:
 * - `nlInput`: A reactive reference to the natural language text entered by the user.
 * - `isLoading`: A reactive boolean indicating whether a translation request is
 *   currently in progress.
 * - `warning`: A reactive reference to a warning message, if any, from the
 *   translation process.
 * - `selectedModel`: A reactive reference to the currently selected model family
 *   for translation (e.g., 'generic', 'pony').
 *
 * Actions:
 * - `translate()`: Asynchronously sends the `nlInput` to the backend for translation.
 *   Upon successful translation, it updates the `usePromptStore` with the new tags.
 *   It also handles error conditions, such as a missing model, and updates the
 *   `warning` state accordingly.
 */
import {defineStore} from 'pinia';
import {ref} from 'vue';
import {translate as apiTranslate} from '../api/backendApi';
import {usePromptStore} from './usePromptStore';
import {useModelStore} from './useModelStore';

export const useTranslateStore = defineStore('translate', () => {
    const nlInput = ref('');
    const isLoading = ref(false);
    const warning = ref(null);
    const selectedModel = ref('generic');

    async function translate() {
        const promptStore = usePromptStore();
        const modelStore = useModelStore();

        isLoading.value = true;
        warning.value = null;

        try {
            const result = await apiTranslate(nlInput.value, selectedModel.value);
            const tags = result.tags.map(name => ({name, valid: true, postCount: 0}));
            promptStore.setFromTranslation(tags);
            warning.value = result.warning;
        } catch (error) {
            console.error('Translation failed:', error);

            if (error.message.includes('Model file not found')) {
                warning.value = 'Model not found. Please download it first.';
                modelStore.isModelReady = false;
            } else {
                warning.value = 'Translation failed.';
            }
        } finally {
            isLoading.value = false;
        }
    }

    return {nlInput, isLoading, warning, selectedModel, translate};
});
