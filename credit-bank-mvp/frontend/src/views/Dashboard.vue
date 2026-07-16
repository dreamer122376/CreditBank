<template>
  <div class="dashboard" v-loading="loading">
    <div class="dashboard-header">
      <div class="header-left">
        <h1 class="page-title">工作台</h1>
        <p class="page-subtitle">欢迎回来，{{ currentUser?.realName || '用户' }}</p>
      </div>
      <div class="header-right">
        <span class="update-time">数据更新于 {{ updateTimeText }}</span>
        <el-button :icon="Refresh" circle size="small" @click="refreshData" :loading="refreshing" />
      </div>
    </div>

    <div class="dashboard-content">
      <!-- 首屏重点卡片 -->
      <HeroCard :role="currentUser?.role" :data="heroData" @click="handleHeroClick" />

      <!-- 核心指标 -->
      <div class="stats-grid">
        <StatCard
          v-for="stat in currentStats"
          :key="stat.label"
          :icon="stat.icon"
          :label="stat.label"
          :value="stat.value"
          :color="stat.color"
          :trend="stat.trend"
        />
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- Admin 大屏：月度趋势 + 角色分布 -->
        <template v-if="currentUser?.role === 'admin' && dashboardData">
          <div class="chart-panel wide">
            <div class="panel-header">
              <span class="panel-title">月度积分趋势</span>
            </div>
            <div ref="barChartRef" class="chart-area"></div>
            <EmptyState v-if="!monthlyTrendData.length" text="暂无月度趋势数据" />
          </div>
          <div class="chart-panel">
            <div class="panel-header">
              <span class="panel-title">用户角色分布</span>
            </div>
            <div ref="pieChartRef" class="chart-area"></div>
            <EmptyState v-if="!categoryData.length" text="暂无角色分布数据" />
          </div>
        </template>

        <!-- 非 Admin：折线图 + 辅助信息 -->
        <template v-if="currentUser?.role === 'student'">
          <div class="chart-panel wide">
            <div class="panel-header">
              <span class="panel-title">积分趋势</span>
              <div class="panel-tabs">
                <span :class="{ active: trendDays === 7 }" @click="switchTrend(7)">近7天</span>
                <span :class="{ active: trendDays === 30 }" @click="switchTrend(30)">近30天</span>
              </div>
            </div>
            <div ref="chartRef" class="chart-area"></div>
            <EmptyState v-if="!pointTrend.length" text="暂无积分趋势数据" />
          </div>
          <div class="side-stack">
            <div class="signin-panel">
              <div class="signin-status">
                <div class="signin-icon" :class="{ signed: signInStatus.hasSignedIn }">
                  <el-icon size="28"><Calendar v-if="!signInStatus.hasSignedIn" /><Check v-else /></el-icon>
                </div>
                <div class="signin-text">
                  <div class="signin-title">{{ signInStatus.hasSignedIn ? '今日已签到' : '今日未签到' }}</div>
                  <div class="signin-desc">连续签到 {{ signInStatus.streak }} 天</div>
                </div>
              </div>
              <el-button
                v-if="!signInStatus.hasSignedIn"
                type="primary"
                class="signin-btn"
                :loading="signingIn"
                @click="handleSignIn"
              >
                立即打卡
              </el-button>
            </div>
            <div class="info-panel">
              <div class="panel-header">
                <span class="panel-title">最近交易</span>
                <span class="panel-more" @click="router.push('/transactions')">全部</span>
              </div>
              <div class="list-content">
                <div class="list-item" v-for="item in filteredTransactions" :key="item.id">
                  <span class="item-name">{{ getBizTypeName(item.bizType) }}</span>
                  <span class="item-amount" :class="Number(item.amount) > 0 ? 'gain' : 'loss'">
                    {{ Number(item.amount) > 0 ? '+' : '' }}{{ item.amount }}
                  </span>
                </div>
                <EmptyState v-if="filteredTransactions.length === 0" text="暂无交易记录" />
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 非学生端：底部列表 -->
      <div v-if="currentUser?.role !== 'student'" class="bottom-section">
        <div class="info-panel">
          <div class="panel-header">
            <span class="panel-title">待办事项</span>
          </div>
          <div class="list-content">
            <div class="list-item" v-for="item in todoList" :key="item.id">
              <span class="item-name">{{ item.typeName }}</span>
              <span class="item-tag" :class="item.statusType">{{ item.status }}</span>
            </div>
            <EmptyState v-if="todoList.length === 0" text="暂无待办事项" />
          </div>
        </div>
        <div class="info-panel">
          <div class="panel-header">
            <span class="panel-title">最近交易</span>
            <span class="panel-more" @click="router.push('/transactions')">全部</span>
          </div>
          <div class="list-content">
            <div class="list-item" v-for="item in filteredTransactions" :key="item.id">
              <span class="item-name">{{ item.userName ? item.userName + ' · ' : '' }}{{ getBizTypeName(item.bizType) }}</span>
              <span class="item-amount" :class="Number(item.amount) > 0 ? 'gain' : 'loss'">
                {{ Number(item.amount) > 0 ? '+' : '' }}{{ item.amount }}
              </span>
            </div>
            <EmptyState v-if="filteredTransactions.length === 0" text="暂无交易记录" />
          </div>
        </div>
      </div>

      <!-- Admin 大屏：同比表格 -->
      <div v-if="currentUser?.role === 'admin' && dashboardData" class="yoy-panel">
        <div class="panel-header">
          <span class="panel-title">核心指标同比</span>
        </div>
        <div class="yoy-grid">
          <div class="yoy-item" v-for="item in dashboardData.yoy" :key="item.name">
            <div class="yoy-name">{{ item.name }}</div>
            <div class="yoy-values">
              <div class="yoy-box">
                <span class="yoy-label">去年</span>
                <span class="yoy-value">{{ formatNumber(item.lastYear) }}</span>
              </div>
              <div class="yoy-box">
                <span class="yoy-label">今年</span>
                <span class="yoy-value current">{{ formatNumber(item.thisYear) }}</span>
              </div>
              <div class="yoy-box">
                <span class="yoy-label">增长</span>
                <span class="yoy-value" :class="item.growth >= 0 ? 'up' : 'down'">
                  {{ item.growthRate }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <EmptyState v-if="!dashboardData.yoy?.length" text="暂无同比数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh, Calendar, Check, User, OfficeBuilding, Coin,
  Trophy, DocumentChecked, Collection, DataLine, School
} from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import {
  getStatsSummary, getTodoList, getRecentTransactions,
  getPointTrend, getDashboardData
} from '@/api/stats'
import { getProfile } from '@/api/profile'
import { signIn, getSignInStatus } from '@/api/signin'
import StatCard from '@/components/StatCard.vue'
import HeroCard from '@/components/HeroCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import * as echarts from 'echarts'

