<template>
  <div class="account-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账户详情</span>
          <el-button type="primary" @click="openEarnDialog">
            <el-icon><Plus /></el-icon>
            加分
          </el-button>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="账户ID">{{ account?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ account?.userName }}</el-descriptions-item>
        <el-descriptions-item label="余额">
          <span class="balance">{{ account?.balance }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ account?.version }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ account?.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ account?.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>积分流水</span>
      </template>
      <el-table :data="transactions" border style="width: 100%;">
        <el-table-column prop="id" label="流水ID" width="120" />
        <el-table-column prop="ruleCode" label="规则编码" />
        <el-table-column prop="changeAmount" label="变动积分">
          <template #default="scope">
            <span :style="{ color: Number(scope.row.changeAmount) > 0 ? '#0b7a4f' : '#e03131' }">
              {{ Number(scope.row.changeAmount) > 0 ? '+' : '' }}{{ scope.row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createdAt" label="创建时间" />
      </el-table>
      <div v-if="transactions.length === 0" style="text-align: center; padding: 40px;">
        暂无流水记录
      </div>
    </el-card>

    <el-dialog v-model="earnDialogVisible" title="积分加分" width="400px">
      <el-form :model="earnForm">
        <el-form-item label="账户">
          <el-input :value="account?.userName" disabled />
        </el-form-item>
        <el-form-item label="选择规则">
          <el-select v-model="earnForm.ruleCode" placeholder="请选择积分规则">
            <el-option v-for="rule in rules" :key="rule.ruleCode" :label="rule.ruleName + ' (' + rule.points + '分)'" :value="rule.ruleCode" />
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
import { getAccount, getTransactions, getRules, earnPoints } from '@/api/point'

const route = useRoute()

const account = ref(null)
const transactions = ref([])
const rules = ref([])
const earnDialogVisible = ref(false)
const earning = ref(false)

const earnForm = ref({
  ruleCode: ''
})

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
    account.value = await getAccount(id)
    transactions.value = await getTransactions(id)
    rules.value = await getRules()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

function openEarnDialog() {
  earnForm.value = { ruleCode: '' }
  earnDialogVisible.value = true
}

async function handleEarn() {
  if (!earnForm.value.ruleCode) {
    ElMessage.warning('请选择积分规则')
    return
  }
  earning.value = true
  try {
    await earnPoints(account.value.id, earnForm.value.ruleCode)
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
