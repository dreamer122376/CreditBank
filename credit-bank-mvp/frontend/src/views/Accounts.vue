<template>
  <div class="accounts">
    <!-- 顶栏 -->
    <div class="toolbar">
      <span class="toolbar-title">用户列表</span>
      <el-button @click="$router.push('/users/op-logs')">📝 操作日志</el-button>
    </div>

    <!-- 用户表格 -->
    <el-card>
      <el-table :data="users" border style="width: 100%;" @selection-change="handleSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)" size="small">{{ getRoleName(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="积分余额" width="100" />
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
            <template v-if="scope.row.role !== 'admin'">
              <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'"
                @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '冻结' : '解冻' }}
              </el-button>
              <el-button size="small" type="danger" plain @click="openResetPw(scope.row)">重置密码</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="users.length === 0" style="text-align:center;padding:40px;color:#868e96;">暂无用户</div>
    </el-card>

    <!-- 批量操作栏 -->
    <div class="batch-bar" v-if="selectedIds.length > 0">
      <span>已选 <strong>{{ selectedIds.length }}</strong> 项</span>
      <el-button type="warning" @click="batchFreeze">批量冻结</el-button>
      <el-button type="success" @click="batchUnfreeze">批量解冻</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus, batchUpdateStatus, resetPassword } from '@/api/user'

const router = useRouter()
const users = ref([])
const selectedIds = ref([])
const tableRef = ref(null)
const resetPwVisible = ref(false)
const resetting = ref(false)
const resetPwForm = ref({ id: null, username: '', realName: '', newPassword: '' })

const ROLE_NAME = { admin: '系统管理员', org_admin: '机构管理员', student: '学生', expert: '专家' }
const ROLE_TYPE = { admin: 'danger', org_admin: 'warning', student: 'success', expert: 'info' }

function getRoleName(r) { return ROLE_NAME[r] || r }
function getRoleType(r) { return ROLE_TYPE[r] || 'info' }
function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }

onMounted(() => { loadData() })

async function loadData() {
  try {
    users.value = await getUsers()
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  }
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
  try {
    await ElMessageBox.confirm(`确定要冻结选中的 ${selectedIds.value.length} 个用户吗？`, '确认批量冻结', { type: 'warning' })
    await batchUpdateStatus(selectedIds.value, 0)
    ElMessage.success('批量冻结成功')
    clearSelection()
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function batchUnfreeze() {
  try {
    await batchUpdateStatus(selectedIds.value, 1)
    ElMessage.success('批量解冻成功')
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

// ==================== 跳转 ====================

function viewAccount(id) { router.push(`/account/${id}`) }
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-title { font-size: 16px; font-weight: 600; color: #2c3e50; }
.batch-bar {
  position: fixed; bottom: 0; left: 220px; right: 0; z-index: 50;
  background: #fff; border-top: 2px solid #3b5bdb;
  padding: 12px 24px; display: flex; align-items: center; gap: 12px;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.08);
}
</style>
