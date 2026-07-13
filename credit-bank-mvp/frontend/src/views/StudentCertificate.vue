<template>
  <div class="certificate-page">
    <div class="toolbar">
      <el-button @click="router.back()">返回</el-button>
      <el-button type="primary" @click="printCert">打印证书</el-button>
    </div>

    <el-card v-if="loading" class="loading-card">加载中...</el-card>

    <section v-else-if="cert" class="certificate" aria-label="学生证书">
      <div class="cert-paper">
        <div class="corner top-left"></div>
        <div class="corner top-right"></div>
        <div class="corner bottom-left"></div>
        <div class="corner bottom-right"></div>
        <div class="watermark">CB</div>

        <header class="cert-header">
          <div class="issuer-mark">
            <span>CB</span>
          </div>
          <div class="issuer-copy">
            <strong>学分银行认证中心</strong>
            <small>Credit Bank Credential Office</small>
          </div>
          <div class="serial">
            <label>证书编号</label>
            <span>{{ cert.certNo }}</span>
          </div>
        </header>

        <main class="cert-body">
          <p class="eyebrow">Official Learning Credential</p>
          <h1>学习能力认证证书</h1>
          <div class="title-rule"></div>

          <div class="recipient-block">
            <span class="recipient-label">兹证明</span>
            <strong>{{ cert.studentName }}</strong>
          </div>

          <p class="statement">
            经平台认证流程审核，已达到
            <strong>{{ cert.certName }}</strong>
            的认证标准与能力要求，特授予本证书。
          </p>
        </main>

        <div class="stamp-layer" aria-hidden="true">
          <svg class="seal-svg bank-seal" viewBox="0 0 180 180" role="img">
            <defs>
              <path id="bankSealArc" d="M 34 92 A 56 56 0 0 1 146 92" />
              <filter id="bankSealRoughen">
                <feTurbulence type="fractalNoise" baseFrequency="0.95" numOctaves="2" seed="8" result="noise" />
                <feDisplacementMap in="SourceGraphic" in2="noise" scale="1.1" />
              </filter>
            </defs>
            <g filter="url(#bankSealRoughen)">
              <circle class="seal-outer" cx="90" cy="90" r="76" />
              <circle class="seal-inner" cx="90" cy="90" r="61" />
              <text class="seal-arc">
                <textPath href="#bankSealArc" startOffset="50%" text-anchor="middle">学 分 银 行 认 证 中 心</textPath>
              </text>
              <text class="seal-star" x="90" y="90" text-anchor="middle">★</text>
              <text class="seal-main" x="90" y="118" text-anchor="middle">认证专用章</text>
              <line class="seal-line" x1="47" y1="126" x2="133" y2="126" />
              <text class="seal-sub" x="90" y="141" text-anchor="middle">CREDIT BANK</text>
            </g>
          </svg>

          <svg class="seal-svg org-seal" viewBox="0 0 180 180" role="img">
            <defs>
              <path id="orgSealArc" d="M 34 92 A 56 56 0 0 1 146 92" />
              <filter id="orgSealRoughen">
                <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2" seed="13" result="noise" />
                <feDisplacementMap in="SourceGraphic" in2="noise" scale="1" />
              </filter>
            </defs>
            <g filter="url(#orgSealRoughen)">
              <circle class="seal-outer" cx="90" cy="90" r="76" />
              <circle class="seal-inner" cx="90" cy="90" r="61" />
              <text class="seal-arc org-arc">
                <textPath href="#orgSealArc" startOffset="50%" text-anchor="middle">{{ sealOrgName }}</textPath>
              </text>
              <text class="seal-star" x="90" y="90" text-anchor="middle">★</text>
              <text class="seal-main" x="90" y="118" text-anchor="middle">机构认证章</text>
              <line class="seal-line" x1="47" y1="126" x2="133" y2="126" />
              <text class="seal-sub" x="90" y="141" text-anchor="middle">AUTHORIZED</text>
            </g>
          </svg>
        </div>

        <footer class="cert-footer">
          <div class="signature-block">
            <div class="signature-line"></div>
            <label>发证机构</label>
            <strong>{{ cert.orgName || '学分银行平台' }}</strong>
          </div>

          <div class="verify-panel">
            <div>
              <label>颁发日期</label>
              <strong>{{ formatDate(cert.issuedAt) }}</strong>
            </div>
            <div>
              <label>有效期至</label>
              <strong>{{ formatDate(cert.validUntil) }}</strong>
            </div>
            <div class="verify-code">
              <label>核验码</label>
              <strong>{{ cert.verifyCode }}</strong>
            </div>
          </div>
        </footer>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getStudentCert } from '@/api/studentCert'

