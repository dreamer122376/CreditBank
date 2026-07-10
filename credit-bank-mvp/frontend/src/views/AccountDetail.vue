<template>
  <div class="account-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户详情</span>
          <el-button type="primary" @click="openEarnDialog">
            <el-icon><Plus /></el-icon>
            加分
          </el-button>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="用户ID">{{ user?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ user?.realName }}</el-descriptions-item>
        <el-descriptions-item label="积分余额">
          <span class="balance">{{ user?.balance }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="角色">{{ getRoleName(user?.role) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ user?.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ user?.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user?.status === 1 ? 'success' : 'danger'">
            {{ user?.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>积分流水</span>
      </template>
      <el-table :data="transactions" border style="width: 100%;">
        <el-table-column prop="id" label="流水ID" width="120" />
        <el-table-column prop="bizType" label="业务类型" width="120">
          <template #default="scope">
            <el-tag type="info">{{ getBizTypeName(scope.row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="变动积分">
          <template #default="scope">
            <span :style="{ color: Number(scope.row.amount) > 0 ? '#0b7a4f' : '#e03131' }">
              {{ Number(scope.row.amount) > 0 ? '+' : '' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间" />
      </el-table>
      <div v-if="transactions.length === 0" style="text-align: center; padding: 40px;">
        暂无流水记录
      </div>
    </el-card>

    <el-dialog v-model="earnDialogVisible" title="积分加分" width="400px">
      <el-form :model="earnForm">
        <el-form-item label="用户">
          <el-input :value="user?.username + ' (' + user?.realName + ')'" disabled />
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUser, getTransactions } from '@/api/user'
import { getRules, earnPoints } from '@/api/point'

const route = useRoute()

const user = ref(null)
const transactions = ref([])
const rules = ref([])
const earnDialogVisible = ref(false)
const earning = ref(false)

const earnForm = ref({
  eventCode: ''
})

const ROLE_NAME = {
  admin: '系统管理员',
  org_admin: '机构管理员',
  student: '学生',
  expert: '专家'
}

const BIZ_TYPE_NAME = {
  REWARD: '奖励',
  CONSUME: '消费',
  EXCHANGE: '兑换'
}

function getRoleName(role) {
  return ROLE_NAME[role] || role
}

function getBizTypeName(bizType) {
  return BIZ_TYPE_NAME[bizType] || bizType
}

onMounted(async () => {
  await loadData()
})

watch(() => route.params.id, async () => {
  await loadData()
})

async function loadData() {
  const id = route.params.id
  if (!id) return
  try {
    user.value = await getUser(id)
    transactions.value = await getTransactions(id)
    rules.value = await getRules()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

function openEarnDialog() {
  earnForm.value = { eventCode: '' }
  earnDialogVisible.value = true
}

async function handleEarn() {
  if (!earnForm.value.eventCode) {
    ElMessage.warning('请选择积分规则')
    return
  }
  earning.value = true
  try {
    await earnPoints(user.value.id, earnForm.value.eventCode)
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

.balance {
  font-weight: 600;
  color: #3b5bdb;
  font-size: 18px;
}
</style>
