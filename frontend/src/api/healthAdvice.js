import request from '@/utils/request'

// 发布健康建议
export function add(data) {
  return request({
    url: '/health-advice',
    method: 'post',
    data
  })
}

// 获取我的健康建议
export function getMine(params) {
  return request({
    url: '/health-advice/mine',
    method: 'get',
    params
  })
}

// 分页查询健康建议
export function getPage(params) {
  return request({
    url: '/health-advice/page',
    method: 'get',
    params
  })
}

// 根据ID获取健康建议
export function getById(id) {
  return request({
    url: `/health-advice/${id}`,
    method: 'get'
  })
}
