<template>
  <div class="dashboard" v-loading="loading">
    <!-- 标题栏 -->
    <header class="dash-header">
      <h1>学分银行数据看板</h1>
      <p>Credit Bank &middot; National Overview</p>
    </header>

    <!-- KPI 卡片行 -->
    <section class="kpi-row">
      <div class="kpi-card" v-for="(kpi, i) in kpiList" :key="kpi.label" :style="{ animationDelay: (i * 0.1) + 's' }">
        <div class="kpi-accent" :style="{ background: kpi.color }"></div>
        <div class="kpi-body">
          <span class="kpi-num">{{ formatNum(kpi.value) }}</span>
          <span class="kpi-label">{{ kpi.label }}</span>
        </div>
      </div>
    </section>

    <!-- 三栏：左侧统计 + 地图(60%) + 右侧统计 -->
    <section class="map-row">
      <!-- 左侧统计面板 -->
      <div class="side-panel left-panel">
        <h3 class="side-title">用户构成</h3>
        <div class="side-stats">
          <div class="side-stat" v-for="item in leftStats" :key="item.label">
            <span class="side-stat-num" :style="{ color: item.color }">{{ formatNum(item.value) }}</span>
            <span class="side-stat-label">{{ item.label }}</span>
            <div class="side-stat-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
          </div>
        </div>
      </div>

      <!-- 中间地图 -->
      <div class="map-hero">
        <div ref="mapRef" class="map-chart"></div>
      </div>

      <!-- 右侧统计面板 -->
      <div class="side-panel right-panel">
        <h3 class="side-title">运营总览</h3>
        <div class="side-stats">
          <div class="side-stat compact" v-for="item in rightStats" :key="item.label">
            <span class="side-stat-num" :style="{ color: item.color }">{{ item.display || formatNum(item.value) }}</span>
            <span class="side-stat-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 底排双栏：同比对比 + 分类饼图 -->
    <section class="dual-row">
      <div class="panel yoy-panel">
        <h3 class="panel-title">核心指标同比对比</h3>
        <el-table :data="data?.yoy || []" border size="small" class="yoy-table">
          <el-table-column prop="name" label="指标" min-width="80" />
          <el-table-column prop="lastYear" label="去年" width="80" />
          <el-table-column prop="thisYear" label="今年" width="80" />
          <el-table-column prop="growth" label="增长" width="90">
            <template #default="row">
              <span :class="row.row.growth >= 0 ? 'value-up' : 'value-down'">
                {{ row.row.growth >= 0 ? '+' : '' }}{{ row.row.growth }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="growthRate" label="增长率" width="80" />
        </el-table>
      </div>
      <div class="panel">
        <h3 class="panel-title">成果分类分布</h3>
        <div ref="categoryRef" class="inner-chart"></div>
      </div>
    </section>

    <!-- 月度趋势 — 全宽 -->
    <section class="trend-section">
      <h3 class="panel-title">月度趋势</h3>
      <div ref="trendRef" class="trend-chart"></div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const loading = ref(true)
const data = ref(null)
const mapRef = ref(null)
const categoryRef = ref(null)
const trendRef = ref(null)

function formatNum(n) { return (n ?? 0).toLocaleString() }

const kpiList = computed(() => {
  const k = data.value?.kpi
  if (!k) return []
  return [
    { label: '终身学习档案数', value: k.totalArchives ?? 0, color: '#3568d4' },
    { label: '学习成果认证数', value: k.totalCertifications ?? 0, color: '#0ea371' },
    { label: '转换规则数', value: k.totalRules ?? 0, color: '#e8850c' },
    { label: '分中心数', value: k.totalCenters ?? 0, color: '#7c4dff' },
  ]
})

// 左侧：用户构成（从 categories 提取角色分布）
const leftStats = computed(() => {
  const cats = data.value?.categories || []
  const total = cats.reduce((s, c) => s + (c.value || 0), 0) || 1
  const colorMap = {
    '学生档案': '#3568d4',
    '机构管理员': '#e8850c',
    '专家': '#0ea371',
    '系统管理员': '#7c4dff',
  }
  return cats.map(c => ({
    label: c.name,
    value: c.value || 0,
    color: colorMap[c.name] || '#5a6884',
    percent: Math.round((c.value || 0) / total * 100),
  }))
})

// 右侧：运营总览（从 dashboard 各字段派生）
const rightStats = computed(() => {
  const d = data.value
  if (!d) return []
  const provinces = d.provinces || []
  const monthly = d.monthlyTrend || []
  const totalEarn = monthly.reduce((s, m) => s + (m.earn || 0), 0)
  const totalExchange = monthly.reduce((s, m) => s + (m.exchange || 0), 0)
  const avgEarn = monthly.length > 0 ? Math.round(totalEarn / monthly.length) : 0
  const topProvince = provinces.length > 0
    ? provinces.reduce((a, b) => (a.value || 0) > (b.value || 0) ? a : b)
    : null

  return [
    { label: '覆盖省份数', value: provinces.length, color: '#3568d4' },
    { label: '认证标准总数', value: d.kpi?.totalRules ?? 0, color: '#0ea371' },
    { label: '积分流水总量', value: d.kpi?.totalCertifications ?? 0, color: '#e8850c' },
    { label: '月均积分获得', value: avgEarn, color: '#7c4dff' },
    { label: '学分转换总量', value: totalExchange, color: '#e03131' },
    { label: '最密集省份', display: topProvince ? topProvince.name : '—', value: 0, color: '#2b8a3e' },
  ]
})

onMounted(async () => {
  try {
    data.value = await request.get('/stats/dashboard')
  } catch (e) { /* ignore */ }
  loading.value = false
  await nextTick()
  renderCharts()
})

async function renderCharts() {
  if (!data.value) return

  // === 中国地图 ===
  if (mapRef.value) {
    try {
      const res = await fetch('/china.geojson')
      const geo = await res.json()
      echarts.registerMap('china', geo)
    } catch (e) { /* GeoJSON 未就绪 */ }

    const maxVal = Math.max(...(data.value.provinces || [{ value: 1 }]).map(p => p.value), 1)
    const mapChart = echarts.init(mapRef.value)
    mapChart.setOption({
      tooltip: {
        trigger: 'item',
        backgroundColor: '#fff',
        borderColor: '#e2e8f0',
        textStyle: { color: '#1a1a2e', fontSize: 13 },
        formatter: '{b}<br/>机构数量：{c}',
      },
      visualMap: {
        min: 0,
        max: maxVal,
        right: 10,
        top: 'center',
        text: ['高', '低'],
        textStyle: { color: '#5a6884', fontSize: 10 },
        inRange: { color: ['#eef2ff', '#bac8ff', '#748ffc', '#4263eb', '#1c3faa'] },
        calculable: true,
        itemWidth: 12,
        itemHeight: 100,
      },
      series: [{
        name: '机构数量',
        type: 'map',
        map: 'china',
        roam: false,
        aspectScale: 0.85,
        layoutCenter: ['50%', '56%'],
        layoutSize: '95%',
        zoom: 1.1,
        label: { show: true, fontSize: 9, color: '#5a6884' },
        emphasis: {
          label: { fontSize: 12, fontWeight: 'bold', color: '#1a1a2e' },
          itemStyle: { areaColor: '#bac8ff' },
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1.2,
          areaColor: '#eef2ff',
        },
        data: data.value.provinces || [],
        animationDurationUpdate: 600,
        animationEasingUpdate: 'cubicInOut',
      }],
    })
    window.addEventListener('resize', () => mapChart.resize())
  }

  // === 分类饼图 ===
  if (categoryRef.value) {
    const c = echarts.init(categoryRef.value)
    const categoryColors = ['#4263eb', '#0ea371', '#e8850c', '#7c4dff', '#e03131', '#2b8a3e']
    c.setOption({
      tooltip: {
        trigger: 'item',
        backgroundColor: '#fff',
        borderColor: '#e2e8f0',
        textStyle: { color: '#1a1a2e' },
        formatter: '{b}: {c} ({d}%)',
      },
      legend: { bottom: 0, textStyle: { color: '#5a6884', fontSize: 12 } },
      color: categoryColors,
      series: [{
        type: 'pie',
        radius: ['48%', '74%'],
        center: ['50%', '46%'],
        data: data.value.categories || [],
        label: { color: '#5a6884', fontSize: 11, formatter: '{b}\n{d}%' },
        labelLine: { lineStyle: { color: '#d0d7e2' } },
        emphasis: {
          itemStyle: { shadowBlur: 12, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.1)' },
        },
      }],
    })
    window.addEventListener('resize', () => c.resize())
  }

  // === 月度趋势 ===
  if (trendRef.value) {
    const t = echarts.init(trendRef.value)
    const months = (data.value.monthlyTrend || []).map(m => m.month)
    t.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#fff',
        borderColor: '#e2e8f0',
        textStyle: { color: '#1a1a2e' },
      },
      legend: {
        data: ['积分获得', '学分转换'],
        textStyle: { color: '#5a6884', fontSize: 12 },
        top: 0,
      },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '14%', containLabel: true },
      xAxis: {
        type: 'category',
        data: months,
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#5a6884', fontSize: 11 },
        axisTick: { show: false },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#f1f3f5' } },
        axisLabel: { color: '#5a6884', fontSize: 11 },
      },
      series: [
        {
          name: '积分获得',
          type: 'bar',
          barWidth: 18,
          data: (data.value.monthlyTrend || []).map(m => m.earn),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#4263eb' },
              { offset: 1, color: '#758bfd' },
            ]),
            borderRadius: [4, 4, 0, 0],
          },
        },
        {
          name: '学分转换',
          type: 'line',
          smooth: true,
          data: (data.value.monthlyTrend || []).map(m => m.exchange),
          itemStyle: { color: '#e8850c' },
          lineStyle: { color: '#e8850c', width: 2.5 },
          symbol: 'circle',
          symbolSize: 7,
        },
      ],
    })
    window.addEventListener('resize', () => t.resize())
  }
}
</script>

