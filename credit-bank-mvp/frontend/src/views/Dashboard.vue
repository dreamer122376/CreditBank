<template>
  <div class="dashboard" v-loading="loading">
    <div class="main-grid" v-if="currentUser?.role === 'student'">
      <div class="left-column">
        <div class="signin-card">
          <div class="signin-top">
            <span class="signin-greeting">欢迎回来</span>
            <span class="username">{{ currentUser?.realName }}</span>
          </div>
          <div class="date-section">
            <div class="date-left">
              <span class="date-month">{{ monthName }}月</span>
              <span class="date-type">{{ monthType }}</span>
            </div>
            <div class="date-center">
              <span class="date-day">{{ currentDay }}</span>
            </div>
            <div class="date-right">
              <span>星期</span>
              <span class="date-weekday">{{ weekdayChar }}</span>
            </div>
          </div>
          <div class="content-area">
            <div class="countdown-container" v-if="!signInStatus.hasSignedIn">
              <div class="countdown-item" v-for="exam in examList" :key="exam.name">
                <span class="countdown-label">{{ exam.name }}</span>
                <span class="countdown-value">{{ exam.days }}天</span>
              </div>
            </div>
            <div class="fortune-container" v-else-if="fortune">
              <div class="fortune-badge" :class="fortune.level">
                <span>{{ fortune.levelText }}</span>
              </div>
              <div class="fortune-grid">
                <div class="fortune-column yi">
                  <span class="fortune-header">宜</span>
                  <span v-for="(item, idx) in fortune.yi" :key="'yi-' + idx" class="fortune-item">{{ item }}</span>
                </div>
                <div class="fortune-column ji">
                  <span class="fortune-header">忌</span>
                  <span v-for="(item, idx) in fortune.ji" :key="'ji-' + idx" class="fortune-item">{{ item }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="signin-bottom">
            <span v-if="signInStatus.streak > 0" class="streak-badge">🔥 {{ signInStatus.streak }}天</span>
            <el-button v-if="!signInStatus.hasSignedIn" type="primary" class="signin-btn" :loading="signingIn" @click="handleSignIn">
              点击打卡
            </el-button>
            <span v-else class="signed-text">今日已签到</span>
          </div>
        </div>
        <div class="stats-card">
          <div class="stat-item" v-for="stat in statCards" :key="stat.label">
            <span class="stat-number">{{ stat.value }}</span>
            <span class="stat-name">{{ stat.label }}</span>
          </div>
        </div>
      </div>
      <div class="right-column">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">积分趋势</span>
            <div class="card-tabs">
              <span :class="{ active: trendDays === 7 }" @click="trendDays = 7; loadPointTrend()">7天</span>
              <span :class="{ active: trendDays === 30 }" @click="trendDays = 30; loadPointTrend()">30天</span>
            </div>
          </div>
          <div ref="chartRef" class="chart-area"></div>
        </div>
        <div class="bottom-cards">
          <div class="info-card">
            <div class="card-header">
              <span class="card-title">待办事项</span>
            </div>
            <div class="list-content">
              <div class="list-item" v-for="item in todoList" :key="item.id">
                <span class="item-name">{{ item.typeName }}</span>
                <span class="item-tag" :class="item.statusType">{{ item.status }}</span>
              </div>
              <div v-if="todoList.length === 0" class="empty-tip">暂无待办事项</div>
            </div>
          </div>
          <div class="info-card">
            <div class="card-header">
              <span class="card-title">最近交易</span>
              <span class="card-more" @click="router.push('/transactions')">全部></span>
            </div>
            <div class="list-content">
              <div class="list-item" v-for="item in filteredTransactions" :key="item.id">
                <span class="item-name">{{ getBizTypeName(item.bizType) }}</span>
                <span class="item-amount" :class="Number(item.amount) > 0 ? 'gain' : 'loss'">
                  {{ Number(item.amount) > 0 ? '+' : '' }}{{ item.amount }}
                </span>
              </div>
              <div v-if="filteredTransactions.length === 0" class="empty-tip">暂无交易记录</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="non-student" v-else>
        <!-- Hero: 待审核事项 -- 突出显示（仅管理员） -->
        <div class="pending-hero" v-if="currentUser?.role === 'admin'">
          <div class="pending-hero-inner">
            <div class="pending-hero-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="M12 8v4"/><path d="M12 16h.01"/></svg>
            </div>
            <div class="pending-hero-text">
              <div class="pending-hero-label">待审核事项</div>
              <div class="pending-hero-count">{{ summary?.pendingCount ?? 0 }}</div>
              <div class="pending-hero-hint">需要您及时处理的审核申请</div>
            </div>
          </div>
        </div>
        <!-- 核心指标：管理员用 secondaryStats（不含待审核），其他角色用 statCards -->
        <div class="stats-full">
          <div class="stat-item" v-for="stat in (currentUser?.role === 'admin' ? secondaryStats : statCards)" :key="stat.label">
            <span class="stat-number">{{ stat.value }}</span>
            <span class="stat-name">{{ stat.label }}</span>
          </div>
        </div>
        <div class="cards-full">
          <div class="info-card">
            <div class="card-header">
              <span class="card-title">待办事项</span>
            </div>
            <div class="list-content">
              <div class="list-item" v-for="item in todoList" :key="item.id">
                <span class="item-name">{{ item.typeName }}</span>
                <span class="item-tag" :class="item.statusType">{{ item.status }}</span>
              </div>
              <div v-if="todoList.length === 0" class="empty-tip">暂无待办事项</div>
            </div>
          </div>
          <div class="info-card">
            <div class="card-header">
              <span class="card-title">最近交易</span>
              <span class="card-more" @click="router.push('/transactions')">全部></span>
            </div>
            <div class="list-content">
              <div class="list-item" v-for="item in filteredTransactions" :key="item.id">
                <span class="item-name">{{ item.userName ? item.userName + ' · ' : '' }}{{ getBizTypeName(item.bizType) }}</span>
                <span class="item-amount" :class="Number(item.amount) > 0 ? 'gain' : 'loss'">
                  {{ Number(item.amount) > 0 ? '+' : '' }}{{ item.amount }}
                </span>
              </div>
              <div v-if="filteredTransactions.length === 0" class="empty-tip">暂无交易记录</div>
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getStatsSummary, getTodoList, getRecentTransactions, getPointTrend } from '@/api/stats'
import { getProfile } from '@/api/profile'
import { signIn, getSignInStatus } from '@/api/signin'
import * as echarts from 'echarts'

