import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

const defaultDuration = 3000

function success(message, options = {}) {
  return ElMessage.success({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

function error(message, options = {}) {
  return ElMessage.error({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

function warning(message, options = {}) {
  return ElMessage.warning({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

function info(message, options = {}) {
  return ElMessage.info({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

function confirm(message, options = {}) {
  return ElMessageBox.confirm(
    message,
    options.title || '提示',
    {
      confirmButtonText: options.confirmText || '确定',
      cancelButtonText: options.cancelText || '取消',
      type: options.type || 'warning',
      confirmButtonClass: 'el-button--primary',
      ...options
    }
  )
}

function alert(message, options = {}) {
  return ElMessageBox.alert(
    message,
    options.title || '提示',
    {
      confirmButtonText: options.confirmText || '确定',
      type: options.type || 'info',
      ...options
    }
  )
}

function prompt(message, options = {}) {
  return ElMessageBox.prompt(
    message,
    options.title || '提示',
    {
      confirmButtonText: options.confirmText || '确定',
      cancelButtonText: options.cancelText || '取消',
      inputValue: options.inputValue || '',
      inputPlaceholder: options.inputPlaceholder || '',
      inputPattern: options.inputPattern || null,
      inputErrorMessage: options.inputErrorMessage || '输入格式不正确',
      type: options.type || 'prompt',
      ...options
    }
  )
}

function notify(message, options = {}) {
  return ElNotification({
    title: options.title || '',
    message,
    type: options.type || 'success',
    duration: options.duration || defaultDuration,
    position: options.position || 'top-right',
    ...options
  })
}

function notifySuccess(message, options = {}) {
  return ElNotification({
    title: options.title || '成功',
    message,
    type: 'success',
    duration: options.duration || defaultDuration,
    position: options.position || 'top-right',
    ...options
  })
}

function notifyError(message, options = {}) {
  return ElNotification({
    title: options.title || '错误',
    message,
    type: 'error',
    duration: options.duration || defaultDuration,
    position: options.position || 'top-right',
    ...options
  })
}

function notifyWarning(message, options = {}) {
  return ElNotification({
    title: options.title || '警告',
    message,
    type: 'warning',
    duration: options.duration || defaultDuration,
    position: options.position || 'top-right',
    ...options
  })
}

function notifyInfo(message, options = {}) {
  return ElNotification({
    title: options.title || '信息',
    message,
    type: 'info',
    duration: options.duration || defaultDuration,
    position: options.position || 'top-right',
    ...options
  })
}

export default {
  success,
  error,
  warning,
  info,
  confirm,
  alert,
  prompt,
  notify,
  notifySuccess,
  notifyError,
  notifyWarning,
  notifyInfo
}