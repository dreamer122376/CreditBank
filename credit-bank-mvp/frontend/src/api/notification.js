import request from './request'

export function getNotifications(params = {}) {
  return request.get('/notifications', { params })
}

export function getUnreadNotificationCount() {
  return request.get('/notifications/unread-count')
}

export function markNotificationRead(id) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/notifications/read-all')
}

export function publishNotification(data) {
  return request.post('/notifications/publish', data)
}
