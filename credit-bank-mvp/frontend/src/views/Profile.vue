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

    <!-- 专家：评审资质 -->
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
        <el-table-column prop="id" label="申请单号" width="90" />
        <el-table-column label="申请领域">
          <template #default="scope">{{ parseForm(scope.row).fieldName || '—' }}</template>
        </el-table-column>
        <el-table-column label="证明材料" min-width="220">
          <template #default="scope">
            <template v-if="attachmentsOf(scope.row).length">
              <div v-for="(att, i) in attachmentsOf(scope.row)" :key="i" class="att-row">
                <span class="att-name">{{ att.name }}</span>
                <el-link v-if="isPreviewable(att)" :href="previewUrl(att)" target="_blank"
                         type="primary">预览</el-link>
                <el-link :href="downloadUrl(att)" type="primary">下载</el-link>
              </div>
            </template>
            <span v-else class="empty-tip">未提供</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="120">
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

    <!-- 申请认证对话框 -->
    <el-dialog v-model="applyVisible" title="申请领域认证" width="560px">
      <el-form :model="applyForm" label-width="100px" label-position="left">
        <el-form-item label="认证标准" required>
          <el-select v-model="applyForm.certStandardId" placeholder="选择要申请的认证标准"
                     style="width: 100%;">
            <el-option v-for="s in applicableStandards" :key="s.id"
                       :label="s.standardName" :value="s.id" />
          </el-select>
          <div v-if="selectedStandard" class="standard-hint">
            {{ standardDesc(selectedStandard) }}
          </div>
        </el-form-item>
        <el-form-item label="领域名称">
          <el-input v-model="applyForm.fieldName" placeholder="默认使用认证标准名称" />
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="applyForm.reason" type="textarea" :rows="3"
                    placeholder="说明你的相关背景与资历" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            style="width: 100%;"
            action="/api/files/upload-attachment"
            name="file"
            :limit="5"
            :file-list="uploadList"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-remove="onUploadRemove"
            :on-error="onUploadError"
          >
            <el-button size="small">
              <el-icon><Paperclip /></el-icon>&nbsp;上传文件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word、图片，单个不超过 10MB，最多 5 个（论文、获奖证书等）</div>
            </template>
          </el-upload>
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
import { Medal, Paperclip } from '@element-plus/icons-vue'
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
const applyForm = ref({ certStandardId: null, fieldName: '', reason: '', attachments: [] })
const uploadList = ref([])

const ALLOWED_EXTS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.pdf', '.doc', '.docx']

// 已持证的标准不再出现在申请下拉里
const applicableStandards = computed(() => {
  const held = new Set(certs.value.map(c => c.certStandardId))
  return standards.value.filter(s => s.isEnabled === 1 && !held.has(s.id))
})

const selectedStandard = computed(() =>
  standards.value.find(s => s.id === applyForm.value.certStandardId))

function standardDesc(s) {
  return `${s.standardName}：要求学员累计积分满 ${s.minCredit} 分，` +
    (s.needExpertApprove === 1 ? '认证申请需持证专家评审签字，' : '认证申请无需专家评审，') +
    `证书有效期 ${s.validityDays} 天`
}

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
  applyForm.value = { certStandardId: null, fieldName: '', reason: '', attachments: [] }
  uploadList.value = []
  applyVisible.value = true
}

function beforeUpload(file) {
  const ext = file.name.includes('.')
    ? file.name.substring(file.name.lastIndexOf('.')).toLowerCase() : ''
  if (!ALLOWED_EXTS.includes(ext)) {
    ElMessage.warning('仅支持 PDF、Word、图片格式')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('单个文件不能超过 10MB')
    return false
  }
  return true
}

function onUploadSuccess(response, file) {
  if (response.code === 200) {
    // 用 uid 精确标识，避免同名文件误删
    applyForm.value.attachments.push({ uid: file.uid, name: file.name, url: response.data })
  } else {
    ElMessage.error(response.message || '上传失败')
    // 只移除失败文件，已上传成功的保持不变
    uploadList.value = uploadList.value.filter(f => f.uid !== file.uid)
  }
}

function onUploadRemove(file) {
  // 用 uid 精确删除：即使多个文件同名，也只删当前移除的那个
  applyForm.value.attachments = applyForm.value.attachments.filter(a => a.uid !== file.uid)
}

function onUploadError(err, file) {
  // 服务器端校验失败（如超10MB/格式不对）：只移除失败文件，保留已上传好的
  ElMessage.error(err?.message || '上传失败，请重试')
  uploadList.value = uploadList.value.filter(f => f.uid !== file.uid)
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
        reason: applyForm.value.reason.trim(),
        attachments: applyForm.value.attachments
      })
    })
    ElMessage.success('申请已提交，等待管理员审核')
    applyVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function parseForm(row) {
  try {
    return row.formData ? JSON.parse(row.formData) : {}
  } catch (e) {
    return {}
  }
}

function attachmentsOf(row) {
  const atts = parseForm(row).attachments
  return Array.isArray(atts) ? atts : []
}

function fileNameFromUrl(url) {
  return (url || '').split('?')[0].split('/').pop()
}

function isPreviewable(att) {
  return /\.(pdf|jpe?g|png|gif|webp)$/i.test(fileNameFromUrl(att.url))
}

function previewUrl(att) {
  return `/api/files/preview/${fileNameFromUrl(att.url)}?name=${encodeURIComponent(att.name)}`
}

function downloadUrl(att) {
  return `/api/files/download/${fileNameFromUrl(att.url)}?name=${encodeURIComponent(att.name)}`
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
  font-size: 13px;
}

.upload-tip {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}

.standard-hint {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 6px;
  background: #f8f9fa;
  border-radius: 4px;
  padding: 6px 10px;
  width: 100%;
}

.att-row {
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 1.8;
}

.att-name {
  color: #495057;
}
</style>
