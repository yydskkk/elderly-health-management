import request from '@/utils/request'

// 录入健康数据（老年用户本人）
export function add(data) {
  return request({
    url: '/health-data',
    method: 'post',
    data
  })
}

// 家属为老年用户录入健康数据
export function familyAdd(elderlyId, data) {
  return request({
    url: `/health-data/family/${elderlyId}`,
    method: 'post',
    data
  })
}

// 查询健康数据列表
export function getList(params) {
  return request({
    url: '/health-data/list',
    method: 'get',
    params
  })
}

// 获取健康数据趋势
export function getTrend(params) {
  return request({
    url: '/health-data/trend',
    method: 'get',
    params
  })
}

// 获取健康数据统计
export function getStatistics(params) {
  return request({
    url: '/health-data/statistics',
    method: 'get',
    params
  })
}

// 获取健康数据类型
export function getTypes() {
  return request({
    url: '/health-data/types',
    method: 'get'
  })
}
