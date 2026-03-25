<template>
  <div class="tag-chip" :class="validationClass">
    <span class="tag-name">{{ tag.name }}</span>
    <span v-if="tag.postCount > 0" class="tag-count">{{ tag.postCount.toLocaleString() }}</span>
    <button v-if="removable" @click="$emit('remove', tag.name)" class="remove-btn">&times;</button>
  </div>
</template>

<script setup>
/**
 * TagChip.vue
 *
 * This component displays a single tag as a chip, optionally showing its post count
 * and providing a removal button. The chip's appearance (border color) dynamically
 * changes based on the tag's validation status (valid, invalid, or unvalidated).
 *
 * Props:
 * - `tag` (Object, required): An object representing the tag, expected to have:
 *   - `name` (String): The name of the tag.
 *   - `postCount` (Number): The number of posts associated with the tag.
 *   - `valid` (Boolean): Indicates if the tag has been validated against the database.
 * - `removable` (Boolean, default: true): If true, a remove button is displayed,
 *   allowing the user to remove the tag.
 *
 * Emits:
 * - `remove` (String): Emitted when the remove button is clicked, passing the
 *   name of the tag to be removed.
 *
 * Computed Properties:
 * - `validationClass`: Determines the CSS class ('valid', 'invalid', 'unvalidated')
 *   based on the `tag.valid` property to style the chip accordingly.
 */
import {computed} from 'vue'

const props = defineProps({
  tag: {type: Object, required: true},
  removable: {type: Boolean, default: true}
})

defineEmits(['remove'])

const validationClass = computed(() => {
  if (props.tag.valid === true) return 'valid'
  if (props.tag.valid === false) return 'invalid'
  return 'unvalidated'
})
</script>

<style scoped>
.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 16px;
  margin: 2px;
  font-size: 13px;
  border: 1px solid;
  white-space: nowrap;
}

.tag-chip.valid {
  border-color: var(--color-success);
  color: var(--color-success);
}

.tag-chip.invalid {
  border-color: var(--color-error);
  color: var(--color-error);
}

.tag-chip.unvalidated {
  border-color: var(--color-warning);
  color: var(--color-warning);
}

.tag-name {
  margin-right: 8px;
}

.tag-count {
  font-size: 11px;
  opacity: 0.7;
}

.remove-btn {
  margin-left: 8px;
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  line-height: 1;
  opacity: 0;
}

.tag-chip:hover .remove-btn {
  opacity: 1;
}
</style>
