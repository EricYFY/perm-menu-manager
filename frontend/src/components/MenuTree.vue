<template>
  <div class="menu-tree-container">
    <!-- 搜索框和操作栏 -->
    <div class="tree-search">
      <el-input
        v-model="searchText"
        placeholder="搜索菜单编码或名称..."
        clearable
        :prefix-icon="Search"
        @input="handleFilter"
        style="flex: 1;"
      />
      <el-button-group class="tree-actions-group">
        <el-tooltip content="一键展开所有" placement="top" :show-after="500">
          <el-button
            :icon="Expand"
            @click="handleExpandAll"
          />
        </el-tooltip>
        <el-tooltip content="一键折叠所有" placement="top" :show-after="500">
          <el-button
            :icon="Fold"
            @click="handleCollapseAll"
          />
        </el-tooltip>
      </el-button-group>
    </div>

    <!-- 树组件 -->
    <div
      class="tree-wrapper"
      v-loading="loading"
      element-loading-text="加载中..."
      @dragover="handleDragOver"
    >
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        node-key="menuCode"
        :expand-on-click-node="false"
        :highlight-current="true"
        :default-expand-all="false"
        :default-expanded-keys="expandedKeys"
        :draggable="!isLocked"
        :allow-drop="handleAllowDrop"
        :filter-node-method="filterNode"
        @node-click="handleNodeClick"
        @node-drop="handleNodeDrop"
        @node-drag-over="handleNodeDragOver"
        @node-drag-leave="handleNodeDragLeave"
        @node-drag-end="handleNodeDragEnd"
      >
        <template #default="{ node, data }">
          <div
            class="tree-node-content"
            :class="{ 'is-drag-target': activeDropNodeCode === data.menuCode }"
          >
            <el-tooltip
              :content="`${data.menuCode} | ${data.menuName}`"
              placement="top-start"
              :show-after="600"
              effect="light"
            >
              <div class="tree-node-info">
                <el-icon class="node-icon">
                  <component :is="data.menuLevel === 9 ? 'Link' : 'Folder'" />
                </el-icon>
                <span class="node-code">{{ data.menuCode }}</span>
                <span class="node-name">{{ data.menuName }}</span>
                <span v-if="data.menuLevel < 9" class="interface-badge" title="下属接口总数">
                  {{ countInterfaces(data) }}
                </span>
              </div>
            </el-tooltip>
            <div class="tree-node-actions" v-if="!isLocked">
              <el-tooltip content="新增子菜单" placement="top" :show-after="500">
                <el-icon class="action-icon add-icon" @click.stop="handleAddChild(data)">
                  <Plus />
                </el-icon>
              </el-tooltip>
              <el-tooltip content="删除菜单" placement="top" :show-after="500">
                <el-icon class="action-icon delete-icon" @click.stop="handleDelete(data)">
                  <Delete />
                </el-icon>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-tree>

      <el-empty v-if="!loading && treeData.length === 0" description="暂无菜单数据" :image-size="80" />
    </div>

    <!-- 新增菜单弹窗 -->
    <MenuAddDialog
      v-model="addDialogVisible"
      :menu-scope="menuScope"
      :parent-menu="addParentMenu"
      :temp-table-name="tempTableName"
      @success="handleAddSuccess"
    />
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { Search, Plus, Delete, Folder, Link, Fold, Expand } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, dragMenu, deleteMenu, updateMenu } from '../api/menu.js'
import MenuAddDialog from './MenuAddDialog.vue'

