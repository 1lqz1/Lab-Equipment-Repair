import request from './request'

export function listDicts(params) {
  return request.get('/dicts', { params })
}

export function createDict(data) {
  return request.post('/dicts', data)
}

export function updateDict(id, data) {
  return request.put(`/dicts/${id}`, data)
}

export function deleteDict(id) {
  return request.delete(`/dicts/${id}`)
}

export function listDictItems(dictCode, params) {
  return request.get(`/dicts/${dictCode}/items`, { params })
}

export function createDictItem(dictId, data) {
  return request.post(`/dicts/${dictId}/items`, data)
}

export function updateDictItem(itemId, data) {
  return request.put(`/dicts/items/${itemId}`, data)
}

export function deleteDictItem(itemId) {
  return request.delete(`/dicts/items/${itemId}`)
}
