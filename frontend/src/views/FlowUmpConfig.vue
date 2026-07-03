<template>
  <div class="flow-ump-config">
    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="交易码(TR_CODE)">
            <el-input v-model="searchForm.trCode" placeholder="请输入交易码" clearable />
          </el-form-item>
          <el-form-item label="交易名称(TR_NAME)">
            <el-input v-model="searchForm.trName" placeholder="请输入交易名称" clearable />
          </el-form-item>
          <el-form-item label="场景说明(TR_DESC)">
            <el-input v-model="searchForm.trDesc" placeholder="请输入场景说明" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="success" @click="handleAdd">新增</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="trCode" label="交易码(TR_CODE)" width="150" />
        <el-table-column prop="trName" label="交易名称(TR_NAME)" width="180" />
        <el-table-column prop="trDesc" label="场景说明(TR_DESC)" width="200" />
        <el-table-column prop="operCommand" label="操作类型(OPER_COMMAND)" width="180" />
        <el-table-column prop="flowStatus" label="流程状态(FLOW_STATUS)" width="180" />
        <el-table-column prop="orderStep" label="当前步骤(ORDER_STEP)" width="180" />
        <el-table-column prop="nextStep" label="下一步骤(NEXT_STEP)" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="850px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="190px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易码(TR_CODE)" prop="trCode">
              <el-input v-model="formData.trCode" placeholder="请输入交易码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交易名称(TR_NAME)" prop="trName">
              <el-input v-model="formData.trName" placeholder="请输入交易名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场景说明(TR_DESC)" prop="trDesc">
              <el-input v-model="formData.trDesc" placeholder="请输入场景说明" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作类型(OPER_COMMAND)" prop="operCommand">
              <el-input v-model="formData.operCommand" placeholder="例如: SUBMIT" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="流程状态(FLOW_STATUS)" prop="flowStatus">
              <el-input v-model="formData.flowStatus" placeholder="例如: PENDING" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="修改标志(EDIT_FLAG)" prop="editFlag">
              <el-input v-model="formData.editFlag" placeholder="请输入修改标志" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前步骤(ORDER_STEP)" prop="orderStep">
              <el-input v-model="formData.orderStep" placeholder="当前步骤代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下一步骤(NEXT_STEP)" prop="nextStep">
              <el-input v-model="formData.nextStep" placeholder="下一步骤代码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="子下一步骤(CHILD_NEXT_STEP)" prop="childNextStep">
              <el-input v-model="formData.childNextStep" placeholder="子步骤代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="条件表达式(CONDITION_EXPR)" prop="conditionExpr">
              <el-input v-model="formData.conditionExpr" placeholder="条件表达式" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>调用方式配置（ESC 或 UMP 至少配置一种）</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <h4>ESC 调用配置</h4>
            <el-form-item label="主机返回码(ESC_CODE)" prop="escCode">
              <el-input v-model="formData.escCode" />
            </el-form-item>
            <el-form-item label="业务类型(BUSINESS_TYPE)" prop="businessType">
              <el-input v-model="formData.businessType" />
            </el-form-item>
            <el-form-item label="货架编号(LAYER_CODE)" prop="layerCode">
              <el-input v-model="formData.layerCode" />
            </el-form-item>
            <el-form-item label="产品代码(MER_DISE_CODE)" prop="merDiseCode">
              <el-input v-model="formData.merDiseCode" />
            </el-form-item>
            <el-form-item label="功能代码(FUNC_CODE)" prop="funcCode">
              <el-input v-model="formData.funcCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <h4>UMP 发送配置</h4>
            <el-form-item label="交换器(EXCHANGE)" prop="exchange">
              <el-input v-model="formData.exchange" />
            </el-form-item>
            <el-form-item label="路由键(ROUTING_KEY)" prop="routingKey">
              <el-input v-model="formData.routingKey" />
            </el-form-item>
            <el-form-item label="消息类型(MSG_TYPE)" prop="msgType">
              <el-input v-model="formData.msgType" />
            </el-form-item>
            <el-form-item label="系统号(MSG_SYS_ID)" prop="msgSysId">
              <el-input v-model="formData.msgSysId" />
            </el-form-item>
            <el-form-item label="发送租户(UMP_TENANTID)" prop="umpTenantid">
              <el-input v-model="formData.umpTenantid" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_BASE = '/perm-menu-manager/api/flow-ump-config'

const loading = ref(false)
const tableData = ref([])
const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  trCode: '',
  trName: '',
  trDesc: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const formRef = ref(null)

const formData = reactive({
  cfgId: '',
  trCode: '',
  trName: '',
  trDesc: '',
  operCommand: '',
  flowStatus: '',
  orderStep: '',
  nextStep: '',
  escCode: '',
  editFlag: '',
  exchange: '',
  routingKey: '',
  msgType: '',
  msgSysId: '',
  businessType: '',
  layerCode: '',
  merDiseCode: '',
  funcCode: '',
  childNextStep: '',
  conditionExpr: '',
  umpTenantid: ''
})

const validateMethod = (rule, value, callback) => {
  const hasEsc = formData.escCode || formData.businessType || formData.layerCode || formData.merDiseCode || formData.funcCode;
  const hasUmp = formData.exchange || formData.routingKey || formData.msgType || formData.msgSysId || formData.umpTenantid;
  
  if (!hasEsc && !hasUmp) {
    callback(new Error('ESC调用或UMP发送至少配置一种方式'))
  } else {
    callback()
  }
}

const rules = {
  trCode: [{ required: true, message: '请输入交易码', trigger: 'blur' }],
  // 绑定一个虚拟规则到表单上用于触发验证
  escCode: [{ validator: validateMethod, trigger: 'blur' }],
  exchange: [{ validator: validateMethod, trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/page`, {
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

const resetSearch = () => {
  searchForm.trCode = ''
  searchForm.trName = ''
  searchForm.trDesc = ''
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增'
  Object.keys(formData).forEach(key => formData[key] = '')
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该记录吗?', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`${API_BASE}/${row.cfgId}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
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
        const method = formData.cfgId ? 'put' : 'post'
        const res = await axios[method](API_BASE, formData)
        if (res.data.code === 200) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.data.message || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

onMounted(() => {
  fetchData()
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
h4 {
  margin-bottom: 15px;
  color: #409EFF;
}
</style>
