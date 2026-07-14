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

export function submitProjectForReview(id) {
  return request.post('/student-projects/' + id + '/submit')
}

export function auditProjectCompletion(id, approve) {
  return request.post('/student-projects/' + id + '/audit', null, { params: { approve } })
}

export function getStudentsByProject(projectId) {
  return request.get('/student-projects/project/' + projectId + '/students')
}

// ==================== 管理端 ====================

/** 机构端：本机构项目列表 */
export function getOrgProjects() {
  return request.get('/projects/org')
}

/** 管理端：项目分页列表 */
export function getAllProjects(params) {
  return request.get('/projects', { params })
}

/** 项目详情（含报名学生） */
export function getOrgProjectDetail(id) {
  return request.get('/projects/' + id)
}

/** 创建项目 */
export function createProject(data) {
  return request.post('/projects', data)
}

/** 编辑项目 */
export function updateProject(id, data) {
  return request.put('/projects/' + id, data)
}

/** 下架项目 */
export function offlineProject(id) {
  return request.post('/projects/' + id + '/offline')
}

/** 审核项目 */
export function auditProject(id, data) {
  return request.post('/projects/' + id + '/audit', data)
}