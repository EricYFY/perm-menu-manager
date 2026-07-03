import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './style.css'
import axios from 'axios'

// 全局 Axios 拦截器注入 X-Env-ID
axios.interceptors.request.use(config => {
  const envId = localStorage.getItem('X-Env-ID')
  if (envId) {
    config.headers['X-Env-ID'] = envId
  }
  return config
})

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.mount('#app')
