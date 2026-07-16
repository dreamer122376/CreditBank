<template>
  <div class="project-student" v-loading="loading">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">项目报名</h1>
        <p class="page-subtitle">浏览并报名参加已上架的学分项目</p>
      </div>
    </div>

    <!-- 卡片列表 -->
    <el-row :gutter="20">
      <el-col :span="8" :xs="24" :sm="12" :md="8" v-for="(item, idx) in list" :key="item.id" class="card-col">
        <el-card class="project-card card-enter card-glow" shadow="hover" @click="goDetail(item.id)" :style="{ animationDelay: idx * 0.08 + 's' }">
          <div class="card-cover" :class="coverPalette(item)">
            <!-- 装饰图形 -->
            <div class="cover-geo ring"></div>
            <div class="cover-geo dot dot-1"></div>
            <div class="cover-geo dot dot-2"></div>
            <div class="cover-diagonal"></div>
            <!-- 文字 -->
            <div class="cover-text-area" style="align-items:center; text-align:center; max-width:80%;">
              <span class="cover-name" style="font-size:19px;">{{ item.name }}</span>
            </div>
            <!-- 积分徽章 -->
            <span v-if="item.creditReward > 0" class="badge-bonus">
              <el-icon><Trophy /></el-icon>+{{ item.creditReward }}
            </span>
            <div class="cover-overlay"></div>
          </div>
          <div class="card-info">
            <div class="card-title" :title="item.name">{{ item.name }}</div>
            <div class="card-meta">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ item.orgName || '未知机构' }}</span>
            </div>
            <div class="card-meta">
              <el-icon><User /></el-icon>
              <span>{{ item.expertName || '暂未指定' }}</span>
            </div>
            <div class="card-desc">
              {{ (item.description || '').substring(0, 70) }}{{ (item.description || '').length > 70 ? '...' : '' }}
            </div>
            <div class="card-footer">
              <el-tag
                :type="item.enrolled ? enrollmentStatusType(item.enrollmentStatus) : statusType(item.status)"
                size="small"
                effect="light"
              >
                {{ item.enrolled ? item.enrollmentStatus : item.statusName }}
              </el-tag>
              <span class="detail-link">
                查看详情
                <el-icon><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state">
      <el-icon class="empty-icon"><FolderOpened /></el-icon>
      <div class="empty-title">暂无可报名项目</div>
      <div class="empty-text">当前没有已上架的项目，请关注后续项目通知。</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Trophy, OfficeBuilding, User, ArrowRight, FolderOpened, Loading } from '@element-plus/icons-vue'
import { getActiveProjects } from '@/api/project'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { currentUser } = useAuth()
const loading = ref(true)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    const studentId = currentUser.value?.id
    list.value = await getActiveProjects(studentId)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push('/project/' + id)
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  return 'info'
}

function enrollmentStatusType(status) {
  if (status === '已完成') return 'success'
  if (status === '待审核') return 'warning'
  if (status === '进行中') return 'warning'
  if (status === '已报名') return 'primary'
  if (status === '已取消') return 'info'
  return 'info'
}

const COVERS = ['cover-blue', 'cover-teal', 'cover-purple', 'cover-amber']
function coverPalette(item) { return COVERS[(item.id || 0) % COVERS.length] }

onMounted(() => { loadData() })
</script>

<style scoped>
.project-student {
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

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

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

.card-col {
  margin-bottom: 20px;
}

.project-card {
  cursor: pointer;
  height: 100%;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.3s ease,
              border-color 0.3s ease;
  border: 1px solid transparent;
}

.project-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(59,91,219,0.18), 0 0 0 1px rgba(59,91,219,0.12);
}
.project-card:hover .card-cover::before {
  opacity: 0.7;
}
.project-card:hover .cover-geo.ring {
  transform: rotate(20deg) scale(1.15);
}
.project-card:hover .cover-geo.dot-1 {
  transform: translate(-4px, -8px) scale(1.5);
}
.project-card:hover .cover-geo.dot-2 {
  transform: translate(4px, -6px) scale(1.5);
}
.project-card:hover .cover-diagonal {
  transform: rotate(-10deg) translateX(16px);
}
.project-card:hover .cover-icon-wrap {
  transform: scale(1.1);
}
.project-card:hover .cover-name {
  text-shadow: 0 4px 16px rgba(0,0,0,0.35);
}

