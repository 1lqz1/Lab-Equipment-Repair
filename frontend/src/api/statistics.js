import request from './request'

export function dashboardStats() {
  return request.get('/statistics/dashboard')
}
