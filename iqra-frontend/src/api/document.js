import request from '@/utils/request'

export const uploadDocument = (file, data) => {
  const formData = new FormData()
  formData.append('file', file)
  if (data.department) formData.append('department', data.department)
  if (data.tags) formData.append('tags', data.tags)
  if (data.uploadBy) formData.append('uploadBy', data.uploadBy)
  return request.post('/document/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getDocumentList = (params) => request.get('/document/list', { params })
export const deleteDocument = (id) => request.delete(`/document/${id}`)