const { currentUser } = useAuth()
const router = useRouter()
const summary = ref(null)
const todoList = ref([])
const recentTransactions = ref([])
const trendDays = ref(7)
const pointTrend = ref([])
const chartRef = ref(null)
let chartInstance = null

const loading = ref(true)
const signInStatus = ref({ hasSignedIn: false, streak: 0 })
const signingIn = ref(false)
const fortune = ref(null)
const now = ref(new Date())

const currentMonth = computed(() => now.value.getMonth() + 1)
const currentDay = computed(() => now.value.getDate())

const monthName = computed(() => {
  const names = ['', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '十一', '十二']
  return names[currentMonth.value]
})

const monthType = computed(() => {
  const days = new Date(now.value.getFullYear(), currentMonth.value, 0).getDate()
  return days === 31 ? '大' : days === 30 ? '小' : ''
})

const weekdayChar = computed(() => {
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return days[now.value.getDay()]
})

const examList = computed(() => {
  const y = now.value.getFullYear()
  const m = currentMonth.value
  let cet = new Date(y, 5, 15)
  if (m > 6) cet = new Date(y + 1, 5, 15)
  let csp = new Date(y, 9, 20)
  if (m > 10) csp = new Date(y + 1, 9, 20)
  const dcet = Math.ceil((cet - now.value) / (1000 * 60 * 60 * 24))
  const dcsp = Math.ceil((csp - now.value) / (1000 * 60 * 60 * 24))
  return [
    { name: '四六级考试', days: dcet },
    { name: 'CSP认证考试', days: dcsp }
  ]
})

