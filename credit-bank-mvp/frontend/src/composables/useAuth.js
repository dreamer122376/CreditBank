import { ref } from 'vue'

const MOCK_USERS = [
  { username: 'admin', password: '123456', realName: '超级管理员', userType: 'SYSTEM_ADMIN', institutionId: null, institutionName: '' },
  { username: 'instadmin', password: '123456', realName: '李机构', userType: 'INSTITUTION_ADMIN', institutionId: 1, institutionName: '北京开放大学' },
  { username: 'student', password: '123456', realName: '张三', userType: 'STUDENT', institutionId: 1, institutionName: '北京开放大学' },
  { username: 'expert', password: '123456', realName: '王教授', userType: 'EXPERT', institutionId: null, institutionName: '清华大学' }
]

const ROLE_NAME = {
  SYSTEM_ADMIN: '系统管理员',
  INSTITUTION_ADMIN: '机构管理员',
  STUDENT: '学生',
  EXPERT: '专家'
}

const currentUser = ref(null)

function loadUser() {
  const saved = localStorage.getItem('cb_user')
  if (saved) {
    currentUser.value = JSON.parse(saved)
  }
}

function login(username, password) {
  const user = MOCK_USERS.find(u => u.username === username && u.password === password)
  if (user) {
    currentUser.value = { ...user }
    localStorage.setItem('cb_user', JSON.stringify(user))
    return true
  }
  return false
}

function logout() {
  currentUser.value = null
  localStorage.removeItem('cb_user')
}

function register(form) {
  const user = {
    username: form.username,
    password: form.password,
    realName: form.realName,
    userType: form.userType,
    institutionId: form.institutionId ? Number(form.institutionId) : null,
    institutionName: form.institutionName || ''
  }
  currentUser.value = user
  localStorage.setItem('cb_user', JSON.stringify(user))
  return user
}

loadUser()

export function useAuth() {
  return {
    currentUser,
    ROLE_NAME,
    login,
    logout,
    register
  }
}
