import request from '@/utils/request'

// 获取总览统计
export function getOverview(params) {
  return request({
    url: '/statistics/overview',
    method: 'get',
    params
  })
}

// 获取用户注册统计
export function getUserRegister(params) {
  return request({
    url: '/statistics/user-register',
    method: 'get',
    params
  })
}

// 获取健康数据统计
export function getHealthData(params) {
  return request({
    url: '/statistics/health-data',
    method: 'get',
    params
  })
}

// 获取预警统计
export function getAlert(params) {
  return request({
    url: '/statistics/alert',
    method: 'get',
    params
  })
}

// 获取健康数据类型分布
export function getDataTypeDistribution(params) {
  return request({
    url: '/statistics/health-data-type-distribution',
    method: 'get',
    params
  })
}
