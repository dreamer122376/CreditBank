<template>
  <div class="cert-requirement">
    <el-page-header @back="goBack" style="margin-bottom: 16px;">
      <template #title>
        <span>返回</span>
      </template>
      <template #content>
        <span class="page-title">执行标准文件</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div>
            <span class="standard-title">{{ standard.standardName }}</span>
            <el-tag size="small" type="info" style="margin-left: 8px;">v{{ standard.version }}</el-tag>
            <el-tag size="small" :type="roleTagType(standard.targetRole)" style="margin-left: 4px;">
              {{ ROLE_NAME[standard.targetRole] }}
            </el-tag>
          </div>
          <el-button size="small" @click="goBack">返回列表</el-button>
        </div>
      </template>

      <div class="meta-row">
        <span class="meta-item"><strong>认证标准ID：</strong>{{ standard.id }}</span>
        <span class="meta-item"><strong>归属机构：</strong>{{ standard.orgName || '平台通用' }}</span>
        <span class="meta-item"><strong>人工审核：</strong>{{ standard.needManualAudit === 1 ? '需要' : '不需要' }}</span>
        <span class="meta-item"><strong>创建时间：</strong>{{ standard.createdAt }}</span>
      </div>

      <el-divider />

      <div class="requirement-body">
        <pre class="requirement-text">{{ standard.requirementText || '（暂无执行标准正文）' }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCertStandardById } from '@/api/certStandard'

const route = useRoute()
const router = useRouter()

const ROLE_NAME = {
  student: '学生',
  expert: '专家',
  org_admin: '机构'
}

const standard = ref({})
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    standard.value = await getCertStandardById(route.params.id)
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
})

function roleTagType(role) {
  const map = { student: 'success', expert: 'warning', org_admin: 'primary' }
  return map[role] || 'info'
}

function goBack() {
  router.push('/cert-standards')
}
</script>

<style scoped>
.page-title {
  font-weight: 600;
  font-size: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.standard-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  color: #495057;
  font-size: 13px;
}

.meta-item strong {
  color: #868e96;
  font-weight: normal;
}

.requirement-body {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 20px 24px;
}

.requirement-text {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  font-size: 14px;
  line-height: 2;
  color: #2c3e50;
}
</style>
