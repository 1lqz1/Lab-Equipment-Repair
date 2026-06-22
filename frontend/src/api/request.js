import { useAuthStore } from '@/stores/auth'

const baseURL = 'http://localhost:8080/api'

async function send(url, options = {}) {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }

  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${baseURL}${url}`, {
    ...options,
    headers,
  })
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.code >= 400) {
    throw new Error(payload?.message || '请求失败')
  }

  return payload
}

function withParams(url, params) {
  if (!params) {
    return url
  }
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, value)
    }
  })
  const query = search.toString()
  return query ? `${url}?${query}` : url
}

export default {
  get(url, config = {}) {
    return send(withParams(url, config.params), { method: 'GET' })
  },
  post(url, data) {
    return send(url, { method: 'POST', body: JSON.stringify(data) })
  },
  put(url, data) {
    return send(url, {
      method: 'PUT',
      body: data === undefined ? undefined : JSON.stringify(data),
    })
  },
  delete(url) {
    return send(url, { method: 'DELETE' })
  },
}