const { currentUser } = useAuth()
const router = useRouter()
const summary = ref(null)
const todoList = ref([])
const recentTransactions = ref([])
const trendDays = ref(7)
const pointTrend = ref([])
const dashboardData = ref(null)
const chartRef = ref(null)
const barChartRef = ref(null)
const pieChartRef = ref(null)
let chartInstance = null
let barChartInstance = null
let pieChartInstance = null

const loading = ref(true)
const refreshing = ref(false)
const signInStatus = ref({ hasSignedIn: false, streak: 0 })
const signingIn = ref(false)
const updateTime = ref(new Date())

const updateTimeText = computed(() => {
  const d = updateTime.value
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
})

const heroData = computed(() => {
  const role = currentUser.value?.role
  const s = summary.value || {}
  const map = {
    admin: {
      icon: DocumentChecked,
      label: '待审核事项',
      value: s.pendingCount || 0,
      desc: '需要您及时处理的审核申请',
      action: '去处理',
      color: 'red',
      path: '/applications'
    },
    org_admin: {
      icon: Coin,
      label: '机构积分池余额',
      value: currentUser.value?.balance || 0,
      desc: currentUser.value?.orgName ? `所属机构：${currentUser.value.orgName}` : '机构积分用于给学生发放奖励',
      action: '查看明细',
      color: 'orange',
      path: '/transactions'
    },
    student: {
      icon: Trophy,
      label: '我的积分余额',
      value: currentUser.value?.balance || 0,
      desc: '参与项目、完成任务可获得积分',
      action: '去赚积分',
      color: 'blue',
      path: '/projects'
    },
    expert: {
      icon: School,
      label: '待评审项目',
      value: s.pendingCount || 0,
      desc: '需要您评审的项目申请',
      action: '去评审',
      color: 'purple',
      path: '/projects/manage'
    }
  }
  return map[role] || map.student
})