function generateFortune(userId = 0) {
  const d = new Date()
  const seed = (d.getFullYear() * 10000 + (d.getMonth() + 1) * 100 + d.getDate()) * 1000000 + (userId || 1)
  
  const levels = [
    { level: 'great-lucky', text: '大吉', w: 5 },
    { level: 'medium-lucky', text: '中吉', w: 15 },
    { level: 'small-lucky', text: '小吉', w: 25 },
    { level: 'peace', text: '中平', w: 30 },
    { level: 'small-bad', text: '小凶', w: 15 },
    { level: 'medium-bad', text: '中凶', w: 8 },
    { level: 'great-bad', text: '大凶', w: 2 }
  ]
  
  const yi = ['刷题', '学习', '写代码', '阅读', '复习', '考试', '参加比赛', '提交PR', '写博客', '锻炼身体', '早睡早起', '喝热水', '整理笔记', '背单词', '做实验', '讨论问题']
  const ji = ['摸鱼', '熬夜', '刷视频', '打游戏', '拖延', '逃课', '迟到', '吃零食', '玩手机', '不写作业', '不复习', '抄代码', '忘记保存', '不注释', '不测试']
  
  const rand = seededRandom(seed)
  const tw = levels.reduce((s, l) => s + l.w, 0)
  let rv = Math.floor(rand() * tw)
  let sel = levels[0]
  for (const l of levels) { if (rv < l.w) { sel = l; break } rv -= l.w }
  
  return {
    level: sel.level,
    levelText: sel.text,
    yi: shuffleArray([...yi], rand).slice(0, Math.floor(rand() * 2) + 1),
    ji: shuffleArray([...ji], rand).slice(0, Math.floor(rand() * 2) + 1)
  }
}

function seededRandom(seed) {
  let s = seed
  return () => { s = Math.sin(s) * 10000; return s - Math.floor(s) }
}

