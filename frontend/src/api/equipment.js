import request from './request'

export function listEquipment(params) {
  return request.get('/equipment', { params })
}

export function createEquipment(data) {
  return request.post('/equipment', data)
}

export function updateEquipment(id, data) {
  return request.put(`/equipment/${id}`, data)
}

export function updateEquipmentStatus(id, status) {
  return request.put(`/equipment/${id}/status`, { status })
}

export function deleteEquipment(id) {
  return request.delete(`/equipment/${id}`)
}