const currentStats = computed(() => {
  if (!summary.value) return []
  const role = currentUser.value?.role
  const s = summary.value
  const stats = {
    admin: [
      { icon: User, label: '平台总用户数', value: s.totalUsers || 0, color: 'blue', trend: 12 },
      { icon: OfficeBuilding, label: '入驻机构数', value: s.totalOrgs || 0, color: 'cyan', trend: 5 },
      { icon: Coin, label: '全平台积分总量', value: s.totalCredit || 0, color: 'orange', trend: 8 },
      { icon: Collection, label: '积分发放次数', value: s.rewardCount || 0, color: 'green', trend: 15 }
    ],
    org_admin: [
      { icon: User, label: '本机构学生数', value: s.orgStudentCount || 0, color: 'blue', trend: 3 },
      { icon: Coin, label: '机构积分池余额', value: currentUser.value?.balance || 0, color: 'orange' },
      { icon: Trophy, label: '本机构项目数', value: s.orgProjectCount || 0, color: 'purple', trend: 6 },
      { icon: DocumentChecked, label: '待审核申请', value: s.pendingCount || 0, color: 'red' }
    ],
    student: [
      { icon: Coin, label: '我的积分余额', value: currentUser.value?.balance || 0, color: 'orange' },
      { icon: Trophy, label: '参与项目数', value: s.joinedProjectCount || 0, color: 'blue', trend: 10 },
      { icon: DocumentChecked, label: '待处理申请', value: s.pendingCount || 0, color: 'purple' },
      { icon: DataLine, label: '本月积分变动', value: s.monthPointChange || 0, color: 'green', trend: 5 }
    ],
    expert: [
      { icon: Coin, label: '评审积分', value: currentUser.value?.balance || 0, color: 'orange' },
      { icon: School, label: '待评审项目', value: s.pendingCount || 0, color: 'red' },
      { icon: Trophy, label: '已评审项目', value: s.reviewedCount || 0, color: 'blue', trend: 8 },
      { icon: User, label: '平台总用户数', value: s.totalUsers || 0, color: 'cyan' }
    ]
  }
  return stats[role] || stats.student
})

const monthlyTrendData = computed(() => dashboardData.value?.monthlyTrend || [])
const categoryData = computed(() => dashboardData.value?.categories || [])

function getBizTypeName(bizType) {
  const map = { REWARD: '奖励', EXCHANGE: '兑换', REFUND: '撤销', ADMIN: '管理员操作', ATTACHMENT: '附加流水', DAILY: '每日打卡' }
  return map[bizType] || bizType
}

function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const filteredTransactions = computed(() => {
  return recentTransactions.value.filter(item => item.bizType !== 'DAILY' && item.bizType !== 'ATTACHMENT')
})

function handleHeroClick(path) {
  if (path) router.push(path)
}

async function refreshData() {
  refreshing.value = true
  await loadAllData()
  updateTime.value = new Date()
  refreshing.value = false
}

async function switchTrend(days) {
  trendDays.value = days
  await loadPointTrend()
}

