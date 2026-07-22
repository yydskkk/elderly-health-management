import request from '@/utils/request'

// 分页查询用户
export function getUserPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

// 编辑用户
export function editUser(data) {
  return request({
    url: '/user',
    method: 'put',
    data
  })
}

// 更新用户状态
export function updateStatus(data) {
  return request({
    url: '/user/status',
    method: 'put',
    data
  })
}

// 重置用户密码
export function resetPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}
