<template>
  <div class="menu-manage-page">
    <!-- 顶部标题栏 -->
    <header class="page-header animate-fade-in">
      <div class="header-left">
        <el-icon class="header-logo"><Grid /></el-icon>
        <h1 class="gradient-text">菜单层级管理系统</h1>
        <el-tag :type="isLocked ? 'info' : 'warning'" effect="dark" style="margin-left: 12px">
          {{ isLocked ? '正式表 (只读)' : '临时表 (编辑中)' }}
        </el-tag>
      </div>
      <div class="header-actions" style="display: flex; gap: 12px; align-items: center;">
        <div class="header-badge" v-if="tempTableName">
          <span class="badge-dot" style="background: var(--warning-color); box-shadow: 0 0 8px rgba(230, 162, 60, 0.6);"></span>
          <span>{{ tempTableName }}</span>
        </div>
        <div class="header-badge" v-else>
          <span class="badge-dot"></span>
          <span>perm_menu</span>
        </div>

        <div style="display: flex; align-items: center; gap: 8px;">
          <span style="font-size: 14px; font-weight: 500; color: var(--text-regular);">子系统:</span>
          <el-input 
            v-model="subsystemCode" 
            placeholder="请输入 SUBSYSTEM_CODE" 
            :disabled="!isLocked"
            style="width: 160px;"
            @change="handleRefresh"
          />
        </div>

        <el-button v-if="isLocked" type="primary" @click="handleUnlock" :loading="unlocking">
          <el-icon><Unlock /></el-icon> 解锁编辑
        </el-button>
        <template v-else>
          <el-button type="success" @click="handleSaveAll" :loading="loadingLogs">
            <el-icon><Check /></el-icon> 保存并执行
          </el-button>
          <el-button type="danger" plain @click="handleCancelEdit">取消编辑</el-button>
        </template>
      </div>
    </header>

    <!-- 页签切换 -->
    <div class="tabs-container animate-fade-in" style="animation-delay: 0.1s;">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane name="11">
          <template #label>
            <div class="tab-label">
              <el-icon><Monitor /></el-icon>
              <span>PC端菜单</span>
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane name="12">
          <template #label>
            <div class="tab-label">
              <el-icon><Iphone /></el-icon>
              <span>APP端菜单</span>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <el-row :gutter="20" class="content-row">
        <!-- 左侧 - 菜单树面板 -->
        <el-col :span="10">
          <div class="plain-light-panel tree-panel animate-slide-left">
            <div class="panel-header">
              <h3 class="panel-title">
                <el-icon><List /></el-icon>
                菜单树
              </h3>
              <el-button type="primary" size="small" @click="handleAddRoot" v-if="!isLocked">
                <el-icon><Plus /></el-icon>
                新增根菜单
              </el-button>
            </div>
            <div class="panel-body">
              <MenuTree
                ref="menuTreeRef"
                :menu-scope="activeTab"
                :is-locked="isLocked"
                :temp-table-name="tempTableName"
                :subsystem-code="subsystemCode"
                @select="handleMenuSelect"
              />
            </div>
          </div>
        </el-col>

        <!-- 右侧 - 详情编辑面板 -->
        <el-col :span="14">
          <div class="plain-light-panel detail-panel animate-slide-right">
            <MenuDetailForm
              :model-value="selectedMenu"
              :is-locked="isLocked"
              :temp-table-name="tempTableName"
              @refresh="handleRefresh"
            />
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- SQL预览确认弹窗 -->
    <SqlPreviewDialog
      v-model="previewDialogVisible"
      :temp-table-name="tempTableName"
      :sql-log="sqlLogs"
      @finish="handlePreviewFinish"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Grid, Monitor, Iphone, List, Plus, Unlock, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MenuTree from '../components/MenuTree.vue'
import MenuDetailForm from '../components/MenuDetailForm.vue'
import SqlPreviewDialog from '../components/SqlPreviewDialog.vue'
import { getSessionStatus, unlockSession, cancelSession, getSqlLog } from '../api/session.js'

// 当前页签：11=PC端, 12=APP端
const activeTab = ref('11')

// 选中的菜单数据
const selectedMenu = ref(null)

// 树组件引用
const menuTreeRef = ref(null)

// 锁定状态与临时表
const isLocked = ref(true)
const tempTableName = ref('')
const subsystemCode = ref('ITS_PORTAL')
const unlocking = ref(false)

// 预览弹窗状态
const previewDialogVisible = ref(false)
const sqlLogs = ref([])
const loadingLogs = ref(false)

/**
 * 页面加载时检查状态
 */
