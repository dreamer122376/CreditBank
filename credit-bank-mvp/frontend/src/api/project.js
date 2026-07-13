import request from './request'

// ==================== 新的 /api/projects 接口 ====================

/** 学生端：查询已上架项目列表 */
export function getActiveProjects() {
  return request.get('/projects/active')
}

/** 项目详情（学生端查看时传 studentId 以标记是否已报名） */
export function getProjectDetail(id, studentId) {
  const params = studentId ? { studentId } : {}
  return request.get('/projects/' + id, { params })
}

/** 学生报名项目 */
export function enrollProject(id) {
  return request.post('/projects/' + id + '/enroll')
}

/** 学生取消报名 */
export function cancelEnrollProject(id) {
  return request.delete('/projects/' + id + '/enroll')
}

/** 我的项目列表 */
export function getMyProjects() {
  return request.get('/projects/my')
}

// ==================== 旧的 /student-projects 接口（兼容保留） ====================

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