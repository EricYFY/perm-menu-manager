<template>
  <div class="detail-form-container" v-if="formData">
    <!-- 表单头部 -->
    <div class="form-header">
      <div class="form-title">
        <el-icon class="title-icon"><EditPen /></el-icon>
        <span>菜单详情编辑</span>
      </div>
      <div class="form-actions">
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </div>
    </div>

    <div class="form-body">
      <el-form
        ref="formRef"
        :model="formData"
        label-width="210px"
        label-position="right"
        size="small"
      >
        <el-collapse v-model="activeCollapse">
          <!-- 基础信息 -->
          <el-collapse-item title="基础信息" name="basic">
            <template #title>
              <el-icon class="collapse-icon"><Document /></el-icon>
              <span>基础信息</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="租户号 (TENANT_ID)">
                  <el-input v-model="formData.tenantId" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单渠道 (MENU_SCOPE)">
                  <el-input :model-value="formData.menuScope === '11' ? '11 - PC端' : '12 - APP端'" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单编码 (MENU_CODE)">
                  <el-input v-model="editMenuCode" placeholder="请输入菜单编码" />
                  <div v-if="editMenuCode !== originalMenuCode" class="code-change-action">
                    <span class="warning-tip">⚠️ 修改将级联更新子菜单！</span>
                    <el-button
                      type="primary"
                      size="small"
                      @click="handleUpdateCode"
                      :loading="updatingCode"
                      style="margin-left: 8px;"
                    >
                      确认修改
                    </el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单名称 (MENU_NAME)">
                  <el-input v-model="formData.menuName" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="上级菜单编码 (UPP_MENU_CODE)">
                  <el-input v-model="formData.uppMenuCode" disabled placeholder="（根菜单无上级）" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记录状态 (STAT)">
                  <el-input v-model="formData.stat" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单级别 (MENU_LEVEL)">
                  <el-input v-model.number="formData.menuLevel" type="number" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排序编号 (SORT_NO)">
                  <el-input v-model="formData.sortNo" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 类型配置 -->
          <el-collapse-item name="type">
            <template #title>
              <el-icon class="collapse-icon"><Setting /></el-icon>
              <span>类型配置</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="菜单类型 (MENU_TYPE)">
                  <el-select v-model="formData.menuType" style="width: 100%">
                    <el-option v-for="item in menuTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单分类 (MENU_KIND)">
                  <el-select v-model="formData.menuKind" style="width: 100%">
                    <el-option label="0 - 菜单" value="0" />
                    <el-option label="1 - 事件" value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单选中 (MENU_CHECKED)">
                  <el-select v-model="formData.menuChecked" style="width: 100%">
                    <el-option label="0 - 普通" value="0" />
                    <el-option label="1 - 联动选中" value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="权限校验 (MENU_VERIFY)">
                  <el-select v-model="formData.menuVerify" style="width: 100%">
                    <el-option label="0 - 不校验" value="0" />
                    <el-option label="1 - 校验自身" value="1" />
                    <el-option label="2 - 校验上级" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单显示 (MENU_DISPLAY)">
                  <el-select v-model="formData.menuDisplay" style="width: 100%">
                    <el-option label="0 - 不显示" value="0" />
                    <el-option label="1 - 显示" value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 服务配置 -->
          <el-collapse-item name="service">
            <template #title>
              <el-icon class="collapse-icon"><Connection /></el-icon>
              <span>服务配置</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="服务码 (TR_CODE)">
                  <el-input v-model="formData.trCode" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="安全验证服务码 (SECURITY_TR_CODE)">
                  <el-input v-model="formData.securityTrCode" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="服务控制属性 (CTRL_ATTI)">
                  <el-input v-model="formData.ctrlAtti" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="服务业务属性 (BIZ_ATTI)">
                  <el-input v-model="formData.bizAtti" placeholder="可选" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 授权与审批 -->
          <el-collapse-item name="auth">
            <template #title>
              <el-icon class="collapse-icon"><Lock /></el-icon>
              <span>授权与审批</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="账户授权属性 (ACCT_AUTH_ATTI)">
                  <el-input v-model="formData.acctAuthAtti" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="账簿授权属性 (ASAC_AUTH_ATTI)">
                  <el-input v-model="formData.asacAuthAtti" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="审批标志 (WORKFLOW_FLAG)">
                  <el-select v-model="formData.workflowFlag" style="width: 100%">
                    <el-option label="0 - 否" value="0" />
                    <el-option label="1 - 支持" value="1" />
                    <el-option label="2 - 强制" value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="审批业务类型 (WORKFLOW_BIZ_TYPE)">
                  <el-select v-model="formData.workflowBizType" style="width: 100%" clearable>
                    <el-option label="1 - 管理类" value="1" />
                    <el-option label="2 - 有金融交易类" value="2" />
                    <el-option label="3 - 无金融交易类" value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 角色权限 -->
          <el-collapse-item name="role">
            <template #title>
              <el-icon class="collapse-icon"><User /></el-icon>
              <span>角色权限</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="管理员菜单 (IS_ADMIN)">
                  <el-switch
                    v-model="formData.isAdmin"
                    active-value="1"
                    inactive-value="0"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作员菜单 (IS_OPERATOR)">
                  <el-switch
                    v-model="formData.isOperator"
                    active-value="1"
                    inactive-value="0"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="普通用户菜单 (IS_USER)">
                  <el-switch
                    v-model="formData.isUser"
                    active-value="1"
                    inactive-value="0"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 链接与显示 -->
          <el-collapse-item name="display">
            <template #title>
              <el-icon class="collapse-icon"><Link /></el-icon>
              <span>链接与显示</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="菜单图标 (MENU_ICON)">
                  <el-input v-model="formData.menuIcon" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单链接类型 (MENU_HERF_TYPE)">
                  <el-select v-model="formData.menuHerfType" style="width: 100%" clearable>
                    <el-option label="1 - 标准菜单链接" value="1" />
                    <el-option label="2 - 原生菜单链接" value="2" />
                    <el-option label="3 - 外部菜单链接" value="3" />
                    <el-option label="4 - 其他" value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单链接 (MENU_HERF)">
                  <el-input v-model="formData.menuHerf" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="跳转访问地址 (JUMP_HERF)">
                  <el-input v-model="formData.jumpHerf" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="菜单属性 (MENU_ATTRIBUTE)">
                  <el-input v-model="formData.menuAttribute" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="新图标库 (ICON_FLAG)">
                  <el-switch
                    v-model="formData.iconFlag"
                    active-value="1"
                    inactive-value="0"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="页面缓存 (IS_KEEP_ALIVE)">
                  <el-switch
                    v-model="formData.isKeepAlive"
                    active-value="1"
                    inactive-value="0"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>

          <!-- 其他信息 -->
          <el-collapse-item name="other">
            <template #title>
              <el-icon class="collapse-icon"><More /></el-icon>
              <span>其他信息</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="系统编码 (SUBSYSTEM_CODE)">
                  <el-input v-model="formData.subsystemCode" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="文件夹编码 (FOLDER_CODE)">
                  <el-input v-model="formData.folderCode" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="业务分类编号 (BIZ_CATEGORY_NO)">
                  <el-input v-model="formData.bizCategoryNo" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="业务分类名称 (BIZ_CATEGORY_NAME)">
                  <el-input v-model="formData.bizCategoryName" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="时间段属性 (TIME_ATTI)">
                  <el-input v-model="formData.timeAtti" placeholder="可选，如：1080000170000" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="版本号 (TB_VERSION)">
                  <el-input v-model="formData.tbVersion" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="描述 (DESCRIPTION)">
                  <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="可选" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </div>
  </div>

  <!-- 未选中状态 -->
  <div class="empty-state" v-else>
    <div class="empty-content animate-fade-in">
      <el-icon class="empty-icon"><Pointer /></el-icon>
      <h3>请选择一个菜单节点</h3>
      <p>点击左侧菜单树中的节点，即可在此处编辑菜单详情</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  EditPen, RefreshLeft, Check, Document, Setting,
  Connection, Lock, User, Link, More, Pointer
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateMenu, updateMenuCode } from '../api/menu.js'

