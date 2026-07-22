import request from '@/utils/request'

// 邀请关联
export function invite(data) {
  return request({
    url: '/relation/invite',
    method: 'post',
    data
  })
}

// 申请关联
export function apply(data) {
  return request({
    url: '/relation/apply',
    method: 'post',
    data
  })
}

// 管理员强制关联
export function adminLink(data) {
  return request({
    url: '/relation/admin-link',
    method: 'post',
    data
  })
}

// 确认关联
export function confirm(id) {
  return request({
    url: `/relation/confirm/${id}`,
    method: 'post'
  })
}

// 拒绝关联
export function reject(id) {
  return request({
    url: `/relation/reject/${id}`,
    method: 'post'
  })
}

// 获取关联列表
export function getList(params) {
  return request({
    url: '/relation/list',
    method: 'get',
    params
  })
}

// 获取我关联的老年用户
export function getMyElderly() {
  return request({
    url: '/relation/my-elderly',
    method: 'get'
  })
}

// 获取我关联的家属
export function getMyFamily() {
  return request({
    url: '/relation/my-family',
    method: 'get'
  })
}

// 解除关联
export function unlink(id) {
  return request({
    url: `/relation/${id}`,
    method: 'delete'
  })
}
