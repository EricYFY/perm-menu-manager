<template>
  <el-dialog title="环境配置管理" v-model="visible" width="800px" append-to-body>
    <div style="margin-bottom: 15px;">
      <el-button type="primary" @click="handleAdd">新增环境</el-button>
    </div>
    
    <el-table :data="envList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="envId" label="环境ID (需唯一)" width="150" />
      <el-table-column prop="envName" label="环境名称" width="150" />
      <el-table-column label="Master 库配置" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.masterUrl }}
        </template>
      </el-table-column>
      <el-table-column label="Second 库配置" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.secondUrl }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 增改弹窗 -->
    <el-dialog :title="isEdit ? '编辑环境' : '新增环境'" v-model="formVisible" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="环境ID" prop="envId">
          <el-input v-model="form.envId" placeholder="例如: DEV, SIT, UAT" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="环境名称" prop="envName">
          <el-input v-model="form.envName" placeholder="例如: 开发环境, 测试环境" />
        </el-form-item>
        
        <el-divider>Master 库配置</el-divider>
        <el-form-item label="JDBC URL" prop="masterUrl">
          <el-input v-model="form.masterUrl" placeholder="jdbc:mysql://..." />
        </el-form-item>
        <el-form-item label="用户名" prop="masterUsername">
          <el-input v-model="form.masterUsername" />
        </el-form-item>
        <el-form-item label="密码" prop="masterPassword">
          <el-input v-model="form.masterPassword" type="password" show-password />
        </el-form-item>

        <el-divider>Second 库配置</el-divider>
        <el-form-item label="JDBC URL" prop="secondUrl">
          <el-input v-model="form.secondUrl" placeholder="jdbc:mysql://..." />
        </el-form-item>
        <el-form-item label="用户名" prop="secondUsername">
          <el-input v-model="form.secondUsername" />
        </el-form-item>
        <el-form-item label="密码" prop="secondPassword">
          <el-input v-model="form.secondPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_BASE = '/perm-menu-manager/api/env'
const visible = ref(false)
const envList = ref([])
const loading = ref(false)

const emit = defineEmits(['env-updated'])

const formVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  envId: '',
  envName: '',
  masterUrl: '',
  masterUsername: '',
  masterPassword: '',
  secondUrl: '',
  secondUsername: '',
  secondPassword: ''
})

const rules = {
  envId: [{ required: true, message: '环境ID不能为空', trigger: 'blur' }],
  envName: [{ required: true, message: '环境名称不能为空', trigger: 'blur' }],
  masterUrl: [{ required: true, message: 'Master URL 不能为空', trigger: 'blur' }],
  masterUsername: [{ required: true, message: 'Master 用户名不能为空', trigger: 'blur' }],
  masterPassword: [{ required: true, message: 'Master 密码不能为空', trigger: 'blur' }],
  secondUrl: [{ required: true, message: 'Second URL 不能为空', trigger: 'blur' }],
  secondUsername: [{ required: true, message: 'Second 用户名不能为空', trigger: 'blur' }],
  secondPassword: [{ required: true, message: 'Second 密码不能为空', trigger: 'blur' }]
}

const open = () => {
  visible.value = true
  fetchEnvs()
}

const fetchEnvs = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/list`)
    if (res.data.code === 200) {
      envList.value = res.data.data || []
      emit('env-updated', envList.value)
    }
  } catch (error) {
    ElMessage.error('获取环境列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.keys(form).forEach(k => form[k] = '')
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.keys(form).forEach(k => form[k] = row[k] || '')
  formVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除环境 [${row.envId}] 吗？`, '警告', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post(`${API_BASE}/delete?envId=${row.envId}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchEnvs()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('请求失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const url = isEdit.value ? `${API_BASE}/update` : `${API_BASE}/add`
        const res = await axios.post(url, form)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          formVisible.value = false
          fetchEnvs()
        } else {
          ElMessage.error(res.data.message || '保存失败')
        }
      } catch (e) {
        ElMessage.error('保存请求异常')
      } finally {
        submitting.value = false
      }
    }
  })
}

defineExpose({
  open,
  fetchEnvs
})
</script>
