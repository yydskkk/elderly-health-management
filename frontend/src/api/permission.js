import request from '@/utils/request'

// 获取权限树
export function getPermissionTree() {
  return request({
    url: '/permission/tree',
    method: 'get'
  })
}

// 获取当前用户菜单
export function getMenus() {
  return request({
    url: '/permission/menus',
    method: 'get'
  })
}
