import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

const message = {
  success(msg, options = {}) {
    ElMessage.success({
      message: msg,
      duration: options.duration || 2000,
      showClose: options.showClose || true,
      type: 'success',
      ...options
    })
  },

  error(msg, options = {}) {
    ElMessage.error({
      message: msg,
      duration: options.duration || 3000,
      showClose: options.showClose || true,
      type: 'error',
      ...options
    })
  },

  warning(msg, options = {}) {
    ElMessage.warning({
      message: msg,
      duration: options.duration || 2500,
      showClose: options.showClose || true,
      type: 'warning',
      ...options
    })
  },

  info(msg, options = {}) {
    ElMessage.info({
      message: msg,
      duration: options.duration || 2000,
      showClose: options.showClose || true,
      type: 'info',
      ...options
    })
  },

  confirm(msg, options = {}) {
    return ElMessageBox.confirm(msg, options.title || '确认', {
      confirmButtonText: options.confirmText || '确定',
      cancelButtonText: options.cancelText || '取消',
      type: options.type || 'warning',
      ...options
    })
  },

  alert(msg, options = {}) {
    return ElMessageBox.alert(msg, options.title || '提示', {
      confirmButtonText: options.confirmText || '确定',
      type: options.type || 'info',
      ...options
    })
  },

  prompt(msg, options = {}) {
    return ElMessageBox.prompt(msg, options.title || '输入', {
      confirmButtonText: options.confirmText || '确定',
      cancelButtonText: options.cancelText || '取消',
      type: options.type || 'prompt',
      inputValue: options.inputValue || '',
      inputPlaceholder: options.inputPlaceholder || '',
      ...options
    })
  },

  notify(msg, options = {}) {
    ElNotification({
      title: options.title || '通知',
      message: msg,
      type: options.type || 'info',
      duration: options.duration || 4000,
      showClose: options.showClose || true,
      position: options.position || 'top-right',
      ...options
    })
  },

  notifySuccess(msg, options = {}) {
    this.notify(msg, { ...options, type: 'success' })
  },

  notifyError(msg, options = {}) {
    this.notify(msg, { ...options, type: 'error' })
  },

  notifyWarning(msg, options = {}) {
    this.notify(msg, { ...options, type: 'warning' })
  },

  notifyInfo(msg, options = {}) {
    this.notify(msg, { ...options, type: 'info' })
  }
}

export default message