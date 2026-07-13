import request from './request'

export function getApplications(role, userId) {
  return request.get('/application/list', { params: { role, userId } })
}

export function submitApplication(app) {
  return request.post('/application/submit', app)
}

export function auditApplication(id, role, userId, approve, reason) {
  return request.post(`/application/${id}/audit`, { role, userId, approve, reason })
}

export function resubmitApplication(id, userId, formData) {
  return request.post(`/application/${id}/resubmit`, { userId, formData })
}
