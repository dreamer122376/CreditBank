<template>
  <div class="applications">
    <el-card>
      <template #header>
        <span>业务流程审批</span>
      </template>
      <el-table :data="apps" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bizTypeName" label="业务类型" width="130" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="orgName" label="所属机构" />
        <el-table-column prop="appliedAt" label="提交时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.appliedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.statusType">{{ scope.row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批进度" width="120">
          <template #default="scope">
            <span v-if="scope.row.flowSteps && scope.row.flowSteps.length">
              {{ doneSteps(scope.row) }}/{{ scope.row.flowSteps.length }} 步
            </span>
            <span v-else-if="isCertBiz(scope.row)" class="muted">自动通过型</span>
            <span v-else class="muted">管理员单审</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
            <template v-if="scope.row.canAudit">
              <el-button size="small" type="success" @click="audit(scope.row, true)">通过</el-button>
              <el-button size="small" type="danger" @click="openReject(scope.row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="apps.length === 0" style="text-align: center; padding: 40px;">
        暂无申请
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="申请详情" width="620px">
      <template v-if="detailRow">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="业务类型">{{ detailRow.bizTypeName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detailRow.applicantName }}</el-descriptions-item>
          <el-descriptions-item v-if="detailRow.orgName" label="所属机构">
            {{ detailRow.orgName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailForm.fieldName" label="申请领域">
            {{ detailForm.fieldName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailForm.reason || detailForm.applyReason" label="申请理由">
            {{ detailForm.reason || detailForm.applyReason }}
          </el-descriptions-item>
          <el-descriptions-item label="证明材料">
            <template v-if="detailAttachments.length">
              <div v-for="(att, i) in detailAttachments" :key="i" class="att-row">
                <span class="att-name">{{ att.name }}</span>
                <el-link v-if="isPreviewable(att)" :href="previewUrl(att)" target="_blank"
                         type="primary">预览</el-link>
                <el-link :href="downloadUrl(att)" type="primary">下载</el-link>
              </div>
            </template>
            <span v-else class="muted">未提供</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detailRow.rejectReason" label="驳回原因">
            {{ detailRow.rejectReason }}
          </el-descriptions-item>
        </el-descriptions>

        <template v-if="detailRow.flowSteps && detailRow.flowSteps.length">
          <el-divider content-position="left">审批流程</el-divider>
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
      </template>
    </el-dialog>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getApplications, auditApplication } from '@/api/application'

const { currentUser } = useAuth()

const apps = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

const detailForm = computed(() => {
  try {
    return detailRow.value?.formData ? JSON.parse(detailRow.value.formData) : {}
  } catch (e) {
    return {}
  }
})

const detailAttachments = computed(() =>
  Array.isArray(detailForm.value.attachments) ? detailForm.value.attachments : [])

onMounted(loadData)

async function loadData() {
  try {
    apps.value = await getApplications(currentUser.value?.role, currentUser.value?.id)
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

async function audit(row, approve, reason) {
  try {
    await auditApplication(row.id, currentUser.value?.role, currentUser.value?.id, approve, reason)
    ElMessage.success(approve ? '已通过' : '已驳回')
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
  return row.flowSteps.filter(s => s.state === 'done').length
}

function isCertBiz(row) {
  return row.bizType === 'CERT_APPLY' || row.bizType === 'EXPERT_CERT'
}

function stepStatus(s) {
  const map = { done: 'success', current: 'process', rejected: 'error', pending: 'wait' }
  return map[s.state] || 'wait'
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
</style>
