import request from './request'

export function getOrganizations() {
  return request.get('/organization/list')
}

export function createOrganization(org) {
  return request.post('/organization/create', org)
}

export function updateOrganization(org) {
  return request.post('/organization/update', org)
}

export function changeOrganizationStatus(id, status) {
  return request.post(`/organization/${id}/status`, { status })
}

export function rejectOrganization(id, reason) {
  return request.post(`/organization/${id}/reject`, { reason })
}
