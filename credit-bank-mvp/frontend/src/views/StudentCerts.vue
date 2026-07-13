<template>
  <div class="student-certs">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <span class="title">学生证书认证</span>
            <span class="subtitle">申请进度和已发证书统一在这里查看</span>
          </div>
          <el-button type="primary" size="small" @click="openApply">
            <el-icon><Plus /></el-icon>
            申请认证
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的申请" name="applications">
          <el-table :data="myApplies" border style="width: 100%;">
            <el-table-column prop="id" label="申请单号" width="100" />
            <el-table-column label="认证标准" min-width="180">
              <template #default="{ row }">{{ standardName(row) }}</template>
            </el-table-column>
            <el-table-column label="提交时间" width="170">
              <template #default="{ row }">{{ formatTime(row.appliedAt) }}</template>
            </el-table-column>
            <el-table-column label="当前环节" width="160">
              <template #default="{ row }">
                <span v-if="row.currentAuditorName">待 {{ row.currentAuditorName }}</span>
                <span v-else>{{ row.statusName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="审核进度" width="130">
              <template #default="{ row }">
                <span v-if="row.flowSteps?.length">{{ doneSteps(row) }}/{{ row.flowSteps.length }} 步</span>
                <span v-else class="muted">{{ row.currentStatus === 3 ? '自动通过' : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="statusName" label="状态" width="130">
              <template #default="{ row }">
                <el-tag :type="row.statusType">{{ row.statusName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="说明" min-width="180">
              <template #default="{ row }">
                <span v-if="row.rejectReason" class="reject-text">{{ row.rejectReason }}</span>
                <span v-else>{{ parseForm(row).reason || parseForm(row).applyReason || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openDetail(row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button
                  v-if="canResubmit(row)"
                  size="small"
                  type="warning"
                  plain
                  @click="openResubmit(row)"
                >
                  <el-icon><RefreshRight /></el-icon>
                  重新提交
                </el-button>
                <el-button
                  v-else-if="certForApply(row)"
                  size="small"
                  type="primary"
                  plain
                  @click="viewCert(certForApply(row))"
                >
                  <el-icon><Medal /></el-icon>
                  证书
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!myApplies.length" description="还没有认证申请" />
        </el-tab-pane>

        <el-tab-pane label="我的证书" name="certificates">
          <el-table :data="myCerts" border style="width: 100%;">
            <el-table-column prop="certNo" label="证书编号" width="190" />
            <el-table-column prop="certName" label="证书名称" min-width="180" />
            <el-table-column prop="orgName" label="发证机构" min-width="150" />
            <el-table-column label="颁发时间" width="170">
              <template #default="{ row }">{{ formatTime(row.issuedAt) }}</template>
            </el-table-column>
            <el-table-column label="有效期至" width="170">
              <template #default="{ row }">{{ formatTime(row.validUntil) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button size="small" type="primary" plain @click="viewCert(row)">
                  <el-icon><View /></el-icon>
                  查看证书
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!myCerts.length" description="暂无已发放证书" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="detailVisible" title="认证申请详情" width="680px">
      <template v-if="detailRow">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请单号">{{ detailRow.id }}</el-descriptions-item>
          <el-descriptions-item label="认证标准">{{ standardName(detailRow) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="detailRow.statusType">{{ detailRow.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请说明">
            {{ parseForm(detailRow).reason || parseForm(detailRow).applyReason || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="证明材料">
            <template v-if="attachmentsOf(detailRow).length">
              <div v-for="(att, index) in attachmentsOf(detailRow)" :key="index" class="att-row">
                <span class="att-name">{{ att.name }}</span>
                <el-link v-if="isPreviewable(att)" :href="previewUrl(att)" target="_blank" type="primary">预览</el-link>
                <el-link :href="downloadUrl(att)" type="primary">下载</el-link>
              </div>
            </template>
            <span v-else class="muted">未提供</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detailRow.rejectReason" label="驳回原因">
            <span class="reject-text">{{ detailRow.rejectReason }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <template v-if="detailRow.flowSteps?.length">
          <el-divider content-position="left">审核流程</el-divider>
          <el-steps align-center>
            <el-step
              v-for="step in detailRow.flowSteps"
              :key="step.nodeId"
              :title="`第 ${step.stepNo} 步`"
              :description="step.auditorName"
              :status="stepStatus(step)"
            />
          </el-steps>
        </template>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applyVisible" :title="editingApplication ? '重新提交认证申请' : '申请学生证书认证'" width="580px">
      <el-form :model="applyForm" label-width="100px" label-position="left">
        <el-form-item label="认证标准" required>
          <el-select
            v-model="applyForm.certStandardId"
            placeholder="选择要申请的认证标准"
            style="width: 100%;"
            @change="loadFlowPreview"
          >
            <el-option
              v-for="standard in applicableStandards"
              :key="standard.id"
              :label="standard.standardName"
              :value="standard.id"
            />
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
        <el-form-item label="申请说明">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="3"
            placeholder="说明你的学习成果或重新提交的补充说明"
          />
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Medal, Paperclip, Plus, RefreshRight, Right, View } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getApplications, resubmitApplication, submitApplication } from '@/api/application'
import { getCertStandards } from '@/api/certStandard'
import { getAuditFlow } from '@/api/auditFlow'
import { getStudentCerts } from '@/api/studentCert'

const router = useRouter()
const { currentUser } = useAuth()

const activeTab = ref('applications')
const standards = ref([])
const myApplies = ref([])
const myCerts = ref([])
const applyVisible = ref(false)
const detailVisible = ref(false)
const detailRow = ref(null)
const flowPreview = ref([])
const editingApplication = ref(null)
const applyForm = ref({ certStandardId: null, reason: '', attachments: [] })
const uploadList = ref([])
const ALLOWED_EXTS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.pdf', '.doc', '.docx']
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('cb_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const applicableStandards = computed(() => standards.value.filter(s => s.isEnabled === 1 && s.targetRole === 'student'))
const selectedStandard = computed(() => standards.value.find(s => s.id === applyForm.value.certStandardId))

onMounted(loadAll)

async function loadAll() {
  const user = currentUser.value
  if (!user?.id) return
  try {
    standards.value = await getCertStandards()
    const applications = await getApplications(user.role, user.id)
    myApplies.value = applications.filter(app => app.bizType === 'CERT_APPLY' && app.applicantId === user.id)
    myCerts.value = await getStudentCerts(user.id, user.role, user.id)
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function openApply() {
  editingApplication.value = null
  applyForm.value = { certStandardId: null, reason: '', attachments: [] }
  uploadList.value = []
  flowPreview.value = []
  applyVisible.value = true
}

async function openResubmit(row) {
  editingApplication.value = row
  const form = parseForm(row)
  applyForm.value = {
    certStandardId: form.certStandardId || form.standardId || null,
    reason: form.reason || form.applyReason || '',
    attachments: attachmentsOf(row).map((att, index) => ({ ...att, uid: att.uid || att.url || `${att.name}-${index}` }))
  }
  uploadList.value = applyForm.value.attachments.map(att => ({ name: att.name, url: att.url, uid: att.uid }))
  applyVisible.value = true
  await loadFlowPreview()
}

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function standardDesc(standard) {
  const auditText = standard.needManualAudit === 1
    ? `需人工审核${standard.flowStepCount ? `（${standard.flowStepCount} 步）` : ''}`
    : '自动通过，提交后立即发证'
  return standard.requirementText ? `${auditText}\n${standard.requirementText}` : auditText
}

async function loadFlowPreview() {
  flowPreview.value = []
  if (!selectedStandard.value || selectedStandard.value.needManualAudit !== 1) return
  try {
    flowPreview.value = await getAuditFlow(selectedStandard.value.id)
  } catch (_) {
    ElMessage.warning('审核流程预览加载失败')
  }
}

async function submitApply() {
  if (!applyForm.value.certStandardId) {
    ElMessage.warning('请选择认证标准')
    return
  }
  const payload = JSON.stringify({
    certStandardId: applyForm.value.certStandardId,
    standardName: selectedStandard.value?.standardName || '',
    reason: applyForm.value.reason.trim(),
    attachments: applyForm.value.attachments
  })
  try {
    if (editingApplication.value) {
      await resubmitApplication(editingApplication.value.id, currentUser.value.id, payload)
      ElMessage.success('申请已重新提交')
    } else {
      await submitApplication({
        bizType: 'CERT_APPLY',
        applicantId: currentUser.value.id,
        formData: payload
      })
      ElMessage.success(selectedStandard.value?.needManualAudit === 1 ? '申请已提交，等待审核' : '申请已通过并发证')
    }
    applyVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function parseForm(row) {
  try { return row?.formData ? JSON.parse(row.formData) : {} } catch (_) { return {} }
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

function attachmentsOf(row) {
  const attachments = parseForm(row).attachments
  return Array.isArray(attachments) ? attachments : []
}

function standardName(row) {
  const form = parseForm(row)
  const id = form.certStandardId || form.standardId
  return form.standardName || standards.value.find(s => s.id === id)?.standardName || (id ? `认证标准 #${id}` : '-')
}

function doneSteps(row) {
  return row.flowSteps?.filter(step => step.state === 'done').length || 0
}

function stepStatus(step) {
  const map = { done: 'success', current: 'process', rejected: 'error', pending: 'wait' }
  return map[step.state] || 'wait'
}

function canResubmit(row) {
  return row.currentStatus === 4
}

function viewCert(row) {
  router.push(`/student-certificate/${row.id}`)
}

function certForApply(app) {
  if (app.currentStatus !== 3) {
    return null
  }
  const standardId = parseForm(app).certStandardId || parseForm(app).standardId
  return myCerts.value.find(cert =>
    cert.applicationId === app.id ||
    (standardId && cert.certStandardId === standardId))
}

function formatTime(time) {
  return time ? String(time).replace('T', ' ') : '-'
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

.standard-hint {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
  background: #f8f9fa;
  border-radius: 4px;
  padding: 6px 10px;
  width: 100%;
  white-space: pre-line;
}

.flow-preview {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
}

.flow-node {
  background: #f1f3f5;
  border-radius: 4px;
  padding: 3px 10px;
  color: #495057;
}

.flow-arrow {
  color: #909399;
}

.muted {
  color: #adb5bd;
  font-size: 12px;
}

.reject-text {
  color: #c0392b;
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

.upload-tip {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}
</style>
