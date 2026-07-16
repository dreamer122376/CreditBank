<template>
  <div class="my-projects" v-loading="loading">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">我的项目</h1>
        <p class="page-subtitle">查看已报名项目的进度和提交完成情况</p>
      </div>
    </div>

    <!-- 卡片列表 -->
    <el-row :gutter="20">
      <el-col :span="8" :xs="24" :sm="12" :md="8" v-for="(item, idx) in list" :key="item.enrollmentId" class="card-col">
        <el-card class="project-card card-enter card-glow" shadow="hover"
          @click="goDetail(item.projectId)" :style="{ animationDelay: idx * 0.08 + 's' }">
          <!-- 封面 -->
          <div class="card-cover" :class="coverPalette(item.projectId)">
            <!-- 装饰图形 -->
            <div class="cover-geo ring"></div>
            <div class="cover-geo dot dot-1"></div>
            <div class="cover-geo dot dot-2"></div>
            <div class="cover-diagonal"></div>
            <!-- 状态图标 -->
            <div class="cover-icon-wrap">
              <el-icon :size="48"><component :is="statusIcon(item.status)" /></el-icon>
            </div>
            <!-- 文字 -->
            <div class="cover-text-area">
              <span class="cover-label">{{ statusLabel(item.status) }}</span>
              <span class="cover-name">{{ item.projectName }}</span>
            </div>
            <!-- 状态徽章 -->
            <span class="badge-status" :class="coverPalette(item.projectId)">{{ item.status }}</span>
            <!-- 底部进度条 -->
            <div class="cover-progress" v-if="item.status === '进行中' || item.status === '待审核'"></div>
            <!-- 渐变遮罩 -->
            <div class="cover-overlay"></div>
          </div>

          <!-- 信息区 -->
          <div class="card-info">
            <div class="card-title" :title="item.projectName">{{ item.projectName }}</div>
            <div class="card-meta">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ item.orgName || '未知机构' }}</span>
            </div>
            <div class="card-meta">
              <el-icon><User /></el-icon>
              <span>{{ item.expertName || '暂未指定' }}</span>
            </div>
            <div class="card-meta">
              <el-icon><Clock /></el-icon>
              <span>报名：{{ fmt(item.enrolledAt) }}</span>
            </div>
            <div class="card-desc">
              {{ (item.description || '').substring(0, 70) }}{{ (item.description || '').length > 70 ? '...' : '' }}
            </div>
            <div class="card-footer">
              <el-tag :type="statusType(item.status)" size="small" effect="light">{{ item.status }}</el-tag>
              <div class="footer-actions">
                <el-button
                  v-if="item.status === '进行中'"
                  size="small" type="success" plain :icon="Check"
                  @click.stop="handleSubmit(item)"
                >提交完成</el-button>
                <span class="detail-link">
                  查看详情 <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state-card">
      <el-icon class="empty-icon"><FolderOpened /></el-icon>
      <div class="empty-title">暂无报名项目</div>
      <div class="empty-text">您还没有报名任何项目，快去项目报名页面看看吧。</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  OfficeBuilding, User, Clock, Check, ArrowRight, FolderOpened,
  VideoPlay, CircleCheck, Timer, CircleClose
} from '@element-plus/icons-vue'
import { getMyProjects, submitProjectForReview } from '@/api/project'

const router = useRouter()
const loading = ref(true)
const list = ref([])

async function loadData() {
  loading.value = true
  try { list.value = await getMyProjects() }
  catch (e) { ElMessage.error(e.message || '加载失败') }
  finally { loading.value = false }
}

function goDetail(projectId) { router.push('/project/' + projectId) }

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

async function handleSubmit(item) {
  try {
    await ElMessageBox.confirm('确定要提交项目完成申请吗？', '提交完成', { type: 'info', confirmButtonText: '确定', cancelButtonText: '取消' })
    await submitProjectForReview(item.enrollmentId)
    ElMessage.success('已提交，等待机构管理员审核')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '提交失败')
  }
}

// --- 状态映射 ---
const COVERS = ['cover-blue', 'cover-teal', 'cover-purple', 'cover-amber']
function coverPalette(id) { return COVERS[(id || 0) % COVERS.length] }

function statusLabel(s) {
  if (s === '进行中') return '学习进行中'
  if (s === '待审核') return '等待审核'
  if (s === '已完成') return '项目已完成'
  if (s === '已取消') return '已取消报名'
  return '已报名项目'
}
function statusIcon(s) {
  if (s === '进行中') return VideoPlay
  if (s === '待审核') return Timer
  if (s === '已完成') return CircleCheck
  if (s === '已取消') return CircleClose
  return CircleCheck
}
function statusType(s) {
  if (s === '已完成') return 'success'
  if (s === '待审核') return 'warning'
  if (s === '进行中') return ''
  if (s === '已报名') return 'primary'
  return 'info'
}
function statusClass(s) { return statusKey(s) }

onMounted(() => { loadData() })
</script>

<style scoped>
.my-projects {
  min-height: calc(100vh - 80px);
  background-color: var(--cb-bg);
  padding: 24px;
}
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: var(--cb-text-2xl); font-weight: 700; color: var(--cb-ink); margin: 0; }
.page-subtitle { font-size: var(--cb-text-sm); color: var(--cb-muted); margin: 0; }

.card-col { margin-bottom: 20px; }