onMounted(async () => {
  try {
    const res = await getSessionStatus()
    if (res.code === 200 && res.data.isLocked) {
      // 检查当前锁定人是不是自己
      const myId = getMyLockId()
      if (res.data.lockedBy === myId) {
        // 我自己锁定的，可以继续编辑
        isLocked.value = false
        tempTableName.value = res.data.tempTableName
        if (res.data.subsystemCode) {
          subsystemCode.value = res.data.subsystemCode
        }
        ElMessage.info('已恢复之前的编辑会话')
      } else {
        // 别人锁定的
        isLocked.value = true
        tempTableName.value = ''
        if (res.data.subsystemCode) {
          subsystemCode.value = res.data.subsystemCode
        }
        ElMessage.warning(`当前由 ${res.data.lockedBy} 在编辑，您只能查看。`)
      }
    } else {
      isLocked.value = true
      tempTableName.value = ''
    }
  } catch (error) {
    console.error('获取会话状态失败', error)
  }
})

/**
 * 获取或生成当前用户标识
 */
function getMyLockId() {
  let id = localStorage.getItem('perm_menu_uid')
  if (!id) {
    id = 'user_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem('perm_menu_uid', id)
  }
  return id
}

/**
 * 解锁编辑
 */
async function handleUnlock() {
  if (!subsystemCode.value || subsystemCode.value.trim() === '') {
    ElMessage.warning('子系统编码不能为空')
    return
  }
  unlocking.value = true
  try {
    const res = await unlockSession(getMyLockId(), subsystemCode.value)
    if (res.code === 200) {
      isLocked.value = false
      tempTableName.value = res.data.tempTableName
      ElMessage.success('解锁成功，进入编辑模式')
      handleRefresh()
    } else {
      ElMessage.error(res.message || '解锁失败')
    }
  } catch (error) {
    ElMessage.error('解锁请求异常，可能已被他人锁定')
  } finally {
    unlocking.value = false
  }
}

/**
 * 取消编辑
 */
function handleCancelEdit() {
  ElMessageBox.confirm(
    '取消编辑将丢弃所有未保存的更改，并删除临时表。是否确认？',
    '警告',
    {
      confirmButtonText: '确定放弃',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelSession(tempTableName.value)
      ElMessage.success('已取消编辑')
      isLocked.value = true
      tempTableName.value = ''
      await nextTick()
      handleRefresh()
    } catch (e) {
      ElMessage.error('取消失败')
    }
  }).catch(() => {})
}

/**
 * 保存并执行
 */
async function handleSaveAll() {
  loadingLogs.value = true
  try {
    const res = await getSqlLog(tempTableName.value)
    if (res.code === 200) {
      sqlLogs.value = res.data || []
      previewDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取日志失败')
    }
  } catch (e) {
    ElMessage.error('获取变更日志异常')
  } finally {
    loadingLogs.value = false
  }
}

/**
 * 预览弹窗完成操作（执行成功）
 * @param {boolean} isDropped 是否已删除临时表
 */
async function handlePreviewFinish(isDropped) {
  if (isDropped) {
    isLocked.value = true
    tempTableName.value = ''
  }
  await nextTick()
  handleRefresh()
}

/**
 * 页签切换
 */
function handleTabChange() {
  selectedMenu.value = null
}

/**
 * 树节点选中
 */
function handleMenuSelect(menuData) {
  selectedMenu.value = menuData
}

/**
 * 新增根菜单
 */
function handleAddRoot() {
  menuTreeRef.value?.handleAddRoot()
}

/**
 * 刷新树（保存/修改编码后触发）
 */
function handleRefresh() {
  menuTreeRef.value?.loadTree()
}
</script>

<style scoped>
.menu-manage-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 20px 28px;
  overflow: hidden;
}

/* 顶部标题栏 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo {
  font-size: 30px;
  color: var(--accent-primary);
  filter: drop-shadow(0 0 8px rgba(79, 140, 255, 0.4));
}

.page-header h1 {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.header-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(79, 140, 255, 0.08);
  border: 1px solid rgba(79, 140, 255, 0.15);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success-color);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
  animation: pulse 2s infinite;
}

/* 页签容器 */
.tabs-container {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 主体内容 */
.main-content {
  flex: 1;
  min-height: 0;
}

.content-row {
  height: 100%;
}

.content-row :deep(.el-col) {
  height: 100%;
}

/* 面板样式 */
.tree-panel,
.detail-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

/* 朴素极致浅色主题面板样式 */
.plain-light-panel {
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04) !important;
  color: #303133 !important;
}

.plain-light-panel .panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebedf0 !important;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.plain-light-panel .panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133 !important;
}

.plain-light-panel .panel-title .el-icon {
  color: #909399 !important;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 16px;
  flex-shrink: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.panel-title .el-icon {
  color: var(--accent-primary);
}

.panel-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
