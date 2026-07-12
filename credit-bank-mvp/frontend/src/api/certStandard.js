import request from './request'

export function getCertStandards() {
  return request.get('/cert-standard/list')
}

export function getCertStandardById(id) {
  return request.get(`/cert-standard/${id}`)
}

export function createCertStandard(standard) {
  return request.post('/cert-standard/create', standard)
}

export function updateCertStandard(standard) {
  return request.post('/cert-standard/update', standard)
}

export function toggleCertStandard(id) {
  return request.post(`/cert-standard/${id}/toggle`)
}