.project-card {
  cursor: pointer; height: 100%;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.3s ease, border-color 0.3s ease;
  border: 1px solid transparent;
}
.project-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(59,91,219,0.18), 0 0 0 1px rgba(59,91,219,0.12);
}
.project-card:hover .card-cover::before { opacity: 0.7; }
.project-card:hover .cover-geo.ring { transform: rotate(20deg) scale(1.15); }
.project-card:hover .cover-geo.dot-1 { transform: translate(-4px, -8px) scale(1.5); }
.project-card:hover .cover-geo.dot-2 { transform: translate(4px, -6px) scale(1.5); }
.project-card:hover .cover-diagonal { transform: rotate(-10deg) translateX(16px); }
.project-card:hover .cover-icon-wrap { transform: scale(1.1); }
.project-card:hover .cover-name { text-shadow: 0 4px 16px rgba(0,0,0,0.35); }
.project-card :deep(.el-card__body) { padding: 0; }

/* ===== 封面 ===== */
.card-cover {
  height: 180px; position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--cb-radius-lg) var(--cb-radius-lg) 0 0;
}
.card-cover::before {
  content: ''; position: absolute; inset: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.12) 0%, transparent 50%),
              radial-gradient(circle at 70% 70%, rgba(255,255,255,0.06) 0%, transparent 50%);
  pointer-events: none; z-index: 0;
}

/* --- 状态色 --- */
.cover-blue   { background: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%); }
.cover-teal   { background: linear-gradient(135deg, #0b7a4f 0%, #20c997 100%); }
.cover-purple { background: linear-gradient(135deg, #7950f2 0%, #9775fa 100%); }
.cover-amber  { background: linear-gradient(135deg, #e8590c 0%, #f59f00 100%); }

/* --- 装饰图形 --- */
.cover-geo { position: absolute; border-radius: 50%; pointer-events: none; z-index: 0; transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cover-diagonal { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cover-icon-wrap { transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cover-name { transition: text-shadow 0.3s ease; }
.cover-geo.ring {
  width: 140px; height: 140px;
  border: 3px solid rgba(255,255,255,0.15);
  top: -40px; right: -40px;
}
.cover-geo.dot {
  width: 20px; height: 20px;
  background: rgba(255,255,255,0.2);
}
.cover-geo.dot-1 { top: 24px; left: 24px; }
.cover-geo.dot-2 { bottom: 20px; right: 32px; width: 12px; height: 12px; }
.cover-diagonal {
  position: absolute; z-index: 0; pointer-events: none;
  width: 200px; height: 3px;
  background: rgba(255,255,255,0.1);
  top: 50%; left: -40px;
  transform: rotate(-25deg);
}

/* --- 图标 --- */
.cover-icon-wrap {
  position: relative; z-index: 1;
  color: rgba(255,255,255,0.25);
  margin-right: 16px; flex-shrink: 0;
}

/* --- 文字区 --- */
.cover-text-area {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; gap: 4px;
  max-width: 60%;
}
.cover-label {
  font-size: 11px; color: rgba(255,255,255,0.7);
  text-transform: uppercase; letter-spacing: 1px;
}
.cover-name {
  font-size: 17px; font-weight: 700; color: #fff;
  text-shadow: 0 2px 8px rgba(0,0,0,0.25);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

/* --- 状态徽章 --- */
.badge-status {
  position: absolute; top: 12px; right: 12px; z-index: 2;
  padding: 4px 10px; border-radius: 20px;
  font-size: var(--cb-text-xs); font-weight: 600;
  box-shadow: var(--cb-shadow-sm);
  color: #fff;
}
.badge-status.cover-blue   { background: rgba(59,91,219,0.35); }
.badge-status.cover-teal   { background: rgba(11,122,79,0.35); }
.badge-status.cover-purple { background: rgba(121,80,242,0.35); }
.badge-status.cover-amber  { background: rgba(245,159,0,0.35); }

/* --- 进度条 --- */
.cover-progress {
  position: absolute; bottom: 0; left: 0; right: 0; height: 3px; z-index: 2;
  background: rgba(255,255,255,0.3);
}
.cover-progress::after {
  content: ''; display: block; height: 100%; width: 60%;
  background: rgba(255,255,255,0.6);
}

/* --- 渐变遮罩 --- */
.cover-overlay {
  position: absolute; inset: 0; z-index: 0; pointer-events: none;
  background: linear-gradient(to top, rgba(0,0,0,0.45) 0%, transparent 50%);
}

/* ===== 信息区 ===== */
.card-info { padding: 18px; }
.card-title {
  font-size: var(--cb-text-lg); font-weight: 600;
  color: var(--cb-charcoal); margin-bottom: 10px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.card-meta {
  display: flex; align-items: center; gap: 6px;
  font-size: var(--cb-text-sm); color: var(--cb-muted); margin-bottom: 6px;
}
.card-meta .el-icon { color: var(--cb-primary-light); flex-shrink: 0; }
.card-desc {
  font-size: var(--cb-text-sm); color: var(--cb-slate); line-height: 1.5;
  margin: 10px 0 12px; min-height: 40px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.card-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 12px; border-top: 1px solid var(--cb-border-light);
}
.footer-actions { display: flex; align-items: center; gap: 10px; }
.detail-link {
  display: inline-flex; align-items: center; gap: 4px;
  color: var(--cb-primary); font-size: var(--cb-text-sm); font-weight: 500;
}
.project-card:hover .detail-link { color: var(--cb-primary-dark); }

/* ===== 空状态 ===== */
.empty-state-card {
  text-align: center; padding: 80px 20px;
  background: var(--cb-bg-card); border-radius: var(--cb-radius-lg);
  border: 1px dashed var(--cb-border); color: var(--cb-muted);
}
.empty-icon { font-size: 56px; margin-bottom: 16px; opacity: 0.5; }
.empty-title { font-size: var(--cb-text-lg); font-weight: 600; color: var(--cb-slate); margin-bottom: 8px; }
.empty-text { font-size: var(--cb-text-sm); }

@media (max-width: 768px) {
  .my-projects { padding: 16px; }
  .footer-actions { flex-wrap: wrap; }
}
</style>
