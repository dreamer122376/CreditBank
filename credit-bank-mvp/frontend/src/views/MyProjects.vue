<template>
  <div class="my-projects">
    <h3 style="margin-bottom:18px;">我报名的项目</h3>

    <el-row :gutter="18" v-loading="loading">
      <el-col :span="8" v-for="item in list" :key="item.enrollmentId" style="margin-bottom: 18px;">
        <el-card class="project-card" shadow="hover" @click="goDetail(item.projectId)">
          <div class="card-cover">
            <span class="cover-text">{{ item.projectName }}</span>
            <span v-if="item.status" class="badge-status">{{ item.status }}</span>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.projectName }}</div>
            <div class="card-meta">🏛️ {{ item.orgName || '未知机构' }}</div>
            <div class="card-meta">👨‍🏫 {{ item.expertName || '暂未指定' }}</div>
            <div class="card-meta">📅 报名时间：{{ fmt(item.enrolledAt) }}</div>
            <div class="card-desc">{{ (item.description || '').substring(0, 80) }}{{ (item.description || '').length > 80 ? '...' : '' }}</div>
            <div class="card-footer">
              <el-tag :type="statusType(item.status)" size="small">{{ item.status }}</el-tag>
              <div style="display:flex;gap:8px;align-items:center;">
                <el-button
                  v-if="item.status === '进行中'"
                  size="small"
                  type="success"
                  plain
                  @click.stop="handleSubmit(item)"
                >提交完成</el-button>
                <span style="color:#3b5bdb;font-size:13px;">查看详情 →</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!loading && list.length === 0" style="text-align:center;padding:60px;color:#868e96;">
      <div style="font-size:48px;margin-bottom:12px;">📋</div>
      <h3>暂无报名项目</h3>
      <p>您还没有报名任何项目，快去项目报名页面看看吧。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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

onMounted(() => { loadData() })
</script>

<style scoped>
.project-card { cursor: pointer; overflow: hidden; border-radius: 8px; }
.project-card:hover { transform: translateY(-2px); }
.card-cover {
  height: 170px;
  background: linear-gradient(135deg, #2b8a3e 0%, #69db7c 100%);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px 8px 0 0;
  margin: -20px -20px 0;
}
.card-cover::after {
  content: '';
  position: absolute; inset: 0;
  background: linear-gradient(to bottom, transparent 50%, rgba(0,0,0,0.35) 100%);
  border-radius: 8px 8px 0 0;
}
.cover-text { color: #fff; font-size: 16px; font-weight: 600; padding: 16px; text-align: center; text-shadow: 0 1px 3px rgba(0,0,0,0.5); position: relative; z-index: 1; }
.badge-status { position: absolute; top: 8px; right: 8px; background: #f59f00; color: #fff;
  padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; z-index: 2; }
.card-info { margin-top: 12px; }
.card-title { font-size: 15px; font-weight: 600; color: #2c3e50; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-meta { font-size: 12px; color: #868e96; margin-bottom: 4px; }
.card-desc { font-size: 13px; color: #495057; margin: 8px 0; line-height: 1.5; }
.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 8px; border-top: 1px solid #f0f0f0; }
</style>
