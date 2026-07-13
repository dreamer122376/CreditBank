<template>
  <div class="student-certs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生证书认证</span>
          <el-button type="primary" size="small" @click="openApply">申请认证</el-button>
        </div>
      </template>
      <p class="intro">选择适合自己的认证标准并提交申请。自动通过型标准提交后即生效；人工审核型会按标准配置的审核流程处理。</p>
    </el-card>

    <el-card style="margin-top: 16px;">
      <template #header><span>我的认证申请</span></template>
      <el-table :data="myApplies" border style="width: 100%;">
        <el-table-column prop="id" label="申请单号" width="100" />
        <el-table-column label="认证标准" min-width="180">
          <template #default="{ row }">{{ parseForm(row).standardName || standardName(row) || '—' }}</template>
        </el-table-column>
        <el-table-column label="审批进度" width="120">
          <template #default="{ row }">
            <span v-if="row.flowSteps?.length">{{ doneSteps(row) }}/{{ row.flowSteps.length }} 步</span>
            <span v-else>{{ row.statusName === '已通过' ? '自动通过' : '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="160">
          <template #default="{ row }"><el-tag :type="row.statusType">{{ row.statusName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="certForApply(row)" size="small" type="primary" plain @click="viewCert(certForApply(row))">
              查看证书
            </el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!myApplies.length" description="还没有认证申请" />
    </el-card>

    <el-card style="margin-top: 16px;">
      <template #header><span>我的证书</span></template>
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
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="viewCert(row)">查看证书</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!myCerts.length" description="暂无已发放证书" />
    </el-card>

    <el-dialog v-model="applyVisible" title="申请学生证书认证" width="580px">
      <el-form :model="applyForm" label-width="100px" label-position="left">
        <el-form-item label="认证标准" required>
          <el-select v-model="applyForm.certStandardId" placeholder="选择要申请的认证标准" style="width: 100%;" @change="loadFlowPreview">
            <el-option v-for="standard in applicableStandards" :key="standard.id" :label="standard.standardName" :value="standard.id" />
          </el-select>
          <div v-if="selectedStandard" class="standard-hint">{{ standardDesc(selectedStandard) }}</div>
        </el-form-item>
        <el-form-item v-if="flowPreview.length" label="审核流程">
          <div class="flow-preview">
            <template v-for="(node, index) in flowPreview" :key="node.id">
              <span class="flow-node">第{{ index + 1 }}步 {{ node.auditorName || `用户#${node.auditorId}` }}</span>
              <el-icon v-if="index < flowPreview.length - 1" class="flow-arrow"><Right /></el-icon>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="可说明你的学习成果或申请原因（选填）" />
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
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Right } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getApplications, submitApplication } from '@/api/application'
import { getCertStandards } from '@/api/certStandard'
import { getAuditFlow } from '@/api/auditFlow'
import { getStudentCerts } from '@/api/studentCert'

const router = useRouter()
const { currentUser } = useAuth()
const standards = ref([])
const myApplies = ref([])
const myCerts = ref([])
const applyVisible = ref(false)
const flowPreview = ref([])
const applyForm = ref({ certStandardId: null, reason: '' })

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
  applyForm.value = { certStandardId: null, reason: '' }
  flowPreview.value = []
  applyVisible.value = true
}

function standardDesc(standard) {
  const auditText = standard.needManualAudit === 1
    ? `需人工审核${standard.flowStepCount ? `（${standard.flowStepCount} 步）` : ''}`
    : '自动通过：提交后立即生效'
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
  try {
    await submitApplication({
      bizType: 'CERT_APPLY',
      applicantId: currentUser.value.id,
      formData: JSON.stringify({
        certStandardId: applyForm.value.certStandardId,
        standardName: selectedStandard.value?.standardName || '',
        reason: applyForm.value.reason.trim()
      })
    })
    ElMessage.success(selectedStandard.value?.needManualAudit === 1 ? '申请已提交，等待审核' : '申请已通过')
    applyVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function parseForm(row) {
  try { return row.formData ? JSON.parse(row.formData) : {} } catch (_) { return {} }
}

function standardName(row) {
  const id = parseForm(row).certStandardId || parseForm(row).standardId
  return standards.value.find(s => s.id === id)?.standardName
}

function doneSteps(row) {
  return row.flowSteps.filter(step => step.state === 'done').length
}

function viewCert(row) {
  router.push(`/student-certificate/${row.id}`)
}

function certForApply(app) {
  if (app.currentStatus !== 3 && app.statusName !== '已通过') {
    return null
  }
  const standardId = parseForm(app).certStandardId || parseForm(app).standardId
  return myCerts.value.find(cert =>
    cert.applicationId === app.id ||
    (standardId && cert.certStandardId === standardId))
}

function formatTime(time) {
  return time ? String(time).replace('T', ' ') : '—'
}
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.intro, .standard-hint { color: #606266; font-size: 13px; line-height: 1.7; margin: 0; white-space: pre-line; }
.standard-hint { margin-top: 6px; background: #f8f9fa; border-radius: 4px; padding: 6px 10px; }
.flow-preview { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; font-size: 13px; }
.flow-node { background: #f1f3f5; border-radius: 4px; padding: 3px 10px; color: #495057; }
.flow-arrow { color: #909399; }
.muted { color: #adb5bd; font-size: 12px; }
</style>
