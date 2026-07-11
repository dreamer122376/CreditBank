import request from './request'

export function getApplications(role, userId) {
  return request.get('/application/list', { params: { role, userId } })
}

export function submitApplication(app) {
  return request.post('/application/submit', app)
}

export function auditApplication(id, role, approve, reason) {
  return request.post(`/application/${id}/audit`, { role, approve, reason })
}
