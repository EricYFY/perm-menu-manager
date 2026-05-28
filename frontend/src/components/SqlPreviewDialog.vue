<template>
  <el-dialog
    v-model="visible"
    title="确认保存并回放"
    width="800px"
    :close-on-click-modal="false"
    :show-close="!executing"
  >
    <div v-loading="executing" element-loading-text="正在回放并校验数据...">
      <div v-if="!executed" class="preview-stage">
        <el-alert
          title="以下是您在临时表中的所有操作，确认后将在正式表中按顺序执行"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <div class="sql-list-container">
          <div v-if="sqlLog.length === 0" class="empty-log">
            未检测到任何变更操作
          </div>
          <div v-else class="sql-item" v-for="(sql, index) in sqlLog" :key="index">
            <span class="sql-index">{{ index + 1 }}.</span>
            <code class="sql-code">{{ sql }}</code>
          </div>
        </div>
      </div>

      <div v-else class="result-stage">
        <el-result
          :icon="diffs.length === 0 ? 'success' : 'warning'"
          :title="diffs.length === 0 ? '执行成功' : '执行完成，但存在数据差异'"
          :sub-title="`成功执行了 ${successCount} 条 SQL`"
        >
          <template #extra>
            <div v-if="diffs.length > 0" class="diff-container">
              <h4>差异详情：</h4>
              <ul>
                <li v-for="(diff, index) in diffs" :key="index">{{ diff }}</li>
              </ul>
            </div>
            <div class="post-actions" style="margin-top: 24px;">
              <p style="margin-bottom: 16px; color: var(--text-secondary);">
                是否要删除当前的临时表？如果不删除，临时表会被保留在数据库中。
              </p>
              <el-button type="danger" @click="handleDropTempTable" :loading="dropping">删除临时表并退出编辑</el-button>
              <el-button @click="handleKeepTempTable">保留并退出编辑</el-button>
            </div>
          </template>
        </el-result>
      </div>
    </div>

    <template #footer v-if="!executed">
      <span class="dialog-footer">
        <el-button @click="visible = false" :disabled="executing">取消</el-button>
        <el-button type="primary" @click="handleCommit" :loading="executing" :disabled="sqlLog.length === 0">
          确认执行
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { commitSession, dropTempTable } from '../api/session.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  tempTableName: {
    type: String,
    required: true
  },
  sqlLog: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'finish'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => {
    emit('update:modelValue', val)
    if (!val) resetState()
  }
})

const executing = ref(false)
const executed = ref(false)
const dropping = ref(false)
const successCount = ref(0)
const diffs = ref([])

function resetState() {
  executed.value = false
  executing.value = false
  dropping.value = false
  successCount.value = 0
  diffs.value = []
}

async function handleCommit() {
  executing.value = true
  try {
    const result = await commitSession(props.tempTableName)
    if (result.code === 200) {
      executed.value = true
      successCount.value = result.data.successCount
      diffs.value = result.data.diff || []
    } else {
      ElMessage.error(result.message || '执行回放失败')
    }
  } catch (error) {
    ElMessage.error('系统异常')
  } finally {
    executing.value = false
  }
}

async function handleDropTempTable() {
  dropping.value = true
  try {
    const result = await dropTempTable(props.tempTableName)
    if (result.code === 200) {
      ElMessage.success('临时表已删除')
      emit('finish', true) // true表示删除了
      visible.value = false
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('系统异常')
  } finally {
    dropping.value = false
  }
}

function handleKeepTempTable() {
  emit('finish', false) // false表示不删除
  visible.value = false
}
</script>

<style scoped>
.sql-list-container {
  max-height: 400px;
  overflow-y: auto;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 12px;
}

.empty-log {
  text-align: center;
  color: var(--text-muted);
  padding: 20px 0;
}

.sql-item {
  display: flex;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--border-color);
}

.sql-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.sql-index {
  color: var(--text-muted);
  width: 30px;
  flex-shrink: 0;
  user-select: none;
}

.sql-code {
  color: #c7254e;
  background-color: #f9f2f4;
  border-radius: 4px;
  padding: 2px 6px;
  word-break: break-all;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}

.diff-container {
  text-align: left;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  padding: 12px 20px;
  border-radius: var(--radius-sm);
  margin-top: 16px;
  color: #e6a23c;
}

.diff-container h4 {
  margin-bottom: 8px;
}

.diff-container ul {
  padding-left: 20px;
}

.diff-container li {
  margin-bottom: 4px;
  font-size: 13px;
}
</style>