const props = defineProps({
  /** 菜单渠道：11=PC端, 12=APP端 */
  menuScope: {
    type: String,
    required: true
  },
  /** 是否锁定状态（不允许编辑） */
  isLocked: {
    type: Boolean,
    default: true
  },
  /** 临时表名 */
  tempTableName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['select'])

// 树组件引用
const treeRef = ref(null)

// 状态
const loading = ref(false)
const treeData = ref([])
const searchText = ref('')
const expandedKeys = ref([])
const addDialogVisible = ref(false)
const addParentMenu = ref(null)

// 拖拽高亮与滚动状态相关定义
const activeDropNodeCode = ref('')
let scrollInterval = null
const scrollSpeed = 10 // 滚动像素值
const edgeThreshold = 50 // 触发滚动的容器边缘高度阈值（px）

// 树组件属性配置
const treeProps = {
  children: 'children',
  label: 'menuName'
}

/**
 * 加载菜单树数据
 * @param {boolean} keepState - 是否保持当前的所有展开与选中状态，默认不保持
 */
async function loadTree(keepState = false) {
  loading.value = true
  
  // 1. 收集当前的展开 keys 与选中的 key
  const expandedKeysList = []
  let currentSelectedKey = null
  if (keepState && treeRef.value) {
    const nodesMap = treeRef.value.store.nodesMap
    for (const key in nodesMap) {
      if (nodesMap[key] && nodesMap[key].expanded) {
        expandedKeysList.push(key)
      }
    }
    currentSelectedKey = treeRef.value.getCurrentKey()
  }

  try {
    const result = await getMenuTree(props.menuScope, '047', props.tempTableName)
    if (result.code === 200) {
      treeData.value = result.data || []
      
      if (keepState) {
        // 保持状态：在 DOM 更新后重新还原展开与选中高亮
        await nextTick()
        if (treeRef.value) {
          // 还原已展开的节点
          expandedKeysList.forEach(key => {
            const node = treeRef.value.store.nodesMap[key]
            if (node) {
              node.expanded = true
            }
          })
          // 还原选中高亮，并向右侧详情同步最新数据
          if (currentSelectedKey) {
            treeRef.value.setCurrentKey(currentSelectedKey)
            const currentNode = treeRef.value.getNode(currentSelectedKey)
            if (currentNode && currentNode.data) {
              emit('select', { ...currentNode.data })
            }
          }
        }
      } else {
        // 默认展开第一层
        if (treeData.value.length > 0) {
          expandedKeys.value = treeData.value.map(item => item.menuCode)
        }
      }
    } else {
      ElMessage.error(result.message || '加载菜单树失败')
    }
  } catch (error) {
    ElMessage.error('加载菜单树失败，请检查后端服务是否启动')
    console.error('加载菜单树异常:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索过滤
 */
function handleFilter() {
  treeRef.value?.filter(searchText.value)
}

/**
 * 一键折叠所有菜单节点
 */
function handleCollapseAll() {
  if (!treeRef.value) return
  const nodes = treeRef.value.store.nodesMap
  for (const key in nodes) {
    if (Object.prototype.hasOwnProperty.call(nodes, key)) {
      nodes[key].expanded = false
    }
  }
}

/**
 * 一键展开所有菜单节点
 */
function handleExpandAll() {
  if (!treeRef.value) return
  const nodes = treeRef.value.store.nodesMap
  for (const key in nodes) {
    if (Object.prototype.hasOwnProperty.call(nodes, key)) {
      nodes[key].expanded = true
    }
  }
}

/**
 * 拖动容器 dragover：处理自动滚动
 */
function handleDragOver(event) {
  const wrapper = event.currentTarget
  if (!wrapper) return

  const rect = wrapper.getBoundingClientRect()
  const relativeY = event.clientY - rect.top

  stopAutoScroll()

  if (relativeY < edgeThreshold) {
    // 靠近顶部，向上滚动
    scrollInterval = setInterval(() => {
      wrapper.scrollTop -= scrollSpeed
    }, 16)
  } else if (rect.bottom - event.clientY < edgeThreshold) {
    // 靠近底部，向下滚动
    scrollInterval = setInterval(() => {
      wrapper.scrollTop += scrollSpeed
    }, 16)
  }
}

/**
 * 停止自动滚动
 */
function stopAutoScroll() {
  if (scrollInterval) {
    clearInterval(scrollInterval)
    scrollInterval = null
  }
}

/**
 * 拖拽移动时 - 记录当前目标高亮节点
 */
function handleNodeDragOver(draggingNode, dropNode) {
  activeDropNodeCode.value = dropNode.data.menuCode
}

/**
 * 拖拽移出时 - 移除当前高亮
 */
function handleNodeDragLeave(draggingNode, dropNode) {
  if (activeDropNodeCode.value === dropNode.data.menuCode) {
    activeDropNodeCode.value = ''
  }
}

/**
 * 拖拽结束时 - 重置高亮并停止自动滚动
 */
function handleNodeDragEnd() {
  activeDropNodeCode.value = ''
  stopAutoScroll()
}

/**
 * 树节点过滤方法
 */
function filterNode(value, data) {
  if (!value) return true
  const keyword = value.toLowerCase()
  return (
    data.menuCode.toLowerCase().includes(keyword) ||
    data.menuName.toLowerCase().includes(keyword)
  )
}

/**
 * 节点点击 - 选中菜单
 */
function handleNodeClick(data) {
  emit('select', { ...data })
}

/**
 * 拖拽放置规则：
 * 1. 允许 inner 拖入内部（作为子节点），但 9 级接口节点不能作为父节点（下面不能挂任何节点）
 * 2. 允许在同一级菜单内进行位置排序（draggingNode 和 dropNode 拥有相同的上级菜单编码 uppMenuCode）
 */
function handleAllowDrop(draggingNode, dropNode, type) {
  if (type === 'inner') {
    // 接口节点不能挂载子节点
    if (dropNode.data.menuLevel === 9) {
      return false
    }
    return true
  }
  // 在同级内进行排序（保证具有相同的父节点）
  if (type === 'prev' || type === 'next') {
    const parentCode1 = draggingNode.data.uppMenuCode || ''
    const parentCode2 = dropNode.data.uppMenuCode || ''
    return parentCode1.trim() === parentCode2.trim()
  }
  return false
}

/**
 * 拖拽完成：
 * 1. 如果是 inner，调用 dragMenu 接口修改层级和父子关系。
 * 2. 无论哪种放置，拖拽落地后，重新提取该父级节点下的所有最新直属子节点顺序，
 *    自动从 "001"、"002"、"003" ... 开始重新计算递增的 SORT_NO 并批量向后端推送更新！
 */
async function handleNodeDrop(draggingNode, dropNode, dropType) {
  loading.value = true
  activeDropNodeCode.value = ''
  stopAutoScroll()
  try {
    // 1. 如果是 inner 拖拽，调用层级移动接口改变父级关系
    if (dropType === 'inner') {
      const result = await dragMenu({
        menuCode: draggingNode.data.menuCode,
        newUppMenuCode: dropNode.data.menuCode,
        menuScope: props.menuScope,
        tenantId: '047'
      }, props.tempTableName)
      if (result.code !== 200) {
        throw new Error(result.message || '跨级层级移动失败')
      }
    }

    // 2. 提取并刷新该节点所在的新父级节点下所有的直属兄弟节点
    const parentNode = draggingNode.parent
    // 若没有父节点，说明被移动到了最外层（根层级）
    const siblings = parentNode ? parentNode.childNodes : treeRef.value.root.childNodes

    // 遍历所有兄弟节点，自动计算三位递增的排序号
    const updatePromises = []
    for (let i = 0; i < siblings.length; i++) {
      const nodeData = siblings[i].data
      const newSortNo = String(i + 1).padStart(3, '0') // 自动算出 "001", "002", "003"...

      // 如果发生了改变，则发包向后端更新
      if (nodeData.sortNo !== newSortNo) {
        nodeData.sortNo = newSortNo // 先在内存中更新

        // 剔除干扰参数并发包保存
        const { children, ...savePayload } = nodeData
        
        // 前端防御：如果是接口（menuKind='1'或level=9），强制将内存和发包的 menuLevel 均设为 9
        if (savePayload.menuKind === '1' || savePayload.menuLevel === 9 || savePayload.menuLevel === '9') {
          savePayload.menuLevel = 9
          nodeData.menuLevel = 9
        }
        
        updatePromises.push(updateMenu(savePayload, props.tempTableName))
      }
    }

    // 并发执行所有兄弟节点的 SORT_NO 更新
    if (updatePromises.length > 0) {
      await Promise.all(updatePromises)
    }

    ElMessage.success('层级排序及排序号更新成功')
    await loadTree(true) // 保持展开和选中状态，并自动向右侧同步最新属性
  } catch (error) {
    ElMessage.error(error.message || '层级排序保存失败')
    await loadTree(true) // 刷新还原并保持状态
  } finally {
    loading.value = false
  }
}

/**
 * 新增子菜单
 */
function handleAddChild(data) {
  addParentMenu.value = data
  addDialogVisible.value = true
}

/**
 * 新增根菜单（由父组件调用）
 */
function handleAddRoot() {
  addParentMenu.value = null
  addDialogVisible.value = true
}

/**
 * 新增成功回调
 */
async function handleAddSuccess() {
  await loadTree()
  emit('select', null)
}

/**
 * 删除菜单
 */
async function handleDelete(data) {
  const childrenCount = countChildren(data)
  let message = `确定要删除菜单「${data.menuName}」(${data.menuCode})吗？`
  if (childrenCount > 0) {
    message += `\n\n该菜单下有 ${childrenCount} 个子菜单，将一并删除！`
  }

  try {
    await ElMessageBox.confirm(message, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: false
    })

    const result = await deleteMenu(props.menuScope, data.menuCode, '047', props.tempTableName)
    if (result.code === 200) {
      ElMessage.success('删除成功')
      await loadTree()
      emit('select', null)
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 递归统计子菜单数量
 */
function countChildren(node) {
  if (!node.children || node.children.length === 0) return 0
  let count = node.children.length
  for (const child of node.children) {
    count += countChildren(child)
  }
  return count
}

/**
 * 递归统计当前菜单节点下属的 9 级接口总数
 */
function countInterfaces(node) {
  let count = 0
  if (!node) return count
  if (node.children && node.children.length > 0) {
    for (const child of node.children) {
      if (child.menuLevel === 9) {
        count++
      }
      count += countInterfaces(child)
    }
  }
  return count
}

// 监听渠道变化，重新加载树
watch(
  () => props.menuScope,
  () => {
    searchText.value = ''
    loadTree()
  },
  { immediate: true }
)

// 暴露方法给父组件
defineExpose({
  loadTree,
  handleAddRoot
})
</script>

<style scoped>
.menu-tree-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tree-search {
  padding: 0 0 12px 0;
  display: flex;
  gap: 8px;
  align-items: center;
}

.tree-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 4px;
}

/* 强力覆盖 element-plus 树组件的样式，实现极致朴素和紧凑 */
:deep(.el-tree) {
  background: transparent !important;
  color: #303133 !important;
}

:deep(.el-tree-node__content) {
  height: 26px !important; /* 紧凑行高 */
  border-radius: 3px;
  transition: background-color var(--transition-fast) !important;
  padding-right: 6px !important;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa !important; /* 极素雅的浅灰悬浮背景 */
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #e8f3ff !important; /* 温和淡雅的选中蓝色背景 */
  border-left: 2.5px solid #4f8cff;
}

:deep(.el-tree-node__expand-icon) {
  color: #c0c4cc !important;
  padding: 4px !important;
  font-size: 11px !important;
}

:deep(.el-tree-node__expand-icon.expanded) {
  color: #909399 !important;
}

/* 树节点自定义内容 */
.tree-node-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 1px 0;
  overflow: hidden;
}

.tree-node-info {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  flex: 1;
}

.node-icon {
  font-size: 13px; /* 更小图标 */
  color: #909399; /* 朴素灰色，没有任何彩色发光 */
  flex-shrink: 0;
}

.node-code {
  font-size: 10px; /* 更小字体 */
  color: #606266;
  background: #f1f5f9; /* 朴素灰色背景 */
  border: 1px solid #e2e8f0;
  padding: 0 4px;
  border-radius: 3px;
  font-family: 'SF Mono', 'Fira Code', monospace;
  white-space: nowrap;
  flex-shrink: 0;
}

.node-name {
  font-size: 12px; /* 字体更小更朴素 */
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 接口数量统计角标样式 */
.interface-badge {
  font-size: 9px;
  color: #64748b; /* 素净石板灰 */
  background: #f1f5f9; /* 朴素淡灰背景 */
  border: 1px solid #e2e8f0;
  padding: 0 4px;
  border-radius: 8px; /* 圆角胶囊 */
  font-family: 'SF Mono', 'Fira Code', monospace;
  margin-left: 4px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 14px;
  height: 12px;
  line-height: 1;
  flex-shrink: 0;
}

/* 操作按钮 */
.tree-node-actions {
  display: flex;
  align-items: center;
  gap: 3px;
  opacity: 0;
  transition: opacity var(--transition-fast);
  flex-shrink: 0;
  margin-left: 6px;
}

:deep(.el-tree-node__content:hover) .tree-node-actions {
  opacity: 1;
}

.action-icon {
  font-size: 15px; /* 增大图标以利于触达 */
  padding: 4px; /* 增大点击内边距 */
  border-radius: 4px;
  cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.add-icon {
  color: #10b981;
}

.add-icon:hover {
  background: rgba(16, 185, 129, 0.12);
}

.delete-icon {
  color: #ef4444;
}

.delete-icon:hover {
  background: rgba(239, 68, 68, 0.12);
}

/* 拖拽悬停目标菜单节点的高亮样式 */
.tree-node-content.is-drag-target {
  background-color: rgba(79, 140, 255, 0.15) !important;
  border: 1px dashed #4f8cff !important;
  border-radius: 4px;
  box-shadow: 0 0 8px rgba(79, 140, 255, 0.3);
  transition: all 0.15s ease-in-out;
}
</style>
