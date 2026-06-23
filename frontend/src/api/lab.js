import request from './request'

export function listLabs() {
  return request.get('/labs')
}

export function queryLabs(params) {
  return request.get('/labs', { params })
}

export function createLab(data) {
  return request.post('/labs', data)
}

export function updateLab(id, data) {
  return request.put(`/labs/${id}`, data)
}

export function enableLab(id) {
  return request.put(`/labs/${id}/enable`)
}

export function disableLab(id) {
  return request.put(`/labs/${id}/disable`)
}

export function deleteLab(id) {
  return request.delete(`/labs/${id}`)
}
