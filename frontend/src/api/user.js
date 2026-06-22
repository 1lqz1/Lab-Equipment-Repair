import request from './request'

export function listUsers(params) {
  return request.get('/users', { params })
}

export function createUser(data) {
  return request.post('/users', data)
}

export function approveUser(id) {
  return request.put(`/users/${id}/approve`)
}

export function rejectUser(id) {
  return request.put(`/users/${id}/reject`)
}

export function disableUser(id) {
  return request.put(`/users/${id}/disable`)
}

export function enableUser(id) {
  return request.put(`/users/${id}/enable`)
}
