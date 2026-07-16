import axios from 'axios'

// 复用统一的 Axios 实例（带环境拦截器）
const request = axios.create({
  baseURL: '/perm-menu-manager/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const envId = localStorage.getItem('X-Env-ID')
  if (envId) {
    config.headers['X-Env-ID'] = envId
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('接口请求失败:', error)
    return Promise.reject(error)
  }
)

/**
 * 获取所有 DICT_ID 列表
 * @param {string} tenantId
 */
export function getDictIds(tenantId = '047') {
  return request.get('/dict/ids', { params: { tenantId } })
}

/**
 * 获取指定 DICT_ID 下的字典条目列表
 * @param {string} dictId
 * @param {string} tenantId
 */
export function getDictEntries(dictId, tenantId = '047') {
  return request.get(`/dict/${dictId}`, { params: { tenantId } })
}

/**
 * 新增字典条目
 * @param {object} dict
 */
export function addDictEntry(dict) {
  return request.post('/dict', dict)
}

/**
 * 更新字典条目
 * @param {object} dict
 */
export function updateDictEntry(dict) {
  return request.post('/dict/update', dict)
}

/**
 * 删除单条字典记录
 * @param {string} dictId
 * @param {string} dictKey
 * @param {string} tenantId
 */
export function deleteDictEntry(dictId, dictKey, tenantId = '047') {
  return request.post(`/dict/delete/${dictId}/${dictKey}`, null, { params: { tenantId } })
}

/**
 * 删除整个 DICT_ID 组
 * @param {string} dictId
 * @param {string} tenantId
 */
export function deleteDictGroup(dictId, tenantId = '047') {
  return request.post(`/dict/delete/${dictId}`, null, { params: { tenantId } })
}
