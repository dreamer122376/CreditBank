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
        <el-table-column prop="expertName" label="指派专家" width="100" />
        <el-table-column prop="appliedAt" label="提交时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.appliedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.statusType">{{ scope.row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" width="140" />
        <el-table-column label="操作" width="230">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">详情</el-button>
            <template v-if="canAudit(scope.row)">
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

    <el-dialog v-model="detailVisible" title="申请详情" width="520px">
      <el-descriptions :column="1" border v-if="detailRow">
        <el-descriptions-item label="业务类型">{{ detailRow.bizTypeName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailRow.applicantName }}</el-descriptions-item>
        <el-descriptions-item v-if="detailForm.fieldName" label="申请领域">
          {{ detailForm.fieldName }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailForm.reason" label="申请理由">
          {{ detailForm.reason }}
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
          <span v-else style="color: #868e96;">未提供</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailRow.rejectReason" label="驳回原因">
          {{ detailRow.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
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

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
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

onMounted(loadData)

async function loadData() {
  try {
    apps.value = await getApplications(currentUser.value?.role, currentUser.value?.id)
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

// 机构管理员在"待机构审核"(1)时可操作，专家在"待专家评审"(2)时可操作，admin 两个环节都可代审
function canAudit(row) {
  const role = currentUser.value?.role
  if (role === 'admin') {
    return row.currentStatus === 1 || row.currentStatus === 2
  }
  if (role === 'org_admin') {
    return row.currentStatus === 1
  }
  if (role === 'expert') {
    return row.currentStatus === 2
  }
  return false
}

async function audit(row, approve, reason) {
  try {
    await auditApplication(row.id, currentUser.value?.role, approve, reason)
    ElMessage.success(approve ? '已通过' : '已驳回')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
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

function formatTime(time) {
  return time ? String(time).replace('T', ' ') : ''
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
</style>
