import request from '@/utils/request'

export const getDepartmentTree = () => request.get('/department/tree')
export const createDepartment = (data) => request.post('/department/create', data)
export const updateDepartment = (data) => request.put('/department/update', data)
export const deleteDepartment = (id) => request.delete(`/department/${id}`)