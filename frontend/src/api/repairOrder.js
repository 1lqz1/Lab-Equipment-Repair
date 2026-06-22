import request from './request'

export function listRepairOrders(params) {
  return request.get('/repair-orders', { params })
}

export function getRepairOrder(id) {
  return request.get(`/repair-orders/${id}`)
}

export function submitRepairOrder(data) {
  return request.post('/repair-orders', data)
}

export function assignRepairOrder(id, data) {
  return request.put(`/repair-orders/${id}/assign`, data)
}

export function rejectRepairOrder(id, data) {
  return request.put(`/repair-orders/${id}/reject`, data)
}

export function startRepair(id) {
  return request.put(`/repair-orders/${id}/start`)
}

export function finishRepair(id, data) {
  return request.put(`/repair-orders/${id}/finish`, data)
}

export function acceptRepair(id, data) {
  return request.put(`/repair-orders/${id}/accept`, data)
}

export function listOrderLogs(id) {
  return request.get(`/repair-orders/${id}/logs`)
}
