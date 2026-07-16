import request from './request'

export function getStatsSummary(role, userId) {
  return request.get('/stats/summary', {
    params: { role, userId }
  })
}

export function getTodoList(role, userId, limit = 10) {
  return request.get('/stats/todo-list', {
    params: { role, userId, limit }
  })
}

export function getRecentTransactions(userId, role, limit = 5) {
  return request.get('/stats/recent-transactions', {
    params: { userId, role, limit }
  })
}

export function getPointTrend(userId, days = 7) {
  return request.get('/stats/point-trend', {
    params: { userId, days }
  })
}

export function getDashboardData() {
  return request.get('/stats/dashboard')
}
