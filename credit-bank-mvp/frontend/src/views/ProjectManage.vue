<template>
  <div class="project-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <div>
            <el-select v-if="isAdmin" v-model="filterStatus" placeholder="状态筛选" clearable size="small" style="width:140px;margin-right:8px;" @change="loadData">
              <el-option v-for="(name, val) in STATUS_NAME" :key="val" :label="name" :value="Number(val)" />
            </el-select>
            <el-button v-if="isOrgAdmin" type="primary" size="small" @click="openCreate">+ 新增项目</el-button>
          </div>
        </div>
      </template>
      <el-table :data="projects" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="项目ID" width="90" />
        <el-table-column prop="name" label="项目名称" min-width="160" />
        <el-table-column prop="orgName" label="机构" width="120" v-if="isAdmin" />
        <el-table-column prop="description" label="项目描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="expertName" label="专家" width="100" />
        <el-table-column prop="creditReward" label="积分奖励" width="90" />
        <el-table-column prop="creditPrice" label="积分费用" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="STATUS_TAG[scope.row.status] || 'info'">
              {{ STATUS_NAME[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="scope">{{ fmt(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="280">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">查看详情</el-button>
            <template v-if="isOrgAdmin && (scope.row.status === 2 || scope.row.status === 0)">
              <el-button size="small" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
            </template>
            <template v-if="isAdmin">
              <el-button v-if="scope.row.status === 0" size="small" type="success" plain @click="handleAudit(scope.row, true)">通过</el-button>
              <el-button v-if="scope.row.status === 0" size="small" type="danger" plain @click="handleAudit(scope.row, false)">驳回</el-button>
              <el-button v-if="scope.row.status === 1" size="small" type="warning" plain @click="handleOffline(scope.row)">下架</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && projects.length === 0" style="text-align: center; padding: 40px;">
        暂无项目数据
      </div>
      <el-pagination
        v-if="isAdmin && total > pageSize"
        style="margin-top:16px;text-align:right;"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑项目' : '新增项目'" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.name" maxlength="100" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="积分奖励">
          <el-input-number v-model="form.creditReward" :min="0" placeholder="完成项目的积分奖励" style="width:100%;" />
        </el-form-item>
        <el-form-item label="报名费用">
          <el-input-number v-model="form.creditPrice" :min="0" placeholder="0 表示免费" style="width:100%;" />
        </el-form-item>
        <el-form-item label="负责专家">
          <el-select v-model="form.expertId" placeholder="请选择专家（可选）" clearable style="width:100%;" filterable>
            <el-option v-for="ex in experts" :key="ex.id" :label="ex.realName + (ex.expertField ? '（' + ex.expertField + '）' : '')" :value="ex.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="项目详情" width="640px">
      <div v-loading="detailLoading">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="项目ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="项目描述">{{ detail.description || '—' }}</el-descriptions-item>
          <el-descriptions-item label="积分奖励">{{ detail.creditReward ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="积分费用">{{ detail.creditPrice ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">
            <el-tag :type="STATUS_TAG[detail.status] || 'info'">{{ STATUS_NAME[detail.status] || '未知' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属机构">{{ detail.orgName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="负责专家">{{ detail.expertName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ fmt(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ fmt(detail.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 报名学生列表仅机构管理员可见，管理员在审批页面处理 -->
        <div class="students-section" v-if="isOrgAdmin">
          <div class="section-title">报名学生（{{ students.length }} 人）</div>
          <el-table :data="students" border size="small" style="width:100%;">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="realName" label="学生姓名" width="120" />
            <el-table-column prop="username" label="账号" width="140" />
            <el-table-column label="报名状态" width="100">
              <template #default="scope">
                <el-tag :type="studentStatusType(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enrolledAt" label="报名时间" width="160">
              <template #default="scope">{{ fmt(scope.row.enrolledAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="160">
              <template #default="scope">
                <template v-if="scope.row.status === '待审核'">
                  <el-button size="small" type="success" plain @click="handleAuditCompletion(scope.row, true)">通过</el-button>
                  <el-button size="small" type="danger" plain @click="handleAuditCompletion(scope.row, false)">驳回</el-button>
                </template>
                <span v-else style="color:#868e96;font-size:12px;">—</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="students.length === 0" style="text-align:center;padding:24px;color:#868e96;">暂无学生报名</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回原因" width="420px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejecting">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getOrgProjects, getAllProjects, createProject, updateProject, offlineProject, auditProject, getOrgProjectDetail, auditProjectCompletion } from '@/api/project'
import { getExperts } from '@/api/expert'

const { currentUser } = useAuth()
const isAdmin = computed(() => currentUser.value?.role === 'admin')
const isOrgAdmin = computed(() => currentUser.value?.role === 'org_admin')

const STATUS_NAME = { 0: '待审核', 1: '已上架', 2: '已驳回', 3: '已下架' }
const STATUS_TAG = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }

const loading = ref(false)
const submitting = ref(false)
const projects = ref([])
const experts = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({})
const students = ref([])
const filterStatus = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 驳回弹窗
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
let pendingRejectId = null

const form = reactive({
  id: null, name: '', description: '', creditReward: 0, creditPrice: 0, expertId: null
})

function resetForm() {
  form.id = null; form.name = ''; form.description = ''; form.creditReward = 0; form.creditPrice = 0; form.expertId = null
}

async function loadData() {
  loading.value = true
  try {
    if (isAdmin.value) {
      const params = { page: pageNum.value, size: pageSize.value }
      if (filterStatus.value !== null && filterStatus.value !== '') {
        params.status = filterStatus.value
      }
      const res = await getAllProjects(params)
      projects.value = res.records || []
      total.value = res.total || 0
    } else {
      projects.value = await getOrgProjects()
      total.value = projects.value.length
    }
  } catch (e) {
    ElMessage.error('加载失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function loadExperts() {
  try {
    const res = await getExperts()
    experts.value = Array.isArray(res) ? res : (res.records || [])
  } catch (e) { /* ignore */ }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.id = row.id
  form.name = row.name || ''
  form.description = row.description || ''
  form.creditReward = row.creditReward ?? 0
  form.creditPrice = row.creditPrice ?? 0
  form.expertId = row.expertId ?? null
  dialogVisible.value = true
}

async function save() {
  if (!form.name) { ElMessage.warning('请输入项目名称'); return }
  submitting.value = true
  try {
    const data = { name: form.name, description: form.description, creditReward: form.creditReward, creditPrice: form.creditPrice, expertId: form.expertId || null }
    if (form.id) {
      await updateProject(form.id, data)
    } else {
      await createProject(data)
    }
    ElMessage.success(form.id ? '修改成功' : '创建成功，等待管理员审核')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 管理员操作 ====================

async function handleAudit(row, approve) {
  if (approve) {
    try {
      await ElMessageBox.confirm('确定要通过项目「' + row.name + '」的审核吗？', '审核通过', { type: 'info' })
      await auditProject(row.id, { approve: true })
      ElMessage.success('审核通过，项目已上架')
      loadData()
    } catch (e) {
      if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
    }
  } else {
    pendingRejectId = row.id
    rejectReason.value = ''
    rejectVisible.value = true
  }
}

async function confirmReject() {
  if (!rejectReason.value.trim()) { ElMessage.warning('请填写驳回原因'); return }
  rejecting.value = true
  try {
    await auditProject(pendingRejectId, { approve: false, reason: rejectReason.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    rejecting.value = false
  }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm('确定要下架项目「' + row.name + '」吗？', '确认下架', { type: 'warning' })
    await offlineProject(row.id)
    ElMessage.success('已下架')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

// ==================== 详情 ====================

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = {}
  students.value = []
  try {
    const res = await getOrgProjectDetail(row.id)
    detail.value = res || {}
    const enrolledStudents = res.enrolledStudents || []
    students.value = enrolledStudents
  } catch (e) {
    ElMessage.error('加载详情失败：' + (e.message || ''))
  } finally {
    detailLoading.value = false
  }
}

async function handleAuditCompletion(row, approve) {
  const action = approve ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm('确定要' + action + '「' + row.realName + '」的完成申请吗？', action + '确认', { type: approve ? 'info' : 'warning' })
    await auditProjectCompletion(row.enrollmentId, approve)
    ElMessage.success('已' + action)
    openDetail(detail.value)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

function studentStatusType(s) {
  if (s === '已完成') return 'success'
  if (s === '待审核') return 'warning'
  if (s === '进行中') return 'warning'
  if (s === '已报名') return 'primary'
  return 'info'
}

function fmt(t) {
  if (!t) return '—'
  const str = String(t)
  return str.length >= 16 ? str.substring(0, 16).replace('T', ' ') : str
}

onMounted(() => { loadData(); loadExperts() })
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.students-section {
  margin-top: 20px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}
</style>
