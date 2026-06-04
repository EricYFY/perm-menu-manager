import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/perm-menu-manager/',
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/perm-menu-manager/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
