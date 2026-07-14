<template>
  <div class="login-wrap">
    <!-- 背景装饰 -->
    <div class="bg-glyph" aria-hidden="true">学</div>

    <div class="login-card">
      <!-- 金色顶饰 -->
      <div class="card-crown" aria-hidden="true"></div>

      <!-- 品牌区 -->
      <div class="brand">
        <div class="brand-emblem">
          <span class="emblem-char">学</span>
        </div>
        <h1 class="brand-title">终身学习学分银行</h1>
        <p class="brand-sub">Credit Bank System</p>
      </div>

      <!-- [TEST-ONLY] 按 G 键呼出测试切换用户区域 -->
      <div class="test-login-area" v-show="showTestArea">
        <div class="test-login-header">
          <el-icon class="test-icon"><Switch /></el-icon>
          <span>测试切换用户</span>
          <el-tag size="small" type="warning" effect="dark" class="test-hint">按 G 隐藏</el-tag>
          <el-icon v-if="testUsersLoading" class="test-icon"><Loading /></el-icon>
        </div>
        <div class="test-users-list">
          <el-button
            v-for="user in testUsers"
            :key="user.id"
            size="small"
            class="test-user-btn"
            @click="handleTestLoginByUser(user.username)"
            :loading="loading"
          >
            {{ user.username }}
            <span class="test-user-role">{{ getRoleName(user.role) }}</span>
          </el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" class="login-form">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" @keyup.enter="handleLogin" />
            </el-form-item>
            <el-button type="primary" size="large" class="btn-block" @click="handleLogin" :loading="loading">登 录</el-button>
            <p class="login-msg" v-if="loginMsg">{{ loginMsg }}</p>
            <div class="login-extra-link">
              <el-button link type="primary" size="small" @click="goOrgRegister">机构入驻申请</el-button>
            </div>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" class="login-form">
            <el-form-item>
              <el-input v-model="registerForm.username" placeholder="登录账号" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.realName" placeholder="真实姓名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" type="password" placeholder="设置密码" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" />
            </el-form-item>
            <el-form-item>
              <el-select v-model="registerForm.userType" placeholder="角色类型" size="large" @change="onUserTypeChange">
                <el-option label="学生" value="student" />
                <el-option label="机构管理员" value="org_admin" />
                <el-option label="专家" value="expert" />
                <el-option label="系统管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="showInstId">
              <el-select v-model="registerForm.institutionId" placeholder="请选择所属机构" size="large">
                <el-option
                  v-for="org in orgList"
                  :key="org.id"
                  :label="org.name"
                  :value="org.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="showInstName">
              <el-input v-model="registerForm.institutionName" placeholder="专家领域" size="large" />
            </el-form-item>
            <el-button type="primary" size="large" class="btn-block" @click="handleRegister" :loading="loading">注 册</el-button>
            <p class="login-msg" v-if="registerMsg">{{ registerMsg }}</p>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { Switch, Loading } from '@element-plus/icons-vue'
import { getTestUsers } from '@/api/user'
import { getOrganizations } from '@/api/organization'

const router = useRouter()
const { login, register, testLogin } = useAuth()

const activeTab = ref('login')
const loading = ref(false)
const loginMsg = ref('')
const registerMsg = ref('')

const loginForm = ref({
  username: '',
  password: ''
})

// [TEST-ONLY] 测试用户列表
const testUsers = ref([])
const testUsersLoading = ref(false)
const showTestArea = ref(false)
const testLoaded = ref(false)

const registerForm = ref({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  userType: 'student',
  institutionId: '',
  institutionName: ''
})

const orgList = ref([])
const orgListLoaded = ref(false)

const showInstId = computed(() => {
  return registerForm.value.userType === 'student' || registerForm.value.userType === 'org_admin' || registerForm.value.userType === 'expert'
})

const showInstName = computed(() => {
  return registerForm.value.userType === 'expert'
})

// [TEST-ONLY] 获取角色中文名称
function getRoleName(role) {
  const names = {
    admin: '系统管理员',
    org_admin: '机构管理员',
    student: '学生',
    expert: '专家'
  }
  return names[role] || role
}

