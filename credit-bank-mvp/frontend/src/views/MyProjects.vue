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
      <el-col :span="8" :xs="24" :sm="12" :md="8" v-for="item in list" :key="item.enrollmentId" class="card-col">
        <el-card class="project-card" shadow="hover" @click="goDetail(item.projectId)">
          <div class="card-cover">
            <span class="cover-text">{{ item.projectName }}</span>
            <span v-if="item.status" class="badge-status" :class="statusClass(item.status)">
              {{ item.status }}
            </span>
            <div class="cover-overlay"></div>
          </div>
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
              <span>报名时间：{{ fmt(item.enrolledAt) }}</span>
            </div>
            <div class="card-desc">
              {{ (item.description || '').substring(0, 70) }}{{ (item.description || '').length > 70 ? '...' : '' }}
            </div>
            <div class="card-footer">
              <el-tag :type="statusType(item.status)" size="small" effect="light">{{ item.status }}</el-tag>
              <div class="footer-actions">
                <el-button
                  v-if="item.status === '进行中'"
                  size="small"
                  type="success"
                  plain
                  :icon="Check"
                  @click.stop="handleSubmit(item)"
                >
                  提交完成
                </el-button>
                <span class="detail-link">
                  查看详情
                  <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state">
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
  OfficeBuilding, User, Clock, Check, ArrowRight, FolderOpened
} from '@element-plus/icons-vue'
import { getMyProjects, submitProjectForReview } from '@/api/project'

const router = useRouter()
const loading = ref(true)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    list.value = await getMyProjects()
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function goDetail(projectId) {
  router.push('/project/' + projectId)
}

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

async function handleSubmit(item) {
  try {
    await ElMessageBox.confirm('确定要提交项目完成申请吗？', '提交完成', { type: 'info' })
    await submitProjectForReview(item.enrollmentId)
    ElMessage.success('已提交，等待机构管理员审核')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '提交失败')
  }
}

function statusType(status) {
  if (status === '已完成') return 'success'
  if (status === '待审核') return 'warning'
  if (status === '进行中') return 'warning'
  if (status === '已报名') return 'primary'
  return 'info'
}

function statusClass(status) {
  if (status === '已完成') return 'success'
  if (status === '待审核') return 'warning'
  if (status === '进行中') return 'warning'
  if (status === '已报名') return 'primary'
  return 'info'
}

onMounted(() => { loadData() })
</script>

<style scoped>
.my-projects {
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
  transition: transform var(--cb-transition-normal), box-shadow var(--cb-transition-normal);
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--cb-shadow-lg);
}

.project-card :deep(.el-card__body) {
  padding: 0;
}

.card-cover {
  height: 170px;
  background: linear-gradient(135deg, var(--cb-success) 0%, var(--cb-success-light) 100%);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--cb-radius-lg) var(--cb-radius-lg) 0 0;
  overflow: hidden;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 40%, rgba(0, 0, 0, 0.45) 100%);
  pointer-events: none;
}

.cover-text {
  color: #fff;
  font-size: var(--cb-text-xl);
  font-weight: 600;
  padding: 16px;
  text-align: center;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 1;
}

.badge-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: var(--cb-text-xs);
  font-weight: 600;
  z-index: 2;
  box-shadow: var(--cb-shadow-sm);
}

.badge-status.primary {
  background: var(--cb-primary);
  color: #fff;
}

.badge-status.success {
  background: var(--cb-success);
  color: #fff;
}

.badge-status.warning {
  background: var(--cb-warning);
  color: #fff;
}

.badge-status.info {
  background: var(--cb-info);
  color: #fff;
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
  color: var(--cb-success-light);
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

.footer-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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
  .my-projects {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .footer-actions {
    flex-wrap: wrap;
  }
}
</style>
