<template>
  <div class="cert-applications">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>证书申请审核</span>
          <el-tag type="info" effect="plain">学生认证 / 专家认证</el-tag>
        </div>
      </template>

      <el-table :data="apps" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bizTypeName" label="申请类型" width="140" />
        <el-table-column label="认证标准" min-width="180">
          <template #default="{ row }">{{ certTitle(row) }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="110" />
        <el-table-column prop="orgName" label="所属机构" min-width="140" />
        <el-table-column prop="appliedAt" label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.appliedAt) }}</template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核进度" width="120">
          <template #default="{ row }">
            <span v-if="row.flowSteps?.length">{{ doneSteps(row) }}/{{ row.flowSteps.length }} 步</span>
            <span v-else class="muted">自动通过型</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <template v-if="row.canAudit">
              <el-button size="small" type="success" @click="audit(row, true)">通过</el-button>
              <el-button size="small" type="danger" @click="openReject(row)">驳回</el-button>
            </template>
            <el-button v-if="row.bizType === 'CERT_APPLY' && row.currentStatus === 3" size="small" type="primary" plain @click="openCertificate(row)">
              证书
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!apps.length" description="暂无证书申请" />
    </el-card>

    <el-dialog v-model="detailVisible" title="证书申请详情" width="760px">
      <template v-if="detailRow">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请类型">{{ detailRow.bizTypeName }}</el-descriptions-item>
          <el-descriptions-item label="认证标准">{{ certTitle(detailRow) }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detailRow.applicantName }}</el-descriptions-item>
          <el-descriptions-item v-if="detailRow.orgName" label="所属机构">{{ detailRow.orgName }}</el-descriptions-item>
          <el-descriptions-item v-if="detailForm.fieldName" label="申请领域">{{ detailForm.fieldName }}</el-descriptions-item>
          <el-descriptions-item v-if="detailForm.reason || detailForm.applyReason" label="申请说明">
            {{ detailForm.reason || detailForm.applyReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="standardDetail?.requirementText" label="认证标准要求">
            <div class="requirement-text">{{ standardDetail.requirementText }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="detailRow.statusType">{{ detailRow.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="detailRow.currentAuditorName" label="当前审核人">
            {{ detailRow.currentAuditorName }}
          </el-descriptions-item>
          <el-descriptions-item label="证明材料">
            <template v-if="detailAttachments.length">
              <div v-for="(att, index) in detailAttachments" :key="index" class="att-row">
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
        <template v-if="detailRow?.canAudit">
          <el-button type="success" @click="audit(detailRow, true)">通过</el-button>
          <el-button type="danger" @click="openReject(detailRow)">驳回</el-button>
        </template>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回证书申请" width="420px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { auditApplication, getApplications } from '@/api/application'
import { getCertStandards } from '@/api/certStandard'
import { getStudentCertByApplication } from '@/api/studentCert'

const CERT_BIZ_TYPES = ['CERT_APPLY', 'EXPERT_CERT']

const router = useRouter()
const { currentUser } = useAuth()

const apps = ref([])
const standards = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

const detailForm = computed(() => parseForm(detailRow.value))
const detailAttachments = computed(() =>
  Array.isArray(detailForm.value.attachments) ? detailForm.value.attachments : [])
const standardDetail = computed(() => findStandard(detailRow.value))

onMounted(loadData)

async function loadData() {
  try {
    const [list, standardList] = await Promise.all([
      getApplications(currentUser.value?.role, currentUser.value?.id),
      getCertStandards()
    ])
    standards.value = standardList
    apps.value = list.filter(app => CERT_BIZ_TYPES.includes(app.bizType))
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

async function audit(row, approve, reason) {
  try {
    await auditApplication(row.id, currentUser.value?.role, currentUser.value?.id, approve, reason)
    ElMessage.success(approve ? '已通过' : '已驳回')
    detailVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function openReject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await audit(rejectTarget.value, false, rejectReason.value)
  rejectVisible.value = false
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
  const standard = findStandard(row)
  const standardId = form.certStandardId || form.standardId
  return form.standardName || form.fieldName || standard?.standardName || (standardId ? `认证标准 #${standardId}` : '-')
}

function findStandard(row) {
  const form = parseForm(row)
  const standardId = form.certStandardId || form.standardId
  return standards.value.find(standard => standard.id === standardId)
}

function doneSteps(row) {
  return row.flowSteps?.filter(step => step.state === 'done').length || 0
}

function stepStatus(step) {
  const map = { done: 'success', current: 'process', rejected: 'error', pending: 'wait' }
  return map[step.state] || 'wait'
}

async function openCertificate(row) {
  try {
    const cert = await getStudentCertByApplication(row.id, currentUser.value?.role, currentUser.value?.id)
    router.push(`/student-certificate/${cert.id}`)
  } catch (error) {
    ElMessage.error(error.message || '证书加载失败')
  }
}

function formatTime(time) {
  return time ? String(time).replace('T', ' ') : ''
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

.requirement-text {
  white-space: pre-line;
  color: #495057;
  line-height: 1.7;
}
</style>
