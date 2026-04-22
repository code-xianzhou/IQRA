import request from '@/utils/request'

export const getModelConfig = () => request.get('/model/config')
export const switchModel = (data) => request.post('/model/switch', data)
export const updateModel = (data) => request.post('/model/update', data)
export const getMcpConfig = () => request.get('/mcp/config')
export const updateMcpConfig = (data) => request.post('/mcp/update', data)
