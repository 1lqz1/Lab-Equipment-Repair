import request from './request'

export function loginApi(data) {
  return request.post('/auth/login', data)
}

export function currentUserApi() {
  return request.get('/auth/me')
}
