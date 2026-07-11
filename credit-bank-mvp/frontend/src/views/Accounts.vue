<template>
  <div class="accounts">
    <el-card v-loading="loading" element-loading-text="加载中...">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="openEarnDialog">
            <el-icon><Plus /></el-icon>
            加分
          </el-button>
        </div>
      </template>
      <div class="filter-bar">
        <el-input v-model="searchText" placeholder="搜索用户名/姓名" style="width: 220px;" clearable @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterRole" placeholder="筛选角色" style="width: 140px;" clearable @change="loadData">
          <el-option label="系统管理员" value="admin" />
          <el-option label="机构管理员" value="org_admin" />
          <el-option label="学生" value="student" />
          <el-option label="专家" value="expert" />
        </el-select>
      </div>
      <el-table :data="users" border style="width: 100%;">
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="balance" label="积分余额">
          <template #default="scope">
            <span class="balance">{{ scope.row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">{{ getRoleName(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="text" @click="viewAccount(scope.row.id)">查看详情</el-button>
            <el-button type="text" @click="selectAccount(scope.row)">加分</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="earnDialogVisible" title="积分加分" width="400px">
      <el-form :model="earnForm">
        <el-form-item label="选择用户">
          <el-select v-model="earnForm.userId" placeholder="请选择用户">
            <el-option v-for="user in users" :key="user.id" :label="user.username + ' (' + user.realName + ')'" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择规则">
          <el-select v-model="earnForm.eventCode" placeholder="请选择积分规则">
            <el-option v-for="rule in rules" :key="rule.eventCode" :label="rule.eventName + ' (' + rule.creditValue + '分)'" :value="rule.eventCode" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="earnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEarn" :loading="earning">确认加分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getUsers } from '@/api/user'
import { getRules, earnPoints } from '@/api/point'

const router = useRouter()

const loading = ref(true)
const users = ref([])
const rules = ref([])
const searchText = ref('')
const filterRole = ref('')
const earnDialogVisible = ref(false)
const earning = ref(false)

const earnForm = ref({
  userId: '',
  eventCode: ''
})

const ROLE_NAME = {
  admin: '系统管理员',
  org_admin: '机构管理员',
  student: '学生',
  expert: '专家'
}

const ROLE_TYPE = {
  admin: 'danger',
  org_admin: 'warning',
  student: 'success',
  expert: 'info'
}

function getRoleName(role) {
  return ROLE_NAME[role] || role
}

function getRoleType(role) {
  return ROLE_TYPE[role] || 'info'
}

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    users.value = await getUsers()
    rules.value = await getRules()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function viewAccount(id) {
  router.push(`/account/${id}`)
}

function selectAccount(user) {
  earnForm.value.userId = user.id
  earnDialogVisible.value = true
}

function openEarnDialog() {
  earnForm.value = { userId: '', eventCode: '' }
  earnDialogVisible.value = true
}

async function handleEarn() {
  if (!earnForm.value.userId || !earnForm.value.eventCode) {
    ElMessage.warning('请选择用户和规则')
    return
  }
  earning.value = true
  try {
    await earnPoints(earnForm.value.userId, earnForm.value.eventCode)
    ElMessage.success('加分成功')
    earnDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '加分失败')
  } finally {
    earning.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.balance {
  font-weight: 600;
  color: #3b5bdb;
}
</style>
