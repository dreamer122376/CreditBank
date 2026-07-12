import request from './request'

/** 查询某认证标准的完整审批链 */
export function getAuditFlow(certStandardId) {
  return request.get(`/cert-audit-flow/list/${certStandardId}`)
}

/** 保存（整体替换）某标准的审批流程 */
export function saveAuditFlow(certStandardId, nodes) {
  return request.post(`/cert-audit-flow/save/${certStandardId}`, nodes)
}

/** 删除某标准的整个审批流程 */
export function deleteAuditFlow(certStandardId) {
  return request.delete(`/cert-audit-flow/${certStandardId}`)
}
