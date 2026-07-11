import request from './request'

// 管理端：分页列表
export function getCampaigns(page = 1, size = 10, status) {
  const params = { page, size }
  if (status !== null && status !== undefined && status !== '') params.status = status
  return request.get('/campaigns', { params })
}

// 学生端：进行中的活动
export function getActiveCampaigns() {
  return request.get('/campaigns/active')
}

// 活动详情
export function getCampaignDetail(id) {
  return request.get('/campaigns/' + id)
}

// 新增
export function createCampaign(data) {
  return request.post('/campaigns', data)
}

// 编辑
export function updateCampaign(id, data) {
  return request.put('/campaigns/' + id, data)
}

// 删除
export function deleteCampaign(id) {
  return request.delete('/campaigns/' + id)
}

// 上传图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
