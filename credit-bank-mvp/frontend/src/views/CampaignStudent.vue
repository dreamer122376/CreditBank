<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">参与活动</h1>
        <p class="page-subtitle">浏览进行中的平台活动，报名参加享受积分翻倍</p>
      </div>
    </div>

    <!-- 卡片列表 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" :xs="24" :sm="12" :md="8" v-for="(item, idx) in list" :key="item.id" class="card-col">
        <el-card class="campaign-card card-enter card-glow" shadow="hover" @click="goDetail(item.id)" :style="{ animationDelay: idx * 0.08 + 's' }">
          <!-- 顶部色条 -->
          <div class="card-accent" :class="'accent-' + (item.multiplier > 1 ? 'hot' : 'normal')"></div>

          <!-- 封面 -->
          <div class="card-cover" :style="coverBg(item)">
            <div class="cover-pattern"></div>
            <div class="cover-content">
              <span class="cover-title">{{ item.title }}</span>
              <div class="cover-badges">
                <span v-if="item.multiplier > 1" class="multiplier-badge">
                  <el-icon><TrendCharts /></el-icon>
                  {{ item.multiplier }}x 积分翻倍
                </span>
                <span class="status-dot" :class="item.status === 1 ? 'active' : ''"></span>
              </div>
            </div>
            <div class="cover-gradient"></div>
          </div>

          <!-- 信息区 -->
          <div class="card-body">
            <!-- 时间线 -->
            <div class="date-range">
              <div class="date-item">
                <el-icon class="date-icon"><Calendar /></el-icon>
                <span>{{ fmtDate(item.startTime) }}</span>
              </div>
              <div class="date-divider">
                <span class="date-days" v-if="daysLeft(item) >= 0">剩 {{ daysLeft(item) }} 天</span>
              </div>
              <div class="date-item">
                <el-icon class="date-icon"><Calendar /></el-icon>
                <span>{{ fmtDate(item.endTime) }}</span>
              </div>
            </div>

            <!-- 主办方 -->
            <div class="org-row" v-if="item.organizer">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ item.organizer }}</span>
            </div>

            <!-- 描述 -->
            <div class="card-desc" v-if="item.description">
              {{ item.description }}
            </div>

            <!-- 底部 -->
            <div class="card-footer">
              <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" effect="light">进行中</el-tag>
              <span class="detail-link">
                查看详情 <el-icon><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state-card">
      <el-icon class="empty-icon"><Promotion /></el-icon>
      <div class="empty-title">暂无进行中的活动</div>
      <div class="empty-text">当前没有正在进行中的平台活动，请关注后续活动通知</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, OfficeBuilding, ArrowRight, TrendCharts, Promotion } from '@element-plus/icons-vue'
import { getActiveCampaigns } from '@/api/campaign'

const router = useRouter()
const loading = ref(true)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    list.value = await getActiveCampaigns()
  } catch (e) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

function goDetail(id) { router.push('/campaign/' + id) }

function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }
function fmtDate(t) { if (!t) return ''; return t.length >= 10 ? t.substring(0, 10) : t }

function daysLeft(item) {
  if (!item.endTime) return -1
  const end = new Date(item.endTime)
  const now = new Date()
  return Math.ceil((end - now) / (1000 * 60 * 60 * 24))
}

const COVERS = [
  'linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%)',
  'linear-gradient(135deg, #0b7a4f 0%, #20c997 100%)',
  'linear-gradient(135deg, #e8590c 0%, #f59f00 100%)',
  'linear-gradient(135deg, #7950f2 0%, #9775fa 100%)',
]
function coverBg(item) {
  if (item.coverImage) return { backgroundImage: 'url(' + item.coverImage + ')' }
  const idx = (item.id || 0) % COVERS.length
  return { background: COVERS[idx] }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container {
  min-height: calc(100vh - 80px);
  background-color: var(--cb-bg);
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left { display: flex; flex-direction: column; gap: 4px; }
.page-title {
  font-size: var(--cb-text-2xl);
  font-weight: 700;
  color: var(--cb-ink);
  margin: 0;
}
.page-subtitle {
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
  margin: 0;
}

.card-col { margin-bottom: 20px; }

.campaign-card {
  cursor: pointer;
  height: 100%;
  transition: transform var(--cb-transition-normal), box-shadow var(--cb-transition-normal);
  position: relative;
  overflow: visible;
}
.campaign-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--cb-shadow-lg);
}
.campaign-card :deep(.el-card__body) { padding: 0; }

/* 顶部色条 */
.card-accent {
  height: 4px;
  border-radius: var(--cb-radius-lg) var(--cb-radius-lg) 0 0;
}
.card-accent.accent-hot {
  background: linear-gradient(90deg, var(--cb-warning), var(--cb-danger-light), var(--cb-warning));
  background-size: 200% 100%;
  animation: accentShimmer 2s ease infinite;
}
.card-accent.accent-normal {
  background: linear-gradient(90deg, var(--cb-primary), var(--cb-primary-light));
}
@keyframes accentShimmer {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* 封面 */
.card-cover {
  height: 160px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}
.cover-pattern {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255,255,255,0.15) 0%, transparent 50%),
              radial-gradient(circle at 20% 80%, rgba(255,255,255,0.08) 0%, transparent 40%);
}
.cover-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 50%);
}
.cover-content {
  position: relative;
  z-index: 1;
  padding: 16px;
  width: 100%;
}
.cover-title {
  color: #fff;
  font-size: var(--cb-text-lg);
  font-weight: 700;
  text-shadow: 0 2px 6px rgba(0,0,0,0.3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.cover-badges {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.multiplier-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, var(--cb-warning) 0%, #f7b731 100%);
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: var(--cb-text-xs);
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(245,159,0,0.4);
}
.status-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 8px rgba(74,222,128,0.6);
  animation: dotPulse 2s ease infinite;
}
@keyframes dotPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* 信息区 */
.card-body { padding: 16px 18px 18px; }

.date-range {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: var(--cb-bg);
  border-radius: var(--cb-radius-md);
  font-size: var(--cb-text-xs);
  color: var(--cb-slate);
}
.date-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.date-icon { font-size: 13px; color: var(--cb-primary-light); }
.date-divider {
  flex: 1;
  height: 1px;
  background: var(--cb-border);
  margin: 0 10px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.date-days {
  position: absolute;
  background: var(--cb-bg);
  padding: 2px 8px;
  font-size: 11px;
  color: var(--cb-warning);
  font-weight: 600;
  white-space: nowrap;
}

.org-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
  margin-bottom: 8px;
}
.org-row .el-icon { color: var(--cb-primary-light); flex-shrink: 0; }

.card-desc {
  font-size: var(--cb-text-sm);
  color: var(--cb-slate);
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--cb-border-light);
}
.detail-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--cb-primary);
  font-size: var(--cb-text-sm);
  font-weight: 500;
  transition: color var(--cb-transition-fast);
}
.campaign-card:hover .detail-link { color: var(--cb-primary-dark); }

/* 空状态 */
.empty-state-card {
  text-align: center;
  padding: 80px 20px;
  background: var(--cb-bg-card);
  border-radius: var(--cb-radius-lg);
  border: 1px dashed var(--cb-border);
  color: var(--cb-muted);
}
.empty-icon { font-size: 56px; margin-bottom: 16px; opacity: 0.5; }
.empty-title { font-size: var(--cb-text-lg); font-weight: 600; color: var(--cb-slate); margin-bottom: 8px; }
.empty-text { font-size: var(--cb-text-sm); }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
}
</style>
