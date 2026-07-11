import request from './request'

export function getProfile(userId) {
  return request.get(`/profile/${userId}`)
}

export function updateProfile(data) {
  return request.post('/profile/update', data)
}

export function changePassword(userId, oldPassword, newPassword) {
  return request.post('/profile/password', { userId, oldPassword, newPassword })
}
