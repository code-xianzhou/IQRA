import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    username: '',
    realName: '',
    department: '',
    role: ''
  }),
  actions: {
    setUser(user) {
      this.id = user.id
      this.username = user.username
      this.realName = user.realName
      this.department = user.department
      this.role = user.role
      localStorage.setItem('userId', user.id)
      localStorage.setItem('username', user.username)
      localStorage.setItem('realName', user.realName)
      localStorage.setItem('department', user.department || '')
      localStorage.setItem('role', user.role)
    },
    clearUser() {
      this.$reset()
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('department')
      localStorage.removeItem('role')
    },
    initUser() {
      // 从localStorage恢复用户状态
      const userId = localStorage.getItem('userId')
      const role = localStorage.getItem('role')
      const username = localStorage.getItem('username')
      const realName = localStorage.getItem('realName')
      const department = localStorage.getItem('department')
      
      if (userId && role) {
        this.id = userId
        this.role = role
        this.username = username || ''
        this.realName = realName || ''
        this.department = department || ''
        // 这里可以根据需要从后端获取完整用户信息
      }
    }
  }
})