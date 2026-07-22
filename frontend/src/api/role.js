import request from '@/utils/request'

// 获取角色列表
export function getRoleList() {
  return request({
    url: '/role/list',
    method: 'get'
  })
}

// 新增角色
export function addRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

// 编辑角色
export function editRole(data) {
  return request({
    url: '/role',
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

// 获取角色的权限列表
export function getRolePermissions(id) {
  return request({
    url: `/role/${id}/permissions`,
    method: 'get'
  })
}

// 为角色分配权限
export function assignPermissions(id, data) {
  return request({
    url: `/role/${id}/permissions`,
    method: 'put',
    data
  })
}
