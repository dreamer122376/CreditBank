import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建 axios 实例，统一配置请求前缀、超时时间和请求头
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求加载计数器，用于追踪当前正在进行的请求数量
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

// 请求拦截器：自动注入 Token 和操作者 ID
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

// 白名单：这些公开 API 即使返回 401 也不触发重定向（游客可直接访问）
const publicApiPaths = ['/exchange-rule/list', '/conversion-rule/list', '/credit-rule/list', '/stats/dashboard']

function isPublicApi(url) {
  return publicApiPaths.some(path => url.includes(path))
}

// 响应拦截器：统一处理业务错误码和 HTTP 状态码
request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    // 业务状态码非 200 说明请求未按预期成功
    if (res.code !== 200) {
      // 401 未登录/过期：清除本地凭证，弹窗引导重新登录（公开 API 除外）
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
      // 403 无权限/被冻结
      if (res.code === 403) {
        ElMessage.warning(res.message || '您已被冻结，无法执行该操作')
        return Promise.reject(new Error(res.message || '无权限'))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 成功时直接返回 data 部分，方便调用方直接使用
    return res.data
  },
  // HTTP 层异常（网络错误、超时、非 2xx 状态码）
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