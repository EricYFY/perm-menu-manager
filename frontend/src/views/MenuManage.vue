<template>
  <div class="menu-manage-page">
    <!-- 顶部标题栏 -->
    <header class="page-header animate-fade-in">
      <div class="header-left">
        <el-icon class="header-logo"><Grid /></el-icon>
        <h1 class="gradient-text">菜单层级管理系统</h1>
      </div>
      <div class="header-badge">
        <span class="badge-dot"></span>
        <span>perm_menu</span>
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
              <el-button type="primary" size="small" @click="handleAddRoot">
                <el-icon><Plus /></el-icon>
                新增根菜单
              </el-button>
            </div>
            <div class="panel-body">
              <MenuTree
                ref="menuTreeRef"
                :menu-scope="activeTab"
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
              @refresh="handleRefresh"
            />
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Grid, Monitor, Iphone, List, Plus } from '@element-plus/icons-vue'
import MenuTree from '../components/MenuTree.vue'
import MenuDetailForm from '../components/MenuDetailForm.vue'

// 当前页签：11=PC端, 12=APP端
const activeTab = ref('11')

// 选中的菜单数据
const selectedMenu = ref(null)

// 树组件引用
const menuTreeRef = ref(null)

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
