import request from '@/utils/request'

export const getSkillList = () => request.get('/skill/list')
export const updateSkill = (data) => request.post('/skill/update', data)
export const batchUpdateSkill = (data) => request.post('/skill/batchUpdate', data)