const props = defineProps({
  /** 选中的菜单数据 */
  modelValue: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

// 表单数据（深拷贝避免直接修改 props）
const formData = ref(null)
const formRef = ref(null)
const saving = ref(false)
const updatingCode = ref(false)

// 菜单编码编辑（单独管理，因修改编码是独立操作）
const editMenuCode = ref('')
const originalMenuCode = ref('')

// 折叠面板默认展开项
const activeCollapse = ref(['basic', 'type'])

// 菜单类型选项
const menuTypeOptions = [
  { label: 'A - 查询类', value: 'A' },
  { label: 'B - 财务类', value: 'B' },
  { label: 'C - 管理类', value: 'C' },
  { label: 'E - 导出类', value: 'E' },
  { label: 'F1 - 文件上传', value: 'F1' },
  { label: 'F2 - 文件下载', value: 'F2' },
  { label: 'L - 登录类', value: 'L' },
  { label: 'S - 安全类', value: 'S' },
  { label: 'T - 定时类', value: 'T' },
  { label: 'P - 调度类', value: 'P' },
  { label: 'O - 其它', value: 'O' }
]

/**
 * 监听选中菜单变化，初始化表单数据
 */
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      // 深拷贝，不包含 children
      const { children, ...data } = newVal
      formData.value = { ...data }
      editMenuCode.value = data.menuCode
      originalMenuCode.value = data.menuCode
    } else {
      formData.value = null
      editMenuCode.value = ''
      originalMenuCode.value = ''
    }
  },
  { immediate: true, deep: true }
)

