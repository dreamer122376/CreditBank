<template>
  <div class="dashboard-map">
    <!-- 标题栏 -->
    <div class="dash-header">
      <h1>学分银行数据看板</h1>
      <p>Credit Bank Data Dashboard</p>
    </div>

    <div class="dash-body" v-loading="loading">
      <!-- KPI 卡片 -->
      <div class="kpi-row">
        <div class="kpi-card">
          <div class="kpi-num">{{ formatNum(data?.kpi?.totalArchives) }}</div>
          <div class="kpi-label">终身学习档案数</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-num">{{ formatNum(data?.kpi?.totalCertifications) }}</div>
          <div class="kpi-label">学习成果认证数</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-num">{{ formatNum(data?.kpi?.totalRules) }}</div>
          <div class="kpi-label">转换规则数</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-num">{{ formatNum(data?.kpi?.totalCenters) }}</div>
          <div class="kpi-label">分中心数</div>
        </div>
      </div>

      <!-- 中排：地图 + 同比 -->
      <div class="mid-row">
        <div class="map-panel">
          <div ref="mapRef" class="map-chart"></div>
        </div>
        <div class="yoy-panel">
          <h3>核心指标同比对比</h3>
          <el-table :data="data?.yoy || []" border size="small">
            <el-table-column prop="name" label="指标" />
            <el-table-column prop="lastYear" label="去年" />
            <el-table-column prop="thisYear" label="今年" />
            <el-table-column prop="growth" label="增长">
              <template #default="row">
                <span :style="{ color: row.row.growth >= 0 ? '#0b7a4f' : '#e03131' }">
                  {{ row.row.growth >= 0 ? '+' : '' }}{{ row.row.growth }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="growthRate" label="增长率" />
          </el-table>
        </div>
      </div>

      <!-- 底排：分类 + 趋势 -->
      <div class="bottom-row">
        <div class="chart-panel">
          <div ref="categoryRef" class="inner-chart"></div>
        </div>
        <div class="chart-panel">
          <div ref="trendRef" class="inner-chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const loading = ref(true)
const data = ref(null)
const mapRef = ref(null)
const categoryRef = ref(null)
const trendRef = ref(null)

function formatNum(n) { return (n ?? 0).toLocaleString() }

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

  // 地图
  if (mapRef.value) {
    try {
      const res = await fetch('/china.geojson')
      const geo = await res.json()
      echarts.registerMap('china', geo)
    } catch (e) {
      // GeoJSON 未就绪，地图区域空白，其他图表正常
    }
    const mapChart = echarts.init(mapRef.value)
    mapChart.setOption({
      tooltip: { trigger: 'item' },
      visualMap: { min: 0, max: Math.max(...(data.value.provinces || [{value:1}]).map(p => p.value)), left: 20, bottom: 10, text: ['高', '低'], inRange: { color: ['#e8f5e9', '#2e7d32'] } },
      series: [{
        name: '机构数量',
        type: 'map', map: 'china', roam: false,
        label: { show: true, fontSize: 10 },
        data: data.value.provinces || []
      }]
    })
    window.addEventListener('resize', () => mapChart.resize())
  }

  // 分类饼图
  if (categoryRef.value) {
    const c = echarts.init(categoryRef.value)
    c.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: data.value.categories || [],
        label: { formatter: '{b}\n{d}%' }
      }]
    })
    window.addEventListener('resize', () => c.resize())
  }

  // 月度趋势
  if (trendRef.value) {
    const t = echarts.init(trendRef.value)
    const months = (data.value.monthlyTrend || []).map(m => m.month)
    t.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['积分获得', '学分转换'] },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series: [
        { name: '积分获得', type: 'bar', data: (data.value.monthlyTrend || []).map(m => m.earn), itemStyle: { color: '#3b5bdb' } },
        { name: '学分转换', type: 'line', data: (data.value.monthlyTrend || []).map(m => m.exchange), itemStyle: { color: '#f59f00' } }
      ]
    })
    window.addEventListener('resize', () => t.resize())
  }
}
</script>

<style scoped>
.dashboard-map {
  min-height: 100vh;
  background: #0a1628;
  color: #fff;
  padding: 20px 28px;
}
.dash-header {
  text-align: center;
  padding: 10px 0 16px;
}
.dash-header h1 {
  font-size: 26px;
  letter-spacing: 4px;
  color: #e0e7ff;
  margin: 0;
}
.dash-header p {
  font-size: 13px;
  color: #8892b0;
  margin: 4px 0 0;
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.kpi-card {
  background: linear-gradient(135deg, #1a2744 0%, #152038 100%);
  border: 1px solid #2a3a5c;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}
.kpi-num { font-size: 28px; font-weight: 700; color: #64b5f6; }
.kpi-label { font-size: 13px; color: #8892b0; margin-top: 6px; }

.mid-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.map-panel {
  background: #1a2744;
  border: 1px solid #2a3a5c;
  border-radius: 8px;
  padding: 12px;
}
.map-chart { height: 420px; }
.yoy-panel {
  background: #1a2744;
  border: 1px solid #2a3a5c;
  border-radius: 8px;
  padding: 12px 16px;
}
.yoy-panel h3 { font-size: 15px; color: #e0e7ff; margin: 0 0 10px; }

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-panel {
  background: #1a2744;
  border: 1px solid #2a3a5c;
  border-radius: 8px;
  padding: 12px;
}
.inner-chart { height: 300px; }

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #1e3050;
  --el-table-border-color: #2a3a5c;
  --el-table-text-color: #c0c8e0;
  --el-table-header-text-color: #8892b0;
}
</style>
