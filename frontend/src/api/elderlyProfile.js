import request from '@/utils/request'

// 获取我的健康档案
export function getMine() {
  return request({
    url: '/elderly-profile/mine',
    method: 'get'
  })
}

// 获取指定老年用户的健康档案
export function getElderlyProfile(id) {
  return request({
    url: `/elderly-profile/elderly/${id}`,
    method: 'get'
  })
}

// 分页查询健康档案
export function getPage(params) {
  return request({
    url: '/elderly-profile/page',
    method: 'get',
    params
  })
}

// 根据ID获取健康档案
export function getById(id) {
  return request({
    url: `/elderly-profile/${id}`,
    method: 'get'
  })
}

// 更新健康档案
export function update(data) {
  return request({
    url: '/elderly-profile',
    method: 'put',
    data
  })
}