function shuffleArray(arr, rand) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(rand() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

const statCards = computed(() => {
  if (!summary.value) return []
  const role = currentUser.value?.role
  const s = summary.value
  const cards = {
    admin: [
      { label: '平台总用户数', value: formatNumber(s.totalUsers) },
      { label: '入驻机构数', value: formatNumber(s.totalOrgs) },
      { label: '全平台积分总量', value: formatNumber(s.totalCredit) }
    ],
    org_admin: [
      { label: '机构积分池余额', value: formatNumber(currentUser.value?.balance || 0) },
      { label: '所属机构', value: currentUser.value?.orgName || '未绑定' },
      { label: '平台总用户数', value: formatNumber(s.totalUsers) },
      { label: '待审核申请', value: formatNumber(s.pendingCount) }
    ],
    student: [
      { label: '我的积分余额', value: formatNumber(currentUser.value?.balance || 0) },
      { label: '待处理申请', value: formatNumber(s.pendingCount) }
    ],
    expert: [
      { label: '评审积分', value: formatNumber(currentUser.value?.balance || 0) },
      { label: '入驻机构数', value: formatNumber(s.totalOrgs) },
      { label: '平台总用户数', value: formatNumber(s.totalUsers) },
      { label: '待评审项目', value: formatNumber(s.pendingCount) }
    ]
  }
  return cards[role] || []
})

// admin 的次要统计（不含待审核事项，因为它在 Hero 区域展示）
const secondaryStats = computed(() => {
  if (!summary.value) return []
  const s = summary.value
  return [
    { label: '平台总用户数', value: formatNumber(s.totalUsers) },
    { label: '入驻机构数', value: formatNumber(s.totalOrgs) },
    { label: '全平台积分总量', value: formatNumber(s.totalCredit) }
  ]
})

function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function getBizTypeName(bizType) {
  const map = { REWARD: '奖励', EXCHANGE: '兑换', REFUND: '撤销', ADMIN: '管理员操作', ATTACHMENT: '附加流水', DAILY: '每日打卡' }
  return map[bizType] || bizType
}

const filteredTransactions = computed(() => {
  return recentTransactions.value.filter(item => item.bizType !== 'DAILY' && item.bizType !== 'ATTACHMENT')
})

onMounted(async () => {
  await loadAllData()
  window.addEventListener('resize', () => chartInstance?.resize())
})

onUnmounted(() => {
  window.removeEventListener('resize', () => chartInstance?.resize())
  if (chartInstance) { chartInstance.dispose(); chartInstance = null }
})

async function loadAllData() {
  loading.value = true
  const role = currentUser.value?.role
  const userId = currentUser.value?.id
  try {
    const promises = [getStatsSummary(role, userId), getTodoList(role, userId, 5), getRecentTransactions(userId, role, role === 'admin' ? 10 : 5), userId ? getProfile(userId) : Promise.resolve(null)]
    if (role === 'student' && userId) promises.push(getPointTrend(userId, trendDays.value), getSignInStatus())
    const results = await Promise.all(promises)
    summary.value = results[0]
    todoList.value = results[1]
    recentTransactions.value = results[2]
    if (results[3]?.orgName) {
      currentUser.value = { ...currentUser.value, orgName: results[3].orgName }
      localStorage.setItem('cb_user', JSON.stringify(currentUser.value))
    }
    if (role === 'student') {
      pointTrend.value = results[4]
      signInStatus.value = results[5] || { hasSignedIn: false, streak: 0 }
      if (signInStatus.value.hasSignedIn) fortune.value = generateFortune(currentUser.value?.id || 0)
      await nextTick()
      initChart()
    }
  } catch (e) { console.error('加载失败:', e) } finally {
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
  } catch (e) { console.error('加载趋势失败:', e) }
}

async function handleSignIn() {
  signingIn.value = true
  try {
    const result = await signIn()
    ElMessage.success(result.message || '签到成功')
    signInStatus.value = { hasSignedIn: true, streak: result.streak || signInStatus.value.streak + 1 }
    currentUser.value = { ...currentUser.value, balance: result.newBalance }
    localStorage.setItem('cb_user', JSON.stringify(currentUser.value))
    fortune.value = generateFortune(currentUser.value?.id || 0)
  } catch (e) {
    ElMessage.error(e.message || '签到失败')
  } finally {
    signingIn.value = false
  }
}

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

function updateChart() {
  if (!chartInstance || !pointTrend.value.length) return
  chartInstance.clear()
  chartInstance.setOption({
      animationDurationUpdate: 400,
      animationEasingUpdate: 'cubicInOut',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(59, 130, 246, 0.95)', borderColor: '#3b82f6', textStyle: { color: '#fff' } },
      grid: { left: '4%', right: '4%', bottom: '6%', top: '8%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: pointTrend.value.map(i => i.date), axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#6b7280', fontSize: 11, rotate: trendDays.value === 30 ? 30 : 0 } },
      yAxis: { type: 'value', min: 0, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f3f4f6' } }, axisLabel: { color: '#6b7280', fontSize: 11 } },
      series: [{
        type: 'line', smooth: true, data: pointTrend.value.map(i => i.balance),
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59, 130, 246, 0.15)' }, { offset: 1, color: 'rgba(59, 130, 246, 0)' }]) },
        lineStyle: { color: '#3b82f6', width: 2 },
        itemStyle: { color: '#3b82f6' },
        symbol: 'circle', symbolSize: 5
      }]
    })
}
</script>

<style scoped>
.dashboard {
  min-height: 100%;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 24px;
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.signin-card {
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(30, 58, 95, 0.08);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.signin-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #1e3a5f 0%, #3d5a8f 100%);
}

.signin-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.signin-greeting {
  font-size: 14px;
  color: #64748b;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a5f;
}

.date-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.date-left,
.date-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 14px;
  color: #64748b;
  line-height: 1.4;
}

.date-type {
  font-size: 12px;
  opacity: 0.6;
}

.date-center {
  font-size: 86px;
  font-weight: 800;
  color: #1e3a5f;
  line-height: 1;
  letter-spacing: -6px;
}

.date-weekday {
  font-weight: 600;
  color: #3d5a8f;
}

.content-area {
  margin-bottom: 20px;
}

.countdown-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.countdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 24px;
  background: #f8fafc;
  border-radius: 20px;
}

.countdown-label {
  font-size: 13px;
  color: #475569;
}

