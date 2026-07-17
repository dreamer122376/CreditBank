<template>
  <div class="transactions">
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">{{ pageTitle }}</span>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleExport">📥 导出CSV</el-button>
      </div>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-form :inline="true" :model="filters" size="small">
          <el-form-item label="用户ID" v-if="showUserIdFilter">
            <el-input v-model.number="filters.userId" placeholder="输入用户ID" style="width:160px;" />
          </el-form-item>
          <el-form-item label="业务类型">
            <el-select v-model="filters.bizType" placeholder="全部" style="width:140px;">
              <el-option label="全部" value="" />
              <el-option label="奖励积分" value="REWARD" />
              <el-option label="积分兑换" value="EXCHANGE" />
              <el-option label="报名项目" value="ENROLL" />
              <el-option label="撤销记录" value="REFUND" />
              <el-option label="每日打卡" value="DAILY" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:280px;" value-format="YYYY-MM-DDTHH:mm:ss" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="list" border style="width: 100%;" v-loading="loading" :size="isAdmin ? 'small' : 'default'">
        <el-table-column prop="id" label="流水ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="65" v-if="showUserIdFilter" />
        <el-table-column prop="userName" label="用户" width="80" v-if="showUserIdFilter" />
        <el-table-column prop="amount" label="金额" width="80">
          <template #default="scope">
            <span :class="scope.row.amount > 0 ? 'text-success' : 'text-danger'">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="70" />
        <el-table-column prop="bizType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getBizType(scope.row.bizType)" size="small">{{ getBizTypeName(scope.row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="150">
          <template #default="scope">
            <span v-if="scope.row.createdAt">{{ fmt(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" v-if="isAdmin">
          <template #default="scope">
            <span v-if="scope.row.bizType === 'REFUND' || scope.row.bizType === 'UPDATE_ADJUST'" style="color:#868e96;font-size:12px;">不可撤</span>
            <span v-else-if="scope.row.reverted" style="color:#868e96;font-size:12px;">已撤销</span>
            <span v-else-if="scope.row.bizType === 'ATTACHMENT'" style="color:#868e96;font-size:12px;">不可撤</span>
            <el-button v-else size="small" type="danger" plain @click="openRevertConfirm(scope.row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && list.length === 0" style="text-align:center;padding:60px;color:#868e96;">
        <div style="font-size:48px;margin-bottom:16px;">📭</div>
        <div>暂无交易记录</div>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination background layout="prev, pager, next, jumper, ->, total" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="revertVisible" title="确认撤销流水" width="480px" :close-on-click-modal="false">
      <div v-if="revertItem">
        <div style="margin-bottom:16px;">
          <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
            <span style="color:#868e96;">流水ID</span>
            <span>{{ revertItem.id }}</span>
          </div>
          <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
            <span style="color:#868e96;">用户ID</span>
            <span>{{ revertItem.userId }}</span>
          </div>
          <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
            <span style="color:#868e96;">原金额</span>
            <span :class="revertItem.amount > 0 ? 'text-success' : 'text-danger'" style="font-weight:600;">
              {{ revertItem.amount > 0 ? '+' : '' }}{{ revertItem.amount }}
            </span>
          </div>
          <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
            <span style="color:#868e96;">描述</span>
            <span>{{ revertItem.description }}</span>
          </div>
        </div>
        <div style="background:#fff3cd;padding:12px;border-radius:4px;margin-bottom:16px;">
          <div style="color:#856404;font-weight:600;margin-bottom:4px;">撤销后将：</div>
          <div style="color:#856404;font-size:13px;">
            <ul style="margin:0;padding-left:20px;">
              <li>生成一条反向流水，金额为 <span :class="-revertItem.amount > 0 ? 'text-success' : 'text-danger'">{{ -revertItem.amount > 0 ? '+' : '' }}{{-revertItem.amount}}</span></li>
              <li>用户余额将 {{ -revertItem.amount > 0 ? '增加' : '减少' }} {{ Math.abs(revertItem.amount) }} 积分</li>
              <li>原流水记录保持不变，不可再次撤销</li>
            </ul>
          </div>
        </div>
        <div style="color:#d93026;font-size:13px;">
          ⚠️ 此操作不可逆，请谨慎操作！
        </div>
      </div>
      <template #footer>
        <el-button @click="revertVisible = false">取消</el-button>
        <el-button type="danger" @click="handleRevert" :loading="reverting">确认撤销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTransactions, exportTransactions, revertTransaction } from '@/api/transaction'

const { currentUser } = useAuth()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filters = ref({
  userId: null,
  bizType: '',
  startTime: '',
  endTime: ''
})

const dateRange = ref([])

const revertVisible = ref(false)
const revertItem = ref(null)
const reverting = ref(false)

const BIZ_TYPE_MAP = {
  REWARD: { name: '奖励积分', type: 'success' },
  EXCHANGE: { name: '积分兑换', type: 'warning' },
  ENROLL: { name: '报名项目', type: 'info' },
  REFUND: { name: '撤销记录', type: 'danger' },
  ATTACHMENT: { name: '附加流水', type: 'primary' },
  UPDATE_ADJUST: { name: '更新补差', type: 'info' },
  ADMIN: { name: '手动修改', type: 'info' },
  DAILY: { name: '每日打卡', type: 'success' }
}

const showUserIdFilter = computed(() => {
  const role = currentUser.value?.role
  return role === 'admin' || role === 'org_admin'
})

const isAdmin = computed(() => {
  return currentUser.value?.role === 'admin'
})

const pageTitle = computed(() => {
  const role = currentUser.value?.role
  if (role === 'admin') return '交易管理'
  if (role === 'org_admin') return '积分流水'
  if (role === 'student') return '我的积分'
  return '交易管理'
})

watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    filters.value.startTime = newVal[0]
    filters.value.endTime = newVal[1]
  } else {
    filters.value.startTime = ''
    filters.value.endTime = ''
  }
})

onMounted(() => {
  initFilters()
  loadData()
})

function initFilters() {
  const role = currentUser.value?.role
  if (role === 'student') {
    filters.value.userId = currentUser.value?.id
  }
}

function getBizTypeName(type) {
  return BIZ_TYPE_MAP[type]?.name || type || '未知'
}

function getBizType(type) {
  return BIZ_TYPE_MAP[type]?.type || 'info'
}

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

function resetFilters() {
  filters.value = { userId: null, bizType: '', startTime: '', endTime: '' }
  dateRange.value = []
  initFilters()
  currentPage.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTransactions(currentPage.value, pageSize.value, filters.value)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  try {
    exportTransactions(filters.value)
  } catch (e) {
    ElMessage.error('导出失败：' + (e.message || '未知错误'))
  }
}

function openRevertConfirm(row) {
  revertItem.value = row
  revertVisible.value = true
}

async function handleRevert() {
  if (!revertItem.value) return
  reverting.value = true
  try {
    await revertTransaction(revertItem.value.id)
    ElMessage.success('撤销成功，已生成反向补差流水')
    revertVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '撤销失败')
  } finally {
    reverting.value = false
  }
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-title { font-size: 16px; font-weight: 600; color: #2c3e50; }
.filter-bar { margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.text-success { color: #0f6e56; font-weight: 600; }
.text-danger { color: #d93026; font-weight: 600; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>