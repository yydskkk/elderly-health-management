import request from '@/utils/request'

// 分页查询日志
export function getPage(params) {
  return request({
    url: '/log/page',
    method: 'get',
    params
  })
}

// 导出日志
export function exportLog(params) {
  return request({
    url: '/log/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
