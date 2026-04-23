import request from '@/utils/request'

export const getUsers = () => request.get('/user/list')
export const createUser = (data) => request.post('/user/create', data)
export const deleteUser = (id) => request.delete(`/user/${id}`)
export const changePassword = (data) => request.post('/auth/change-password', data)
export const resetPassword = (id) => request.post(`/user/reset-password/${id}`)