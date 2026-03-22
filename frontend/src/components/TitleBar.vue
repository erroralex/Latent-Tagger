<template>
  <header class="title-bar">
    <div class="title">TagSmith</div>
    <div class="window-controls">
      <button @click="minimize" class="window-control">-</button>
      <button @click="maximize" class="window-control">o</button>
      <button @click="close" class="window-control-close">x</button>
    </div>
  </header>
</template>

<script setup>
const minimize = () => window.electronAPI?.minimizeWindow()
const maximize = () => window.electronAPI?.maximizeWindow()
const close = () => window.electronAPI?.closeWindow()
</script>

<style scoped>
.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 32px;
  background: #2c2c2c;
  color: #fff;
  -webkit-app-region: drag;
  user-select: none;
}
.title {
  padding-left: 12px;
  font-weight: bold;
}
.window-controls {
  -webkit-app-region: no-drag;
  display: flex;
}
.window-control, .window-control-close {
  width: 45px;
  height: 32px;
  border: none;
  background: transparent;
  color: #fff;
  font-size: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.window-control:hover {
  background: #444;
}
.window-control-close:hover {
  background: #e81123;
}
</style>