// [TEST-ONLY] 加载测试用户列表（首次显示时加载）
async function loadTestUsers() {
  if (testLoaded.value) return
  testLoaded.value = true
  testUsersLoading.value = true
  try {
    testUsers.value = await getTestUsers()
  } catch (error) {
    console.error('加载测试用户列表失败:', error)
  } finally {
    testUsersLoading.value = false
  }
}

// [TEST-ONLY] 按 G 键切换显示测试区域
function onKeydown(e) {
  if (e.key === 'g' || e.key === 'G') {
    showTestArea.value = !showTestArea.value
    if (showTestArea.value) {
      loadTestUsers()
    }
  }
}

// [TEST-ONLY]
onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

watch(activeTab, (newTab) => {
  if (newTab === 'register' && !orgListLoaded.value) {
    loadOrgList()
  }
})

// [TEST-ONLY]
onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
})

async function handleLogin() {
  const { username, password } = loginForm.value
  if (!username || !password) {
    loginMsg.value = '请输入账号和密码'
    return
  }
  loading.value = true
  loginMsg.value = ''
  try {
    await login(username, password)
    loading.value = false
    router.push('/dashboard')
  } catch (error) {
    loading.value = false
    loginMsg.value = error.message || '登录失败，请稍后重试'
  }
}

// [TEST-ONLY]
async function handleTestLoginByUser(username) {
  loading.value = true
  loginMsg.value = ''
  try {
    await testLogin(username)
    loading.value = false
    router.push('/dashboard')
  } catch (error) {
    loading.value = false
    loginMsg.value = error.message || '切换失败，请稍后重试'
  }
}

async function loadOrgList() {
  if (orgListLoaded.value) return
  try {
    orgList.value = await getOrganizations()
    orgListLoaded.value = true
  } catch (error) {
    console.error('加载机构列表失败:', error)
  }
}

function onUserTypeChange() {
  registerForm.value.institutionId = ''
  registerForm.value.institutionName = ''
}

async function handleRegister() {
  const { username, password, confirmPassword, realName, userType } = registerForm.value
  if (!username || !password || !realName) {
    registerMsg.value = '请填写完整信息'
    return
  }
  if (password !== confirmPassword) {
    registerMsg.value = '两次输入的密码不一致'
    return
  }
  if ((userType === 'student' || userType === 'org_admin' || userType === 'expert') && !registerForm.value.institutionId) {
    registerMsg.value = '请选择所属机构'
    return
  }
  registerMsg.value = ''
  try {
    await register(registerForm.value)
    router.push('/dashboard')
  } catch (error) {
    registerMsg.value = error.message || '注册失败，请稍后重试'
  }
}

function goOrgRegister() {
  router.push('/org-register')
}
</script>

<style scoped>
/* ===== 设计系统 ===== */
:root {
  --ink: #0F1B2D;
  --gold: #C8914A;
  --gold-light: #DFB66A;
  --gold-glow: rgba(200, 145, 74, 0.35);
  --frost: #F8F7F4;
  --charcoal: #1A202C;
  --slate: #4A5568;
  --muted: #8C8CA0;
  --border-light: #E8E5DF;
}

/* ===== 全局 ===== */
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--ink);
  position: relative;
  overflow: hidden;
}

/* ===== 背景书法水印 ===== */
.bg-glyph {
  position: absolute;
  font-family: 'Noto Serif SC', 'SimSun', serif;
  font-size: clamp(280px, 50vw, 600px);
  font-weight: 700;
  color: rgba(255, 255, 255, 0.035);
  line-height: 1;
  pointer-events: none;
  user-select: none;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-8deg);
  letter-spacing: -0.08em;
}

/* ===== 卡片 ===== */
.login-card {
  position: relative;
  width: 420px;
  max-width: 92vw;
  background: var(--frost);
  border-radius: 16px;
  padding: 40px 36px 32px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.06);
  z-index: 1;
  animation: cardIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 金色顶饰 */
.card-crown {
  position: absolute;
  top: 0;
  left: 40px;
  right: 40px;
  height: 4px;
  background: linear-gradient(90deg, transparent 0%, var(--gold) 20%, var(--gold-light) 50%, var(--gold) 80%, transparent 100%);
  border-radius: 0 0 3px 3px;
}

/* ===== 品牌区 ===== */
.brand {
  text-align: center;
  margin-bottom: 28px;
}

