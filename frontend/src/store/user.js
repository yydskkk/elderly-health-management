import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') || {},
    permissions: []
  }),
  getters: {
    isLogin: (state) => !!state.token,
    roleCode: (state) => state.userInfo.roleCode || '',
    roleName: (state) => state.userInfo.roleName || '',
    avatar: (state) => state.userInfo.avatar || ''
  },
  actions: {
    // 登录
    async login(loginForm) {
      const data = await loginApi(loginForm)
      const token = data?.token || data
      this.token = token
      localStorage.setItem('token', token)
      return data
    },

    // 获取用户信息
    async getUserInfo() {
      const data = await getUserInfoApi()
      this.userInfo = data || {}
      this.permissions = data?.permissions || []
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return data
    },

    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      this.permissions = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 更新本地用户信息
    updateUserInfo(data) {
      this.userInfo = { ...this.userInfo, ...data }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
