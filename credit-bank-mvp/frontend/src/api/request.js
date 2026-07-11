import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：自动附带当前登录用户ID
request.interceptors.request.use(config => {
  try {
    const saved = localStorage.getItem('cb_user')
    if (saved) {
      const user = JSON.parse(saved)
      if (user && user.id) {
        config.headers['X-Operator-Id'] = user.id
      }
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
    return Promise.reject(error)
  }
)

export default request
