import request from '@/utils/request'

// 分页查询干预方案
export function getPage(params) {
  return request({
    url: '/intervention-plan/page',
    method: 'get',
    params
  })
}

// 更新干预方案
export function update(data) {
  return request({
    url: '/intervention-plan',
    method: 'put',
    data
  })
}

// 终止干预方案
export function terminate(id) {
  return request({
    url: `/intervention-plan/terminate/${id}`,
    method: 'put'
  })
}

// 获取干预方案反馈列表
export function getFeedbacks(id) {
  return request({
    url: `/intervention-plan/${id}/feedbacks`,
    method: 'get'
  })
}

// 获取我的干预方案
export function getMine(params) {
  return request({
    url: '/intervention-plan/mine',
    method: 'get',
    params
  })
}

// 提交干预方案反馈
export function addFeedback(data) {
  return request({
    url: '/intervention-plan/feedback',
    method: 'post',
    data
  })
}
