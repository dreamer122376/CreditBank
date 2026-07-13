import request from './request'

export function getStudentCerts(studentId, role, userId) {
  return request.get(`/student-cert/student/${studentId}`, { params: { role, userId } })
}

export function getStudentCert(id, role, userId) {
  return request.get(`/student-cert/${id}`, { params: { role, userId } })
}

export function getStudentCertByApplication(applicationId, role, userId) {
  return request.get(`/student-cert/application/${applicationId}`, { params: { role, userId } })
}

export function revokeStudentCert(id, role, userId, reason) {
  return request.post(`/student-cert/${id}/revoke`, { role, userId, reason })
}

export function verifyStudentCert(certNo, verifyCode) {
  return request.get('/student-cert/verify', { params: { certNo, verifyCode } })
}
