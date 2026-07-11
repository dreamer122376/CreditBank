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

      <el-table :data="list" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="流水ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">
            <span :class="scope.row.amount > 0 ? 'text-success' : 'text-danger'">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后余额" width="130" />
        <el-table-column prop="bizType" label="业务类型" width="120">
          <template #default="scope">
            <el-tag :type="getBizType(scope.row.bizType)" size="small">{{ getBizTypeName(scope.row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.createdAt">{{ fmt(scope.row.createdAt) }}</span>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage } from 'element-plus'
import { getTransactions, exportTransactions } from '@/api/transaction'

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

const BIZ_TYPE_MAP = {
  REWARD: { name: '奖励积分', type: 'success' },
  EXCHANGE: { name: '积分兑换', type: 'warning' },
  ENROLL: { name: '报名项目', type: 'info' }
}

const showUserIdFilter = computed(() => {
  const role = currentUser.value?.role
  return role === 'admin' || role === 'org_admin'
})

const pageTitle = computed(() => {
  const role = currentUser.value?.role
  if (role === 'admin') return '交易管理'
  if (role === 'org_admin') return '积分流水'
  if (role === 'student') return '我的钱包'
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
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-title { font-size: 16px; font-weight: 600; color: #2c3e50; }
.filter-bar { margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.text-success { color: #0f6e56; font-weight: 600; }
.text-danger { color: #d93026; font-weight: 600; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>