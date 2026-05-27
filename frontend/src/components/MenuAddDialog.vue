<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="parentMenu ? `新增子菜单 - 上级：${parentMenu.menuCode} ${parentMenu.menuName}` : '新增根菜单'"
    width="960px"
    :close-on-click-modal="false"
    destroy-on-close
    append-to-body
    align-center
    @open="handleOpen"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="210px"
      label-position="right"
      size="small"
    >
      <el-row :gutter="20">
        <!-- 基础信息组 -->
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
          <el-form-item label="菜单编码 (MENU_CODE)" prop="menuCode">
            <el-input v-model="formData.menuCode" placeholder="请输入菜单编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单名称 (MENU_NAME)" prop="menuName">
            <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="上级菜单编码 (UPP_MENU_CODE)">
            <el-input v-model="formData.uppMenuCode" disabled placeholder="（根菜单无上级）" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="记录状态 (STAT)">
            <el-input v-model="formData.stat" placeholder="默认 1" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单级别 (MENU_LEVEL)">
            <el-input v-model.number="formData.menuLevel" type="number" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序编号 (SORT_NO)">
            <el-input v-model="formData.sortNo" placeholder="请输入排序编号，如 01" />
          </el-form-item>
        </el-col>

        <!-- 类型与校验组 -->
        <el-col :span="12">
          <el-form-item label="菜单类型 (MENU_TYPE)" prop="menuType">
            <el-select v-model="formData.menuType" style="width: 100%">
              <el-option v-for="item in menuTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单分类 (MENU_KIND)" prop="menuKind">
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

        <!-- 服务接口组 -->
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
            <el-input v-model="formData.ctrlAtti" placeholder="默认 000000000100000" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="服务业务属性 (BIZ_ATTI)">
            <el-input v-model="formData.bizAtti" placeholder="可选" />
          </el-form-item>
        </el-col>

        <!-- 授权与审批组 -->
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
            <el-select v-model="formData.workflowBizType" style="width: 100%" clearable placeholder="可选">
              <el-option label="1 - 管理类" value="1" />
              <el-option label="2 - 有金融交易类" value="2" />
              <el-option label="3 - 无金融交易类" value="3" />
            </el-select>
          </el-form-item>
        </el-col>

        <!-- 角色权限组 -->
        <el-col :span="8">
          <el-form-item label="管理员菜单 (IS_ADMIN)">
            <el-switch v-model="formData.isAdmin" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="操作员菜单 (IS_OPERATOR)">
            <el-switch v-model="formData.isOperator" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="用户菜单 (IS_USER)">
            <el-switch v-model="formData.isUser" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-col>

        <!-- 链接与跳转（超长输入框，独占一行） -->
        <el-col :span="12">
          <el-form-item label="菜单链接类型 (MENU_HERF_TYPE)">
            <el-select v-model="formData.menuHerfType" style="width: 100%" clearable placeholder="可选">
              <el-option label="1 - 标准菜单链接" value="1" />
              <el-option label="2 - 原生菜单链接" value="2" />
              <el-option label="3 - 外部菜单链接" value="3" />
              <el-option label="4 - 其他" value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="菜单图标 (MENU_ICON)">
            <el-input v-model="formData.menuIcon" placeholder="可选" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="菜单链接 (MENU_HERF)">
            <el-input v-model="formData.menuHerf" placeholder="请输入菜单链接（可选）" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="跳转访问地址 (JUMP_HERF)">
            <el-input v-model="formData.jumpHerf" placeholder="请输入跳转目标地址（可选）" />
          </el-form-item>
        </el-col>

        <!-- 分类与标识 -->
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
          <el-form-item label="菜单属性 (MENU_ATTRIBUTE)">
            <el-input v-model="formData.menuAttribute" placeholder="默认 10000000" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="版本号 (TB_VERSION)">
            <el-input v-model="formData.tbVersion" placeholder="默认 3.0.0" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="新图标库 (ICON_FLAG)">
            <el-switch v-model="formData.iconFlag" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="页面缓存 (IS_KEEP_ALIVE)">
            <el-switch v-model="formData.isKeepAlive" active-value="1" inactive-value="0" />
          </el-form-item>
        </el-col>

        <!-- 扩展大输入框（独占一行） -->
        <el-col :span="24">
          <el-form-item label="时间段属性 (TIME_ATTI)">
            <el-input v-model="formData.timeAtti" placeholder="可选，格式如：1080000170000" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="描述 (DESCRIPTION)">
            <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述信息（可选）" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { addMenu } from '../api/menu.js'

const props = defineProps({
  /** 弹窗可见性 */
  modelValue: {
    type: Boolean,
    default: false
  },
  /** 菜单渠道 */
  menuScope: {
    type: String,
    required: true
  },
  /** 父菜单数据（为 null 时新增根菜单） */
  parentMenu: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)

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

// 表单数据
const formData = reactive(getDefaultFormData())

// 验证规则
const formRules = {
  menuCode: [
    { required: true, message: '请输入菜单编码', trigger: 'blur' }
  ],
  menuName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  menuType: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ],
  menuKind: [
    { required: true, message: '请选择菜单分类', trigger: 'change' }
  ]
}

/**
 * 获取默认表单数据
 */
function getDefaultFormData() {
  return {
    tenantId: '047',
    stat: '1',
    menuScope: '',
    menuCode: '',
    menuName: '',
    menuLevel: 1,
    menuType: 'A',
    uppMenuCode: null,
    menuChecked: '0',
    menuKind: '0',
    menuVerify: '1',
    menuDisplay: '1',
    trCode: null,
    securityTrCode: null,
    ctrlAtti: '000000000100000',
    bizAtti: null,
    acctAuthAtti: null,
    asacAuthAtti: null,
    timeAtti: null,
    workflowFlag: '0',
    workflowBizType: null,
    isAdmin: '0',
    isOperator: '0',
    isUser: '0',
    sortNo: '01',
    subsystemCode: null,
    folderCode: null,
    bizCategoryNo: null,
    bizCategoryName: null,
    menuIcon: null,
    menuHerfType: null,
    menuHerf: null,
    menuAttribute: '10000000',
    iconFlag: null,
    isKeepAlive: null,
    jumpHerf: null,
    tbVersion: '3.0.0',
    description: null
  }
}

/**
 * 弹窗打开时初始化表单
 */
function handleOpen() {
  Object.assign(formData, getDefaultFormData())
  formData.menuScope = props.menuScope

  if (props.parentMenu) {
    formData.uppMenuCode = props.parentMenu.menuCode
    formData.menuLevel = (props.parentMenu.menuLevel || 0) + 1
  } else {
    formData.uppMenuCode = null
    formData.menuLevel = 1
  }
}

/**
 * 提交新增
 */
async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const result = await addMenu({ ...formData })
    if (result.code === 200) {
      ElMessage.success('新增菜单成功')
      emit('update:modelValue', false)
      emit('success')
    } else {
      ElMessage.error(result.message || '新增失败')
    }
  } catch (error) {
    ElMessage.error('新增失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 强力紧凑化新增弹窗内表单项 */
:deep(.el-form-item) {
  margin-bottom: 8px !important; /* 压缩每一行间距 */
}

:deep(.el-form-item__label) {
  font-size: 12px !important; /* 字体更小更精致 */
  color: var(--text-secondary) !important;
  font-weight: 500;
}

:deep(.el-dialog__body) {
  padding: 16px 24px !important; /* 精简弹窗主体间距 */
}
</style>
