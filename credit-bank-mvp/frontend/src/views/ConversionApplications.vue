<template>
  <div class="conversion-applications" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>转换申请审核</span>
          <div class="filter-buttons">
            <el-button :type="statusFilter === undefined ? 'primary' : 'default'" size="small" @click="statusFilter = undefined">全部</el-button>
            <el-button :type="statusFilter === 0 ? 'primary' : 'default'" size="small" @click="statusFilter = 0">待审核</el-button>
            <el-button :type="statusFilter === 1 ? 'primary' : 'default'" size="small" @click="statusFilter = 1">已通过</el-button>
            <el-button :type="statusFilter === 2 ? 'primary' : 'default'" size="small" @click="statusFilter = 2">已驳回</el-button>
          </div>
        </div>
      </template>
      <el-table :data="applications" border style="width: 100%;" size="small" :max-height="tableMaxHeight">
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="studentName" label="申请人" width="80" />
        <el-table-column prop="applyType" label="申请类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.applyType === 'RULE_CONVERT' ? 'primary' : 'warning'" size="small">
              {{ scope.row.applyType === 'RULE_CONVERT' ? '已有规则转换' : '新增规则申请' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="转换规则" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.ruleName">{{ scope.row.ruleName }}</span>
            <span v-else>-</span>
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
        <el-table-column prop="convertedType" label="转换后成果类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.convertedType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.rejectReason" class="reject-reason">{{ scope.row.rejectReason }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="140">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <div class="action-buttons">
                <el-button size="small" type="success" @click="handleApprove(scope.row)">通过</el-button>
                <el-button size="small" type="danger" @click="handleReject(scope.row)">驳回</el-button>
              </div>
            </template>
            <span v-else style="color:#868e96;font-size:12px;">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="applications.length === 0" style="text-align: center; padding: 40px;">
        暂无转换申请记录
      </div>
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConversionApplications,
  auditConversionApplication
} from '@/api/conversionApplication'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const loading = ref(true)
const applications = ref([])
const statusFilter = ref(undefined)
const rejectDialogVisible = ref(false)
const rejectForm = ref({ reason: '' })
const currentRejectId = ref(null)

const tableMaxHeight = computed(() => {
  return Math.max(400, window.innerHeight - 280) + 'px'
})

onMounted(async () => {
  await loadApplications()
})

watch(statusFilter, async () => {
  await loadApplications()
})

async function loadApplications() {
  loading.value = true
  try {
    applications.value = await getConversionApplications(statusFilter.value)
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

function getStatusText(status) {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已驳回'
    default: return '未知'
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm('确定要通过该转换申请吗？通过后将自动发放积分。', '确认通过', { type: 'warning' })
    await auditConversionApplication(row.id, true, '')
    ElMessage.success('审核通过')
    await loadApplications()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审核失败')
    }
  }
}

function handleReject(row) {
  currentRejectId.value = row.id
  rejectForm.value = { reason: '' }
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.value.reason || rejectForm.value.reason.trim() === '') {
    ElMessage.warning('请填写驳回原因')
    return
  }
  try {
    await auditConversionApplication(currentRejectId.value, false, rejectForm.value.reason)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '驳回失败')
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-buttons {
  display: flex;
  gap: 4px;
}

.arrow {
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}

.name-cell {
  white-space: normal;
  word-break: break-all;
  line-height: 1.4;
}

.reject-reason {
  color: #f56c6c;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 3px;
}

.action-buttons .el-button {
  padding: 3px 8px;
  font-size: 11px;
}
</style>