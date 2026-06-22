import request from './request'

export function listUsers(params) {
  return request.get('/users', { params })
}
