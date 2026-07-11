import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  try {
    const token = localStorage.getItem('cb_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
  } catch (e) { /* ignore */ }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('cb_user')
      localStorage.removeItem('cb_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
