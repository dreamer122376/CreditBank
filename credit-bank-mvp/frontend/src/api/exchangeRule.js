import request from './request'

export function getExchangeRules() {
  return request.get('/exchange-rule/list')
}

export function createExchangeRule(rule) {
  return request.post('/exchange-rule/create', rule)
}

export function updateExchangeRule(rule) {
  return request.post('/exchange-rule/update', rule)
}

export function toggleExchangeRule(id) {
  return request.post(`/exchange-rule/${id}/toggle`)
}