.brand-emblem {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: var(--ink);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(15, 27, 45, 0.25);
}

.emblem-char {
  font-family: 'Noto Serif SC', 'SimSun', serif;
  font-size: 26px;
  font-weight: 700;
  color: var(--gold);
  line-height: 1;
}

.brand-title {
  font-family: 'Noto Serif SC', 'SimSun', serif;
  font-size: 22px;
  font-weight: 700;
  color: var(--charcoal);
  margin: 0 0 6px;
  letter-spacing: 2px;
}

.brand-sub {
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 13px;
  font-weight: 400;
  color: var(--muted);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin: 0;
}

/* ===== 测试区域 ===== */
.test-login-area {
  margin-bottom: 20px;
  padding: 14px;
  background: rgba(200, 145, 74, 0.08);
  border: 1px solid rgba(200, 145, 74, 0.2);
  border-radius: 10px;
}

.test-login-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #856404;
}

.test-icon {
  font-size: 15px;
}

.test-hint {
  margin-left: auto;
  font-size: 11px;
}

.test-users-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.test-user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  font-size: 12px;
  background: rgba(200, 145, 74, 0.12) !important;
  border-color: rgba(200, 145, 74, 0.25) !important;
  color: #7A5A20 !important;
  border-radius: 6px;
  transition: all 0.2s;
}

.test-user-btn:hover {
  background: rgba(200, 145, 74, 0.25) !important;
  border-color: rgba(200, 145, 74, 0.4) !important;
}

.test-user-role {
  font-size: 10px;
  opacity: 0.7;
}

/* ===== Tabs ===== */
.login-tabs {
  margin-bottom: 0;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.login-tabs :deep(.el-tabs__item) {
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: var(--muted);
  height: 40px;
  line-height: 40px;
  padding: 0 24px;
  letter-spacing: 1px;
  transition: color 0.2s;
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: var(--charcoal);
  font-weight: 600;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background: var(--gold);
  height: 2px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  background: var(--border-light);
  height: 1px;
}

/* ===== 表单 ===== */
.login-form {
  width: 100%;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  background: #FFFFFF;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  box-shadow: none;
  padding: 4px 14px;
  transition: border-color 0.25s, box-shadow 0.25s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--gold);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--gold);
  box-shadow: 0 0 0 3px var(--gold-glow);
}

.login-form :deep(.el-input__inner) {
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 14px;
  color: var(--charcoal);
  height: 44px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: var(--muted);
  font-weight: 400;
}

/* Select */
.login-form :deep(.el-select) {
  width: 100%;
}

.login-form :deep(.el-select .el-input__wrapper) {
  height: 44px;
}

/* ===== 按钮 ===== */
.btn-block {
  width: 100%;
  height: 46px;
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 10px;
  border: none;
  color: #FFFFFF !important;
  transition: transform 0.2s, box-shadow 0.2s;
  background: linear-gradient(135deg, #B8860B 0%, #8B6508 100%) !important;
  box-shadow: 0 4px 14px rgba(139, 101, 8, 0.45) !important;
}

.btn-block:hover {
  background: linear-gradient(135deg, #D4A017 0%, #B8860B 100%) !important;
  box-shadow: 0 6px 20px rgba(139, 101, 8, 0.55) !important;
  transform: translateY(-1px);
}

.btn-block:active {
  transform: scale(0.98);
}

/* ===== 消息 ===== */
.login-msg {
  min-height: 20px;
  margin-top: 10px;
  font-size: 13px;
  color: #E53E3E;
  text-align: center;
  font-weight: 500;
}

/* ===== 响应式 ===== */
@media (max-width: 480px) {
  .login-card {
    padding: 28px 20px 24px;
  }

  .brand-title {
    font-size: 18px;
  }

  .brand-sub {
    font-size: 11px;
  }

  .card-crown {
    left: 20px;
    right: 20px;
  }
}

/* ===== 动效偏好 ===== */
@media (prefers-reduced-motion: reduce) {
  .login-card {
    animation: none;
  }

  .btn-block {
    transition: none;
  }

  .btn-block:active {
    transform: none;
  }
}

.login-extra-link {
  text-align: center;
  margin-top: 12px;
}
</style>
