/**
 * signin.js - 签到 API（额外注释）
 * 功能：每日签到打卡、查询当日签到状态、获取签到历史。
 */
import request from './request'

// 执行签到
export function signIn() {
  return request.post('/sign-in')
}

// 获取当天签到状态（返回值：{ hasSignedIn, streak }）
export function getSignInStatus() {
  return request.get('/sign-in/status')
}

// 获取签到历史记录，默认近 30 天
export function getSignInHistory(limit = 30) {
  return request.get('/sign-in/history', { params: { limit } })
}