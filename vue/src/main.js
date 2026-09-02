import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/assets/css/global.css'

import App from './App.vue'
import router from './router'
import request from './utils/request'

const app = createApp(App)

app.config.globalProperties.$request = request
app.config.globalProperties.$baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090'

app.use(router)
app.use(ElementPlus, { size: 'small' })
app.mount('#app')
