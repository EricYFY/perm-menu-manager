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
 * @param {string} menuScope - 菜单渠道（11=PC端, 12=APP端）
 * @param {string} tenantId - 租户号
 */
export function getMenuTree(menuScope, tenantId = '047') {
  return request.get(`/menu/tree/${menuScope}`, {
    params: { tenantId }
  })
}

/**
 * 新增菜单
 * @param {object} data - 菜单数据
 */
export function addMenu(data) {
  return request.post('/menu', data)
}

/**
 * 更新菜单（普通字段）
 * @param {object} data - 菜单数据
 */
export function updateMenu(data) {
  return request.put('/menu', data)
}

/**
 * 修改菜单编码（含级联更新子菜单）
 * @param {object} data - { oldMenuCode, newMenuCode, menuScope, tenantId }
 */
export function updateMenuCode(data) {
  return request.put('/menu/code', data)
}

/**
 * 拖拽移动菜单
 * @param {object} data - { menuCode, newUppMenuCode, menuScope, tenantId }
 */
export function dragMenu(data) {
  return request.put('/menu/drag', data)
}

/**
 * 删除菜单（级联删除子菜单）
 * @param {string} menuScope - 菜单渠道
 * @param {string} menuCode - 菜单编码
 * @param {string} tenantId - 租户号
 */
export function deleteMenu(menuScope, menuCode, tenantId = '047') {
  return request.delete(`/menu/${menuScope}/${menuCode}`, {
    params: { tenantId }
  })
}
