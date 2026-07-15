import request from './request'

// ==================== 认证 ====================

export function login(username, password) {
  return request.post('/user/login', { username, password })
}

export function register(data) {
  return request.post('/user/register', data)
}

// [TEST-ONLY] 测试用跳过密码登录
export function testLogin(username) {
  return request.post('/user/test-login', { username })
}

// [TEST-ONLY] 测试用获取用户列表（无需认证）
export function getTestUsers() {
  return request.get('/user/test-users')
}

// ==================== 用户管理 ====================

export function getUsers() {
  return request.get('/users')
}

export function getAuditorCandidates() {
  return request.get('/users/auditor-candidates')
}

export function getUser(id) {
  return request.get(`/users/${id}`)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

// ==================== 状态操作 ====================

export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, { status })
}

export function batchUpdateStatus(ids, status) {
  return request.put('/users/batch-status', { ids, status })
}

// ==================== 重置密码 ====================

export function resetPassword(id, newPassword) {
  return request.put(`/users/${id}/reset-pw`, { newPassword })
}

// ==================== 积分流水 ====================

export function getTransactions(userId) {
  return request.get(`/user/${userId}/transactions`)
}

// ==================== 操作日志 ====================

export function getOpLogs(params = {}) {
  return request.get('/users/op-logs', { params })
}

// ==================== 解冻申诉 ====================

export function submitUnfreezeAppeal(reason) {
  const saved = localStorage.getItem('cb_user')
  const user = saved ? JSON.parse(saved) : {}
  return request.post('/application/submit', {
    bizType: 'UNFREEZE_APPEAL',
    applicantId: user.id,
    formData: JSON.stringify({ reason })
  })
}
