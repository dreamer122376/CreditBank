import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

// Element Plus 消息提示默认显示时长（毫秒）
const defaultDuration = 3000

// --- 轻量消息提示（ElMessage），适合瞬时反馈 ---

// 成功提示
function success(message, options = {}) {
  return ElMessage.success({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

// 错误提示
function error(message, options = {}) {
  return ElMessage.error({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

// 警告提示
function warning(message, options = {}) {
  return ElMessage.warning({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

// 普通信息提示
function info(message, options = {}) {
  return ElMessage.info({
    message,
    duration: options.duration || defaultDuration,
    ...options
  })
}

// --- 对话框（ElMessageBox），适合需要用户确认的操作 ---

// 确认弹窗（确定/取消），常用于删除等危险操作前二次确认
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

// 提示弹窗（仅确定按钮）
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

// 输入弹窗，接收用户输入文本
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

// --- 通知栏（ElNotification），顶部弹出，适合系统级消息 ---

// 通用通知
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

// 成功通知（预设标题"成功"）
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

// 错误通知（预设标题"错误"）
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

// 警告通知（预设标题"警告"）
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

// 信息通知（预设标题"信息"）
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