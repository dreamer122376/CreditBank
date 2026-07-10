<template>
  <div class="accounts">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账户列表</span>
          <el-button type="primary" @click="openEarnDialog">
            <el-icon><Plus /></el-icon>
            加分
          </el-button>
        </div>
      </template>
      <el-table :data="accounts" border style="width: 100%;">
        <el-table-column prop="id" label="账户ID" width="100" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="balance" label="余额">
          <template #default="scope">
            <span class="balance">{{ scope.row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column prop="updatedAt" label="更新时间" />
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
        <el-form-item label="选择账户">
          <el-select v-model="earnForm.accountId" placeholder="请选择账户">
            <el-option v-for="acc in accounts" :key="acc.id" :label="acc.userName" :value="acc.id" />
          </el-select>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAccounts, getRules, earnPoints } from '@/api/point'

const router = useRouter()

const accounts = ref([])
const rules = ref([])
const earnDialogVisible = ref(false)
const earning = ref(false)

const earnForm = ref({
  accountId: '',
  ruleCode: ''
})

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    accounts.value = await getAccounts()
    rules.value = await getRules()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

function viewAccount(id) {
  router.push(`/account/${id}`)
}

function selectAccount(account) {
  earnForm.value.accountId = account.id
  earnDialogVisible.value = true
}

function openEarnDialog() {
  earnForm.value = { accountId: '', ruleCode: '' }
  earnDialogVisible.value = true
}

async function handleEarn() {
  if (!earnForm.value.accountId || !earnForm.value.ruleCode) {
    ElMessage.warning('请选择账户和规则')
    return
  }
  earning.value = true
  try {
    await earnPoints(earnForm.value.accountId, earnForm.value.ruleCode)
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
}
</style>
