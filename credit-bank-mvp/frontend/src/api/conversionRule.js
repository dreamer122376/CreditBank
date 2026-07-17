/**
 * conversionRule.js - 转换规则 API（额外注释）
 * 功能：转换规则的 CRUD + 启用/停用切换。
 */
import request from './request'

// 获取转换规则列表；enabledOnly=true 时只返回已启用的
export function getConversionRules(enabledOnly = false) {
  const params = enabledOnly ? { enabled: true } : {}
  return request.get('/conversion-rule/list', { params })
}

// 获取单条规则详情
export function getConversionRuleDetail(id) {
  return request.get('/conversion-rule/' + id)
}

// 创建转换规则
export function createConversionRule(data) {
  return request.post('/conversion-rule/create', data)
}

// 更新转换规则
export function updateConversionRule(data) {
  return request.post('/conversion-rule/update', data)
}

// 删除转换规则
export function deleteConversionRule(id) {
  return request.delete('/conversion-rule/' + id)
}

// 切换规则启用/停用状态
export function toggleConversionRule(id, isEnabled) {
  return request.post('/conversion-rule/' + id + '/toggle', { isEnabled })
}