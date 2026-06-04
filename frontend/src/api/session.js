import axios from 'axios'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/perm-menu-manager/api',
  timeout: 15000
})

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('接口请求失败:', error)
    return Promise.reject(error)
  }
)

/**
 * 获取当前锁状态
 */
export function getSessionStatus() {
  return request.get('/session/status')
}

/**
 * 解锁编辑模式（创建临时表）
 * @param {string} lockedBy - 锁定人唯一标识
 * @param {string} subsystemCode - 子系统编码过滤
 */
export function unlockSession(lockedBy, subsystemCode = '') {
  return request({
    url: '/session/unlock',
    method: 'post',
    data: { lockedBy, subsystemCode }
  })
}

/**
 * 获取 SQL 日志列表
 * @param {string} tempTableName - 临时表名
 */
export function getSqlLog(tempTableName) {
  return request.get('/session/sql-log', {
    params: { tempTableName }
  })
}

/**
 * 确认提交（回放 SQL）
 * @param {string} tempTableName - 临时表名
 */
export function commitSession(tempTableName) {
  return request.post('/session/commit', { tempTableName })
}

/**
 * 删除临时表
 * @param {string} tempTableName - 临时表名
 */
export function dropTempTable(tempTableName) {
  return request.post('/session/drop-temp', { tempTableName })
}

/**
 * 取消编辑会话
 * @param {string} tempTableName - 临时表名
 */
export function cancelSession(tempTableName) {
  return request.post('/session/cancel', { tempTableName })
}
