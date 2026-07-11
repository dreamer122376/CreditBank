<template>
  <div class="my-projects">
    <h3 style="margin-bottom:18px;">我报名的项目</h3>

    <el-row :gutter="18" v-loading="loading">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 18px;">
        <el-card class="project-card" shadow="hover">
          <div class="card-cover">
            <span class="cover-text">{{ item.name }}</span>
            <span v-if="item.studentProjectStatus" class="badge-status">{{ item.studentProjectStatus }}</span>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.name }}</div>
            <div class="card-meta">📅 {{ fmt(item.createdAt) }}</div>
            <div class="card-meta" v-if="item.status === 1">
              <el-tag type="success" size="small">进行中</el-tag>
            </div>
            <div class="card-meta" v-else-if="item.status === 0">
              <el-tag type="warning" size="small">未开始</el-tag>
            </div>
            <div class="card-meta" v-else>
              <el-tag type="info" size="small">已结束</el-tag>
            </div>
            <div class="card-desc">{{ (item.description || '').substring(0, 80) }}{{ (item.description || '').length > 80 ? '...' : '' }}</div>
            <div class="card-footer">
              <span style="color:#3b5bdb;font-size:13px;">查看详情 →</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!loading && list.length === 0" style="text-align:center;padding:60px;color:#868e96;">
      <div style="font-size:48px;margin-bottom:12px;">📋</div>
      <h3>暂无报名项目</h3>
      <p>您还没有报名任何项目，请关注后续项目通知。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyProjectDetail } from '@/api/project'

const loading = ref(true)
const list = ref([])

async function loadData() {
  loading.value = true
  try {
    const user = await getMyProjectDetail()
    if (user && user.projects && user.studentProjects) {
      const spMap = {}
      user.studentProjects.forEach(sp => {
        spMap[sp.projectId] = sp.status
      })
      list.value = user.projects.map(project => ({
        ...project,
        studentProjectStatus: spMap[project.id]
      }))
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

onMounted(() => { loadData() })
</script>

<style scoped>
.project-card { cursor: pointer; overflow: hidden; border-radius: 8px; }
.project-card:hover { transform: translateY(-2px); }
.card-cover {
  height: 170px;
  background: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%);
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
.card-footer { display: flex; justify-content: flex-end; padding-top: 8px; border-top: 1px solid #f0f0f0; }
</style>