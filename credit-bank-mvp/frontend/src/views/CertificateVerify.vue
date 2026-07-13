<template>
  <div class="verify-page">
    <section class="verify-shell">
      <div class="brand">
        <div class="brand-mark">CB</div>
        <div>
          <h1>证书验真</h1>
          <p>输入证书编号和核验码，确认学分银行证书状态。</p>
        </div>
      </div>

      <el-card class="verify-card">
        <el-form :model="form" label-position="top">
          <el-form-item label="证书编号">
            <el-input v-model="form.certNo" placeholder="例如 CB-20260713-0008-0005" clearable />
          </el-form-item>
          <el-form-item label="核验码">
            <el-input v-model="form.verifyCode" placeholder="请输入证书上的核验码" clearable />
          </el-form-item>
          <el-button type="primary" :loading="loading" @click="verify">立即核验</el-button>
        </el-form>
      </el-card>

      <el-card v-if="result" class="result-card" :class="{ invalid: !result.valid }">
        <div class="result-head">
          <el-tag :type="result.valid ? 'success' : 'danger'" size="large">
            {{ result.message }}
          </el-tag>
          <span class="status">{{ statusText(result.status) }}</span>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="证书编号">{{ result.certNo }}</el-descriptions-item>
          <el-descriptions-item label="持证人">{{ result.studentName }}</el-descriptions-item>
          <el-descriptions-item label="证书名称">{{ result.certName }}</el-descriptions-item>
          <el-descriptions-item label="签发机构">{{ result.orgName }}</el-descriptions-item>
          <el-descriptions-item label="签发时间">{{ formatTime(result.issuedAt) }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ formatTime(result.validUntil) }}</el-descriptions-item>
          <el-descriptions-item v-if="result.revokedAt" label="作废时间">
            {{ formatTime(result.revokedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { verifyStudentCert } from '@/api/studentCert'

const route = useRoute()
const loading = ref(false)
const result = ref(null)
const form = ref({
  certNo: '',
  verifyCode: ''
})

onMounted(() => {
  form.value.certNo = route.query.certNo || ''
  form.value.verifyCode = route.query.verifyCode || ''
  if (form.value.certNo && form.value.verifyCode) {
    verify()
  }
})

async function verify() {
  if (!form.value.certNo.trim() || !form.value.verifyCode.trim()) {
    ElMessage.warning('请填写证书编号和核验码')
    return
  }
  loading.value = true
  result.value = null
  try {
    result.value = await verifyStudentCert(form.value.certNo.trim(), form.value.verifyCode.trim())
  } catch (error) {
    ElMessage.error(error.message || '核验失败')
  } finally {
    loading.value = false
  }
}

function statusText(status) {
  const map = { VALID: '有效证书', REVOKED: '已作废', EXPIRED: '已过期' }
  return map[status] || status || ''
}

function formatTime(time) {
  return time ? String(time).replace('T', ' ').slice(0, 19) : '-'
}
</script>

<style scoped>
.verify-page {
  min-height: 100vh;
  background: #f3f5f8;
  padding: 48px 20px;
}

.verify-shell {
  max-width: 760px;
  margin: 0 auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}

.brand-mark {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border: 2px solid #173b67;
  color: #173b67;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 22px;
  font-weight: 700;
  background: #fff;
}

h1 {
  margin: 0;
  color: #173b67;
  font-size: 28px;
}

p {
  margin: 6px 0 0;
  color: #606266;
}

.verify-card,
.result-card {
  border-radius: 6px;
}

.result-card {
  margin-top: 16px;
  border-top: 4px solid #0f8a5f;
}

.result-card.invalid {
  border-top-color: #c0392b;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.status {
  color: #606266;
  font-size: 13px;
}
</style>
