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
        <el-table-column label="操作" width="170">
          <template #default="scope">
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getApplications, auditApplication } from '@/api/application'

const { currentUser } = useAuth()

const apps = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)

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
