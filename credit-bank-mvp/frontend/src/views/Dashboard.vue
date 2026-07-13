<template>
  <div class="dashboard">
    <div class="welcome">
      <h2>欢迎回来，{{ currentUser?.realName }}</h2>
      <p>{{ welcomeText }}</p>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="stat in statCards" :key="stat.label">
        <el-card class="stat-card" :class="stat.color">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}<small v-if="stat.unit">{{ stat.unit }}</small></div>
        </el-card>
      </el-col>
      <el-col :span="12" v-if="currentUser?.role === 'student'">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>积分变化趋势</span>
              <span>
                <el-switch v-model="trendDays" :active-value="30" :inactive-value="7" @change="loadPointTrend" />
                <span style="margin-left: 8px;">{{ trendDays }}天</span>
              </span>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
            </div>
          </template>
          <el-table :data="todoList" border style="width: 100%;">
            <el-table-column prop="typeName" label="类型" />
            <el-table-column prop="initiator" label="发起人" />
            <el-table-column prop="time" label="提交时间">
              <template #default="scope">
                {{ formatTime(scope.row.time) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.statusType">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="todoList.length === 0" style="text-align: center; padding: 30px; color: #868e96;">
            暂无待办事项
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近交易记录</span>
            </div>
          </template>
          <el-table :data="recentTransactions" border style="width: 100%;">
            <el-table-column prop="bizType" label="类型">
              <template #default="scope">
                {{ getBizTypeName(scope.row.bizType) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="变动">
              <template #default="scope">
                <span :style="{ color: Number(scope.row.amount) > 0 ? '#0b7a4f' : '#e03131' }">
                  {{ Number(scope.row.amount) > 0 ? '+' : '' }}{{ scope.row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="balanceAfter" label="余额" />
            <el-table-column prop="description" label="来源" />
            <el-table-column prop="createdAt" label="时间">
              <template #default="scope">
                {{ formatShortTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="recentTransactions.length === 0" style="text-align: center; padding: 30px; color: #868e96;">
            暂无交易记录
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useAuth } from '@/composables/useAuth';
import { getStatsSummary, getTodoList, getRecentTransactions, getPointTrend } from '@/api/stats';
import { getProfile } from '@/api/profile';
import * as echarts from 'echarts';
const { currentUser } = useAuth();
const summary = ref(null);
const todoList = ref([]);
const recentTransactions = ref([]);
const trendDays = ref(7);
const pointTrend = ref([]);
const chartRef = ref(null);
let chartInstance = null;

const welcomeText = computed(() => {
  const texts = {
    admin: '系统管理员负责全局配置与监管，包括机构审核、规则定义、全局对账等核心职能。',
    org_admin: '机构管理员负责本机构的专属规则、项目发布、活动管理、专家聘用及积分池流水查看。',
    student: '在这里查看积分余额、参与项目活动、申请学分转换，记录你的终身学习成长轨迹。',
    expert: '专家可查看聘用机构、评审指派的项目、维护个人资料与研究方向。'
  }
  return texts[currentUser.value?.role] || ''
})

const statCards = computed(() => {
  if (!summary.value) return []
  const role = currentUser.value?.role
  const s = summary.value
  const cards = {
    admin: [
      { label: '平台总用户数', value: formatNumber(s.totalUsers), unit: '人', color: '' },
      { label: '入驻机构数', value: formatNumber(s.totalOrgs), unit: '家', color: 'green' },
      { label: '待审核事项', value: formatNumber(s.pendingCount), unit: '条', color: 'orange' },
      { label: '全平台积分总量', value: formatNumber(s.totalCredit), unit: '', color: 'red' }
    ],
    org_admin: [
      { label: '机构积分池余额', value: formatNumber(currentUser.value?.balance || 0), unit: '', color: 'green' },
      { label: '所属机构', value: currentUser.value?.orgName || '未绑定机构', unit: '', color: '' },
      { label: '平台总用户数', value: formatNumber(s.totalUsers), unit: '人', color: 'orange' },
      { label: '待审核申请', value: formatNumber(s.pendingCount), unit: '条', color: 'red' }
    ],
    student: [
      { label: '我的积分余额', value: formatNumber(currentUser.value?.balance || 0), unit: '', color: '' },
      { label: '待处理申请', value: formatNumber(s.pendingCount), unit: '条', color: 'red' }
    ],
    expert: [
      { label: '评审积分', value: formatNumber(currentUser.value?.balance || 0), unit: '', color: 'red' },
      { label: '入驻机构数', value: formatNumber(s.totalOrgs), unit: '家', color: 'orange' },
      { label: '平台总用户数', value: formatNumber(s.totalUsers), unit: '人', color: 'green' },
      { label: '待评审项目', value: formatNumber(s.pendingCount), unit: '个', color: '' }
    ]
  }
  return cards[role] || []
})

function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

function formatShortTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}-${day} ${h}:${min}`
}

function getBizTypeName(bizType) {
  const map = {
    REWARD: '奖励',
    EXCHANGE: '兑换',
    REFUND: '退款',
    ADMIN: '管理员操作'
  }
  return map[bizType] || bizType
}

onMounted(async () => {
  await loadAllData()
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

async function loadAllData() {
  const role = currentUser.value?.role
  const userId = currentUser.value?.id
  try {
    const promises = [
      getStatsSummary(role, userId),
      getTodoList(role, userId, 4),
      getRecentTransactions(userId, 4),
      userId ? getProfile(userId) : Promise.resolve(null)
    ]
    if (role === 'student' && userId) {
      promises.push(getPointTrend(userId, trendDays.value))
    }
    const results = await Promise.all(promises)
    summary.value = results[0]
    todoList.value = results[1]
    recentTransactions.value = results[2]
    if (results[3] && results[3].orgName) {
      currentUser.value = { ...currentUser.value, orgName: results[3].orgName }
      localStorage.setItem('cb_user', JSON.stringify(currentUser.value))
    }
    if (role === 'student') {
      pointTrend.value = results[4]
      await nextTick()
      initChart()
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

async function loadPointTrend() {
  const userId = currentUser.value?.id
  if (!userId) return
  try {
    pointTrend.value = await getPointTrend(userId, trendDays.value)
    await nextTick()
    updateChart()
  } catch (error) {
    console.error('加载积分趋势失败:', error)
  }
}

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

function updateChart() {
  if (!chartInstance || !pointTrend.value.length) return
  const dates = pointTrend.value.map(item => item.date)
  const balances = pointTrend.value.map(item => item.balance)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>积分余额: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: trendDays.value === 30 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      min: 0
    },
    series: [{
      name: '积分余额',
      type: 'line',
      smooth: true,
      data: balances,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 91, 219, 0.3)' },
          { offset: 1, color: 'rgba(59, 91, 219, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#3b5bdb',
        width: 2
      },
      itemStyle: {
        color: '#3b5bdb'
      }
    }]
  }
  chartInstance.setOption(option)
}
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome {
  background: linear-gradient(135deg, #3b5bdb 0%, #5c7bf5 100%);
  color: #fff;
  border-radius: 10px;
  padding: 22px 26px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(59, 91, 219, 0.25);
}

.welcome h2 {
  font-size: 20px;
  margin-bottom: 6px;
}

.welcome p {
  opacity: 0.9;
  font-size: 13px;
}

.stat-card {
  border-left: 4px solid #3b5bdb;
}

.stat-card.green {
  border-left-color: #0f6e56;
}

.stat-card.orange {
  border-left-color: #f59f00;
}

.stat-card.red {
  border-left-color: #e03131;
}

.stat-label {
  font-size: 13px;
  color: #868e96;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-value small {
  font-size: 14px;
  font-weight: normal;
  color: #868e96;
  margin-left: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 200px;
}
</style>
