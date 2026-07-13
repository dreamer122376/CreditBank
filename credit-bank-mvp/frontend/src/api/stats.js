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

export function getRecentTransactions(userId, limit = 5) {
  return request.get('/stats/recent-transactions', {
    params: { userId, limit }
  })
}

export function getPointTrend(userId, days = 7) {
  return request.get('/stats/point-trend', {
    params: { userId, days }
  })
}
