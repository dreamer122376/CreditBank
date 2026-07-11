<template>
  <div class="profile">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>基础资料</span>
          </template>
          <el-form :model="form" label-width="90px">
            <el-form-item label="登录账号">
              <el-input :model-value="user.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-input :model-value="ROLE_NAME[user.role] || user.role" disabled />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="form.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form :model="pwdForm" label-width="90px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password
                        placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirm" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="user.role === 'expert'" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span>我的评审资质</span>
          <el-button type="primary" size="small" @click="openApply">申请领域认证</el-button>
        </div>
      </template>

      <div v-if="certs.length > 0" class="badge-wall">
        <el-tag v-for="cert in certs" :key="cert.id" class="cert-badge" effect="plain">
          <el-icon><Medal /></el-icon>
          {{ cert.fieldName }}
        </el-tag>
      </div>
      <div v-else class="empty-tip">
        暂无评审资质。通过领域认证后，你才能被指派评审对应认证标准的申请。
      </div>

      <el-divider content-position="left">认证申请记录</el-divider>
      <el-table :data="myApplies" border style="width: 100%;">
        <el-table-column prop="id" label="申请单号" width="100" />
        <el-table-column label="申请内容">
          <template #default="scope">{{ applyContent(scope.row) }}</template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="130">
          <template #default="scope">
            <el-tag :type="scope.row.statusType">{{ scope.row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" />
      </el-table>
      <div v-if="myApplies.length === 0" style="text-align: center; padding: 24px; color: #868e96;">
        暂无申请记录
      </div>
    </el-card>

    <el-dialog v-model="applyVisible" title="申请领域认证" width="480px">
      <el-form :model="applyForm" label-width="90px">
        <el-form-item label="认证标准" required>
          <el-select v-model="applyForm.certStandardId" placeholder="选择要申请的认证标准"
                     style="width: 100%;">
            <el-option v-for="s in applicableStandards" :key="s.id"
                       :label="s.standardName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="领域名称">
          <el-input v-model="applyForm.fieldName" placeholder="默认使用认证标准名称" />
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="applyForm.reason" type="textarea" :rows="3"
                    placeholder="说明你的相关背景与资历" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getProfile, updateProfile, changePassword } from '@/api/profile'
import { getCertsByExpert } from '@/api/expertCert'
import { getCertStandards } from '@/api/certStandard'
import { getApplications, submitApplication } from '@/api/application'

const { currentUser, ROLE_NAME } = useAuth()

const user = ref({})
const form = ref({ realName: '', phone: '', email: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '', confirm: '' })
const certs = ref([])
const standards = ref([])
const myApplies = ref([])
const applyVisible = ref(false)
const applyForm = ref({ certStandardId: null, fieldName: '', reason: '' })

// 已持证的标准不再出现在申请下拉里
const applicableStandards = computed(() => {
  const held = new Set(certs.value.map(c => c.certStandardId))
  return standards.value.filter(s => s.isEnabled === 1 && !held.has(s.id))
})

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
    if (user.value.role === 'expert') {
      certs.value = await getCertsByExpert(id)
      standards.value = await getCertStandards()
      // role 传非预设值走"按申请人"过滤分支，查的是自己提交的申请
      const applies = await getApplications('self', id)
      myApplies.value = applies.filter(a => a.bizType === 'EXPERT_CERT')
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

function openApply() {
  applyForm.value = { certStandardId: null, fieldName: '', reason: '' }
  applyVisible.value = true
}

async function submitApply() {
  if (!applyForm.value.certStandardId) {
    ElMessage.warning('请选择认证标准')
    return
  }
  if (!applyForm.value.reason.trim()) {
    ElMessage.warning('请填写申请理由')
    return
  }
  try {
    await submitApplication({
      bizType: 'EXPERT_CERT',
      applicantId: currentUser.value.id,
      formData: JSON.stringify({
        certStandardId: applyForm.value.certStandardId,
        fieldName: applyForm.value.fieldName.trim(),
        reason: applyForm.value.reason.trim()
      })
    })
    ElMessage.success('申请已提交，等待管理员审核')
    applyVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function applyContent(row) {
  return row.bizTypeName || row.bizType
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.badge-wall {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cert-badge {
  color: #9a6700;
  border-color: #d4a72c;
  background: #fff8e1;
  font-weight: 600;
  padding: 0 12px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.empty-tip {
  color: #868e96;
  padding: 12px 0;
}
</style>
