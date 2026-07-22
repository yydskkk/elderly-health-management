import request from '@/utils/request'

// 分页查询预警通知
export function getNotificationPage(params) {
  return request({
    url: '/alert/notification/page',
    method: 'get',
    params
  })
}

// 获取未读预警通知数量
export function getUnreadCount() {
  return request({
    url: '/alert/notification/unread-count',
    method: 'get'
  })
}

// 标记单条预警通知为已读
export function readNotification(id) {
  return request({
    url: `/alert/notification/read/${id}`,
    method: 'put'
  })
}

// 标记全部预警通知为已读
export function readAllNotifications() {
  return request({
    url: '/alert/notification/read-all',
    method: 'put'
  })
}

// 分页查询预警记录
export function getRecordPage(params) {
  return request({
    url: '/alert/record/page',
    method: 'get',
    params
  })
}

// 根据ID获取预警记录
export function getRecordById(id) {
  return request({
    url: `/alert/record/${id}`,
    method: 'get'
  })
}

// 处理预警记录
export function handleRecord(data) {
  return request({
    url: '/alert/record/handle',
    method: 'put',
    data
  })
}
