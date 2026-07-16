<template>
  <div class="applications" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div v-for="item in statItems" :key="item.key"
           class="stat-card" :class="{ active: activeTab === 'conversion' ? convStatusFilter === item.key : statFilter === item.key }"
           @click="toggleStat(item.key)">
        <div class="stat-value" :style="{ color: item.color }">{{ item.count }}</div>
        <div class="stat-label">{{ item.label }}</div>
      </div>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-tabs">
            <span class="tab" :class="{ active: activeTab === 'biz' }" @click="switchTab('biz')">业务流程</span>
            <span class="tab" :class="{ active: activeTab === 'cert' }" @click="switchTab('cert')">证书申请</span>
            <span class="tab" :class="{ active: activeTab === 'conversion' }" @click="switchTab('conversion')">转换申请</span>
          </div>
          <div class="header-right">
            <el-tag v-if="statFilter" closable type="primary" effect="plain" @close="statFilter = null">
              {{ statItems.find(item => item.key === statFilter)?.label }}
            </el-tag>
            <el-tag v-if="activeTab === 'cert'" type="info" effect="plain">学生认证 / 专家认证</el-tag>
          </div>
        </div>
      </template>

      <el-table v-if="activeTab !== 'conversion'" :data="filteredApps" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="65" />
        <el-table-column prop="bizTypeName" label="申请类型" width="130" />
        <el-table-column v-if="activeTab === 'cert'" label="认证标准" min-width="160">
          <template #default="{ row }">{{ certTitle(row) }}</template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="orgName" label="所属机构" min-width="120" />
        <el-table-column prop="appliedAt" label="提交时间" width="155">
          <template #default="scope">
            {{ formatTime(scope.row.appliedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="130">
          <template #default="scope">
            <el-tag :type="scope.row.statusType">{{ scope.row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="90">
          <template #default="scope">
            <span v-if="scope.row.flowSteps && scope.row.flowSteps.length">
              {{ doneSteps(scope.row) }}/{{ scope.row.flowSteps.length }}
            </span>
            <span v-else-if="isCertBiz(scope.row)" class="muted">自动</span>
            <span v-else class="muted">单审</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="activeTab === 'cert' ? 260 : 200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
            <template v-if="scope.row.canAudit">
              <el-button size="small" type="success" @click="audit(scope.row, true)">通过</el-button>
              <el-button size="small" type="danger" @click="openReject(scope.row)">驳回</el-button>
            </template>
            <el-button v-if="scope.row.bizType === 'CERT_APPLY' && scope.row.currentStatus === STATUS_APPROVED"
                       size="small" type="primary" plain @click="openCertificate(scope.row)">
              证书
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="filteredConversionApps" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="65" />
        <el-table-column prop="studentName" label="申请人" width="100" />
        <el-table-column prop="applyType" label="申请类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.applyType === 'RULE_CONVERT' ? 'primary' : 'warning'" size="small">
              {{ scope.row.applyType === 'RULE_CONVERT' ? '已有规则转换' : '新增规则申请' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="转换规则" min-width="180">
          <template #default="scope">
            <span v-if="scope.row.ruleName">{{ scope.row.ruleName }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="原成果名称" min-width="150">
          <template #default="scope">
            <span class="name-cell">{{ scope.row.originalName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalOrgName" label="原成果机构" min-width="100" />
        <el-table-column prop="originalType" label="原成果类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.originalType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="转换" width="40">
          <template #default>
            <span class="arrow">➜</span>
          </template>
        </el-table-column>
        <el-table-column prop="convertedName" label="转换后成果名称" min-width="150">
          <template #default="scope">
            <span class="name-cell">{{ scope.row.convertedName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="convertedOrgName" label="转换后成果机构" min-width="100" />
        <el-table-column prop="convertedType" label="转换后成果类型" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.convertedType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getConvStatusType(scope.row.status)" size="small">
              {{ getConvStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.rejectReason" class="reject-text">{{ scope.row.rejectReason }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="155">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <el-button size="small" type="success" @click="auditConversion(scope.row, true)">通过</el-button>
              <el-button size="small" type="danger" @click="openConvReject(scope.row)">驳回</el-button>
            </template>
            <span v-else class="muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="activeTab !== 'conversion' && !filteredApps.length" :description="statFilter ? '当前筛选条件下暂无申请' : (activeTab === 'cert' ? '暂无证书申请' : '暂无业务流程申请')" />
      <el-empty v-if="activeTab === 'conversion' && !filteredConversionApps.length" description="暂无转换申请记录" />
    </el-card>

    <!-- 申请详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailRow?.bizType === 'CERT_APPLY' || detailRow?.bizType === 'EXPERT_CERT' ? '证书申请详情' : '申请详情'" width="720px">
      <template v-if="detailRow">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="业务类型">{{ detailRow.bizTypeName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detailRow.applicantName }}</el-descriptions-item>
          <el-descriptions-item v-if="detailRow.orgName" label="所属机构">{{ detailRow.orgName }}</el-descriptions-item>
          <el-descriptions-item v-if="isCertBiz(detailRow)" label="认证标准">{{ certTitle(detailRow) }}</el-descriptions-item>
          <el-descriptions-item v-if="detailForm.fieldName" label="申请领域">{{ detailForm.fieldName }}</el-descriptions-item>
          <el-descriptions-item v-if="detailForm.reason || detailForm.applyReason" label="申请理由">
            {{ detailForm.reason || detailForm.applyReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="standardDetail?.requirementText" label="认证标准要求">
            <div class="requirement-text">{{ standardDetail.requirementText }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="证明材料">
            <template v-if="detailAttachments.length">
              <div v-for="(att, i) in detailAttachments" :key="i" class="att-row">
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

        <template v-if="detailRow.flowSteps && detailRow.flowSteps.length">
          <el-divider content-position="left">审核流程</el-divider>
          <el-steps align-center>
            <el-step v-for="s in detailRow.flowSteps" :key="s.nodeId"
                     :title="'第 ' + s.stepNo + ' 步'"
                     :description="s.auditorName"
                     :status="stepStatus(s)" />
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

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回申请" width="420px">
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

    <!-- 转换申请驳回弹窗 -->
    <el-dialog v-model="convRejectVisible" title="驳回转换申请" width="420px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="convRejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convRejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmConvReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getApplications, auditApplication } from '@/api/application'
import { getCertStandards } from '@/api/certStandard'
import { getStudentCertByApplication } from '@/api/studentCert'
import { getConversionApplications, auditConversionApplication } from '@/api/conversionApplication'

const CERT_BIZ_TYPES = ['CERT_APPLY', 'EXPERT_CERT']
const STATUS_IN_REVIEW = 1
const STATUS_APPROVED = 2
const STATUS_REJECTED = 3
const IN_REVIEW_STATUSES = [STATUS_IN_REVIEW]

const router = useRouter()
const { currentUser } = useAuth()

const loading = ref(true)
const apps = ref([])
const standards = ref([])
const activeTab = ref('biz')
const statFilter = ref(null)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

const conversionApps = ref([])
const convStatusFilter = ref(null)
const convRejectVisible = ref(false)
const convRejectReason = ref('')
const convRejectTarget = ref(null)

const detailForm = computed(() => {
  try {
    return detailRow.value?.formData ? JSON.parse(detailRow.value.formData) : {}
  } catch (e) {
    return {}
  }
})

const detailAttachments = computed(() =>
  Array.isArray(detailForm.value.attachments) ? detailForm.value.attachments : [])

const standardDetail = computed(() => findStandard(detailRow.value))

const certApps = computed(() => apps.value.filter(app => CERT_BIZ_TYPES.includes(app.bizType)))

const bizApps = computed(() => apps.value.filter(app => !CERT_BIZ_TYPES.includes(app.bizType)))

const statItems = computed(() => {
  if (activeTab.value === 'conversion') {
    return [
      { key: 'pending', label: '待审核', color: '#e8590c', count: conversionApps.value.filter(app => app.status === 0).length },
      { key: 'approved', label: '已通过', color: '#2f9e44', count: conversionApps.value.filter(app => app.status === 1).length },
      { key: 'rejected', label: '已驳回', color: '#c0392b', count: conversionApps.value.filter(app => app.status === 2).length },
      { key: 'all', label: '全部', color: '#495057', count: conversionApps.value.length }
    ]
  }
  const list = activeTab.value === 'cert' ? certApps.value : bizApps.value
  return [
    { key: 'mine', label: activeTab.value === 'cert' ? '待我审核' : '待处理', color: '#e8590c', count: list.filter(app => app.canAudit).length },
    { key: 'inReview', label: '审核中', color: '#f08c00', count: list.filter(app => IN_REVIEW_STATUSES.includes(app.currentStatus)).length },
    { key: 'approved', label: '已通过', color: '#2f9e44', count: list.filter(app => app.currentStatus === STATUS_APPROVED).length },
    { key: 'rejected', label: '已驳回', color: '#c0392b', count: list.filter(app => app.currentStatus === STATUS_REJECTED).length }
  ]
})

const currentApps = computed(() => activeTab.value === 'cert' ? certApps.value : bizApps.value)

const filteredConversionApps = computed(() => {
  if (!convStatusFilter.value) return conversionApps.value
  switch (convStatusFilter.value) {
    case 'pending': return conversionApps.value.filter(app => app.status === 0)
    case 'approved': return conversionApps.value.filter(app => app.status === 1)
    case 'rejected': return conversionApps.value.filter(app => app.status === 2)
    default: return conversionApps.value
  }
})

const filteredApps = computed(() => {
  if (!statFilter.value) return currentApps.value
  const list = currentApps.value
  switch (statFilter.value) {
    case 'mine': return list.filter(app => app.canAudit)
    case 'inReview': return list.filter(app => IN_REVIEW_STATUSES.includes(app.currentStatus))
    case 'approved': return list.filter(app => app.currentStatus === STATUS_APPROVED)
    case 'rejected': return list.filter(app => app.currentStatus === STATUS_REJECTED)
    default: return list
  }
})

function toggleStat(key) {
  if (activeTab.value === 'conversion') {
    convStatusFilter.value = convStatusFilter.value === key ? null : key
  } else {
    statFilter.value = statFilter.value === key ? null : key
  }
}

function switchTab(tab) {
  activeTab.value = tab
  statFilter.value = null
  convStatusFilter.value = null
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [list, standardList, convList] = await Promise.all([
      getApplications(currentUser.value?.role, currentUser.value?.id),
      getCertStandards(),
      getConversionApplications()
    ])
    standards.value = standardList
    apps.value = list
    conversionApps.value = convList
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
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

function doneSteps(row) {
  return row.flowSteps?.filter(s => s.state === 'done').length || 0
}

function isCertBiz(row) {
  return row && CERT_BIZ_TYPES.includes(row.bizType)
}

function stepStatus(s) {
  const map = { done: 'success', current: 'process', rejected: 'error', pending: 'wait' }
  return map[s.state] || 'wait'
}

function formatTime(time) {
  return time ? String(time).replace('T', ' ') : ''
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
  return standards.value.find(s => s.id === standardId)
}

function parseForm(row) {
  try {
    return row?.formData ? JSON.parse(row.formData) : {}
  } catch (_) {
    return {}
  }
}

async function openCertificate(row) {
  try {
    const cert = await getStudentCertByApplication(row.id, currentUser.value?.role, currentUser.value?.id)
    router.push(`/student-certificate/${cert.id}`)
  } catch (error) {
    ElMessage.error(error.message || '证书加载失败')
  }
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

function getConvStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

function getConvStatusText(status) {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已驳回'
    default: return '未知'
  }
}

async function auditConversion(row, approve, reason = '') {
  try {
    if (approve) {
      await ElMessageBox.confirm('确定要通过该转换申请吗？通过后将自动发放积分。', '确认通过', { type: 'warning' })
    }
    await auditConversionApplication(row.id, approve, reason)
    ElMessage.success(approve ? '审核通过' : '已驳回')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审核失败')
    }
  }
}

function openConvReject(row) {
  convRejectTarget.value = row
  convRejectReason.value = ''
  convRejectVisible.value = true
}

async function confirmConvReject() {
  if (!convRejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await auditConversion(convRejectTarget.value, false, convRejectReason.value)
  convRejectVisible.value = false
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-tabs {
  display: flex;
  gap: 0;
}

.header-tabs .tab {
  padding: 6px 18px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s;
  user-select: none;
}

.header-tabs .tab:hover {
  color: #1e3a5f;
  background: #f1f5f9;
}

.header-tabs .tab.active {
  color: #fff;
  background: #1e3a5f;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 14px 18px;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.stat-card:hover {
  border-color: #adb5bd;
}

.stat-card.active {
  border-color: #3b5bdb;
  box-shadow: 0 0 0 2px rgba(59, 91, 219, 0.12);
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  color: #868e96;
  font-size: 13px;
  margin-top: 2px;
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

.name-cell {
  white-space: normal;
  word-break: break-all;
  line-height: 1.4;
}

.arrow {
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}
</style>
