import request from './request'

export function getAllCerts() {
  return request.get('/expert-cert/list')
}

export function getCertsByExpert(expertId) {
  return request.get(`/expert-cert/expert/${expertId}`)
}

export function getCertifiedExperts(certStandardId) {
  return request.get('/expert-cert/certified-experts', { params: { certStandardId } })
}
