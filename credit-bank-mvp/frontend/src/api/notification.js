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

export function getPublishedNotifications(params = {}) {
  return request.get('/notifications/published', { params })
}

export function revokeNotification(id) {
  return request.put(`/notifications/${id}/revoke`)
}

export function confirmNotification(id) {
  return request.put(`/notifications/${id}/confirm`)
}
