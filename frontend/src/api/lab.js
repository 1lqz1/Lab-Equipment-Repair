import request from './request'

export function listLabs() {
  return request.get('/labs')
}

export function createLab(data) {
  return request.post('/labs', data)
}
