import request from './request'

export function earnPoints(userId, eventCode) {
  return request.post('/points/earn', { userId, eventCode })
}

export function getRules() {
  return request.get('/credit-rule/list')
}