<style scoped>
/* ====== 全局基础 ====== */
.dashboard {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 28px 36px 40px;
  max-width: 1480px;
  margin: 0 auto;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* ====== 标题 ====== */
.dash-header {
  text-align: center;
  padding: 4px 0 24px;
}
.dash-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: 2px;
  margin: 0;
}
.dash-header p {
  font-size: 13px;
  color: #94a3b8;
  margin: 6px 0 0;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* ====== KPI 卡片 ====== */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.kpi-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
  display: flex;
  overflow: hidden;
  animation: kpiIn 0.5s ease both;
}
.kpi-accent {
  width: 4px;
  flex-shrink: 0;
}
.kpi-body {
  flex: 1;
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.kpi-num {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  font-variant-numeric: tabular-nums;
  letter-spacing: -1px;
}
.kpi-label {
  font-size: 13px;
  color: #5a6884;
  font-weight: 400;
}

@keyframes kpiIn {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ====== 三栏地图区域 ====== */
.map-row {
  display: grid;
  grid-template-columns: 1fr 3fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

/* 侧栏面板 */
.side-panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
}
.side-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f2f5;
  letter-spacing: 0.3px;
}
.side-stats {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
}

/* 左侧带进度条的统计项 */
.side-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
}
.side-stat-num {
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.side-stat-label {
  font-size: 12px;
  color: #5a6884;
}
.side-stat-bar {
  height: 3px;
  border-radius: 2px;
  margin-top: 4px;
  transition: width 0.6s ease;
  min-width: 4px;
}

/* 右侧紧凑统计项 */
.side-stat.compact {
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f6f8;
}
.side-stat.compact:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.side-stat.compact .side-stat-num {
  font-size: 20px;
}

/* 地图容器 */
.map-hero {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 4px 20px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.map-chart {
  width: 100%;
  height: 500px;
}

/* ====== 双栏区域 ====== */
.dual-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}
.panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
  padding: 20px 22px;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 14px;
  letter-spacing: 0.3px;
}
.inner-chart {
  width: 100%;
  height: 280px;
}

