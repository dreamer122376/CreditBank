import { ref } from 'vue'
import { login as apiLogin, register as apiRegister } from '@/api/user'

const ROLE_NAME = {
  admin: '系统管理员',
  org_admin: '机构管理员',
  student: '学生',
  expert: '专家'
}

const currentUser = ref(null)

function loadUser() {
  const saved = localStorage.getItem('cb_user')
  if (saved) {
    currentUser.value = JSON.parse(saved)
  }
}

async function login(username, password) {
  try {
    const user = await apiLogin(username, password)
    currentUser.value = user
    localStorage.setItem('cb_user', JSON.stringify(user))
    return true
  } catch (error) {
    return false
  }
}

function logout() {
  currentUser.value = null
  localStorage.removeItem('cb_user')
}

async function register(form) {
  const data = {
    username: form.username,
    password: form.password,
    realName: form.realName,
    role: form.userType,
    orgId: form.institutionId ? Number(form.institutionId) : null,
    expertField: form.institutionName || ''
  }
  try {
    const user = await apiRegister(data)
    currentUser.value = user
    localStorage.setItem('cb_user', JSON.stringify(user))
    return true
  } catch (error) {
    return false
  }
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
