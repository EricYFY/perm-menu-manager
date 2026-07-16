<template>
  <div class="role-button-manage">
    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="交易模块(trans_module)">
            <el-select v-model="searchForm.transModule" placeholder="请选择交易模块" clearable style="width: 260px;">
              <el-option v-for="opt in options.transModule" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
          </el-form-item>
          <el-form-item label="产品代码(busi_type)">
            <el-select v-model="searchForm.busiType" placeholder="请选择产品代码" clearable style="width: 260px;">
              <el-option v-for="opt in options.busiType" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户角色(user_role)">
            <el-select v-model="searchForm.userRole" placeholder="请选择用户角色" clearable style="width: 260px;">
              <el-option v-for="opt in options.userRole" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="success" @click="handleAddGroup">新增</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 分组数据表格 -->
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column label="交易模块(trans_module)">
          <template #default="{ row }">
            {{ row.transModule }}<span v-if="row.transModuleDesc"> - {{ row.transModuleDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column label="产品代码(busi_type)">
          <template #default="{ row }">
            {{ row.busiType }}<span v-if="row.busiTypeDesc"> - {{ row.busiTypeDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户角色(user_role)">
          <template #default="{ row }">
            {{ row.userRole }}<span v-if="row.userRoleDesc"> - {{ row.userRoleDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="btnCount" label="按钮数量" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetails(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog :title="detailsState.title" v-model="detailsState.visible" width="900px" destroy-on-close>
      <div style="margin-bottom: 15px;">
        <el-button type="success" @click="handleAddBtn">新增按钮</el-button>
      </div>
      <el-table :data="detailsState.data" v-loading="detailsState.loading" border style="width: 100%">
        <el-table-column label="功能按钮(fun_button)">
          <template #default="{ row }">
            {{ row.funButton }}<span v-if="row.funButtonDesc"> - {{ row.funButtonDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="seq" label="顺序号(seq)" width="100" />
        <el-table-column prop="dataStatus" label="数据状态(data_status)" width="120" />
        <el-table-column prop="conditionExpr" label="条件表达式(condition_expr)" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注(remark)" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditBtn(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDeleteBtn(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增分组弹窗 (支持添加多个按钮) -->
    <el-dialog title="新增分组及按钮" v-model="groupDialogVisible" width="1000px" append-to-body>
      <el-form :model="groupFormData" :rules="groupRules" ref="groupFormRef" label-width="180px">
        <el-form-item label="交易模块(trans_module)" prop="transModule">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="groupFormData.transModule" placeholder="请选择交易模块" @change="val => handleGroupOptionChange('transModule', val)" style="flex: 1">
              <el-option v-for="opt in options.transModule" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('transModule', '交易模块')">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="交易模块描述(trans_module_desc)" prop="transModuleDesc">
          <el-input v-model="groupFormData.transModuleDesc" placeholder="关联描述(自动带出或手动输入)" />
        </el-form-item>

        <el-form-item label="产品代码(busi_type)" prop="busiType">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="groupFormData.busiType" placeholder="请选择产品代码" @change="val => handleGroupOptionChange('busiType', val)" style="flex: 1">
              <el-option v-for="opt in options.busiType" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('busiType', '产品代码')">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="产品代码描述(busi_type_desc)" prop="busiTypeDesc">
          <el-input v-model="groupFormData.busiTypeDesc" placeholder="关联描述(自动带出或手动输入)" />
        </el-form-item>

        <el-form-item label="用户角色(user_role)" prop="userRole">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="groupFormData.userRole" placeholder="请选择用户角色" @change="val => handleGroupOptionChange('userRole', val)" style="flex: 1">
              <el-option v-for="opt in options.userRole" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('userRole', '用户角色')">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="角色描述(user_role_desc)" prop="userRoleDesc">
          <el-input v-model="groupFormData.userRoleDesc" placeholder="关联描述(自动带出或手动输入)" />
        </el-form-item>

        <el-divider>功能按钮配置</el-divider>
        <el-button type="success" @click="addGroupButton" style="margin-bottom: 15px;">添加一行按钮</el-button>
        <el-table :data="groupFormData.buttons" border style="width: 100%">
          <el-table-column label="功能按钮(fun_button)" width="220">
            <template #default="{ row, $index }">
              <div style="display: flex; gap: 5px;">
                <el-select v-model="row.funButton" placeholder="请选择" @change="val => handleBtnOptionChange($index, val)" style="flex: 1;">
                  <el-option v-for="opt in options.funButton" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
                </el-select>
                <el-button size="small" @click="openAddOption('funButton', '功能按钮')">新</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="按钮描述(fun_button_desc)" width="180">
            <template #default="{ row }">
              <el-input v-model="row.funButtonDesc" placeholder="自动带出" />
            </template>
          </el-table-column>
          <el-table-column label="顺序号(seq)" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.seq" :min="1" controls-position="right" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="数据状态(data_status)" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.dataStatus" placeholder="请输入数据状态" />
            </template>
          </el-table-column>
          <el-table-column label="条件表达式(condition_expr)" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.conditionExpr" placeholder="请输入条件表达式" />
            </template>
          </el-table-column>
          <el-table-column label="备注(remark)" min-width="120">
            <template #default="{ row }">
              <el-input v-model="row.remark" placeholder="请输入备注" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link @click="removeGroupButton($index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGroupForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 (复用) -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" append-to-body>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="180px">
        <el-form-item label="交易模块(trans_module)" prop="transModule">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="formData.transModule" placeholder="请选择交易模块" @change="val => handleOptionChange('transModule', val)" style="flex: 1" :disabled="isChildEdit">
              <el-option v-for="opt in options.transModule" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('transModule', '交易模块')" :disabled="isChildEdit">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="交易模块描述(trans_module_desc)" prop="transModuleDesc">
          <el-input v-model="formData.transModuleDesc" placeholder="关联描述(自动带出或手动输入)" :disabled="isChildEdit" />
        </el-form-item>

        <el-form-item label="产品代码(busi_type)" prop="busiType">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="formData.busiType" placeholder="请选择产品代码" @change="val => handleOptionChange('busiType', val)" style="flex: 1" :disabled="isChildEdit">
              <el-option v-for="opt in options.busiType" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('busiType', '产品代码')" :disabled="isChildEdit">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="产品代码描述(busi_type_desc)" prop="busiTypeDesc">
          <el-input v-model="formData.busiTypeDesc" placeholder="关联描述(自动带出或手动输入)" :disabled="isChildEdit" />
        </el-form-item>

        <el-form-item label="用户角色(user_role)" prop="userRole">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="formData.userRole" placeholder="请选择用户角色" @change="val => handleOptionChange('userRole', val)" style="flex: 1" :disabled="isChildEdit">
              <el-option v-for="opt in options.userRole" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('userRole', '用户角色')" :disabled="isChildEdit">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="角色描述(user_role_desc)" prop="userRoleDesc">
          <el-input v-model="formData.userRoleDesc" placeholder="关联描述(自动带出或手动输入)" :disabled="isChildEdit" />
        </el-form-item>

        <el-form-item label="功能按钮(fun_button)" prop="funButton">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-select v-model="formData.funButton" placeholder="请选择功能按钮" @change="val => handleOptionChange('funButton', val)" style="flex: 1">
              <el-option v-for="opt in options.funButton" :key="opt.code" :label="`${opt.code} - ${opt.desc}`" :value="opt.code" />
            </el-select>
            <el-button @click="openAddOption('funButton', '功能按钮')">新增</el-button>
          </div>
        </el-form-item>
        <el-form-item label="按钮描述(fun_button_desc)" prop="funButtonDesc">
          <el-input v-model="formData.funButtonDesc" placeholder="关联描述(自动带出或手动输入)" />
        </el-form-item>

        <el-form-item label="顺序号(seq)" prop="seq">
          <el-input-number v-model="formData.seq" :min="1" />
        </el-form-item>
        <el-form-item label="数据状态(data_status)" prop="dataStatus">
          <el-input v-model="formData.dataStatus" placeholder="请输入数据状态" />
        </el-form-item>
        <el-form-item label="条件表达式(condition_expr)" prop="conditionExpr">
          <el-input v-model="formData.conditionExpr" placeholder="请输入条件表达式" />
        </el-form-item>
        <el-form-item label="备注(remark)" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增枚举值弹窗 -->
    <el-dialog :title="`新增 ${addOptionState.title} 枚举值`" v-model="addOptionState.visible" width="400px" append-to-body>
      <el-form :model="addOptionState.form" label-width="80px">
        <el-form-item label="代码">
          <el-input v-model="addOptionState.form.code" placeholder="例如: SYS" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addOptionState.form.desc" placeholder="例如: 系统管理" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addOptionState.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddOption">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_BASE = '/perm-menu-manager/api/fun-permission'

const loading = ref(false)
const tableData = ref([])
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  transModule: '',
  busiType: '',
  userRole: ''
})

const options = reactive({
  transModule: [],
  busiType: [],
  userRole: [],
  funButton: []
})

const fetchOptions = async (field) => {
  try {
    const res = await axios.get(`${API_BASE}/options/${field}`)
    if (res.data.code === 200) {
      options[field] = res.data.data
    }
  } catch (error) {
    console.error(`Failed to load options for ${field}`, error)
  }
}

// 详情弹窗状态
const detailsState = reactive({
  visible: false,
  title: '',
  loading: false,
  data: [],
  parentRow: null
})

// === 新增分组弹窗相关状态 ===
const groupDialogVisible = ref(false)
const groupFormRef = ref(null)
const groupFormData = reactive({
  transModule: '',
  transModuleDesc: '',
  busiType: '',
  busiTypeDesc: '',
  userRole: '',
  userRoleDesc: '',
  buttons: []
})

const groupRules = {
  transModule: [{ required: true, message: '请选择或输入交易模块', trigger: 'change' }],
  transModuleDesc: [{ required: true, message: '交易模块描述不能为空', trigger: 'blur' }],
  busiType: [{ required: true, message: '请选择或输入产品代码', trigger: 'change' }],
  busiTypeDesc: [{ required: true, message: '产品代码描述不能为空', trigger: 'blur' }]
}

const handleGroupOptionChange = (field, val) => {
  const opt = options[field].find(o => o.code === val)
  if (opt) {
    groupFormData[`${field}Desc`] = opt.desc
  }
}

const handleBtnOptionChange = (index, val) => {
  const opt = options.funButton.find(o => o.code === val)
  if (opt) {
    groupFormData.buttons[index].funButtonDesc = opt.desc
  }
}

const addGroupButton = () => {
  groupFormData.buttons.push({
    funButton: '',
    funButtonDesc: '',
    seq: groupFormData.buttons.length + 1,
    dataStatus: '',
    conditionExpr: '',
    remark: ''
  })
}

const removeGroupButton = (index) => {
  groupFormData.buttons.splice(index, 1)
}

const submitGroupForm = async () => {
  if (!groupFormRef.value) return
  await groupFormRef.value.validate(async (valid) => {
    if (valid) {
      if (groupFormData.buttons.length === 0) {
        ElMessage.warning('请至少添加一个功能按钮')
        return
      }
      for (let i = 0; i < groupFormData.buttons.length; i++) {
        const btn = groupFormData.buttons[i]
        if (!btn.funButton || !btn.funButtonDesc) {
          ElMessage.warning(`第 ${i + 1} 行的功能按钮及描述不能为空`)
          return
        }
      }
      try {
        const payload = groupFormData.buttons.map(btn => ({
          transModule: groupFormData.transModule,
          transModuleDesc: groupFormData.transModuleDesc,
          busiType: groupFormData.busiType,
          busiTypeDesc: groupFormData.busiTypeDesc,
          userRole: groupFormData.userRole,
          userRoleDesc: groupFormData.userRoleDesc,
          ...btn
        }))
        const res = await axios.post(`${API_BASE}/batch`, payload)
        if (res.data.code === 200) {
          ElMessage.success('新增分组成功')
          groupDialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.data.message || '新增分组失败')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}
// =============================

const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const isChildEdit = ref(false)
const formRef = ref(null)

const formData = reactive({
  permissionId: '',
  transModule: '',
  transModuleDesc: '',
  busiType: '',
  busiTypeDesc: '',
  userRole: '',
  userRoleDesc: '',
  funButton: '',
  funButtonDesc: '',
  dataStatus: '',
  remark: '',
  seq: 1,
  conditionExpr: ''
})

const rules = {
  transModule: [{ required: true, message: '请选择或输入交易模块', trigger: 'change' }],
  transModuleDesc: [{ required: true, message: '交易模块描述不能为空', trigger: 'blur' }],
  busiType: [{ required: true, message: '请选择或输入产品代码', trigger: 'change' }],
  busiTypeDesc: [{ required: true, message: '产品代码描述不能为空', trigger: 'blur' }],
  funButton: [{ required: true, message: '请选择或输入功能按钮', trigger: 'change' }],
  funButtonDesc: [{ required: true, message: '功能按钮描述不能为空', trigger: 'blur' }]
}

const handleOptionChange = (field, val) => {
  const opt = options[field].find(o => o.code === val)
  if (opt) {
    formData[`${field}Desc`] = opt.desc
  }
}

const addOptionState = reactive({
  visible: false,
  field: '',
  title: '',
  form: {
    code: '',
    desc: ''
  }
})

const openAddOption = (field, title) => {
  addOptionState.field = field
  addOptionState.title = title
  addOptionState.form.code = ''
  addOptionState.form.desc = ''
  addOptionState.visible = true
}

const confirmAddOption = () => {
  if (!addOptionState.form.code || !addOptionState.form.desc) {
    ElMessage.warning('代码和描述不能为空')
    return
  }
  const { field, form } = addOptionState
  const existing = options[field].find(o => o.code === form.code)
  if (!existing) {
    options[field].push({ code: form.code, desc: form.desc })
  } else {
    existing.desc = form.desc
  }
  
  if (groupDialogVisible.value) {
    if (field === 'funButton') {
       // We can't automatically fill the table row from a global dialog easily, let the user select it
       ElMessage.success('选项添加成功，请在下拉框中选择')
    } else {
      groupFormData[field] = form.code
      groupFormData[`${field}Desc`] = form.desc
    }
  } else {
    formData[field] = form.code
    formData[`${field}Desc`] = form.desc
  }
  addOptionState.visible = false
}

// 主页查询分组数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/group-page`, {
      params: {
        current: page.current,
        size: page.size,
        ...searchForm
      }
    })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      page.total = res.data.data.total
    } else {
      ElMessage.error(res.data.message || '查询异常')
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 查询分组详情下的按钮
const fetchDetails = async (row) => {
  detailsState.loading = true
  try {
    const res = await axios.get(`${API_BASE}/page`, {
      params: {
        current: 1,
        size: 500, // 获取所有该组的按钮
        transModule: row.transModule,
        busiType: row.busiType,
        userRole: row.userRole
      }
    })
    if (res.data.code === 200) {
      detailsState.data = res.data.data.records
    } else {
      ElMessage.error(res.data.message || '获取详情异常')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    detailsState.loading = false
  }
}

const openDetails = (row) => {
  detailsState.parentRow = row
  detailsState.title = `详情：${row.transModuleDesc} - ${row.busiTypeDesc} - ${row.userRoleDesc}`
  detailsState.visible = true
  fetchDetails(row)
}

const resetSearch = () => {
  searchForm.transModule = ''
  searchForm.busiType = ''
  searchForm.userRole = ''
  fetchData()
}

// 主页完全新增组
const handleAddGroup = () => {
  groupFormData.transModule = ''
  groupFormData.transModuleDesc = ''
  groupFormData.busiType = ''
  groupFormData.busiTypeDesc = ''
  groupFormData.userRole = ''
  groupFormData.userRoleDesc = ''
  groupFormData.buttons = [
    { funButton: '', funButtonDesc: '', seq: 1, dataStatus: '', conditionExpr: '', remark: '' }
  ]
  groupDialogVisible.value = true
}

// 详情页新增当前组按钮
const handleAddBtn = () => {
  dialogTitle.value = '新增功能按钮'
  isChildEdit.value = true
  Object.keys(formData).forEach(key => formData[key] = '')
  formData.seq = 1
  
  // 回填父组属性
  const pr = detailsState.parentRow
  formData.transModule = pr.transModule
  formData.transModuleDesc = pr.transModuleDesc
  formData.busiType = pr.busiType
  formData.busiTypeDesc = pr.busiTypeDesc
  formData.userRole = pr.userRole
  formData.userRoleDesc = pr.userRoleDesc
  
  dialogVisible.value = true
}

// 编辑按钮
const handleEditBtn = (row) => {
  dialogTitle.value = '编辑功能按钮'
  isChildEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除按钮
const handleDeleteBtn = (row) => {
  ElMessageBox.confirm('确认删除该记录吗?', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`${API_BASE}/delete/${row.permissionId}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        // 刷新详情
        fetchDetails(detailsState.parentRow)
        // 刷新主表
        fetchData()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = formData.permissionId ? `${API_BASE}/update` : API_BASE
        const res = await axios.post(url, formData)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          
          if (detailsState.visible) {
            fetchDetails(detailsState.parentRow)
          }
          fetchData()
          
          // 重新获取下拉列表选项
          fetchOptions('transModule')
          fetchOptions('busiType')
          fetchOptions('userRole')
          fetchOptions('funButton')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

onMounted(() => {
  fetchData()
  fetchOptions('transModule')
  fetchOptions('busiType')
  fetchOptions('userRole')
  fetchOptions('funButton')
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
