import request from './request'

export function getConversionRules(enabledOnly = false) {
  const params = enabledOnly ? { enabled: true } : {}
  return request.get('/conversion-rule/list', { params })
}

export function getConversionRuleDetail(id) {
  return request.get('/conversion-rule/' + id)
}

export function createConversionRule(data) {
  return request.post('/conversion-rule/create', data)
}

export function updateConversionRule(data) {
  return request.post('/conversion-rule/update', data)
}

export function deleteConversionRule(id) {
  return request.delete('/conversion-rule/' + id)
}

export function toggleConversionRule(id, isEnabled) {
  return request.post('/conversion-rule/' + id + '/toggle', { isEnabled })
}