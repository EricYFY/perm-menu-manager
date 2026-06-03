<template>
  <el-dialog
    title="接口调用历史流水查询"
    v-model="visible"
    width="1000px"
    destroy-on-close
    append-to-body
  >
    <div class="filter-bar">
      <el-form :inline="true" size="small">
        <el-form-item label="客户号(CUST_NO)">
          <el-input v-model="searchCustNo" placeholder="请输入客户号" style="width: 150px" />
        </el-form-item>
        <el-form-item label="流水状态(JNL_STAT)">
          <el-select v-model="searchJnlStat" placeholder="请选择状态" style="width: 120px" clearable>
            <el-option label="A-接收" value="A" />
            <el-option label="P-处理" value="P" />
            <el-option label="S-成功" value="S" />
            <el-option label="F-失败" value="F" />
            <el-option label="U-未知" value="U" />
            <el-option label="W-工单" value="W" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="平台交易流水 (tbsp_jnl)" name="jnl"></el-tab-pane>
      <el-tab-pane label="平台查询流水 (tbsp_jnl_query)" name="query"></el-tab-pane>
    </el-tabs>

    <div class="table-container" v-loading="loading">
      <el-table :data="tableData" border stripe height="400" size="small">
        <!-- 动态列 -->
        <el-table-column prop="custNo" label="客户号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="trCode" label="接口编码 (trCode)" min-width="140" show-overflow-tooltip />
        <el-table-column prop="jnlStat" label="流水状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatType(row.jnlStat)" size="small">{{ row.jnlStat }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizStat" label="业务状态" width="100" />
        <el-table-column prop="trDate" label="交易日期" width="100" />
        <el-table-column prop="trTime" label="交易时间" width="100" />
        <el-table-column prop="serialNo" label="流水号" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSearch"
        @current-change="fetchData"
      />
    </div>
  </el-dialog>

  <!-- 详情抽屉 -->
  <el-drawer
    v-model="detailVisible"
    title="流水详情信息"
    size="800px"
    append-to-body
  >
    <el-tabs v-model="detailActiveTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-descriptions :column="2" border size="small" class="detail-desc">
          <el-descriptions-item v-for="(val, key) in currentRow" :key="key" :label="fieldMap[key] || key">
            {{ val }}
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
      <el-tab-pane label="报文信息" name="message" v-if="activeTab === 'jnl'">
        <div v-loading="loadingData" class="message-container">
          <div class="message-block">
            <h4>请求信息 (REQUEST_CONTEXT)</h4>
            <el-input 
              type="textarea" 
              :rows="10" 
              v-model="reqContext" 
              readonly 
              placeholder="暂无请求信息" 
            />
          </div>
          <div class="message-block">
            <h4>返回信息 (RESPONSE_CONTEXT)</h4>
            <el-input 
              type="textarea" 
              :rows="10" 
              v-model="resContext" 
              readonly 
              placeholder="暂无返回信息" 
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-drawer>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getJnlPage, getJnlData } from '../api/jnl.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  trCode: {
    type: String,
    required: true
  },
  custNo: {
    type: Array,
    default: () => []
  },
  channelNo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('jnl')
const searchCustNo = ref('')
const searchJnlStat = ref('')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const detailVisible = ref(false)
const currentRow = ref({})
const detailActiveTab = ref('basic')
const loadingData = ref(false)
const reqContext = ref('')
const resContext = ref('')

