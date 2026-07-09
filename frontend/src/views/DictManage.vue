<template>
  <div class="dict-manage-page">
    <!-- 顶部标题栏 -->
    <header class="page-header animate-fade-in">
      <div class="header-left">
        <el-icon class="header-logo"><Collection /></el-icon>
        <h1 class="gradient-text">字典管理</h1>
      </div>
      <div class="header-actions">
        <span style="font-size: 14px; color: var(--text-regular);">租户号：</span>
        <el-input
          v-model="tenantId"
          style="width: 110px;"
          placeholder="TenantId"
          @change="handleRefresh"
        />
      </div>
    </header>

    <!-- 主体内容：左侧 DICT_ID 列表 + 右侧详情 -->
    <div class="main-content">
      <el-row :gutter="20" class="content-row">
        <!-- 左侧：DICT_ID 列表 -->
        <el-col :span="8">
          <div class="plain-light-panel list-panel animate-slide-left">
            <div class="panel-header">
              <h3 class="panel-title">
                <el-icon><Folder /></el-icon>
                字典 ID 列表
              </h3>
              <el-button type="primary" size="small" @click="openAddGroupDialog">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
            <div class="panel-body">
              <div style="padding: 0 16px 10px 16px;">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索字典 ID..."
                  clearable
                />
              </div>
              <div v-if="loadingIds" class="loading-wrapper">
                <el-skeleton :rows="8" animated />
              </div>
              <el-menu
                v-else
                :default-active="selectedDictId"
                @select="handleSelectDictId"
                class="dict-id-menu"
              >
                <el-menu-item
                  v-for="item in filteredDictIds"
                  :key="item.dictId"
                  :index="item.dictId"
                >
                  <el-icon><Document /></el-icon>
                  <span class="dict-id-text" :title="item.remark">{{ item.dictId }} <span v-if="item.remark" class="dict-remark">({{ item.remark }})</span></span>
                  <el-button
                    type="danger"
                    size="small"
                    plain
                    class="delete-group-btn"
                    @click.stop="handleDeleteGroup(item.dictId)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-menu-item>
                <div v-if="dictIds.length === 0" class="empty-tip">暂无字典数据</div>
              </el-menu>
            </div>
          </div>
        </el-col>

        <!-- 右侧：字典条目详情 -->
        <el-col :span="16">
          <div class="plain-light-panel detail-panel animate-slide-right">
            <div class="panel-header">
              <h3 class="panel-title">
                <el-icon><List /></el-icon>
                <span v-if="selectedDictId">{{ selectedDictId }} 的字典条目</span>
                <span v-else style="color: var(--text-placeholder);">请从左侧选择一个字典 ID</span>
              </h3>
              <el-button
                v-if="selectedDictId"
                type="primary"
                size="small"
                @click="openAddEntryDialog"
              >
                <el-icon><Plus /></el-icon>
                新增条目
              </el-button>
            </div>
            <div class="panel-body">
              <div v-if="!selectedDictId" class="empty-tip" style="margin-top: 60px;">
                <el-empty description="请从左侧选择一个字典 ID 查看其条目" />
              </div>
              <div v-else-if="loadingEntries" class="loading-wrapper">
                <el-skeleton :rows="6" animated />
              </div>
              <el-table
                v-else
                :data="dictEntries"
                stripe
                style="width: 100%"
                :max-height="tableMaxHeight"
              >
                <el-table-column prop="dictKey" label="字典键值 (DICT_KEY)" min-width="140" show-overflow-tooltip />
                <el-table-column prop="dictValue" label="字典值 (DICT_VALUE)" min-width="160" show-overflow-tooltip />
                <el-table-column prop="stat" label="状态 (STAT)" width="100" />
                <el-table-column prop="enumKey" label="枚举键值 (ENUM_KEY)" min-width="140" show-overflow-tooltip />
                <el-table-column prop="relKey" label="关联键值 (REL_KEY)" min-width="140" show-overflow-tooltip />
                <el-table-column prop="scene" label="场景 (SCENE)" width="100" show-overflow-tooltip />
                <el-table-column prop="sortNo" label="排序编号 (SORT_NO)" width="140" />
                <el-table-column prop="remark" label="说明 (REMARK)" min-width="140" show-overflow-tooltip />
                <el-table-column label="操作" width="110" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" link @click="openEditEntryDialog(row)">
                      编辑
                    </el-button>
                    <el-button type="danger" size="small" link @click="handleDeleteEntry(row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 新增字典组弹窗 -->
    <el-dialog v-model="addGroupDialogVisible" title="新增字典 ID" width="460px" :close-on-click-modal="false">
      <el-form :model="newGroupForm" label-width="150px" ref="newGroupFormRef" :rules="groupRules">
        <el-form-item label="字典ID (DICT_ID)" prop="dictId">
          <el-input v-model="newGroupForm.dictId" placeholder="请输入字典ID" />
        </el-form-item>
        <el-form-item label="字典键值 (DICT_KEY)" prop="dictKey">
          <el-input v-model="newGroupForm.dictKey" placeholder="请输入首条字典键值" />
        </el-form-item>
        <el-form-item label="字典值 (DICT_VALUE)" prop="dictValue">
          <el-input v-model="newGroupForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="状态 (STAT)" prop="stat">
          <el-input v-model="newGroupForm.stat" placeholder="请输入状态" />
        </el-form-item>
        <el-form-item label="场景 (SCENE)" prop="scene">
          <el-input v-model="newGroupForm.scene" placeholder="请输入场景" />
        </el-form-item>
        <el-form-item label="排序编号 (SORT_NO)" prop="sortNo">
          <el-input v-model="newGroupForm.sortNo" placeholder="排序号" />
        </el-form-item>
        <el-form-item label="枚举键值 (ENUM_KEY)">
          <el-input v-model="newGroupForm.enumKey" placeholder="可选" />
        </el-form-item>
        <el-form-item label="关联键值 (REL_KEY)">
          <el-input v-model="newGroupForm.relKey" placeholder="可选" />
        </el-form-item>
        <el-form-item label="说明 (REMARK)">
          <el-input v-model="newGroupForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAddGroup">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑字典条目弹窗 -->
    <el-dialog
      v-model="entryDialogVisible"
      :title="entryDialogMode === 'add' ? '新增字典条目' : '编辑字典条目'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="entryForm" label-width="150px" ref="entryFormRef" :rules="entryRules">
        <el-form-item label="字典ID (DICT_ID)">
          <el-input :value="selectedDictId" disabled />
        </el-form-item>
        <el-form-item label="字典键值 (DICT_KEY)" prop="dictKey">
          <el-input v-model="entryForm.dictKey" :disabled="entryDialogMode === 'edit'" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="字典值 (DICT_VALUE)" prop="dictValue">
          <el-input v-model="entryForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="状态 (STAT)" prop="stat">
          <el-input v-model="entryForm.stat" placeholder="请输入状态" />
        </el-form-item>
        <el-form-item label="场景 (SCENE)" prop="scene">
          <el-input v-model="entryForm.scene" placeholder="请输入场景" />
        </el-form-item>
        <el-form-item label="排序编号 (SORT_NO)" prop="sortNo">
          <el-input v-model="entryForm.sortNo" placeholder="排序号" />
        </el-form-item>
        <el-form-item label="枚举键值 (ENUM_KEY)">
          <el-input v-model="entryForm.enumKey" placeholder="可选" />
        </el-form-item>
        <el-form-item label="关联键值 (REL_KEY)">
          <el-input v-model="entryForm.relKey" placeholder="可选" />
        </el-form-item>
        <el-form-item label="说明 (REMARK)">
          <el-input v-model="entryForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveEntry">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Collection, Folder, Document, List, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDictIds,
  getDictEntries,
  addDictEntry,
  updateDictEntry,
  deleteDictEntry,
  deleteDictGroup
} from '../api/dict.js'

