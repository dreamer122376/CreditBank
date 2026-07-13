import request from './request'

export function getMyProjects() {
  return request.get('/student-projects/my-projects')
}

export function getMyProjectDetail() {
  return request.get('/student-projects/my-detail')
}

export function registerProject(projectId) {
  return request.post('/student-projects/register', null, { params: { projectId } })
}

export function updateProjectStatus(id, status) {
  return request.put('/student-projects/' + id + '/status', null, { params: { status } })
}

export function getStudentsByProject(projectId) {
  return request.get('/student-projects/project/' + projectId + '/students')
}

// ==================== 管理端 ====================

export function getOrgProjects() {
  return request.get('/project/my-org')
}

export function getAllProjects() {
  return request.get('/project/list')
}

export function getProjectDetail(id) {
  return request.get('/project/' + id + '/detail')
}

export function createProject(data) {
  return request.post('/project/create', data)
}

export function updateProject(data) {
  return request.post('/project/update', data)
}

export function deleteProject(id) {
  return request.delete('/project/' + id)
}