const route = useRoute()
const router = useRouter()
const { currentUser } = useAuth()
const cert = ref(null)
const loading = ref(true)

const sealOrgName = computed(() => {
  const name = cert.value?.orgName || '学分银行平台'
  return name.length <= 8 ? name.split('').join(' ') : name
})

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
  max-width: 1160px;
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
  position: relative;
  background: #fff;
  padding: 30px;
  box-shadow: 0 18px 46px rgba(26, 38, 58, 0.18);
}

.cert-paper {
  position: relative;
  min-height: 690px;
  aspect-ratio: 1.414 / 1;
  overflow: hidden;
  padding: 54px 68px 48px;
  text-align: center;
  border: 1px solid #b68b39;
  background:
    radial-gradient(circle at 50% 48%, rgba(25, 64, 114, 0.065), transparent 0 24%, transparent 36%),
    repeating-linear-gradient(45deg, rgba(182, 139, 57, 0.035) 0 1px, transparent 1px 12px),
    linear-gradient(135deg, #fffdf8 0%, #f7f1e6 100%);
}

.cert-paper::before,
.cert-paper::after {
  content: "";
  position: absolute;
  inset: 18px;
  pointer-events: none;
}

.cert-paper::before {
  border: 2px solid #173b67;
}

.cert-paper::after {
  inset: 27px;
  border: 1px solid rgba(182, 139, 57, 0.8);
}

.corner {
  position: absolute;
  z-index: 1;
  width: 74px;
  height: 74px;
  border-color: #b68b39;
  pointer-events: none;
}

.corner.top-left {
  top: 28px;
  left: 28px;
  border-top: 3px solid;
  border-left: 3px solid;
}

.corner.top-right {
  top: 28px;
  right: 28px;
  border-top: 3px solid;
  border-right: 3px solid;
}

.corner.bottom-left {
  bottom: 28px;
  left: 28px;
  border-bottom: 3px solid;
  border-left: 3px solid;
}

.corner.bottom-right {
  right: 28px;
  bottom: 28px;
  border-right: 3px solid;
  border-bottom: 3px solid;
}

.watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  color: rgba(23, 59, 103, 0.055);
  font-family: Georgia, "Times New Roman", serif;
  font-size: 210px;
  font-weight: 700;
  line-height: 1;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.cert-header,
.cert-body,
.cert-footer {
  position: relative;
  z-index: 2;
}

.cert-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.issuer-mark {
  width: 54px;
  height: 54px;
  border: 2px solid #173b67;
  color: #173b67;
  display: grid;
  place-items: center;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 21px;
  font-weight: 700;
}

.issuer-copy strong {
  display: block;
  color: #173b67;
  font-size: 17px;
  font-weight: 700;
}

.issuer-copy small {
  display: block;
  margin-top: 3px;
  color: #6c5b40;
  font-size: 11px;
  text-transform: uppercase;
}

.serial {
  min-width: 230px;
  border-left: 1px solid rgba(23, 59, 103, 0.24);
  padding-left: 18px;
}

.serial label,
.verify-panel label,
.signature-block label {
  display: block;
  color: #7c6d55;
  font-size: 12px;
  margin-bottom: 5px;
}

.serial span {
  color: #173b67;
  font-family: "Times New Roman", Georgia, serif;
  font-size: 16px;
  font-weight: 700;
}

.cert-body {
  padding-top: 70px;
}

.eyebrow {
  margin: 0;
  color: #8a6a2b;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 13px;
  text-transform: uppercase;
}

h1 {
  margin: 14px 0 16px;
  color: #173b67;
  font-family: "SimSun", "Songti SC", serif;
  font-size: 48px;
  font-weight: 700;
  letter-spacing: 0;
}

.title-rule {
  width: 230px;
  height: 5px;
  margin: 0 auto;
  border-top: 1px solid #b68b39;
  border-bottom: 1px solid #b68b39;
}

.recipient-block {
  margin: 52px auto 28px;
}

.recipient-label {
  display: block;
  color: #6c5b40;
  font-size: 15px;
  margin-bottom: 6px;
}

.recipient-block strong {
  display: inline-block;
  min-width: 360px;
  border-bottom: 2px solid #173b67;
  color: #1f2d3d;
  font-family: "SimSun", "Songti SC", serif;
  font-size: 46px;
  font-weight: 700;
  line-height: 1.45;
}

.statement {
  max-width: 720px;
  margin: 0 auto;
  color: #27384d;
  font-size: 19px;
  line-height: 2.1;
}

.statement strong {
  color: #173b67;
  font-weight: 700;
}

.stamp-layer {
  position: absolute;
  z-index: 4;
  left: 52%;
  bottom: 112px;
  width: 360px;
  height: 164px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  pointer-events: none;
  transform: translateX(-50%);
}

.seal-svg {
  width: 158px;
  height: 158px;
  color: rgba(174, 31, 29, 0.84);
  opacity: 0.9;
  mix-blend-mode: multiply;
  overflow: visible;
}

.bank-seal,
.org-seal {
  transform: rotate(0deg);
}

.org-seal {
  color: rgba(186, 42, 34, 0.82);
}

.seal-outer,
.seal-inner {
  fill: none;
  stroke: currentColor;
}

.seal-outer {
  stroke-width: 5.2;
}

.seal-inner {
  stroke-width: 1.8;
  stroke-dasharray: 2 2.8;
}

.seal-arc {
  fill: currentColor;
  font-family: "KaiTi", "STKaiti", "SimKai", "SimSun", serif;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 2px;
}

.org-arc {
  font-size: 14px;
  letter-spacing: 1.4px;
}

.seal-star {
  fill: currentColor;
  font-family: "SimSun", serif;
  font-size: 37px;
  font-weight: 700;
}

.seal-main {
  fill: currentColor;
  font-family: "KaiTi", "STKaiti", "SimKai", "SimSun", serif;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 2px;
}

.seal-line {
  stroke: currentColor;
  stroke-width: 1.4;
}

.seal-sub {
  fill: currentColor;
  font-family: "Times New Roman", Georgia, serif;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0;
}

.cert-footer {
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: end;
  gap: 84px;
  margin-top: 72px;
  text-align: left;
}

.signature-block {
  padding-left: 16px;
}

.signature-line {
  width: 210px;
  height: 1px;
  margin-bottom: 12px;
  background: #173b67;
}

.signature-block strong {
  color: #27384d;
  font-size: 15px;
}

.verify-panel {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 14px;
}

.verify-panel > div {
  border-bottom: 1px solid rgba(23, 59, 103, 0.22);
  padding-bottom: 8px;
}

.verify-panel strong {
  color: #27384d;
  font-size: 14px;
}

.verify-code {
  grid-column: 1 / -1;
}

.verify-code strong {
  color: #173b67;
  font-family: "Times New Roman", Georgia, serif;
  font-size: 16px;
  letter-spacing: 0;
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

  .cert-paper {
    min-height: auto;
    width: 297mm;
    height: 210mm;
  }
}
</style>
