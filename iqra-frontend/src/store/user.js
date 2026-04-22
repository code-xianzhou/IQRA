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
      localStorage.setItem('role', user.role)
    },
    clearUser() {
      this.$reset()
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
    }
  }
})