onMounted(async () => {
  await loadAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

function handleResize() {
  chartInstance?.resize()
  barChartInstance?.resize()
  pieChartInstance?.resize()
}

function disposeCharts() {
  chartInstance?.dispose(); chartInstance = null
  barChartInstance?.dispose(); barChartInstance = null
  pieChartInstance?.dispose(); pieChartInstance = null
}

async function loadAllData() {
  loading.value = true
  const role = currentUser.value?.role
  const userId = currentUser.value?.id
  try {
    const promises = [
      getStatsSummary(role, userId),
      getTodoList(role, userId, 5),
      getRecentTransactions(userId, role, role === 'admin' ? 10 : 5),
      userId ? getProfile(userId) : Promise.resolve(null)
    ]
    if (role === 'student' && userId) {
      promises.push(getPointTrend(userId, trendDays.value))
      promises.push(getSignInStatus())
    }
    if (role === 'admin') {
      promises.push(getDashboardData())
    }
    const results = await Promise.all(promises)
    summary.value = results[0]
    todoList.value = results[1]
    recentTransactions.value = results[2]
    if (results[3]?.orgName) {
      currentUser.value = { ...currentUser.value, orgName: results[3].orgName }
      localStorage.setItem('cb_user', JSON.stringify(currentUser.value))
    }

    let idx = 4
    if (role === 'student') {
      pointTrend.value = results[idx]
      signInStatus.value = results[idx + 1] || { hasSignedIn: false, streak: 0 }
      idx += 2
    }
    if (role === 'admin') {
      dashboardData.value = results[idx]
    }

    await nextTick()
    initCharts()
  } catch (e) {
    console.error('加载失败:', e)
  } finally {
    loading.value = false
  }
}

async function loadPointTrend() {
  const userId = currentUser.value?.id
  if (!userId) return
  try {
    pointTrend.value = await getPointTrend(userId, trendDays.value)
    await nextTick()
    updateChart()
  } catch (e) {
    console.error('加载趋势失败:', e)
  }
}

async function handleSignIn() {
  signingIn.value = true
  try {
    const result = await signIn()
    ElMessage.success(result.message || '签到成功')
    signInStatus.value = { hasSignedIn: true, streak: result.streak || signInStatus.value.streak + 1 }
    currentUser.value = { ...currentUser.value, balance: result.newBalance }
    localStorage.setItem('cb_user', JSON.stringify(currentUser.value))
  } catch (e) {
    ElMessage.error(e.message || '签到失败')
  } finally {
    signingIn.value = false
  }
}

function initCharts() {
  if (currentUser.value?.role === 'student') initChart()
  if (currentUser.value?.role === 'admin') {
    initBarChart()
    initPieChart()
  }
}

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value, 'dark')
  updateChart()
}

function updateChart() {
  if (!chartInstance || !pointTrend.value.length) return
  chartInstance.setOption({
    backgroundColor: 'transparent',
    animationDurationUpdate: 400,
    animationEasingUpdate: 'cubicInOut',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      borderColor: 'rgba(59, 130, 246, 0.3)',
      textStyle: { color: '#e2e8f0' }
    },
    grid: { left: '3%', right: '4%', bottom: '6%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: pointTrend.value.map(i => i.date),
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11, rotate: trendDays.value === 30 ? 30 : 0 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: pointTrend.value.map(i => i.balance),
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 130, 246, 0.35)' },
          { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
        ])
      },
      lineStyle: { color: '#60a5fa', width: 3 },
      itemStyle: { color: '#60a5fa', borderWidth: 2, borderColor: '#0f172a' },
      symbol: 'circle',
      symbolSize: 6
    }]
  }, true)
}

function initBarChart() {
  if (!barChartRef.value) return
  barChartInstance = echarts.init(barChartRef.value, 'dark')
  if (!monthlyTrendData.value.length) return
  barChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      borderColor: 'rgba(59, 130, 246, 0.3)',
      textStyle: { color: '#e2e8f0' }
    },
    legend: { textStyle: { color: '#94a3b8' }, top: 0 },
    grid: { left: '3%', right: '4%', bottom: '6%', top: '12%', containLabel: true },
    xAxis: {
      type: 'category',
      data: monthlyTrendData.value.map(i => i.month?.slice(5) || ''),
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [
      {
        name: '积分发放',
        type: 'bar',
        data: monthlyTrendData.value.map(i => i.earn || 0),
        itemStyle: { color: '#3b82f6', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '积分兑换',
        type: 'bar',
        data: monthlyTrendData.value.map(i => i.exchange || 0),
        itemStyle: { color: '#10b981', borderRadius: [4, 4, 0, 0] }
      }
    ]
  }, true)
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChartInstance = echarts.init(pieChartRef.value, 'dark')
  if (!categoryData.value.length) return
  const colors = ['#3b82f6', '#f59e0b', '#8b5cf6', '#10b981']
  pieChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(15, 23, 42, 0.95)',
      borderColor: 'rgba(59, 130, 246, 0.3)',
      textStyle: { color: '#e2e8f0' }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#0f172a', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#f1f5f9' } },
      data: categoryData.value.map((item, idx) => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: colors[idx % colors.length] }
      }))
    }]
  }, true)
}
</script>

