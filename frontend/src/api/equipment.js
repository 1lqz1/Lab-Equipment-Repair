import request from './request'

export function listEquipment(params) {
  return request.get('/equipment', { params })
}

export function createEquipment(data) {
  return request.post('/equipment', data)
}
