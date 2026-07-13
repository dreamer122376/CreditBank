import request from './request'

export function signIn() {
  return request.post('/signin')
}

export function getSignInStatus() {
  return request.get('/signin/status')
}

export function getSignInHistory(limit = 30) {
  return request.get('/signin/history', { params: { limit } })
}