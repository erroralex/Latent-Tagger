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
  promptStore.insertTag({ name: hintName, valid: true, postCount: 0 });
  hints.value = hints.value.filter(h => h !== hintName);
};
</script>

<style scoped>
.implication-hints {
  margin-top: 16px;
}

.hints-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.implication-hints .hints-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.implication-hints .hint {
  background: var(--bg-input);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  padding: 4px 8px;
  border-radius: var(--border-radius-md, 8px);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
}

.implication-hints .hint:hover {
  background: var(--accent-primary, var(--btn-fill));
  border-color: var(--accent-primary, var(--border-light));
}
</style>