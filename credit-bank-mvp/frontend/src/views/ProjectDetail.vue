<template>
  <div class="project-detail" v-loading="loading">
    <template v-if="project">
      <!-- 封面 -->
      <div class="detail-cover">
        <div class="cover-overlay">
          <span class="cover-placeholder">{{ project.name }}</span>
        </div>
      </div>

      <!-- 信息卡片 -->
      <el-card class="detail-card">
        <h2 class="detail-title">{{ project.name }}</h2>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🏛️</span>
              <div><div class="meta-label">主办机构</div><div class="meta-val">{{ project.orgName || '未设置' }}</div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">👨‍🏫</span>
              <div><div class="meta-label">负责专家</div><div class="meta-val">{{ project.expertName || '未指定' }}</div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🏷️</span>
              <div><div class="meta-label">{{ project.enrolled ? '报名状态' : '项目状态' }}</div><div class="meta-val"><el-tag :type="project.enrolled ? enrollmentStatusType(project.enrollmentStatus) : statusType(project.status)" size="small">{{ project.enrolled ? project.enrollmentStatus : project.statusName }}</el-tag></div></div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top:12px;">
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🎁</span>
              <div><div class="meta-label">积分奖励</div><div class="meta-val" style="color:#f59f00;font-weight:700;">+{{ project.creditReward || 0 }} 积分</div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">💰</span>
              <div><div class="meta-label">报名费用</div><div class="meta-val">{{ project.creditPrice || 0 }} 积分</div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">📅</span>
              <div><div class="meta-label">发布时间</div><div class="meta-val">{{ fmt(project.createdAt) }}</div></div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 项目详情 -->
      <el-card class="detail-card">
        <template #header><h3 style="margin:0;">📝 项目简介</h3></template>
        <div class="detail-desc" v-if="project.description">
          <p v-for="(line, idx) in descLines" :key="idx">{{ line }}</p>
        </div>
        <div v-else style="color:#868e96;">暂无项目简介</div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="detail-actions">
        <el-button @click="goBack">← 返回列表</el-button>
        <template v-if="project.status === 1 && !isFrozen">
          <el-button v-if="!project.enrolled" type="success" @click="handleEnroll" :loading="enrolling">✅ 立即报名</el-button>
          <el-button v-else type="warning" @click="handleCancel" :loading="canceling">↩️ 取消报名</el-button>
        </template>
      </div>
    </template>
    <div v-else-if="!loading" style="text-align:center;padding:60px;color:#868e96;">❌ 项目不存在</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectDetail, enrollProject, cancelEnrollProject } from '@/api/project'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const route = useRoute()
const { currentUser, isFrozen } = useAuth()
const loading = ref(true)
const project = ref(null)
const enrolling = ref(false)
const canceling = ref(false)

const descLines = computed(() => {
  if (!project.value?.description) return []
  return project.value.description.split('\n').filter(l => l.trim())
})

async function loadDetail() {
  loading.value = true
  try {
    const studentId = currentUser.value?.id
    project.value = await getProjectDetail(route.params.id, studentId)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/projects')
  }
}

async function handleEnroll() {
  if (project.value.status !== 1) {
    ElMessage.warning('该项目当前不可报名')
    return
  }
  try {
    await ElMessageBox.confirm('确定报名【' + project.value.name + '】吗？', '确认报名', { type: 'info' })
  } catch (e) {
    return
  }

  enrolling.value = true
  try {
    await enrollProject(project.value.id)
    ElMessage.success('报名成功')
    project.value.enrolled = true
  } catch (e) {
    ElMessage.error(e.message || '报名失败')
  } finally {
    enrolling.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消报名吗？', '确认取消', { type: 'warning' })
  } catch (e) {
    return
  }

  canceling.value = true
  try {
    await cancelEnrollProject(project.value.id)
    ElMessage.success('已取消报名')
    project.value.enrolled = false
  } catch (e) {
    ElMessage.error(e.message || '取消报名失败')
  } finally {
    canceling.value = false
  }
}

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

function statusType(s) {
  if (s === 1) return 'success'
  if (s === 0) return 'warning'
  if (s === 2) return 'danger'
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

onMounted(() => { loadDetail() })
</script>

<style scoped>
.detail-cover {
  height: 300px;
  background-size: cover;
  background-position: center;
  border-radius: 10px;
  background-color: #3b5bdb;
  background-image: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%);
  position: relative;
  overflow: hidden;
  margin-bottom: 18px;
}
.cover-overlay {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(to bottom, transparent 60%, rgba(0,0,0,0.3) 100%);
}
.cover-placeholder {
  color: #fff; font-size: 22px; font-weight: 600;
  text-shadow: 0 1px 4px rgba(0,0,0,0.3); text-align: center; padding: 24px;
}
.detail-card { margin-bottom: 18px; }
.detail-title { font-size: 20px; font-weight: 700; margin-bottom: 14px; color: #2c3e50; }
.meta-item { display: flex; align-items: flex-start; gap: 8px; }
.meta-icon { font-size: 18px; flex-shrink: 0; }
.meta-label { font-size: 12px; color: #868e96; }
.meta-val { font-size: 14px; color: #2c3e50; }
.detail-desc { line-height: 1.8; color: #495057; }
.detail-desc p { margin-bottom: 8px; }
.detail-actions { display: flex; gap: 10px; padding: 8px 0; }
</style>
