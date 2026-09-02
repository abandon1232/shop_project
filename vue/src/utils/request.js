import axios from 'axios'
import router from '@/router'

export function applyAuthHeader(config, storage = localStorage) {
  config.headers ||= {}
  try {
    const user = JSON.parse(storage.getItem('xm-user') || '{}')
    if (user.token) {
      config.headers.token = user.token
    }
  } catch {
    // Malformed browser storage should not break an otherwise anonymous request.
  }
  return config
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090',
  timeout: 30000,
})

request.interceptors.request.use(
  config => applyAuthHeader(config),
  error => Promise.reject(error),
)

request.interceptors.response.use(
  response => {
    let result = response.data
    if (typeof result === 'string') {
      result = result ? JSON.parse(result) : result
    }
    if (result?.code === '401') {
      router.push('/login')
    }
    return result
  },
  error => Promise.reject(error),
)

export default request
