import request from './request'

export function earnPoints(accountId, ruleCode) {
  return request.post('/points/earn', { accountId, ruleCode })
}

export function getAccounts() {
  return request.get('/points/accounts')
}

export function getAccount(id) {
  return request.get(`/points/account/${id}`)
}

export function getTransactions(accountId) {
  return request.get(`/points/account/${accountId}/transactions`)
}

export function getRules() {
  return request.get('/points/rules')
}
