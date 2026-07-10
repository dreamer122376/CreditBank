import request from './request'

export function getExperts() {
  return request.get('/expert/list')
}

export function createExpert(user) {
  return request.post('/expert/create', user)
}

export function updateExpert(user) {
  return request.post('/expert/update', user)
}

export function changeExpertStatus(id, status) {
  return request.post(`/expert/${id}/status`, { status })
}
