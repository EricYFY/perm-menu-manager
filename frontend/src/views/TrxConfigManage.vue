<template>
  <div class="trx-config-page">
    <header class="page-header animate-fade-in">
      <div class="header-left">
        <el-icon class="header-logo"><Setting /></el-icon>
        <h1 class="gradient-text">接口关键字段配置</h1>
      </div>
      <el-card class="box-card">
        <div class="header-actions">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="交易码">
              <el-input v-model="searchForm.trCode" placeholder="精确搜索" clearable @keyup.enter="loadConfigs" />
            </el-form-item>
            <el-form-item label="产品名称">
              <el-input v-model="searchForm.productName" placeholder="模糊搜索" clearable @keyup.enter="loadConfigs" />
            </el-form-item>
            <el-form-item label="交易业务名称">
              <el-input v-model="searchForm.busiName" placeholder="模糊搜索" clearable @keyup.enter="loadConfigs" />
            </el-form-item>
            <el-form-item label="交易名称">
              <el-input v-model="searchForm.trName" placeholder="模糊搜索" clearable @keyup.enter="loadConfigs" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadConfigs"><el-icon><Search /></el-icon> 搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
          <div class="action-buttons">
            <el-button type="primary" @click="openAddDialog">
              <el-icon><Plus /></el-icon> 新增配置
            </el-button>
            <el-button icon="Refresh" @click="loadConfigs" :loading="loading">刷新</el-button>
          </div>
        </div>

        <div class="main-content animate-slide-up">
          <div class="plain-light-panel">
            <el-table
              :data="configs"
              stripe
              style="width: 100%"
              :max-height="tableMaxHeight"
              v-loading="loading"
            >
              <!-- 复合主键 -->
              <el-table-column prop="trCode" label="交易码名称 (TR_CODE)" min-width="150" fixed="left" show-overflow-tooltip />
              <el-table-column prop="language" label="语言代码 (LANGUAGE)" min-width="140" fixed="left" show-overflow-tooltip />
              
              <!-- 其他字段 -->
              <el-table-column prop="productCode" label="产品代码 (PRODUCT_CODE)" min-width="160" show-overflow-tooltip />
              <el-table-column prop="productName" label="产品名称 (PRODUCT_NAME)" min-width="160" show-overflow-tooltip />
              <el-table-column prop="busiType" label="业务类型 (BUSI_TYPE)" min-width="160" show-overflow-tooltip />
              <el-table-column prop="busiName" label="业务名称 (BUSI_NAME)" min-width="160" show-overflow-tooltip />
              <el-table-column prop="trName" label="交易名称 (TR_NAME)" min-width="150" show-overflow-tooltip />
              <el-table-column prop="trCcyFld" label="交易币种配置 (TR_CCY_FLD)" min-width="180" show-overflow-tooltip />
              <el-table-column prop="trAmtFld" label="交易金额配置 (TR_AMT_FLD)" min-width="180" show-overflow-tooltip />
              <el-table-column prop="trRefFld" label="交易流水配置 (TR_REF_FLD)" min-width="180" show-overflow-tooltip />
              <el-table-column prop="trCustNameFld" label="对手名称配置 (TR_CUST_NAME_FLD)" min-width="190" show-overflow-tooltip />
              <el-table-column prop="trCustAcctFld" label="对手账号配置 (TR_CUST_ACCT_FLD)" min-width="190" show-overflow-tooltip />
              <el-table-column prop="trBicFld" label="对手银行BIC (TR_BIC_FLD)" min-width="180" show-overflow-tooltip />
              <el-table-column prop="custAcctFld" label="我方账号配置 (CUST_ACCT_FLD)" min-width="190" show-overflow-tooltip />
              <el-table-column prop="imageNoFld" label="影像编号配置 (IMAGE_NO_FLD)" min-width="190" show-overflow-tooltip />
              <el-table-column prop="signIdFld" label="签约ID配置 (SIGN_ID_FLD)" min-width="180" show-overflow-tooltip />
              <el-table-column prop="featureFld" label="扩展字段配置 (FEATURE_FLD)" min-width="180" show-overflow-tooltip />
              <el-table-column prop="conditionExprFld" label="条件表达配置 (CONDITION_EXPR_FLD)" min-width="210" show-overflow-tooltip />

              <el-table-column label="操作" width="130" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
                  <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-card>
    </header>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增接口配置' : '编辑接口配置'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="220px" ref="formRef" :rules="rules">
        <el-row>
          <el-col :span="24">
            <el-form-item label="交易码名称 (TR_CODE)" prop="trCode">
              <el-input v-model="form.trCode" :disabled="dialogMode === 'edit'" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="浏览器语言代码 (LANGUAGE)" prop="language">
              <el-input v-model="form.language" :disabled="dialogMode === 'edit'" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品代码 (PRODUCT_CODE)" prop="productCode">
              <el-input v-model="form.productCode" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品名称 (PRODUCT_NAME)">
              <el-input v-model="form.productName" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="业务类型编号 (BUSI_TYPE)" prop="busiType">
              <el-input v-model="form.busiType" placeholder="必填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易业务名称 (BUSI_NAME)">
              <el-input v-model="form.busiName" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易名称 (TR_NAME)">
              <el-input v-model="form.trName" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易币种代码描述 (TR_CCY_FLD)">
              <el-input v-model="form.trCcyFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易金额描述 (TR_AMT_FLD)">
              <el-input v-model="form.trAmtFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易流水业务 (TR_REF_FLD)">
              <el-input v-model="form.trRefFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易对手名称 (TR_CUST_NAME_FLD)">
              <el-input v-model="form.trCustNameFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交易对手账号 (TR_CUST_ACCT_FLD)">
              <el-input v-model="form.trCustAcctFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="对手银行BIC (TR_BIC_FLD)">
              <el-input v-model="form.trBicFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="我方客户账号 (CUST_ACCT_FLD)">
              <el-input v-model="form.custAcctFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="影像编号配置 (IMAGE_NO_FLD)">
              <el-input v-model="form.imageNoFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="签约ID配置 (SIGN_ID_FLD)">
              <el-input v-model="form.signIdFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="扩展字段配置 (FEATURE_FLD)">
              <el-input v-model="form.featureFld" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="条件表达配置 (CONDITION_EXPR_FLD)">
              <el-input v-model="form.conditionExprFld" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Setting, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConfigList,
  addTrxConfig as addConfig,
  updateTrxConfig as updateConfig,
  deleteTrxConfig as deleteConfig
} from '../api/trxConfig.js'

