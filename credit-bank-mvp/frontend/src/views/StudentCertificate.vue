<template>
  <div class="certificate-page">
    <div class="toolbar">
      <el-button @click="router.back()">返回</el-button>
      <el-button type="primary" @click="printCert">打印证书</el-button>
    </div>

    <el-card v-if="loading" class="loading-card">加载中...</el-card>

    <section v-else-if="cert" class="certificate">
      <div class="cert-border">
        <div class="brand">Credit Bank</div>
        <h1>学习能力认证证书</h1>
        <p class="cert-no">证书编号：{{ cert.certNo }}</p>

        <div class="recipient">
          <span>{{ cert.studentName }}</span>
        </div>

        <p class="statement">
          经平台认证流程审核，该学生已达到
          <strong>{{ cert.certName }}</strong>
          的认证要求，特此证明。
        </p>

        <div class="meta-grid">
          <div>
            <label>发证机构</label>
            <strong>{{ cert.orgName || '学分银行平台' }}</strong>
          </div>
          <div>
            <label>颁发日期</label>
            <strong>{{ formatDate(cert.issuedAt) }}</strong>
          </div>
          <div>
            <label>有效期至</label>
            <strong>{{ formatDate(cert.validUntil) }}</strong>
          </div>
          <div>
            <label>核验码</label>
            <strong>{{ cert.verifyCode }}</strong>
          </div>
        </div>

        <div class="seal">
          <span>学分银行</span>
          <small>认证专用章</small>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getStudentCert } from '@/api/studentCert'

const route = useRoute()
const router = useRouter()
const { currentUser } = useAuth()
const cert = ref(null)
const loading = ref(true)

onMounted(loadCert)

async function loadCert() {
  try {
    cert.value = await getStudentCert(route.params.id, currentUser.value?.role, currentUser.value?.id)
  } catch (error) {
    ElMessage.error(error.message || '证书加载失败')
  } finally {
    loading.value = false
  }
}

function formatDate(time) {
  if (!time) return '—'
  return String(time).slice(0, 10)
}

function printCert() {
  window.print()
}
</script>

<style scoped>
.certificate-page {
  max-width: 980px;
  margin: 0 auto;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 16px;
}

.loading-card {
  text-align: center;
  color: #606266;
}

.certificate {
  background: #fff;
  padding: 28px;
  box-shadow: 0 10px 30px rgba(31, 45, 61, 0.12);
}

.cert-border {
  position: relative;
  min-height: 620px;
  border: 8px double #2f5597;
  padding: 58px 68px;
  text-align: center;
  background:
    linear-gradient(135deg, rgba(47, 85, 151, 0.08), transparent 42%),
    linear-gradient(315deg, rgba(198, 142, 45, 0.1), transparent 45%),
    #fffdf8;
}

.brand {
  color: #2f5597;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

h1 {
  margin: 32px 0 10px;
  color: #1f2d3d;
  font-size: 38px;
  font-weight: 700;
  letter-spacing: 0;
}

.cert-no {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.recipient {
  margin: 58px auto 34px;
  max-width: 420px;
  border-bottom: 2px solid #c68e2d;
  color: #1f2d3d;
  font-size: 42px;
  font-weight: 700;
  line-height: 1.6;
}

.statement {
  max-width: 680px;
  margin: 0 auto 42px;
  color: #303133;
  font-size: 18px;
  line-height: 2;
}

.statement strong {
  color: #2f5597;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px 28px;
  max-width: 620px;
  margin: 0 auto;
  text-align: left;
}

.meta-grid div {
  border-bottom: 1px solid #dcdfe6;
  padding-bottom: 10px;
}

.meta-grid label {
  display: block;
  color: #909399;
  font-size: 12px;
  margin-bottom: 4px;
}

.meta-grid strong {
  color: #303133;
  font-size: 15px;
}

.seal {
  position: absolute;
  right: 68px;
  bottom: 54px;
  width: 118px;
  height: 118px;
  border: 4px solid #c0392b;
  border-radius: 50%;
  color: #c0392b;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: rotate(-12deg);
  font-weight: 700;
}

.seal small {
  margin-top: 4px;
  font-size: 12px;
}

@media print {
  :global(.sidebar),
  :global(.topbar),
  .toolbar {
    display: none !important;
  }

  :global(.main) {
    margin-left: 0 !important;
  }

  :global(.content) {
    padding: 0 !important;
    background: #fff !important;
  }

  .certificate-page {
    max-width: none;
  }

  .certificate {
    box-shadow: none;
    padding: 0;
  }
}
</style>
