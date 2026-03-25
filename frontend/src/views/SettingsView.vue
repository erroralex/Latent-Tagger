<template>
  <div class="settings-view">
    <h2>Settings</h2>
    <div class="setting">
      <label for="theme-select">Theme:</label>
      <select id="theme-select" v-model="currentTheme" @change="applyTheme($event.target.value)">
        <option v-for="theme in availableThemes" :key="theme" :value="theme">
          {{ theme }}
        </option>
      </select>
    </div>
  </div>
</template>

<script setup>
/**
 * SettingsView.vue
 *
 * This component provides the user interface for configuring application settings.
 * Currently, it allows the user to select and apply a different visual theme.
 *
 * It utilizes the `useTheme` composable to retrieve the currently active theme,
 * a list of available themes, and the function to apply a newly selected theme.
 * The selection triggers an immediate application of the new theme via the
 * `@change` event.
 *
 * @uses useTheme For theme management (current theme, available themes, and apply function).
 */
import { useTheme } from '../composables/useTheme';

const { currentTheme, availableThemes, applyTheme } = useTheme();
</script>

<style scoped>
.settings-view {
  padding: var(--spacing-md);
}
.setting {
  margin-bottom: var(--spacing-md);
}
label {
  display: block;
  margin-bottom: var(--spacing-xs);
}
</style>