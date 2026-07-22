import request from '@/utils/request'

// 获取辖区列表
export function getList(params) {
  return request({
    url: '/region/list',
    method: 'get',
    params
  })
}

// 新增辖区
export function add(data) {
  return request({
    url: '/region',
    method: 'post',
    data
  })
}

// 修改辖区
export function update(data) {
  return request({
    url: '/region',
    method: 'put',
    data
  })
}

// 删除辖区
export function remove(id) {
  return request({
    url: `/region/${id}`,
    method: 'delete'
  })
}

// 更新辖区状态
export function updateStatus(id, data) {
  return request({
    url: `/region/status/${id}`,
    method: 'put',
    params: { status: data.status }
  })
}

// 分配辖区
export function assign(data) {
  return request({
    url: '/region/assign',
    method: 'post',
    data
  })
}

// 取消辖区分配
export function removeAssign(data) {
  return request({
    url: '/region/assign',
    method: 'delete',
    data
  })
}

// 获取辖区下的用户
export function getUsers(id) {
  return request({
    url: `/region/${id}/users`,
    method: 'get'
  })
}

// 获取我的辖区
export function getMyRegions() {
  return request({
    url: '/region/my-regions',
    method: 'get'
  })
}
