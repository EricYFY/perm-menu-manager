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
 * 分页查询接口调用历史流水
 * @param {string} type - 查询类型 (jnl 或 query)
 * @param {string} trCode - 交易编码
 * @param {string} custNo - 客户号
 * @param {string} jnlStat - 流水状态
 * @param {string} channelNo - 渠道号
 * @param {number} pageNo - 页码
 * @param {number} pageSize - 每页大小
 */
export function getJnlPage(type, trCode, custNo, jnlStat, channelNo, pageNo = 1, pageSize = 20) {
  return request({
    url: '/jnl/page',
    method: 'get',
    params: {
      type,
      trCode,
      custNo,
      jnlStat,
      channelNo,
      pageNo,
      pageSize
    }
  })
}

/**
 * 获取流水关联的报文数据
 * @param {string} custNo - 客户号
 * @param {string} serialNo - 流水号
 */
export function getJnlData(custNo, serialNo) {
  return request({
    url: '/jnl/data',
    method: 'get',
    params: {
      custNo,
      serialNo
    }
  })
}

/**
 * 检查接口是否被成功调用
 * @param {string} trCode - 接口编码
 * @param {Array<string>} custNos - 客户号列表
 * @param {string} channelNo - 渠道号
 */
export function checkJnlSuccess(trCode, custNos, channelNo) {
  return request({
    url: '/jnl/check-success',
    method: 'get',
    params: {
      trCode,
      custNos: custNos ? custNos.join(',') : '',
      channelNo
    }
  })
}
