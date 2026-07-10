import request from './request'

export function login(username, password) {
  return request.post('/user/login', { username, password })
}

export function getUsers() {
  return request.get('/user/list')
}

export function getUser(id) {
  return request.get(`/user/${id}`)
}

export function getTransactions(userId) {
  return request.get(`/user/${userId}/transactions`)
}