.countdown-value {
  font-size: 14px;
  font-weight: 600;
  color: #f97316;
  background: rgba(249, 115, 22, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
}

.fortune-container {
  text-align: center;
}

.fortune-badge {
  display: inline-block;
  padding: 6px 20px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
}

.fortune-badge.great-lucky { background: rgba(234, 88, 12, 0.1); color: #ea580c; }
.fortune-badge.medium-lucky { background: rgba(202, 138, 4, 0.1); color: #ca8a04; }
.fortune-badge.small-lucky { background: rgba(5, 150, 105, 0.1); color: #059669; }
.fortune-badge.peace { background: rgba(30, 58, 95, 0.1); color: #1e3a5f; }
.fortune-badge.small-bad { background: rgba(124, 58, 237, 0.1); color: #7c3aed; }
.fortune-badge.medium-bad { background: rgba(219, 39, 119, 0.1); color: #db2777; }
.fortune-badge.great-bad { background: rgba(220, 38, 38, 0.1); color: #dc2626; }

.fortune-grid {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.fortune-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: left;
}

.fortune-header {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.yi .fortune-header { color: #059669; }
.ji .fortune-header { color: #dc2626; }

.fortune-item {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
}

.yi .fortune-item { background: rgba(5, 150, 105, 0.08); color: #047857; }
.ji .fortune-item { background: rgba(220, 38, 38, 0.08); color: #991b1b; }

.signin-bottom {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.streak-badge {
  font-size: 13px;
  color: #f97316;
  font-weight: 500;
}

.signin-btn {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border: none;
  border-radius: 24px;
  padding: 12px 36px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.35);
  transition: all 0.3s ease;
}

.signin-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.45);
}

.signed-text {
  font-size: 14px;
  color: #059669;
  font-weight: 500;
}

.stats-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(30, 58, 95, 0.05);
  border: 1px solid #e2e8f0;
  display: flex;
  gap: 24px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #1e3a5f;
  margin-bottom: 4px;
}

.stat-name {
  font-size: 12px;
  color: #64748b;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(30, 58, 95, 0.05);
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e3a5f;
}

.card-more {
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
}

.card-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 8px;
}

.card-tabs span {
  font-size: 12px;
  color: #64748b;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.card-tabs span.active {
  background: #fff;
  color: #1e3a5f;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-area {
  width: 100%;
  height: 220px;
}

.bottom-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(30, 58, 95, 0.05);
  border: 1px solid #e2e8f0;
}

.info-card .card-header {
  margin-bottom: 14px;
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
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.item-name {
  font-size: 13px;
  color: #475569;
}

.item-tag {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
}

.item-tag.success { background: rgba(5, 150, 105, 0.1); color: #059669; }
.item-tag.warning { background: rgba(217, 119, 6, 0.1); color: #d97706; }
.item-tag.danger { background: rgba(220, 38, 38, 0.1); color: #dc2626; }
.item-tag.info { background: rgba(30, 58, 95, 0.1); color: #1e3a5f; }

.item-amount {
  font-size: 13px;
  font-weight: 600;
}

.item-amount.gain { color: #059669; }
.item-amount.loss { color: #dc2626; }

.empty-tip {
  text-align: center;
  padding: 16px;
  color: #94a3b8;
  font-size: 12px;
}

.non-student {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 待审核事项 Hero 卡片 */
.pending-hero {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(30, 58, 95, 0.08);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  position: relative;
}

.pending-hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #e11d48 0%, #fb7185 100%);
}

.pending-hero-inner {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 32px 36px;
}

.pending-hero-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #fef2f2 0%, #ffe4e6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e11d48;
  flex-shrink: 0;
}

.pending-hero-text {
  flex: 1;
}

.pending-hero-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.pending-hero-count {
  font-size: 56px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.1;
  letter-spacing: -3px;
}

.pending-hero-hint {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 6px;
}

.stats-full {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stats-full .stat-item {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(30, 58, 95, 0.05);
  border: 1px solid #e2e8f0;
}

.cards-full {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 1024px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
  .bottom-cards,
  .cards-full {
    grid-template-columns: 1fr;
  }
  .date-center {
    font-size: 64px;
  }
  .stats-card {
    flex-direction: column;
  }
  .pending-hero-inner {
    flex-direction: column;
    text-align: center;
    padding: 24px 20px;
  }
  .pending-hero-count {
    font-size: 42px;
  }
}
</style>