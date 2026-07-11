import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let loadingCount = 0

function showLoading() {
  loadingCount++
}

function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
  }
}

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    showLoading()
    return config
  },
  error => {
    hideLoading()
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('user')
          localStorage.removeItem('token')
          router.push('/login')
        })
        return Promise.reject(new Error('登录已过期'))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    hideLoading()
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            localStorage.removeItem('user')
            localStorage.removeItem('token')
            router.push('/login')
          })
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求错误：${status}`)
      }
    } else if (error.message) {
      if (error.message.includes('timeout')) {
        ElMessage.error('请求超时，请稍后重试')
      } else if (error.message.includes('Network Error')) {
        ElMessage.error('网络连接异常，请检查网络')
      } else {
        ElMessage.error(error.message)
      }
    } else {
      ElMessage.error('未知错误')
    }
    return Promise.reject(error)
  }
)

export default request