import { defineStore } from 'pinia'
import { getUnreadCount as getUnreadCountApi } from '@/api/alert'

export const useAlertStore = defineStore('alert', {
  state: () => ({
    unreadCount: 0
  }),
  actions: {
    // 获取未读预警数量
    async getUnreadCount() {
      try {
        const data = await getUnreadCountApi()
        this.unreadCount = typeof data === 'number' ? data : (data?.count || 0)
      } catch (e) {
        // 静默处理，避免影响主流程
      }
      return this.unreadCount
    },

    // 未读数 +1
    incrementUnread() {
      this.unreadCount += 1
    },

    // 未读数 -1（不低于0）
    decrementUnread() {
      if (this.unreadCount > 0) {
        this.unreadCount -= 1
      }
    },

    // 重置未读数
    resetUnread() {
      this.unreadCount = 0
    }
  }
})
