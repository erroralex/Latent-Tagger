/**
 * usePromptStore
 *
 * This Pinia store manages the current prompt's tags within the application.
 * It provides functionalities to add, remove, and clear tags, as well as
 * to generate a raw text representation of the current tags.
 *
 * State:
 * - `tags`: A reactive reference to an array of tag objects currently in the prompt.
 *
 * Getters:
 * - `rawText`: A computed property that returns a comma-separated string of
 *   all tag names currently in the `tags` array.
 * - `asString`: An alias for `rawText`.
 *
 * Actions:
 * - `insertTag(tagResult)`: Adds a new tag to the prompt if it's not already present.
 * - `removeTag(name)`: Removes a tag from the prompt based on its name.
 * - `clearAll()`: Clears all tags from the prompt.
 * - `setFromTranslation(tagResultArray)`: Replaces the current tags with a new
 *   array of tag objects, typically used after a translation operation.
 */
import {defineStore} from 'pinia'
import {ref, computed} from 'vue'

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

    return {tags, rawText, asString: rawText, insertTag, removeTag, clearAll, setFromTranslation}
})
