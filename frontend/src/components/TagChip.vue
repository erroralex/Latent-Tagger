<template>
  <div class="tag-chip" :class="validationClass">
    <span class="tag-name">{{ tag.name }}</span>
    <span v-if="tag.postCount > 0" class="tag-count">{{ tag.postCount.toLocaleString() }}</span>
    <button v-if="removable" @click="$emit('remove', tag.name)" class="remove-btn">&times;</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  tag: { type: Object, required: true },
  removable: { type: Boolean, default: true }
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
.valid {
  border-color: #4caf50;
  color: #4caf50;
}
.invalid {
  border-color: #f44336;
  color: #f44336;
}
.unvalidated {
  border-color: #ff9800;
  color: #ff9800;
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
