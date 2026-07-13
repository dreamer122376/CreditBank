<template>
  <div class="certificate-page">
    <div class="toolbar">
      <el-button @click="router.back()">返回</el-button>
      <el-button v-if="canRevoke" type="danger" plain @click="revokeCert">作废证书</el-button>
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
          <div class="issuer-mark">CB</div>
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
          <SealSvg class="bank-seal" title="学分银行认证中心" main="认证专用章" sub="CREDIT BANK" />
          <SealSvg class="org-seal" :title="sealOrgName" main="机构认证章" sub="AUTHORIZED" />
        </div>

        <footer class="cert-footer">
          <div class="signature-block">
            <label>发证机构</label>
            <strong>{{ cert.orgName || '学分银行平台' }}</strong>
            <div class="signature-line"></div>
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
            <div>
              <label>核验码</label>
              <strong>{{ cert.verifyCode }}</strong>
            </div>
            <div class="verify-link">
              <label>验真入口</label>
              <span>{{ verifyUrl }}</span>
            </div>
          </div>
        </footer>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { getStudentCert, revokeStudentCert } from '@/api/studentCert'

const SealSvg = defineComponent({
  props: {
    title: { type: String, required: true },
    main: { type: String, required: true },
    sub: { type: String, required: true }
  },
  setup(props) {
    const arcId = `sealArc-${Math.random().toString(36).slice(2)}`
    const filterId = `sealRough-${Math.random().toString(36).slice(2)}`
    return () => h('svg', { class: 'seal-svg', viewBox: '0 0 180 180' }, [
      h('defs', [
        h('path', { id: arcId, d: 'M 34 92 A 56 56 0 0 1 146 92' }),
        h('filter', { id: filterId }, [
          h('feTurbulence', { type: 'fractalNoise', baseFrequency: '0.9', numOctaves: '2', seed: '11', result: 'noise' }),
          h('feDisplacementMap', { in: 'SourceGraphic', in2: 'noise', scale: '1' })
        ])
      ]),
      h('g', { filter: `url(#${filterId})` }, [
        h('circle', { class: 'seal-outer', cx: '90', cy: '90', r: '76' }),
        h('circle', { class: 'seal-inner', cx: '90', cy: '90', r: '61' }),
        h('text', { class: 'seal-arc' }, [
          h('textPath', { href: `#${arcId}`, startOffset: '50%', 'text-anchor': 'middle' }, spacedTitle(props.title))
        ]),
        h('text', { class: 'seal-star', x: '90', y: '90', 'text-anchor': 'middle' }, '★'),
        h('text', { class: 'seal-main', x: '90', y: '118', 'text-anchor': 'middle' }, props.main),
        h('line', { class: 'seal-line', x1: '47', y1: '126', x2: '133', y2: '126' }),
        h('text', { class: 'seal-sub', x: '90', y: '141', 'text-anchor': 'middle' }, props.sub)
      ])
    ])
  }
})

const route = useRoute()
const router = useRouter()
const { currentUser } = useAuth()
const cert = ref(null)
const loading = ref(true)

const sealOrgName = computed(() => cert.value?.orgName || '学分银行平台')

const canRevoke = computed(() => {
  const role = currentUser.value?.role
  return cert.value && (role === 'admin' || role === 'org_admin')
})

const verifyUrl = computed(() => {
  if (!cert.value) return ''
  const query = new URLSearchParams({
    certNo: cert.value.certNo || '',
    verifyCode: cert.value.verifyCode || ''
  })
  return `${window.location.origin}/certificate-verify?${query.toString()}`
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

function spacedTitle(text) {
  return text && text.length <= 10 ? text.split('').join(' ') : text
}

function formatDate(time) {
  return time ? String(time).slice(0, 10) : '-'
}

function printCert() {
  window.print()
}

async function revokeCert() {
  try {
    const { value } = await ElMessageBox.prompt('请输入作废原因。作废后该证书将无法继续作为有效证书验真。', '作废证书', {
      confirmButtonText: '确认作废',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPattern: /\S+/,
      inputErrorMessage: '请填写作废原因',
      type: 'warning'
    })
    await revokeStudentCert(cert.value.id, currentUser.value?.role, currentUser.value?.id, value)
    ElMessage.success('证书已作废')
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '作废失败')
    }
  }
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

.top-left {
  top: 28px;
  left: 28px;
  border-top: 3px solid;
  border-left: 3px solid;
}

.top-right {
  top: 28px;
  right: 28px;
  border-top: 3px solid;
  border-right: 3px solid;
}

.bottom-left {
  bottom: 28px;
  left: 28px;
  border-bottom: 3px solid;
  border-left: 3px solid;
}

.bottom-right {
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
  left: 61%;
  bottom: 138px;
  width: 340px;
  height: 164px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  pointer-events: none;
  transform: translateX(-50%);
}

:deep(.seal-svg) {
  width: 158px;
  height: 158px;
  color: rgba(174, 31, 29, 0.84);
  opacity: 0.9;
  mix-blend-mode: multiply;
  overflow: visible;
}

:deep(.org-seal) {
  color: rgba(186, 42, 34, 0.82);
}

:deep(.seal-outer),
:deep(.seal-inner) {
  fill: none;
  stroke: currentColor;
}

:deep(.seal-outer) {
  stroke-width: 5.2;
}

:deep(.seal-inner) {
  stroke-width: 1.8;
  stroke-dasharray: 2 2.8;
}

:deep(.seal-arc) {
  fill: currentColor;
  font-family: "KaiTi", "STKaiti", "SimKai", "SimSun", serif;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1.4px;
}

:deep(.seal-star) {
  fill: currentColor;
  font-family: "SimSun", serif;
  font-size: 37px;
  font-weight: 700;
}

:deep(.seal-main) {
  fill: currentColor;
  font-family: "KaiTi", "STKaiti", "SimKai", "SimSun", serif;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 2px;
}

:deep(.seal-line) {
  stroke: currentColor;
  stroke-width: 1.4;
}

:deep(.seal-sub) {
  fill: currentColor;
  font-family: "Times New Roman", Georgia, serif;
  font-size: 9px;
  font-weight: 700;
}

.cert-footer {
  z-index: 5;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  align-items: end;
  gap: 72px;
  margin-top: 72px;
  text-align: left;
}

.signature-block {
  display: flex;
  flex-direction: column;
  padding-left: 16px;
  margin-bottom: 42px;
}

.signature-line {
  width: 210px;
  height: 2px;
  flex: 0 0 2px;
  margin-top: 9px;
  background: #173b67;
}

.signature-block label {
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

.verify-link {
  grid-column: 1 / -1;
}

.verify-link span {
  display: block;
  color: #173b67;
  font-size: 12px;
  word-break: break-all;
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
