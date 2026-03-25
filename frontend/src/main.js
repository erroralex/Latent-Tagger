import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useTheme } from './composables/useTheme'
import './assets/css/components/base.css'
import './assets/css/components/layout.css'
import './assets/css/components/buttons.css'
import './assets/css/components/primevue-overrides.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const { currentTheme, applyTheme } = useTheme();
applyTheme(currentTheme.value);

app.mount('#app')
