<template>
  <div class="org-register-wrap">
    <div class="register-panel">
      <!-- 左侧品牌区 -->
      <div class="panel-left">
        <div class="brand-area">
          <div class="brand-logo">
            <el-icon :size="40"><OfficeBuilding /></el-icon>
          </div>
          <h2 class="brand-name">学分银行</h2>
          <p class="brand-desc">机构入驻平台</p>
          <div class="brand-divider"></div>
          <p class="brand-tagline">加入终身学习生态<br/>共创学分认证未来</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>免费入驻，零门槛加入</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>发布项目，管理学员</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>积分互通，资源共享</span>
            </div>
          </div>
        </div>
        <div class="brand-footer">
          <el-button link class="query-link" @click="showQuery = true">
            <el-icon><Search /></el-icon> 查询申请进度
          </el-button>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="panel-right">
        <div class="form-header">
          <h3 v-if="!showQuery">提交入驻申请</h3>
          <h3 v-else>查询申请进度</h3>
          <p v-if="!showQuery">填写以下信息，审核通过后系统将自动创建管理员账号</p>
          <p v-else>输入申请时填写的机构名称和申请人姓名</p>
        </div>

        <!-- 提交申请表单 -->
        <el-form v-if="!showQuery" :model="form" label-position="top" class="register-form">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="机构名称" required>
                <el-input v-model="form.orgName" placeholder="请输入机构全称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="申请人" required>
                <el-input v-model="form.applicantName" placeholder="请输入您的姓名" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="联系人">
                <el-input v-model="form.contactPerson" placeholder="机构联系人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" required>
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="机构地址">
            <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入机构地址" />
          </el-form-item>

          <el-button type="primary" size="large" class="submit-btn" @click="handleSubmit" :loading="loading">
            提交入驻申请
          </el-button>

          <p class="form-msg" v-if="msg" :class="{ error: isError }">{{ msg }}</p>
        </el-form>

        <!-- 查询进度表单 -->
        <el-form v-else :model="queryForm" label-position="top" class="register-form">
          <el-form-item label="机构名称" required>
            <el-input v-model="queryForm.orgName" placeholder="请输入申请时填写的机构名称" />
          </el-form-item>
          <el-form-item label="申请人" required>
            <el-input v-model="queryForm.applicantName" placeholder="请输入申请时填写的申请人姓名" />
          </el-form-item>

          <el-form-item class="btn-form-item">
            <el-button type="primary" size="large" class="submit-btn" @click="handleQuery" :loading="querying">
              查询
            </el-button>
          </el-form-item>

          <div v-if="queryResult" class="query-result">
            <el-alert
              :title="queryResult.message"
              :type="queryResult.status === 3 ? 'success' : queryResult.status === 4 ? 'error' : 'warning'"
              :closable="false"
              show-icon
            />
            <div class="result-credentials" v-if="queryResult.status === 3">
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="管理员账号">{{ queryResult.adminUsername }}</el-descriptions-item>
                <el-descriptions-item label="初始密码">{{ queryResult.adminPassword }}</el-descriptions-item>
              </el-descriptions>
              <p class="credential-hint">登录后请尽快修改密码</p>
            </div>
          </div>

          <el-form-item class="btn-form-item">
            <el-button type="primary" class="back-form-link" @click="showQuery = false">
              ← 返回提交申请
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <el-button link type="primary" @click="goLogin">已有账号？返回登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { OfficeBuilding, Check, Search } from '@element-plus/icons-vue'
import { submitApplication } from '@/api/application'
import request from '@/api/request'

const router = useRouter()
const showQuery = ref(false)

const form = ref({
  orgName: '',
  applicantName: '',
  contactPerson: '',
  contactPhone: '',
  address: ''
})

const loading = ref(false)
const msg = ref('')
const isError = ref(false)

const queryForm = ref({ orgName: '', applicantName: '' })
const querying = ref(false)
const queryResult = ref(null)

async function handleQuery() {
  const { orgName, applicantName } = queryForm.value
  if (!orgName || !applicantName) {
    queryResult.value = { status: -1, message: '请填写机构名称和申请人' }
    return
  }
  querying.value = true
  try {
    queryResult.value = await request.get('/application/org-register-status', {
      params: { orgName, applicantName }
    })
  } catch (e) {
    queryResult.value = { status: -1, message: '查询失败，请稍后重试' }
  } finally {
    querying.value = false
  }
}

