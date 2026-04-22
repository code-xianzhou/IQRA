import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    username: '',
    realName: '',
    department: ''
  }),
  actions: {
    setUser(user) {
      this.id = user.id
      this.username = user.username
      this.realName = user.realName
      this.department = user.department
      localStorage.setItem('userId', user.id)
    },
    clearUser() {
      this.$reset()
      localStorage.removeItem('userId')
    }
  }
})
