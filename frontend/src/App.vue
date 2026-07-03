<template>
  <div class="app-container">
    <div class="header-bar" style="display: flex; background-color: #545c64;">
      <el-menu mode="horizontal" router :default-active="$route.path" background-color="#545c64" text-color="#fff" active-text-color="#ffd04b" style="flex: 1; border-bottom: none;">
        <el-menu-item index="/">
          <el-icon><House /></el-icon> 主页
        </el-menu-item>
        <el-menu-item index="/menu">
          <el-icon><Menu /></el-icon> 菜单层级管理
        </el-menu-item>
        <el-menu-item index="/role-button">
          <el-icon><User /></el-icon> 角色按钮管理
        </el-menu-item>
        <el-menu-item index="/flow-ump">
          <el-icon><Connection /></el-icon> 总线视角扭转管理
        </el-menu-item>
      </el-menu>
      
      <!-- 右侧环境切换区域 -->
      <div style="display: flex; align-items: center; padding-right: 20px; gap: 10px; border-bottom: solid 1px var(--el-menu-border-color);">
        <span style="color: #fff; font-size: 14px;">当前环境:</span>
        <el-select v-model="currentEnv" placeholder="请选择环境" @change="onEnvChange" style="width: 150px" :teleported="false">
          <el-option v-for="env in envList" :key="env.envId" :label="env.envName + ' (' + env.envId + ')'" :value="env.envId" />
        </el-select>
        <el-button type="primary" @click="openEnvManager">⚙️ 环境管理</el-button>
      </div>
    </div>
    
    <div class="main-content">
      <router-view v-if="envReady" />
    </div>

    <EnvManagerDialog ref="envManagerRef" @env-updated="onEnvListUpdated" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { House, Menu, User, Connection } from '@element-plus/icons-vue'
import EnvManagerDialog from './components/EnvManagerDialog.vue'

const envManagerRef = ref(null)
const currentEnv = ref(localStorage.getItem('X-Env-ID') || '')
const envList = ref([])
const envReady = ref(false)

const openEnvManager = () => {
  envManagerRef.value?.open()
}

const onEnvListUpdated = (list) => {
  envList.value = list
  if (list.length > 0 && !list.find(e => e.envId === currentEnv.value)) {
    // If current env is invalid, select the first one
    currentEnv.value = list[0].envId
    localStorage.setItem('X-Env-ID', currentEnv.value)
    // Reload page to apply changes
    window.location.reload()
  } else {
    envReady.value = true
  }
}

const onEnvChange = (val) => {
  localStorage.setItem('X-Env-ID', val)
  // 刷新整个应用，使得各个组件重新请求数据
  window.location.reload()
}

onMounted(() => {
  // 初始加载一次环境列表
  envManagerRef.value?.fetchEnvs()
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background-color: var(--el-bg-color-page);
}
</style>
