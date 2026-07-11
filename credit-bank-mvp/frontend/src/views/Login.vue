<template>
  <div class="login-wrap">
    <div class="login-box">
      <div class="logo">
        <h1>终身学习学分银行</h1>
        <p>Credit Bank System</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" @keyup.enter="handleLogin" />
            </el-form-item>
            <el-button type="primary" size="large" class="btn-block" @click="handleLogin" :loading="loading">登 录</el-button>
            <p class="login-msg" v-if="loginMsg">{{ loginMsg }}</p>
            <div class="login-tip">
              <b>演示账号（密码均为 123456）：</b><br>
              admin · 系统管理员　　instadmin · 机构管理员<br>
              student · 学生　　　　expert · 专家
            </div>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请设置登录账号" size="large" />
            </el-form-item>
            <el-form-item prop="realName">
              <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请设置密码" size="large" />
            </el-form-item>
            <el-form-item prop="userType">
              <el-select v-model="registerForm.userType" placeholder="请选择角色类型" size="large">
                <el-option label="学生" value="student" />
                <el-option label="机构管理员" value="org_admin" />
                <el-option label="专家" value="expert" />
                <el-option label="系统管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="showInstId">
              <el-input v-model="registerForm.institutionId" type="number" placeholder="所属机构ID" size="large" />
            </el-form-item>
            <el-form-item v-if="showInstName">
              <el-input v-model="registerForm.institutionName" placeholder="所属机构名称" size="large" />
            </el-form-item>
            <el-button type="success" size="large" class="btn-block" @click="handleRegister">注 册</el-button>
            <p class="login-msg" v-if="registerMsg">{{ registerMsg }}</p>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { login, register } = useAuth()

const activeTab = ref('login')
const loading = ref(false)
const loginMsg = ref('')
const registerMsg = ref('')

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  realName: '',
  password: '',
  userType: 'student',
  institutionId: '',
  institutionName: ''
})

const showInstId = computed(() => {
  return registerForm.value.userType === 'student' || registerForm.value.userType === 'org_admin'
})

const showInstName = computed(() => {
  return registerForm.value.userType === 'expert'
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

async function handleRegister() {
  const { username, password, realName } = registerForm.value
  if (!username || !password || !realName) {
    registerMsg.value = '请填写完整信息'
    return
  }
  registerMsg.value = ''
  try {
    const success = await register(registerForm.value)
    if (success) {
      router.push('/dashboard')
    } else {
      registerMsg.value = '注册失败，请稍后重试'
    }
  } catch (error) {
    registerMsg.value = '注册失败，请稍后重试'
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%);
}

.login-box {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.logo {
  text-align: center;
  margin-bottom: 24px;
}

.logo h1 {
  font-size: 22px;
  color: #3b5bdb;
  margin-bottom: 6px;
}

.logo p {
  font-size: 13px;
  color: #868e96;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-form {
  width: 100%;
}

.btn-block {
  width: 100%;
}

.login-msg {
  min-height: 20px;
  margin-top: 8px;
  font-size: 13px;
  color: #e03131;
  text-align: center;
}

.login-tip {
  margin-top: 16px;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 12px;
  color: #868e96;
  line-height: 1.7;
}

.login-tip b {
  color: #495057;
}
</style>