const fieldMap = {
  tenantId: '租户编码 (TENANT_ID)',
  jnlStat: '流水状态 (JNL_STAT)',
  bizStat: '业务状态 (BIZ_STAT)',
  channelNo: '交易渠道 (CHANNEL_NO)',
  custNo: '交易客户 (CUST_NO)',
  organizationNo: '组织编号 (ORGANIZATION_NO)',
  userId: '交易用户ID (USER_ID)',
  userNo: '交易用户 (USER_NO)',
  userName: '交易用户名 (USER_NAME)',
  trCode: '交易编码 (TR_CODE)',
  prodGroupNo: '产品分组编号 (PROD_GROUP_NO)',
  prodCode: '产品编号 (PROD_CODE)',
  menuCode: '菜单编码 (MENU_CODE)',
  menuName: '菜单名称 (MENU_NAME)',
  trDate: '交易日期 (TR_DATE)',
  trTime: '交易时间 (TR_TIME)',
  serialNo: '交易流水 (SERIAL_NO)',
  reqDate: '消费方请求日期 (REQ_DATE)',
  reqTime: '消费方请求时间 (REQ_TIME)',
  reqSerialNo: '消费方请求流水 (REQ_SERIAL_NO)',
  origDate: '原始方请求日期 (ORIG_DATE)',
  origTime: '原始方请求时间 (ORIG_TIME)',
  origSerialNo: '原始方请求流水 (ORIG_SERIAL_NO)',
  referSerialNo: '关联经办流水 (REFER_SERIAL_NO)',
  outSerialNo: '外部系统流水号 (OUT_SERIAL_NO)',
  traceId: '全链路跟踪号 (TRACE_ID)',
  orderNo: '业务订单号 (ORDER_NO)',
  assetNo: '资产编号 (ASSET_NO)',
  assetUnit: '资产单位 (ASSET_UNIT)',
  amt: '交易金额 (AMT)',
  ip: 'IP地址 (IP)',
  mac: 'MAC地址 (MAC)',
  deviceNo: '设备识别号 (DEVICE_NO)',
  deviceName: '设备名称 (DEVICE_NAME)',
  printTimes: '凭证打印次数 (PRINT_TIMES)',
  respCode: '返回编码 (RESP_CODE)',
  respMsg: '返回信息 (RESP_MSG)',
  respExt: '扩展信息 (RESP_EXT)',
  summary: '摘要信息 (SUMMARY)',
  extJson: '扩展信息JSON (EXT_JSON)',
  creDate: '创建日期 (CRE_DATE)',
  updDate: '更新日期 (UPD_DATE)',
  userType: '交易用户类型 (USER_TYPE)',
  menuType: '菜单类型 (MENU_TYPE)'
}

watch(
  () => visible.value,
  (newVal) => {
    if (newVal) {
      searchCustNo.value = (props.custNo && props.custNo.length > 0) ? props.custNo[0] : '2001993301'
      searchJnlStat.value = ''
      activeTab.value = 'jnl'
      handleSearch()
    }
  }
)

function handleTabChange() {
  handleSearch()
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

async function fetchData() {
  if (!searchCustNo.value || searchCustNo.value.length < 2) {
    ElMessage.warning('客户号不合法，无法查询分表')
    tableData.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const res = await getJnlPage(
      activeTab.value,
      props.trCode,
      searchCustNo.value,
      searchJnlStat.value,
      props.channelNo,
      currentPage.value,
      pageSize.value
    )
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询流水发生异常')
  } finally {
    loading.value = false
  }
}

async function showDetail(row) {
  currentRow.value = row
  detailActiveTab.value = 'basic'
  detailVisible.value = true
  
  if (activeTab.value === 'query') {
    return
  }

  // 查询报文数据
  loadingData.value = true
  reqContext.value = ''
  resContext.value = ''
  try {
    const res = await getJnlData(row.custNo, row.serialNo)
    if (res.code === 200 && res.data) {
      reqContext.value = res.data.requestContext || ''
      resContext.value = res.data.responseContext || ''
    }
  } catch (error) {
    console.error('获取报文异常', error)
  } finally {
    loadingData.value = false
  }
}

function getStatType(stat) {
  if (!stat) return 'info'
  if (stat.includes('S')) return 'success'
  if (stat.includes('F')) return 'danger'
  if (stat.includes('P') || stat.includes('A')) return 'warning'
  return 'info'
}
</script>

<style scoped>
.filter-bar {
  margin-bottom: 12px;
}
.table-container {
  margin-bottom: 16px;
}
.pagination-bar {
  display: flex;
  justify-content: flex-end;
}
.detail-desc {
  padding: 0 16px;
}
.message-container {
  padding: 8px 16px;
}
.message-block {
  margin-bottom: 20px;
}
.message-block h4 {
  margin: 0 0 8px 0;
  color: var(--text-regular);
  font-size: 14px;
}
</style>
