import axios from 'axios'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api',
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
  return request.put('/menu', data, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 修改菜单编码（含级联更新子菜单）
 * @param {object} data - { oldMenuCode, newMenuCode, menuScope, tenantId }
 * @param {string} tempTableName - 临时表名
 */
export function updateMenuCode(data, tempTableName = '') {
  return request.put('/menu/code', data, {
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}

/**
 * 拖拽移动菜单
 * @param {object} data - { menuCode, newUppMenuCode, menuScope, tenantId }
 * @param {string} tempTableName - 临时表名
 */
export function dragMenu(data, tempTableName = '') {
  return request.put('/menu/drag', data, {
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
  return request.delete(`/menu/${menuScope}/${menuCode}`, {
    params: { tenantId },
    headers: tempTableName ? { 'X-Temp-Table': tempTableName } : {}
  })
}
