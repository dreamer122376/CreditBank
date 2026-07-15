<template>
  <div class="accounts" v-loading="loading">
    <!-- 顶栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">用户列表</span>
        <el-select v-model="filterRole" placeholder="筛选角色" class="role-filter">
          <el-option value="admin" label="系统管理员" />
          <el-option value="org_admin" label="机构管理员" />
          <el-option value="student" label="学生" />
          <el-option value="expert" label="专家" />
        </el-select>
      </div>
      <el-button @click="$router.push('/op-logs')">📝 操作日志</el-button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="searchKeyword" placeholder="搜索用户名/真实姓名" clearable style="width: 240px;" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 120px;">
        <el-option :value="1" label="正常" />
        <el-option :value="0" label="已冻结" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <!-- 用户表格 -->
    <el-card>
      <el-table :data="paginatedUsers" border style="width: 100%;" @selection-change="handleSelectionChange" ref="tableRef">
        <el-table-column v-if="isAdmin" type="selection" width="50" :selectable="row => row.role !== 'admin'" />
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        
        <el-table-column v-if="showBalance" prop="balance" :label="balanceLabel" width="140" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="160">
          <template #default="scope">
            <span v-if="scope.row.lastLoginAt">{{ fmt(scope.row.lastLoginAt) }}</span>
            <span v-else style="color:#adb5bd;">从未登录</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240">
          <template #default="scope">
            <el-button size="small" @click="viewAccount(scope.row.id)">详情</el-button>
            <el-button v-if="showBalance" size="small" type="primary" @click="openEarnDialog(scope.row)">
  {{ scope.row.role === 'org_admin' ? '积分池加分' : '加分' }}
</el-button>
            <template v-if="isAdmin && scope.row.role !== 'admin'">
              <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'"
                @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '冻结' : '解冻' }}
              </el-button>
              <el-button size="small" type="danger" plain @click="openResetPw(scope.row)">重置密码</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="paginatedUsers.length === 0" style="text-align:center;padding:40px;color:#868e96;">暂无用户</div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalUsers > 0">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="totalUsers"
          :page-size="pageSize"
          v-model:current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 批量操作栏 -->
    <div class="batch-bar" v-if="isAdmin && selectedIds.length > 0">
      <span>已选 <strong>{{ selectedIds.length }}</strong> 项
        <template v-if="freezeCount > 0 || unfreezeCount > 0">
          （可冻结 <strong>{{ freezeCount }}</strong> / 可解冻 <strong>{{ unfreezeCount }}</strong>）
        </template>
      </span>
      <el-button type="warning" @click="batchFreeze" :disabled="freezeCount === 0">批量冻结</el-button>
      <el-button type="success" @click="batchUnfreeze" :disabled="unfreezeCount === 0">批量解冻</el-button>
      <el-button @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetPwVisible" title="重置密码" width="400px" :close-on-click-modal="false">
      <el-form :model="resetPwForm">
        <el-form-item label="用户">
          <span>{{ resetPwForm.realName }}（{{ resetPwForm.username }}）</span>
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="resetPwForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPw" :loading="resetting">确认重置</el-button>
      </template>
    </el-dialog>

    <!-- 加分弹窗 -->
    <el-dialog v-model="earnDialogVisible" :title="selectedEarnUser?.role === 'org_admin' ? '积分池加分' : '积分加分'" width="400px">
      <el-form :model="earnForm">
        <el-form-item label="用户">
          <el-input :value="selectedEarnUser?.username + ' (' + selectedEarnUser?.realName + ')'" disabled />
        </el-form-item>
        <template v-if="selectedEarnUser?.role === 'org_admin'">
          <el-form-item label="增加积分">
            <el-input-number v-model="earnForm.creditValue" :min="1" :max="999999" placeholder="请输入增加的积分值" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="选择规则">
            <el-select v-model="earnForm.eventCode" placeholder="请选择积分规则">
              <el-option v-for="rule in rules" :key="rule.eventCode" :label="rule.eventName + ' (' + rule.creditValue + '分)'" :value="rule.eventCode" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="earnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEarn" :loading="earning">确认加分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getUsers, updateUserStatus, batchUpdateStatus, resetPassword } from '@/api/user'
import { getRules, earnPoints } from '@/api/point'

const router = useRouter()
const { currentUser } = useAuth()
const loading = ref(true)
const users = ref([])
const selectedIds = ref([])
const tableRef = ref(null)
const resetPwVisible = ref(false)
const resetting = ref(false)
const resetPwForm = ref({ id: null, username: '', realName: '', newPassword: '' })
const filterRole = ref('student')
const earnDialogVisible = ref(false)
const earning = ref(false)
const earnForm = ref({ eventCode: '', creditValue: 0 })
const selectedEarnUser = ref(null)
const rules = ref([])

const searchKeyword = ref('')
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const ROLE_NAME = { admin: '系统管理员', org_admin: '机构管理员', student: '学生', expert: '专家' }
const ROLE_TYPE = { admin: 'danger', org_admin: 'warning', student: 'success', expert: 'info' }

const isAdmin = computed(() => currentUser.value?.role === 'admin')

const filteredUsers = computed(() => {
  let result = users.value.filter(u => u.role === filterRole.value)
  
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(u => 
      (u.username && u.username.toLowerCase().includes(kw)) ||
      (u.realName && u.realName.toLowerCase().includes(kw))
    )
  }
  
  if (filterStatus.value !== null && filterStatus.value !== '') {
    result = result.filter(u => u.status === filterStatus.value)
  }
  
  return result
})

