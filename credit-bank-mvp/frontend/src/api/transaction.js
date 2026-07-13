import request from './request'
import axios from 'axios'

export function getTransactions(page = 1, size = 10, filters = {}) {
  const params = { page, size }
  if (filters.userId != null) params.userId = filters.userId
  if (filters.bizType != null && filters.bizType !== '') params.bizType = filters.bizType
  if (filters.startTime != null && filters.startTime !== '') params.startTime = filters.startTime
  if (filters.endTime != null && filters.endTime !== '') params.endTime = filters.endTime
  return request.get('/transactions', { params })
}

export function getTransactionDetail(id) {
  return request.get('/transactions/' + id)
}

export async function exportTransactions(filters = {}) {
  const token = localStorage.getItem('cb_token')
  
  const params = new URLSearchParams()
  if (filters.userId != null) params.set('userId', filters.userId)
  if (filters.bizType != null && filters.bizType !== '') params.set('bizType', filters.bizType)
  if (filters.startTime != null && filters.startTime !== '') params.set('startTime', filters.startTime)
  if (filters.endTime != null && filters.endTime !== '') params.set('endTime', filters.endTime)
  
  const headers = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  const response = await axios.get('/api/transactions/export', {
    params: params,
    responseType: 'blob',
    headers: headers
  })
  
  const contentDisposition = response.headers['content-disposition']
  let filename = 'transactions.csv'
  if (contentDisposition) {
    const match = contentDisposition.match(/filename="(.+)"/)
    if (match && match[1]) {
      filename = match[1]
    }
  }
  
  const blob = new Blob([response.data], { type: 'text/csv;charset=UTF-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}