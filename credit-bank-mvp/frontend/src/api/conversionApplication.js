/**
 * conversionApplication.js - 转换申请 API（额外注释）
 * 功能：转换申请提交、审核、列表查询。
 */
import request from './request'

// 获取转换申请列表，可按状态筛选
export function getConversionApplications(status) {
  const params = status !== undefined ? { status } : {}
  return request.get('/conversion-application/list', { params })
}

// 获取单条申请详情
export function getConversionApplicationDetail(id) {
  return request.get('/conversion-application/' + id)
}

// 提交转换申请
export function submitConversionApplication(data) {
  return request.post('/conversion-application/submit', data)
}

// 审核转换申请（通过/驳回）
export function auditConversionApplication(id, approve, reason) {
  return request.post('/conversion-application/' + id + '/audit', { approve, reason })
}