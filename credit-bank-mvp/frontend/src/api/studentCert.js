import request from './request'

export function getStudentCerts(studentId, role, userId) {
  return request.get(`/student-cert/student/${studentId}`, { params: { role, userId } })
}

export function getStudentCert(id, role, userId) {
  return request.get(`/student-cert/${id}`, { params: { role, userId } })
}
