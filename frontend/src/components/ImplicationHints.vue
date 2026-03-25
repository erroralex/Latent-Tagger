<template>
  <div class="implication-hints" v-if="hints.length > 0">
    <div class="hints-label">Suggested Additions:</div>
    <div class="hints-container">
      <span
          v-for="hint in hints"
          :key="hint"
          class="hint"
          @click="addHint(hint)"
      >
        {{ hint }}
      </span>
    </div>
  </div>
</template>

<script setup>
/**
 * ImplicationHints.vue
 *
 * This component displays a list of suggested tags based on a provided base tag.
 * It's used to help users quickly add related tags that are often used together
 * with the one they just added.
 *
 * It listens for changes to the `tagName` prop, fetches the implications from
 * the backend, and displays them as clickable elements. Clicking a hint adds it
 * to the current prompt via the `usePromptStore`.
 *
 * @prop {String} tagName The name of the tag to fetch implications for.
 * @uses getImplications For fetching implications from the backend server.
 * @uses usePromptStore For adding a suggested tag to the user's prompt.
 */
import { ref, watch } from 'vue';
import { getImplications } from '../api/backendApi'; // Corrected import
import { usePromptStore } from '../stores/usePromptStore';

const props = defineProps({
  tagName: {
    type: String,
    required: true
  }
});

const hints = ref([]);
const promptStore = usePromptStore();

watch(() => props.tagName, async (newVal) => {
  if (newVal) {
    try {
      hints.value = await getImplications(newVal); // Corrected function call
    } catch (e) {
      console.error("Failed to load implications", e);
      hints.value = [];
    }
  } else {
    hints.value = [];
  }
}, { immediate: true });

const addHint = (hintName) => {
  promptStore.addTag({ name: hintName, type: 0, postCount: 0 }); // Type/Count defaults
  hints.value = hints.value.filter(h => h !== hintName);
};
</script>

<style scoped>
.implication-hints {
  margin-top: 16px;
}

.implication-hints .hints-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.implication-hints .hint {
  background: var(--color-background-mute);
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
}

.implication-hints .hint:hover {
  background: var(--color-background-soft);
}
</style>