/**
 * 重置表单
 */
function handleReset() {
  if (props.modelValue) {
    const { children, ...data } = props.modelValue
    formData.value = { ...data }
    editMenuCode.value = data.menuCode
  }
}

/**
 * 保存菜单（普通字段更新）
 */
async function handleSave() {
  saving.value = true
  try {
    const result = await updateMenu(formData.value)
    if (result.code === 200) {
      ElMessage.success('保存成功')
      emit('refresh')
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

/**
 * 修改菜单编码（含级联更新子菜单）
 */
async function handleUpdateCode() {
  if (!editMenuCode.value.trim()) {
    ElMessage.warning('菜单编码不能为空')
    return
  }

  try {
    await ElMessageBox.confirm(
      `将菜单编码从「${originalMenuCode.value}」修改为「${editMenuCode.value}」，所有子菜单的上级菜单编码将同步更新。是否继续？`,
      '修改菜单编码确认',
      {
        confirmButtonText: '确认修改',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    updatingCode.value = true
    const result = await updateMenuCode({
      oldMenuCode: originalMenuCode.value,
      newMenuCode: editMenuCode.value.trim(),
      menuScope: formData.value.menuScope,
      tenantId: formData.value.tenantId
    })

    if (result.code === 200) {
      ElMessage.success('菜单编码修改成功')
      originalMenuCode.value = editMenuCode.value.trim()
      formData.value.menuCode = editMenuCode.value.trim()
      emit('refresh')
    } else {
      ElMessage.error(result.message || '修改失败')
      editMenuCode.value = originalMenuCode.value
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('修改失败')
      editMenuCode.value = originalMenuCode.value
    }
  } finally {
    updatingCode.value = false
  }
}
</script>

<style scoped>
.detail-form-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 表单头部 */
.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
}

.form-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.title-icon {
  font-size: 22px;
  color: var(--accent-primary);
}

.form-actions {
  display: flex;
  gap: 10px;
}

/* 表单内容 */
.form-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

/* 折叠面板图标 */
.collapse-icon {
  margin-right: 8px;
  color: var(--accent-primary);
}

/* 强力紧凑化 Element Plus 表单项 */
:deep(.el-form-item) {
  margin-bottom: 8px !important; /* 压缩行间距 */
}

:deep(.el-form-item__label) {
  font-size: 12px !important; /* 字体更小 */
  color: var(--text-secondary) !important;
  font-weight: 500;
}

:deep(.el-collapse-item__header) {
  font-size: 13px !important; /* 折叠面板标题更小 */
  height: 36px !important; /* 面板头部变紧凑 */
  line-height: 36px !important;
}

:deep(.el-collapse-item__content) {
  padding: 12px 0 4px !important; /* 压缩面板折叠内容的内间距 */
}

/* 空状态 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-content {
  text-align: center;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 64px;
  color: rgba(79, 140, 255, 0.2);
  margin-bottom: 20px;
}

.empty-content h3 {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.empty-content p {
  font-size: 14px;
  color: var(--text-muted);
}

/* 菜单编码修改提示栏样式 */
.code-change-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
  background: #fffbeb !important;
  border: 1px solid #fde68a !important;
  padding: 4px 8px;
  border-radius: 4px;
  width: 100%;
}

.warning-tip {
  font-size: 11px;
  color: #d97706;
  font-weight: 500;
}
</style>
