import request from '@/utils/request'

// 获取预警规则列表
export function getList(params) {
  return request({
    url: '/alert-rule/list',
    method: 'get',
    params
  })
}

// 新增预警规则
export function add(data) {
  return request({
    url: '/alert-rule',
    method: 'post',
    data
  })
}

// 修改预警规则
export function update(data) {
  return request({
    url: '/alert-rule',
    method: 'put',
    data
  })
}

// 删除预警规则
export function remove(id) {
  return request({
    url: `/alert-rule/${id}`,
    method: 'delete'
  })
}

// 更新预警规则状态
export function updateStatus(id, data) {
  return request({
    url: `/alert-rule/status/${id}`,
    method: 'put',
    params: { status: data.status }
  })
}

// 获取个性化预警规则（含全局规则回退），userId 为老人用户ID
export function getPersonal(userId) {
  return request({
    url: `/alert-rule/personal/${userId}`,
    method: 'get'
  })
}

// 新增个性化预警规则
export function addPersonal(data) {
  return request({
    url: '/alert-rule/personal',
    method: 'post',
    data
  })
}

// 删除个性化预警规则（恢复使用全局规则）
export function deletePersonal(id) {
  return request({
    url: `/alert-rule/personal/${id}`,
    method: 'delete'
  })
}
