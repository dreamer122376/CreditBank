import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, testLogin as apiTestLogin } from '@/api/user'

const ROLE_NAME = {
  admin: '系统管理员',
  org_admin: '机构管理员',
  student: '学生',
  expert: '专家'
}

const currentUser = ref(null)
const token = ref(null)

function loadUser() {
  const savedUser = localStorage.getItem('cb_user')
  const savedToken = localStorage.getItem('cb_token')
  if (savedUser) {
    currentUser.value = JSON.parse(savedUser)
  }
  if (savedToken) {
    token.value = savedToken
  }
}

async function login(username, password) {
  const result = await apiLogin(username, password)
  currentUser.value = result.user
  token.value = result.token
  localStorage.setItem('cb_user', JSON.stringify(result.user))
  localStorage.setItem('cb_token', result.token)
}

function logout() {
  currentUser.value = null
  token.value = null
  localStorage.removeItem('cb_user')
  localStorage.removeItem('cb_token')
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
  const result = await apiRegister(data)
  currentUser.value = result.user
  token.value = result.token
  localStorage.setItem('cb_user', JSON.stringify(result.user))
  localStorage.setItem('cb_token', result.token)
}

// [TEST-ONLY] 测试用跳过密码登录
async function testLogin(username) {
  const result = await apiTestLogin(username)
  currentUser.value = result.user
  token.value = result.token
  localStorage.setItem('cb_user', JSON.stringify(result.user))
  localStorage.setItem('cb_token', result.token)
}

const isFrozen = computed(() => currentUser.value?.status === 0)

loadUser()

export function useAuth() {
  return {
    currentUser,
    token,
    isFrozen,
    ROLE_NAME,
    login,
    logout,
    register,
    testLogin
  }
}
