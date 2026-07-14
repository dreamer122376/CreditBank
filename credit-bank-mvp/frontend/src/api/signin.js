import request from './request'

export function signIn() {
  return request.post('/sign-in')
}

export function getSignInStatus() {
  return request.get('/sign-in/status')
}

export function getSignInHistory(limit = 30) {
  return request.get('/sign-in/history', { params: { limit } })
}