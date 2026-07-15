import request from './request'

export function getConversionApplications(status) {
  const params = status !== undefined ? { status } : {}
  return request.get('/conversion-application/list', { params })
}

export function getConversionApplicationDetail(id) {
  return request.get('/conversion-application/' + id)
}

export function submitConversionApplication(data) {
  return request.post('/conversion-application/submit', data)
}

export function auditConversionApplication(id, approve, reason) {
  return request.post('/conversion-application/' + id + '/audit', { approve, reason })
}