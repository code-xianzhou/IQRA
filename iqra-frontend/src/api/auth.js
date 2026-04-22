import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.get('/auth/logout')
export const getCurrentUser = () => request.get('/auth/current')
