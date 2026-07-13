import request from './request'

export function earnPoints(userId, eventCode) {
  return request.post('/points/earn', { userId, eventCode })
}

export function getRules(enabledOnly = false) {
  const params = enabledOnly ? { enabled: true } : {}
  return request.get('/credit-rule/list', { params })
}

export function getRuleDetail(id) {
  return request.get('/credit-rule/' + id)
}

export function createRule(data) {
  return request.post('/credit-rule/create', data)
}

export function updateRule(data) {
  return request.post('/credit-rule/update', data)
}

export function deleteRule(id) {
  return request.delete('/credit-rule/' + id)
}

export function toggleRule(id, isEnabled) {
  return request.post('/credit-rule/' + id + '/toggle', { isEnabled })
}

export function getProjects() {
  return request.get('/projects/active')
}