const tenantId = ref('047')
const dictIds = ref([])
const selectedDictId = ref('')
const dictEntries = ref([])
const loadingIds = ref(false)
const loadingEntries = ref(false)
const saving = ref(false)

const searchQuery = ref('')
const filteredDictIds = computed(() => {
  if (!searchQuery.value) return dictIds.value
  const lowerQuery = searchQuery.value.toLowerCase()
  return dictIds.value.filter(item => 
    (item.dictId && item.dictId.toLowerCase().includes(lowerQuery)) ||
    (item.remark && item.remark.toLowerCase().includes(lowerQuery))
  )
})

// 计算表格最大高度
const tableMaxHeight = computed(() => window.innerHeight - 280)

// 新增字典组弹窗
const addGroupDialogVisible = ref(false)
const newGroupFormRef = ref(null)
const newGroupForm = ref({
  dictId: '',
  dictKey: '',
  dictValue: '',
  stat: '1',
  scene: '',
  sortNo: '1',
  enumKey: '',
  relKey: '',
  remark: ''
})
const groupRules = {
  dictId: [{ required: true, message: '请输入字典ID', trigger: 'blur' }],
  dictKey: [{ required: true, message: '请输入字典键值', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  scene: [{ required: true, message: '请输入场景', trigger: 'blur' }],
  sortNo: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

// 新增/编辑字典条目弹窗
const entryDialogVisible = ref(false)
const entryDialogMode = ref('add') // 'add' | 'edit'
const entryFormRef = ref(null)
const entryForm = ref({
  dictKey: '',
  dictValue: '',
  stat: '1',
  scene: '',
  sortNo: '1',
  enumKey: '',
  relKey: '',
  remark: ''
})
const entryRules = {
  dictKey: [{ required: true, message: '请输入字典键值', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  scene: [{ required: true, message: '请输入场景', trigger: 'blur' }],
  sortNo: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

onMounted(() => {
  loadDictIds()
})

/** 加载 DICT_ID 列表 */
async function loadDictIds() {
  loadingIds.value = true
  try {
    const res = await getDictIds(tenantId.value)
    if (res.code === 200) {
      dictIds.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载字典ID列表失败')
    }
  } catch (e) {
    ElMessage.error('请求失败：' + e.message)
  } finally {
    loadingIds.value = false
  }
}

/** 加载指定 DICT_ID 下的条目 */
async function loadEntries(dictId) {
  loadingEntries.value = true
  try {
    const res = await getDictEntries(dictId, tenantId.value)
    if (res.code === 200) {
      dictEntries.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载字典条目失败')
    }
  } catch (e) {
    ElMessage.error('请求失败：' + e.message)
  } finally {
    loadingEntries.value = false
  }
}

/** 选中左侧 DICT_ID */
function handleSelectDictId(id) {
  selectedDictId.value = id
  loadEntries(id)
}

/** 刷新（租户号变更时） */
function handleRefresh() {
  dictIds.value = []
  selectedDictId.value = ''
  dictEntries.value = []
  loadDictIds()
}

/** 打开新增字典组弹窗 */
function openAddGroupDialog() {
  newGroupForm.value = { dictId: '', dictKey: '', dictValue: '', stat: '1', scene: '', sortNo: '1', enumKey: '', relKey: '', remark: '' }
  addGroupDialogVisible.value = true
}

/** 确定新增字典组（含首条条目） */
async function handleAddGroup() {
  await newGroupFormRef.value?.validate()
  saving.value = true
  try {
    const dict = {
      tenantId: tenantId.value,
      stat: newGroupForm.value.stat || '1',
      dictId: newGroupForm.value.dictId,
      dictKey: newGroupForm.value.dictKey,
      dictValue: newGroupForm.value.dictValue,
      scene: newGroupForm.value.scene,
      sortNo: newGroupForm.value.sortNo,
      enumKey: newGroupForm.value.enumKey || null,
      relKey: newGroupForm.value.relKey || null,
      remark: newGroupForm.value.remark || null
    }
    const res = await addDictEntry(dict)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      addGroupDialogVisible.value = false
      await loadDictIds()
      // 自动选中新增的字典组
      selectedDictId.value = newGroupForm.value.dictId
      await loadEntries(selectedDictId.value)
    } else {
      ElMessage.error(res.message || '新增失败')
    }
  } catch (e) {
    if (e?.message) ElMessage.error('操作失败：' + e.message)
  } finally {
    saving.value = false
  }
}

/** 删除整个字典组 */
function handleDeleteGroup(dictId) {
  ElMessageBox.confirm(
    `确认删除字典组 "${dictId}" 及其下所有条目吗？此操作不可撤销。`,
    '危险操作',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
  ).then(async () => {
    try {
      const res = await deleteDictGroup(dictId, tenantId.value)
      if (res.code === 200) {
        ElMessage.success('已删除')
        if (selectedDictId.value === dictId) {
          selectedDictId.value = ''
          dictEntries.value = []
        }
        await loadDictIds()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败：' + e.message)
    }
  }).catch(() => {})
}

/** 打开新增条目弹窗 */
function openAddEntryDialog() {
  entryDialogMode.value = 'add'
  entryForm.value = { dictKey: '', dictValue: '', stat: '1', scene: '', sortNo: '1', enumKey: '', relKey: '', remark: '' }
  entryDialogVisible.value = true
}

/** 打开编辑条目弹窗 */
function openEditEntryDialog(row) {
  entryDialogMode.value = 'edit'
  entryForm.value = { ...row }
  entryDialogVisible.value = true
}

/** 保存条目（新增或更新） */
async function handleSaveEntry() {
  await entryFormRef.value?.validate()
  saving.value = true
  try {
    const dict = {
      tenantId: tenantId.value,
      stat: entryForm.value.stat || '1',
      dictId: selectedDictId.value,
      dictKey: entryForm.value.dictKey,
      dictValue: entryForm.value.dictValue,
      scene: entryForm.value.scene,
      sortNo: entryForm.value.sortNo,
      enumKey: entryForm.value.enumKey || null,
      relKey: entryForm.value.relKey || null,
      remark: entryForm.value.remark || null
    }
    const res = entryDialogMode.value === 'add'
      ? await addDictEntry(dict)
      : await updateDictEntry(dict)

    if (res.code === 200) {
      ElMessage.success(entryDialogMode.value === 'add' ? '新增成功' : '更新成功')
      entryDialogVisible.value = false
      await loadEntries(selectedDictId.value)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e?.message) ElMessage.error('操作失败：' + e.message)
  } finally {
    saving.value = false
  }
}

/** 删除单条字典条目 */
function handleDeleteEntry(row) {
  ElMessageBox.confirm(
    `确认删除字典条目 "${row.dictKey}" 吗？`,
    '确认删除',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await deleteDictEntry(row.dictId, row.dictKey, row.tenantId)
      if (res.code === 200) {
        ElMessage.success('已删除')
        await loadEntries(selectedDictId.value)
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败：' + e.message)
    }
  }).catch(() => {})
}
</script>

<style scoped>
.dict-manage-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo {
  font-size: 28px;
  color: var(--accent-primary);
  filter: drop-shadow(0 0 8px rgba(79, 140, 255, 0.4));
}

.page-header h1 {
  font-size: 22px;
  font-weight: 700;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

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

.list-panel,
.detail-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.plain-light-panel {
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: var(--radius-lg, 8px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04) !important;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebedf0;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.panel-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

/* 左侧 DICT_ID 菜单 */
.dict-id-menu {
  border-right: none !important;
}

.dict-id-menu :deep(.el-menu-item) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 6px;
  margin-bottom: 2px;
}

.dict-id-menu :deep(.el-menu-item span.dict-id-text) {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dict-remark {
  color: var(--text-placeholder, #909399);
  font-size: 12px;
  margin-left: 4px;
}

.delete-group-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.dict-id-menu :deep(.el-menu-item:hover) .delete-group-btn {
  opacity: 1;
}

.empty-tip {
  color: var(--text-placeholder, #c0c4cc);
  text-align: center;
  padding: 20px 0;
  font-size: 14px;
}

.loading-wrapper {
  padding: 12px;
}
</style>