<style scoped>
.dashboard {
  min-height: 100%;
  background: radial-gradient(ellipse at top, #1e293b 0%, #0f172a 50%, #020617 100%);
  padding: 24px;
  color: #e2e8f0;
}

.dashboard-header {
  max-width: 1600px;
  margin: 0 auto 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #f8fafc;
}

.page-subtitle {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-time {
  font-size: 12px;
  color: #64748b;
}

.dashboard-content {
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.charts-section:has(> .wide:only-child) {
  grid-template-columns: 1fr;
}

.side-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-panel,
.info-panel,
.signin-panel,
.yoy-panel {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(8px);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #f1f5f9;
}

.panel-tabs {
  display: flex;
  gap: 4px;
  background: rgba(15, 23, 42, 0.5);
  padding: 4px;
  border-radius: 8px;
}

.panel-tabs span {
  font-size: 12px;
  color: #94a3b8;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.panel-tabs span.active {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  font-weight: 500;
}

.chart-area {
  width: 100%;
  height: 280px;
}

.signin-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.signin-status {
  display: flex;
  align-items: center;
  gap: 14px;
}

.signin-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(245, 158, 11, 0.12);
  color: #fbbf24;
  flex-shrink: 0;
}

.signin-icon.signed {
  background: rgba(16, 185, 129, 0.12);
  color: #34d399;
}

.signin-title {
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
  margin-bottom: 4px;
}

.signin-desc {
  font-size: 12px;
  color: #94a3b8;
}

.signin-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border: none;
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 600;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel-more {
  font-size: 12px;
  color: #60a5fa;
  cursor: pointer;
  transition: color 0.2s;
}

.panel-more:hover {
  color: #93c5fd;
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 11px 12px;
  background: rgba(15, 23, 42, 0.4);
  border-radius: 10px;
  transition: background 0.2s;
}

.list-item:hover {
  background: rgba(15, 23, 42, 0.6);
}

.item-name {
  font-size: 13px;
  color: #cbd5e1;
}

.item-tag {
  font-size: 11px;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 4px;
}

.item-tag.success { background: rgba(16, 185, 129, 0.12); color: #34d399; }
.item-tag.warning { background: rgba(245, 158, 11, 0.12); color: #fbbf24; }
.item-tag.danger { background: rgba(239, 68, 68, 0.12); color: #f87171; }
.item-tag.info { background: rgba(59, 130, 246, 0.12); color: #60a5fa; }

.item-amount {
  font-size: 13px;
  font-weight: 600;
}

.item-amount.gain { color: #34d399; }
.item-amount.loss { color: #f87171; }

.yoy-panel {
  padding: 20px;
}

.yoy-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.yoy-item {
  background: rgba(15, 23, 42, 0.4);
  border-radius: 12px;
  padding: 16px;
}

.yoy-name {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 12px;
}

.yoy-values {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.yoy-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.yoy-label {
  font-size: 11px;
  color: #64748b;
}

.yoy-value {
  font-size: 16px;
  font-weight: 700;
  color: #e2e8f0;
}

.yoy-value.current {
  color: #60a5fa;
}

.yoy-value.up {
  color: #34d399;
}

.yoy-value.down {
  color: #f87171;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-section,
  .bottom-section,
  .yoy-grid {
    grid-template-columns: 1fr;
  }
  .yoy-values {
    flex-direction: row;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .signin-panel {
    flex-direction: column;
    align-items: flex-start;
  }
  .yoy-values {
    flex-direction: column;
  }
}
</style>
