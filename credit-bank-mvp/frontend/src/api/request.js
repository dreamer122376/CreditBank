import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
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
    try {
      const token = localStorage.getItem('cb_token')
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
      const saved = localStorage.getItem('cb_user')
      if (saved) {
        const user = JSON.parse(saved)
        if (user && user.id) {
          config.headers['X-Operator-Id'] = user.id
        }
      }
    } catch (e) { /* ignore */ }
    showLoading()
    return config
  },
  error => {
    hideLoading()
    return Promise.reject(error)
  }
)

const publicApiPaths = ['/exchange-rule/list', '/conversion-rule/list', '/stats/dashboard']

function isPublicApi(url) {
  return publicApiPaths.some(path => url.includes(path))
}

request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401 && !isPublicApi(response.config.url)) {
        localStorage.removeItem('cb_user')
        localStorage.removeItem('cb_token')
        ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          router.push('/login')
        }).catch(() => {})
        return Promise.reject(new Error('登录已过期'))
      }
      if (res.code === 403) {
        ElMessage.warning(res.message || '您已被冻结，无法执行该操作')
        return Promise.reject(new Error(res.message || '无权限'))
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
          if (!isPublicApi(error.config.url)) {
            localStorage.removeItem('cb_user')
            localStorage.removeItem('cb_token')
            ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              router.push('/login')
            }).catch(() => {})
          }
          break
        case 403:
          ElMessage.warning(data?.message || '无权限访问')
          break
        case 404:
          ElMessage.error(data?.message || '请求资源不存在')
          break
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误，请稍后重试')
          break
        case 502:
          ElMessage.error('网关错误，请稍后重试')
          break
        case 503:
          ElMessage.error('服务暂时不可用，请稍后重试')
          break
        default:
          ElMessage.error(data?.message || `请求错误：${status}`)
      }
    } else if (error.message) {
      if (error.message.includes('timeout')) {
        ElMessage.error('请求超时，请稍后重试')
      } else if (error.message.includes('Network Error')) {
        ElMessage.error('网络连接异常，请检查网络')
      } else if (error.message.includes('abort')) {
        /* 取消请求，不提示 */
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