import axios from 'axios'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/perm-menu-manager/api',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const envId = localStorage.getItem('X-Env-ID')
  if (envId) {
    config.headers['X-Env-ID'] = envId
  }
  return config
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
 * 获取菜单树
 * @param {string} menuScope - 菜单渠道 (11/12)
 * @param {string} tenantId - 租户号
 * @param {string} tempTableName - 临时表名
 * @param {string} subsystemCode - 子系统编码
 */
export function getMenuTree(menuScope, tenantId = '047', tempTableName = '', subsystemCode = '') {
  return request({
    url: '/menu/tree',
    method: 'get',
    params: { menuScope, tenantId, subsystemCode },
    headers: {
      'X-Temp-Table': tempTableName
    }
  })
}

/**
 * 新增菜单
 * @param {object} data - 菜单数据
 * @param {string} tempTableName - 临时表名
 */
export function addMenu(data, tempTableName = '') {
  return request.post('/menu', data, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 更新菜单（普通字段）
 * @param {object} data - 菜单数据
 * @param {string} tempTableName - 临时表名
 */
export function updateMenu(data, tempTableName = '') {
  return request.post('/menu/update', data, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 修改菜单编码（含级联更新子菜单）
 * @param {object} data - { oldMenuCode, newMenuCode, menuScope, tenantId }
 * @param {string} tempTableName - 临时表名
 */
export function updateMenuCode(data, tempTableName = '') {
  return request.post('/menu/code', data, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 拖拽移动菜单
 * @param {object} data - { menuCode, newUppMenuCode, menuScope, tenantId }
 * @param {string} tempTableName - 临时表名
 */
export function dragMenu(data, tempTableName = '') {
  return request.post('/menu/drag', data, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 删除菜单（级联删除子菜单）
 * @param {string} menuScope - 菜单渠道
 * @param {string} menuCode - 菜单编码
 * @param {string} tenantId - 租户号
 * @param {string} tempTableName - 临时表名
 */
export function deleteMenu(menuScope, menuCode, tenantId = '047', tempTableName = '') {
  return request.post(`/menu/delete/${menuScope}/${menuCode}?tenantId=${tenantId}`, {}, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 查询指定菜单被加挂的产品与功能列表
 * @param {string} menuScope - 菜单渠道
 * @param {string} menuCode - 菜单编码
 * @param {string} tenantId - 租户号
 */
export function getFeatureMounts(menuScope, menuCode, tenantId = '047') {
  return request({
    url: '/menu/feature-mounts',
    method: 'get',
    params: { menuScope, menuCode, tenantId }
  })
}

/**
 * 查询产品功能列表（支持模糊匹配）
 * @param {string} tenantId - 租户号
 * @param {string} keyword - 关键词
 */
export function getProdFeatures(tenantId = '047', keyword = '') {
  return request({
    url: '/menu/prod-features',
    method: 'get',
    params: { tenantId, keyword }
  })
}

/**
 * 新增菜单功能加挂
 * @param {object} data - 加挂记录实体
 */
export function addFeatureMount(data) {
  return request.post('/menu/feature-mount/add', data)
}

/**
 * 删除菜单功能加挂
 * @param {string} menuScope - 菜单渠道
 * @param {string} menuCode - 菜单编码
 * @param {string} prodCode - 产品编号
 * @param {string} featureId - 功能ID
 * @param {string} tenantId - 租户号
 */
export function deleteFeatureMount(menuScope, menuCode, prodCode, featureId, tenantId = '047') {
  return request.post('/menu/feature-mount/delete', {}, {
    params: { menuScope, menuCode, prodCode, featureId, tenantId }
  })
}
