import request from '@/utils/request'

export const ask = (data) => request.post('/chat/ask', data)
export const getHistory = (params) => request.get('/chat/history', { params })
export const deleteHistory = (params) => request.delete('/chat/history', { params })