const totalUsers = computed(() => filteredUsers.value.length)

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const showBalance = computed(() => ['student', 'org_admin'].includes(filterRole.value))
const balanceLabel = computed(() => filterRole.value === 'org_admin' ? '机构积分池余额' : '积分余额')

function getRoleName(r) { return ROLE_NAME[r] || r }
function getRoleType(r) { return ROLE_TYPE[r] || 'info' }
function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }

const freezeCount = computed(() => selectedIds.value.filter(id => {
  const u = users.value.find(u => u.id === id)
  return u && u.status === 1 && u.role !== 'admin'
}).length)

const unfreezeCount = computed(() => selectedIds.value.filter(id => {
  const u = users.value.find(u => u.id === id)
  return u && u.status === 0 && u.role !== 'admin'
}).length)

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    users.value = await getUsers()
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
}

function resetFilters() {
  searchKeyword.value = ''
  filterStatus.value = null
  currentPage.value = 1
}

function handlePageChange() {
  tableRef.value?.clearSelection()
  selectedIds.value = []
}

// ==================== 选择 ====================

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}
function clearSelection() {
  tableRef.value?.clearSelection()
  selectedIds.value = []
}

// ==================== 单个状态切换 ====================

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '冻结' : '解冻'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户「${row.realName}」吗？`, `确认${actionText}`, { type: 'warning' })
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${actionText}成功`)
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

// ==================== 批量操作 ====================

async function batchFreeze() {
  // 只冻结当前状态为正常的用户
  const eligible = selectedIds.value.filter(id => {
    const u = users.value.find(u => u.id === id)
    return u && u.status === 1 && u.role !== 'admin'
  })
  if (eligible.length === 0) {
    ElMessage.warning('所选用户均为已冻结或系统管理员，无需操作')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要冻结选中的 ${eligible.length} 个用户吗？`, '确认批量冻结', { type: 'warning' })
    await batchUpdateStatus(eligible, 0)
    ElMessage.success(`批量冻结成功：${eligible.length} 个`)
    clearSelection()
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function batchUnfreeze() {
  // 只解冻当前状态为冻结的用户
  const eligible = selectedIds.value.filter(id => {
    const u = users.value.find(u => u.id === id)
    return u && u.status === 0 && u.role !== 'admin'
  })
  if (eligible.length === 0) {
    ElMessage.warning('所选用户均为正常状态或系统管理员，无需操作')
    return
  }
  try {
    await batchUpdateStatus(eligible, 1)
    ElMessage.success(`批量解冻成功：${eligible.length} 个`)
    clearSelection()
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

// ==================== 重置密码 ====================

function openResetPw(row) {
  resetPwForm.value = { id: row.id, username: row.username, realName: row.realName, newPassword: '' }
  resetPwVisible.value = true
}

async function handleResetPw() {
  if (!resetPwForm.value.newPassword) { ElMessage.warning('请输入新密码'); return }
  resetting.value = true
  try {
    await resetPassword(resetPwForm.value.id, resetPwForm.value.newPassword)
    ElMessage.success('密码重置成功')
    resetPwVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '重置失败')
  } finally {
    resetting.value = false
  }
}

// ==================== 加分 ====================

function openEarnDialog(row) {
  selectedEarnUser.value = row
  earnForm.value = { eventCode: '', creditValue: 0 }
  if (row.role !== 'org_admin' && rules.value.length === 0) {
    loadRules()
  }
  earnDialogVisible.value = true
}

async function loadRules() {
  try {
    rules.value = await getRules()
  } catch (error) {
    ElMessage.error('加载积分规则失败')
  }
}

async function handleEarn() {
  if (!selectedEarnUser.value) return
  if (selectedEarnUser.value.role === 'org_admin') {
    if (!earnForm.value.creditValue || earnForm.value.creditValue <= 0) {
      ElMessage.warning('请输入增加的积分值')
      return
    }
    earning.value = true
    try {
      await earnPoints(selectedEarnUser.value.id, 'ADMIN', earnForm.value.creditValue)
      ElMessage.success('积分池加分成功')
      earnDialogVisible.value = false
      await loadData()
    } catch (error) {
      ElMessage.error(error.message || '积分池加分失败')
    } finally {
      earning.value = false
    }
  } else {
    if (!earnForm.value.eventCode) {
      ElMessage.warning('请选择积分规则')
      return
    }
    earning.value = true
    try {
      await earnPoints(selectedEarnUser.value.id, earnForm.value.eventCode)
      ElMessage.success('加分成功')
      earnDialogVisible.value = false
      await loadData()
    } catch (error) {
      ElMessage.error(error.message || '加分失败')
    } finally {
      earning.value = false
    }
  }
}

// ==================== 跳转 ====================

function viewAccount(id) { router.push(`/account/${id}`) }
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; align-items: center; gap: 12px; }
.toolbar-title { font-size: 16px; font-weight: 600; color: #2c3e50; }
.role-filter { width: 140px; }
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  margin-bottom: 16px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.batch-bar {
  position: fixed; bottom: 0; left: 220px; right: 0; z-index: 50;
  background: #fff; border-top: 2px solid #3b5bdb;
  padding: 12px 24px; display: flex; align-items: center; gap: 12px;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.08);
}
</style>
