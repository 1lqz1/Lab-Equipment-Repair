import request from './request'

export function getProfile() {
  return request.get('/profile')
}

export function updateProfile(data) {
  return request.put('/profile', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/profile/avatar', formData)
}
