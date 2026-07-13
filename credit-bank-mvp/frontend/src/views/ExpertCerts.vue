<template>
  <div class="expert-certs">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <span class="title">我的评审资质</span>
            <span class="subtitle">通过领域认证后，可参与对应标准的证书审核</span>
          </div>
          <el-button type="primary" size="small" @click="openApply">
            <el-icon><Plus /></el-icon>
            申请领域认证
          </el-button>
        </div>
      </template>

      <div v-if="certs.length > 0" class="badge-wall">
        <el-tag v-for="cert in certs" :key="cert.id" class="cert-badge" effect="plain">
          <el-icon><Medal /></el-icon>
          {{ cert.fieldName }}
        </el-tag>
      </div>
      <el-empty v-else description="暂无评审资质" />
    </el-card>

    <el-card style="margin-top: 16px;">
      <template #header>
        <span class="title">认证申请记录</span>
      </template>
      <el-table :data="myApplies" border style="width: 100%;">
        <el-table-column prop="id" label="申请单号" width="100" />
        <el-table-column label="申请领域" min-width="160">
          <template #default="{ row }">{{ certTitle(row) }}</template>
        </el-table-column>
        <el-table-column label="证明材料" min-width="220">
          <template #default="{ row }">
            <template v-if="attachmentsOf(row).length">
              <div v-for="(att, index) in attachmentsOf(row)" :key="index" class="att-row">
                <span class="att-name">{{ att.name }}</span>
                <el-link v-if="isPreviewable(att)" :href="previewUrl(att)" target="_blank" type="primary">预览</el-link>
                <el-link :href="downloadUrl(att)" type="primary">下载</el-link>
              </div>
            </template>
            <span v-else class="muted">未提供</span>
          </template>
        </el-table-column>
        <el-table-column label="审核进度" width="120">
          <template #default="{ row }">
            <span v-if="row.flowSteps?.length">{{ doneSteps(row) }}/{{ row.flowSteps.length }} 步</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="180">
          <template #default="{ row }">
            <span v-if="row.rejectReason" class="reject-text">{{ row.rejectReason }}</span>
            <span v-else>{{ parseForm(row).reason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canResubmit(row)" size="small" type="warning" plain @click="openResubmit(row)">
              <el-icon><RefreshRight /></el-icon>
              重新提交
            </el-button>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="myApplies.length === 0" description="暂无申请记录" />
    </el-card>

    <el-dialog v-model="applyVisible" :title="editingApplication ? '重新提交领域认证' : '申请领域认证'" width="620px">
      <el-form :model="applyForm" label-width="100px" label-position="left">
        <el-form-item label="认证标准" required>
          <el-select
            v-model="applyForm.certStandardId"
            placeholder="选择要申请的认证标准"
            style="width: 100%;"
            @change="loadFlowPreview"
          >
            <el-option v-for="standard in applicableStandards" :key="standard.id" :label="standard.standardName" :value="standard.id" />
          </el-select>
          <div v-if="selectedStandard" class="standard-hint">{{ standardDesc(selectedStandard) }}</div>
        </el-form-item>
        <el-form-item v-if="flowPreview.length" label="审核流程">
          <div class="flow-preview">
            <template v-for="(node, index) in flowPreview" :key="node.id">
              <span class="flow-node">第 {{ index + 1 }} 步 {{ node.auditorName || `用户#${node.auditorId}` }}</span>
              <el-icon v-if="index < flowPreview.length - 1" class="flow-arrow"><Right /></el-icon>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="领域名称">
          <el-input v-model="applyForm.fieldName" placeholder="默认使用认证标准名称" />
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="说明你的相关背景、成果或补充说明" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            style="width: 100%;"
            action="/api/files/upload-attachment"
            name="file"
            :headers="uploadHeaders"
            :limit="5"
            :file-list="uploadList"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-remove="onUploadRemove"
            :on-error="onUploadError"
          >
            <el-button size="small">
              <el-icon><Paperclip /></el-icon>
              上传文件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word、图片，单个不超过 10MB，最多 5 个</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">
          {{ editingApplication ? '重新提交' : '提交申请' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, Paperclip, Plus, RefreshRight, Right } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getCertsByExpert } from '@/api/expertCert'
import { getCertStandards } from '@/api/certStandard'
import { getAuditFlow } from '@/api/auditFlow'
import { getApplications, resubmitApplication, submitApplication } from '@/api/application'

const { currentUser } = useAuth()

const certs = ref([])
const standards = ref([])
const myApplies = ref([])
const applyVisible = ref(false)
const editingApplication = ref(null)
const applyForm = ref({ certStandardId: null, fieldName: '', reason: '', attachments: [] })
const uploadList = ref([])
const flowPreview = ref([])

const ALLOWED_EXTS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.pdf', '.doc', '.docx']
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('cb_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const applicableStandards = computed(() => {
  const held = new Set(certs.value.map(cert => cert.certStandardId))
  return standards.value.filter(standard => {
    const selected = standard.id === applyForm.value.certStandardId
    return standard.isEnabled === 1 && standard.targetRole === 'expert' && (selected || !held.has(standard.id))
  })
})

const selectedStandard = computed(() =>
  standards.value.find(standard => standard.id === applyForm.value.certStandardId))

onMounted(loadAll)

async function loadAll() {
  const id = currentUser.value?.id
  if (!id) return
  try {
    certs.value = await getCertsByExpert(id)
    standards.value = await getCertStandards()
    const applies = await getApplications(currentUser.value?.role, id)
    myApplies.value = applies.filter(app => app.bizType === 'EXPERT_CERT' && app.applicantId === id)
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function standardDesc(standard) {
  const audit = standard.needManualAudit === 1
    ? `需人工审核${standard.flowStepCount ? `（${standard.flowStepCount} 级流程）` : ''}`
    : '达标自动通过'
  const header = `${standard.standardName} v${standard.version || '1.0'} · ${audit}`
  return standard.requirementText ? `${header}\n${standard.requirementText}` : header
}

async function loadFlowPreview() {
  flowPreview.value = []
  const standard = selectedStandard.value
  if (!standard || standard.needManualAudit !== 1) return
  try {
    flowPreview.value = await getAuditFlow(standard.id)
  } catch (_) {
    flowPreview.value = []
  }
}

function openApply() {
  editingApplication.value = null
  applyForm.value = { certStandardId: null, fieldName: '', reason: '', attachments: [] }
  uploadList.value = []
  flowPreview.value = []
  applyVisible.value = true
}

async function openResubmit(row) {
  editingApplication.value = row
  const form = parseForm(row)
  const attachments = Array.isArray(form.attachments) ? form.attachments : []
  applyForm.value = {
    certStandardId: form.certStandardId || form.standardId || null,
    fieldName: form.fieldName || '',
    reason: form.reason || '',
    attachments: attachments.map((att, index) => ({ ...att, uid: att.uid || att.url || `${att.name}-${index}` }))
  }
  uploadList.value = applyForm.value.attachments.map(att => ({ name: att.name, url: att.url, uid: att.uid }))
  applyVisible.value = true
  await loadFlowPreview()
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
    applyForm.value.attachments.push({ uid: file.uid, name: file.name, url: response.data })
  } else {
    ElMessage.error(response.message || '上传失败')
    uploadList.value = uploadList.value.filter(item => item.uid !== file.uid)
  }
}

function onUploadRemove(file) {
  applyForm.value.attachments = applyForm.value.attachments.filter(att => att.uid !== file.uid)
}

function onUploadError(err, file) {
  ElMessage.error(err?.message || '上传失败，请重试')
  uploadList.value = uploadList.value.filter(item => item.uid !== file.uid)
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
  const payload = JSON.stringify({
    certStandardId: applyForm.value.certStandardId,
    fieldName: applyForm.value.fieldName.trim(),
    reason: applyForm.value.reason.trim(),
    attachments: applyForm.value.attachments
  })
  try {
    if (editingApplication.value) {
      await resubmitApplication(editingApplication.value.id, currentUser.value.id, payload)
      ElMessage.success('申请已重新提交')
    } else {
      await submitApplication({
        bizType: 'EXPERT_CERT',
        applicantId: currentUser.value.id,
        formData: payload
      })
      ElMessage.success('申请已提交')
    }
    applyVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function parseForm(row) {
  try {
    return row?.formData ? JSON.parse(row.formData) : {}
  } catch (_) {
    return {}
  }
}

function certTitle(row) {
  const form = parseForm(row)
  const id = form.certStandardId || form.standardId
  return form.fieldName || form.standardName || standards.value.find(standard => standard.id === id)?.standardName || (id ? `认证标准 #${id}` : '-')
}

function attachmentsOf(row) {
  const attachments = parseForm(row).attachments
  return Array.isArray(attachments) ? attachments : []
}

function doneSteps(row) {
  return row.flowSteps?.filter(step => step.state === 'done').length || 0
}

function canResubmit(row) {
  return row.currentStatus === 4
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
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.title {
  font-weight: 600;
  color: #2c3e50;
}

.subtitle {
  margin-left: 10px;
  color: #868e96;
  font-size: 13px;
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

.standard-hint {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 6px;
  background: #f8f9fa;
  border-radius: 4px;
  padding: 6px 10px;
  width: 100%;
  white-space: pre-line;
  max-height: 140px;
  overflow-y: auto;
}

.flow-preview {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
  color: #495057;
}

.flow-node {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f1f3f5;
  border-radius: 4px;
  padding: 3px 10px;
}

.flow-arrow {
  color: #adb5bd;
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

.muted {
  color: #adb5bd;
  font-size: 12px;
}

.reject-text {
  color: #c0392b;
}

.upload-tip {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}
</style>