const searchForm = reactive({
  trCode: '',
  productName: '',
  busiName: '',
  trName: ''
})

const configs = ref([])
const loading = ref(false)
const saving = ref(false)

const tableMaxHeight = computed(() => window.innerHeight - 300)

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)

const defaultForm = {
  productCode: '',
  productName: '',
  busiType: '',
  busiName: '',
  trCode: '',
  trName: '',
  trCcyFld: '',
  trAmtFld: '',
  language: 'zh-CN',
  trRefFld: '',
  trCustNameFld: '',
  trCustAcctFld: '',
  trBicFld: '',
  custAcctFld: '',
  imageNoFld: '',
  signIdFld: '',
  featureFld: '',
  conditionExprFld: ''
}

const form = ref({ ...defaultForm })

const rules = {
  productCode: [{ required: true, message: '必填项', trigger: 'blur' }],
  busiType: [{ required: true, message: '必填项', trigger: 'blur' }],
  trCode: [{ required: true, message: '必填项', trigger: 'blur' }],
  language: [{ required: true, message: '必填项', trigger: 'blur' }]
}

onMounted(() => {
  loadConfigs()
})

async function loadConfigs() {
  loading.value = true
  try {
    const res = await getConfigList(searchForm)
    if (res.code === 200) {
      configs.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载配置失败')
    }
  } catch (e) {
    ElMessage.error('请求失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.trCode = ''
  searchForm.productName = ''
  searchForm.busiName = ''
  searchForm.trName = ''
  loadConfigs()
}

function openAddDialog() {
  dialogMode.value = 'add'
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    let res
    if (dialogMode.value === 'add') {
      res = await addConfig(form.value)
    } else {
      res = await updateConfig(form.value)
    }

    if (res.code === 200) {
      ElMessage.success(dialogMode.value === 'add' ? '新增成功' : '更新成功')
      dialogVisible.value = false
      await loadConfigs()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e?.message) ElMessage.error('操作失败: ' + e.message)
  } finally {
    saving.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `确认删除 交易码[${row.trCode}] 语言[${row.language}] 的配置吗？`,
    '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await deleteConfig(row.trCode, row.language)
      if (res.code === 200) {
        ElMessage.success('已删除')
        await loadConfigs()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败: ' + e.message)
    }
  }).catch(() => {})
}
</script>

<style scoped>
.trx-config-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.search-form {
  flex: 1;
}

.search-form .el-form-item {
  margin-bottom: 10px;
}

.box-card {
  min-height: 500px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
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
  gap: 12px;
}

.main-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.plain-light-panel {
  flex: 1;
  background: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: var(--radius-lg, 8px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 16px;
  overflow: hidden;
}
</style>
