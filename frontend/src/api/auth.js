import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 忘记密码（重置密码）
export function forgotPassword(data) {
  return request({
    url: '/auth/forgot-password',
    method: 'post',
    data
  })
}

// 发送验证码
export function sendCode(data) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    data
  })
}

// 获取当前登录用户信息
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}
