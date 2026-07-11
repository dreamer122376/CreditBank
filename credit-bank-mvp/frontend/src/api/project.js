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