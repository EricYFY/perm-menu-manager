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
 * 获取所有接口配置
 */
export function getConfigList(params = {}) {
  return request({
    url: '/trx-config',
    method: 'get',
    params
  })
}

/**
 * 新增配置
 */
export function addTrxConfig(config) {
  return request.post('/trx-config', config)
}

/**
 * 更新配置
 */
export function updateTrxConfig(config) {
  return request.put('/trx-config', config)
}

/**
 * 删除配置
 */
export function deleteTrxConfig(trCode, language) {
  return request.delete(`/trx-config/${trCode}/${language}`)
}
