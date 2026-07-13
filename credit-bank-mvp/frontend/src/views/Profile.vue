<template>
  <div class="profile">
    <!-- 头部信息横幅 -->
    <el-card class="hero-card">
      <div class="hero">
        <div class="hero-avatar">{{ (user.realName || '?').charAt(0) }}</div>
        <div class="hero-info">
          <div class="hero-name">
            {{ user.realName || '未命名' }}
            <el-tag size="small" class="role-tag">{{ ROLE_NAME[user.role] || user.role }}</el-tag>
          </div>
          <div class="hero-sub">账号 {{ user.username }}</div>
        </div>
        <div class="hero-right" v-if="user.role === 'student' || user.role === 'expert'">
          <div class="hero-balance">{{ formatNumber(user.balance) }}</div>
          <div class="hero-balance-label">当前积分</div>
        </div>
      </div>
    </el-card>

    <!-- 基础资料 / 修改密码，两卡等高对齐 -->
    <el-row :gutter="16" class="form-row">
      <el-col :span="12">
        <el-card class="full-card">
          <template #header>
            <span>基础资料</span>
          </template>
          <el-form :model="form" label-width="100px" label-position="left" class="tidy-form">
            <el-form-item label="登录账号">
              <el-input :model-value="user.username" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="form.realName" placeholder="真实姓名" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="邮箱" />
            </el-form-item>
          </el-form>
          <div class="card-actions">
            <el-button type="primary" @click="saveProfile">保存资料</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="full-card">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form :model="pwdForm" label-width="100px" label-position="left" class="tidy-form">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password
                        placeholder="当前使用的密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password
                        placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirm" type="password" show-password
                        placeholder="再输入一次新密码" />
            </el-form-item>
          </el-form>
          <div class="card-actions">
            <el-button type="primary" @click="savePassword">修改密码</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 专家：资质入口（管理已迁至"我的资质"页） -->
    <el-card v-if="user.role === 'expert'" class="cert-entry" style="margin-top: 16px;">
      <div class="entry-row">
        <span class="empty-tip">评审资质与领域认证申请已移至"我的资质"页面。</span>
        <el-button size="small" type="primary" plain @click="$router.push('/my-certs')">
          前往我的资质
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getProfile, updateProfile, changePassword } from '@/api/profile'

const { currentUser, ROLE_NAME } = useAuth()

const user = ref({})
const form = ref({ realName: '', phone: '', email: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '', confirm: '' })

onMounted(loadAll)

async function loadAll() {
  const id = currentUser.value?.id
  if (!id) return
  try {
    user.value = await getProfile(id)
    form.value = {
      realName: user.value.realName || '',
      phone: user.value.phone || '',
      email: user.value.email || ''
    }
  } catch (error) {
    ElMessage.error(error.message || '加载资料失败')
  }
}

async function saveProfile() {
  try {
    const updated = await updateProfile({
      userId: currentUser.value.id,
      realName: form.value.realName,
      phone: form.value.phone,
      email: form.value.email
    })
    user.value = updated
    // 同步本地登录态，顶栏姓名等随之更新
    localStorage.setItem('cb_user', JSON.stringify({ ...currentUser.value, ...updated }))
    currentUser.value = { ...currentUser.value, ...updated }
    ElMessage.success('资料已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function savePassword() {
  if (!pwdForm.value.newPassword || pwdForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await changePassword(currentUser.value.id, pwdForm.value.oldPassword, pwdForm.value.newPassword)
    ElMessage.success('密码已修改')
    pwdForm.value = { oldPassword: '', newPassword: '', confirm: '' }
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  }
}

function formatNumber(n) {
  return (n ?? 0).toLocaleString()
}
</script>

<style scoped>
.hero-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.hero {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #3b5bdb;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  flex-shrink: 0;
}

.hero-info {
  flex: 1;
}

.hero-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-sub {
  color: #868e96;
  font-size: 13px;
  margin-top: 4px;
}

.hero-right {
  text-align: right;
}

.hero-balance {
  font-size: 26px;
  font-weight: 700;
  color: #0b7a4f;
  font-variant-numeric: tabular-nums;
}

.hero-balance-label {
  font-size: 12px;
  color: #868e96;
}

.form-row {
  margin-top: 16px;
}

.full-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.full-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tidy-form {
  flex: 1;
}

.card-actions {
  padding-top: 8px;
  border-top: 1px solid #f1f3f5;
  text-align: right;
}

.entry-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.empty-tip {
  color: #868e96;
  font-size: 13px;
}
</style>