/* ====== 趋势区域 ====== */
.trend-section {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04), 0 2px 12px rgba(0, 0, 0, 0.03);
  padding: 20px 22px;
}
.trend-section .panel-title {
  margin-bottom: 12px;
}
.trend-chart {
  width: 100%;
  height: 280px;
}

/* ====== 同比表格微调 ====== */
.yoy-panel {
  display: flex;
  flex-direction: column;
}
.yoy-table {
  flex: 1;
}
.value-up   { color: #0ea371; font-weight: 600; font-variant-numeric: tabular-nums; }
.value-down { color: #e03131; font-weight: 600; font-variant-numeric: tabular-nums; }

:deep(.yoy-table .el-table__header th) {
  background: #f8f9fb;
  color: #5a6884;
  font-weight: 500;
  font-size: 12px;
}
:deep(.yoy-table .el-table__body td) {
  color: #1a1a2e;
  font-size: 13px;
}

/* ====== 响应式 ====== */
@media (max-width: 1200px) {
  .map-row {
    grid-template-columns: 1fr;
  }
  .side-panel {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
  }
  .side-title { width: 100%; }
  .side-stats { flex-direction: row; flex-wrap: wrap; gap: 20px; }
  .side-stat { min-width: 120px; }
  .map-chart { height: 400px; }
}
@media (max-width: 1024px) {
  .dashboard { padding: 16px 12px 24px; }
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .dual-row { grid-template-columns: 1fr; }
  .map-chart { height: 360px; }
  .dash-header h1 { font-size: 20px; }
}
@media (max-width: 600px) {
  .kpi-row { grid-template-columns: 1fr; }
  .kpi-num { font-size: 22px; }
  .side-stats { flex-direction: column; }
}
</style>