.project-card :deep(.el-card__body) {
  padding: 0;
}

/* ===== 封面 ===== */
.card-cover {
  height: 180px; position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--cb-radius-lg) var(--cb-radius-lg) 0 0;
  background: linear-gradient(135deg, var(--cb-primary) 0%, var(--cb-primary-light) 100%);
}
.card-cover::before {
  content: ''; position: absolute; inset: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.12) 0%, transparent 50%),
              radial-gradient(circle at 70% 70%, rgba(255,255,255,0.06) 0%, transparent 50%);
  pointer-events: none; z-index: 0;
}

.cover-blue   { background: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%); }
.cover-teal   { background: linear-gradient(135deg, #0b7a4f 0%, #20c997 100%); }
.cover-purple { background: linear-gradient(135deg, #7950f2 0%, #9775fa 100%); }
.cover-amber  { background: linear-gradient(135deg, #e8590c 0%, #f59f00 100%); }

/* 装饰图形 */
.cover-geo { position: absolute; border-radius: 50%; pointer-events: none; z-index: 0; transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cover-diagonal { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cover-icon-wrap { transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.cover-name { transition: text-shadow 0.3s ease; }
.cover-geo.ring {
  width: 140px; height: 140px; border: 3px solid rgba(255,255,255,0.15);
  top: -40px; right: -40px;
}
.cover-geo.dot {
  width: 20px; height: 20px; background: rgba(255,255,255,0.2);
}
.cover-geo.dot-1 { top: 24px; left: 24px; }
.cover-geo.dot-2 { bottom: 20px; right: 32px; width: 12px; height: 12px; }
.cover-diagonal {
  position: absolute; z-index: 0; pointer-events: none;
  width: 200px; height: 3px; background: rgba(255,255,255,0.1);
  top: 50%; left: -40px; transform: rotate(-25deg);
}

/* 图标 */
.cover-icon-wrap {
  position: relative; z-index: 1;
  color: rgba(255,255,255,0.25); margin-right: 16px; flex-shrink: 0;
}

/* 文字 */
.cover-text-area {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; gap: 4px; max-width: 55%;
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

/* 积分徽章 */
.badge-bonus {
  position: absolute; top: 12px; right: 12px;
  display: inline-flex; align-items: center; gap: 3px;
  background: var(--cb-warning); color: #fff;
  padding: 4px 10px; border-radius: 20px;
  font-size: var(--cb-text-xs); font-weight: 600;
  z-index: 2; box-shadow: var(--cb-shadow-sm);
}

.cover-overlay {
  position: absolute; inset: 0; z-index: 0; pointer-events: none;
  background: linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 50%);
}

.card-info {
  padding: 18px;
}

.card-title {
  font-size: var(--cb-text-lg);
  font-weight: 600;
  color: var(--cb-charcoal);
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
  margin-bottom: 6px;
}

.card-meta .el-icon {
  color: var(--cb-primary-light);
  flex-shrink: 0;
}

.card-desc {
  font-size: var(--cb-text-sm);
  color: var(--cb-slate);
  line-height: 1.5;
  margin: 10px 0 12px;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
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

.project-card:hover .detail-link {
  color: var(--cb-primary-dark);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--cb-muted);
  background: var(--cb-bg-card);
  border-radius: var(--cb-radius-lg);
  border: 1px dashed var(--cb-border);
}

.empty-state .empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
  color: var(--cb-muted);
  opacity: 0.6;
}

.empty-state .empty-title {
  font-size: var(--cb-text-lg);
  font-weight: 600;
  color: var(--cb-slate);
  margin-bottom: 8px;
}

.empty-state .empty-text {
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
}

@media (max-width: 768px) {
  .project-student {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