function goLogin() {
  router.push('/login')
}

async function handleSubmit() {
  const { orgName, applicantName, contactPhone } = form.value
  if (!orgName || !applicantName || !contactPhone) {
    isError.value = true
    msg.value = '请填写所有必填项'
    return
  }

  loading.value = true
  isError.value = false
  msg.value = ''
  try {
    await submitApplication({
      bizType: 'ORG_REGISTER',
      formData: JSON.stringify({
        orgName,
        applicantName,
        contactPerson: form.value.contactPerson,
        contactPhone,
        address: form.value.address
      })
    })
    loading.value = false
    msg.value = '提交成功，请等待平台管理员审核。审核通过后系统将自动创建管理员账号。'
    form.value = { orgName: '', applicantName: '', contactPerson: '', contactPhone: '', address: '' }
  } catch (error) {
    loading.value = false
    isError.value = true
    msg.value = error.message || '提交失败，请稍后重试'
  }
}
</script>

<style scoped>
.org-register-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(59,91,219,0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(121,80,242,0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(11,122,79,0.04) 0%, transparent 60%),
    linear-gradient(160deg, #eef2fb 0%, #e8eef8 30%, #f0f4fc 60%, #eaf0fa 100%);
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}
.org-register-wrap::before {
  content: '';
  position: absolute; inset: 0; pointer-events: none;
  background:
    radial-gradient(circle at 15% 85%, rgba(59,91,219,0.06) 0%, transparent 30%),
    radial-gradient(circle at 85% 15%, rgba(121,80,242,0.05) 0%, transparent 30%);
}


/* ===== 整体面板 ===== */
.register-panel {
  display: flex;
  width: 880px;
  max-width: 100%;
  min-height: 560px;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
  border-radius: var(--cb-radius-xl);
  box-shadow:
    0 20px 60px rgba(59,91,219,0.15),
    0 0 0 1px rgba(59,91,219,0.08),
    inset 0 1px 0 rgba(255,255,255,0.8);
  overflow: hidden;
  animation: panelPop 0.6s ease both;
  transition: box-shadow 0.3s ease;
  position: relative;
}
.register-panel::after {
  content: '';
  position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, var(--cb-primary), #7950f2, var(--cb-primary-light), #0b7a4f);
  background-size: 300% 100%;
  animation: topBarShimmer 4s ease infinite;
  z-index: 10; pointer-events: none;
}
@keyframes topBarShimmer {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}
.register-panel:hover {
  box-shadow:
    0 24px 72px rgba(59,91,219,0.22),
    0 0 0 2px rgba(59,91,219,0.12);
}
@keyframes panelPop {
  from { opacity: 0; transform: scale(0.95) translateY(20px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}

/* ===== 左侧品牌区 ===== */
.panel-left {
  width: 340px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #1a5c8a 0%, #0b7a4f 20%, #6c3fc0 40%, #764ba2 60%, #2d46b9 80%, #10986a 100%);
  background-size: 300% 300%;
  animation: blueGreenShift 6s ease infinite;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px 32px;
}
@keyframes blueGreenShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.brand-area { flex: 1; }

/* 左侧元素逐项弹入 */
.panel-left .brand-logo,
.panel-left .brand-name,
.panel-left .brand-desc,
.panel-left .brand-divider,
.panel-left .brand-tagline,
.panel-left .feature-item,
.panel-left .brand-footer {
  animation: itemPop 0.5s ease both;
}
.panel-left .brand-logo     { animation-delay: 0.1s; }
.panel-left .brand-name     { animation-delay: 0.15s; }
.panel-left .brand-desc     { animation-delay: 0.2s; }
.panel-left .brand-divider  { animation-delay: 0.25s; }
.panel-left .brand-tagline  { animation-delay: 0.3s; }
.panel-left .feature-item:nth-child(1) { animation-delay: 0.35s; }
.panel-left .feature-item:nth-child(2) { animation-delay: 0.4s; }
.panel-left .feature-item:nth-child(3) { animation-delay: 0.45s; }
.panel-left .brand-footer   { animation-delay: 0.5s; }
@keyframes itemPop {
  from { opacity: 0; transform: translateX(-20px); }
  to   { opacity: 1; transform: translateX(0); }
}

.brand-logo {
  width: 64px; height: 64px;
  border-radius: var(--cb-radius-lg);
  background: rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 16px;
  backdrop-filter: blur(4px);
}
.brand-name {
  font-size: 22px; font-weight: 700;
  margin: 0 0 4px;
  letter-spacing: 2px;
}
.brand-desc {
  font-size: 14px; color: rgba(255,255,255,0.75);
  margin: 0 0 24px;
}
.brand-divider {
  width: 48px; height: 2px;
  background: rgba(255,255,255,0.3);
  margin-bottom: 20px;
}
.brand-tagline {
  font-size: 14px; color: rgba(255,255,255,0.7);
  line-height: 1.8;
  margin: 0 0 28px;
}
.brand-features {
  display: flex; flex-direction: column; gap: 12px;
}
.feature-item {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: rgba(255,255,255,0.8);
}
.feature-item .el-icon {
  color: #4ade80; flex-shrink: 0;
}
.brand-footer {
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.15);
}
.query-link {
  color: rgba(255,255,255,0.8); font-size: 13px;
  transition: all 0.3s ease;
}
.query-link:hover {
  color: #fff;
  transform: translateX(4px);
  text-shadow: 0 0 8px rgba(255,255,255,0.3);
}

/* ===== 右侧表单区 ===== */
.panel-right {
  flex: 1;
  padding: 40px 36px;
  display: flex; flex-direction: column;
}
.form-header {
  margin-bottom: 28px;
}
.form-header h3 {
  font-size: var(--cb-text-2xl); font-weight: 700;
  color: var(--cb-ink); margin: 0 0 6px;
}
.form-header p {
  font-size: var(--cb-text-sm); color: var(--cb-muted); margin: 0;
}

.register-form { flex: 1; }
.submit-btn {
  width: 100%; margin-top: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #3b5bdb 100%);
  background-size: 300% 300%;
  background-position: 0% 50%;
  border: none; color: #fff; font-weight: 600; letter-spacing: 2px;
  box-shadow: 0 4px 15px rgba(102,126,234,0.3);
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(118,75,162,0.4);
  animation: btnShimmer 0.8s ease forwards;
}
@keyframes btnShimmer {
  from { background-position: 0% 50%; }
  to   { background-position: 100% 50%; }
}
.submit-btn:active { transform: translateY(0); }

.form-msg {
  min-height: 20px; margin-top: 10px;
  font-size: var(--cb-text-sm);
  color: var(--cb-success); text-align: center;
}
.form-msg.error { color: var(--cb-danger); }

.query-result { margin-top: 16px; }
.result-credentials { margin-top: 12px; }
.credential-hint {
  margin-top: 8px; font-size: var(--cb-text-xs);
  color: var(--cb-warning); text-align: center;
}
.back-form-link {
  margin-top: 12px; width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #3b5bdb 100%) !important;
  background-size: 300% 300% !important;
  background-position: 0% 50% !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600;
  letter-spacing: 2px;
  box-shadow: 0 4px 15px rgba(102,126,234,0.3);
  transition: all 0.3s ease;
}
.back-form-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(118,75,162,0.4);
  animation: btnShimmer 0.8s ease forwards;
}
.btn-form-item {
  margin-bottom: 0;
}
.btn-form-item .el-form-item__content {
  line-height: 1;
}

.form-footer {
  text-align: center; padding-top: 20px;
  border-top: 1px solid var(--cb-border-light); margin-top: auto;
}
.form-footer .el-button {
  font-size: var(--cb-text-sm); color: var(--cb-muted);
  transition: color 0.3s ease;
}
.form-footer .el-button:hover {
  color: var(--cb-primary);
}

/* 输入框渐变焦点 */
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(102,126,234,0.15) !important;
  border-color: #667eea !important;
}
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 3px rgba(102,126,234,0.15) !important;
  border-color: #667eea !important;
}

@media (max-width: 768px) {
  .register-panel { flex-direction: column; width: 100%; }
  .panel-left { width: 100%; padding: 28px 24px; }
  .panel-right { padding: 28px 24px; }
}
</style>
