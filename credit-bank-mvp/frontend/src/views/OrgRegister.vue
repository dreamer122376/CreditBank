<template>
  <div class="register-wrap">
    <div class="register-card">
      <div class="crest">
        <div class="shield">CB</div>
        <div class="ribbon">学分银行</div>
      </div>

      <div class="header">
        <h1>机构入驻</h1>
        <p class="subtitle" v-if="activeTab === 'apply'">填写以下信息，审核通过后系统将自动创建管理员账号</p>
        <p class="subtitle" v-else>输入机构名称和申请人查询审核进度</p>
      </div>

      <div class="tab-switch">
        <span class="tab" :class="{ active: activeTab === 'apply' }" @click="activeTab = 'apply'">提交申请</span>
        <span class="tab" :class="{ active: activeTab === 'query' }" @click="activeTab = 'query'">查询进度</span>
      </div>

      <!-- 提交申请 -->
      <div class="form-scroll" v-if="activeTab === 'apply'">
        <el-form :model="form" label-position="top" class="register-form">
          <div class="section-title">机构信息</div>

          <el-form-item label="机构名称" required>
            <el-input v-model="form.orgName" placeholder="请输入机构全称" />
          </el-form-item>

          <el-form-item label="申请人" required>
            <el-input v-model="form.applicantName" placeholder="请输入您的姓名" />
          </el-form-item>

          <el-form-item label="联系人" required>
            <el-input v-model="form.contactPerson" placeholder="请输入机构联系人" />
          </el-form-item>

          <el-form-item label="联系电话" required>
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>

          <el-form-item label="机构地址">
            <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入机构地址" />
          </el-form-item>

          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading">
            提交申请
          </el-button>

          <p class="register-msg" v-if="msg" :class="{ error: isError }">{{ msg }}</p>
        </el-form>
      </div>

      <!-- 查询进度 -->
      <div class="form-scroll" v-else>
        <el-form :model="queryForm" label-position="top" class="register-form">
          <div class="section-title">查询申请进度</div>

          <el-form-item label="机构名称" required>
            <el-input v-model="queryForm.orgName" placeholder="请输入申请时填写的机构名称" />
          </el-form-item>

          <el-form-item label="申请人" required>
            <el-input v-model="queryForm.applicantName" placeholder="请输入申请时填写的申请人姓名" />
          </el-form-item>

          <el-button type="primary" class="submit-btn" @click="handleQuery" :loading="querying">
            查询
          </el-button>

          <div class="query-result" v-if="queryResult">
            <el-alert :title="queryResult.message" :type="queryResult.status === 3 ? 'success' : queryResult.status === 4 ? 'error' : 'warning'" :closable="false" show-icon />
            <div class="result-detail" v-if="queryResult.status === 3">
              <p><b>管理员账号：</b>{{ queryResult.adminUsername }}</p>
              <p><b>初始密码：</b>{{ queryResult.adminPassword }}</p>
              <p class="hint">请使用以上账号登录后尽快修改密码</p>
            </div>
          </div>
        </el-form>
      </div>

      <div class="footer">
        <el-button link class="back-link" @click="goLogin">已有账号？返回登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { submitApplication } from '@/api/application'
import request from '@/api/request'

const router = useRouter()
const activeTab = ref('apply')

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

// 查询进度
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
  const { orgName, applicantName, contactPerson, contactPhone } = form.value
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
        contactPerson,
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
.register-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 20%, rgba(139, 115, 85, 0.25) 0%, transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(90, 70, 50, 0.25) 0%, transparent 40%),
    linear-gradient(160deg, #1a1714 0%, #2b211b 50%, #1a1714 100%);
  padding: 40px 20px;
}

.register-card {
  width: 520px;
  max-width: 100%;
  background: #f5f0e6;
  border: 6px double #5c4033;
  border-radius: 4px;
  box-shadow:
    0 24px 60px rgba(0, 0, 0, 0.45),
    inset 0 0 0 1px #d4c5a9;
  position: relative;
  overflow: hidden;
}

.register-card::before {
  content: '';
  position: absolute;
  inset: 8px;
  border: 1px solid #8b7355;
  pointer-events: none;
}

.crest {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 28px;
  margin-bottom: 8px;
}

.shield {
  width: 72px;
  height: 84px;
  background: linear-gradient(145deg, #7a1e1e 0%, #5a1515 100%);
  color: #f5f0e6;
  font-family: 'Times New Roman', Times, serif;
  font-size: 26px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  clip-path: polygon(50% 0%, 100% 12%, 100% 55%, 50% 100%, 0% 55%, 0% 12%);
  text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.3);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);
}

.ribbon {
  margin-top: 8px;
  font-size: 12px;
  letter-spacing: 6px;
  color: #5c4033;
}

.header {
  text-align: center;
  padding: 0 32px 20px;
  border-bottom: 1px solid #d4c5a9;
  margin: 0 24px 20px;
}

.header h1 {
  font-family: 'Times New Roman', Times, serif;
  font-size: 22px;
  color: #3e2b1f;
  margin: 0 0 6px;
}

.subtitle {
  font-size: 13px;
  color: #6b5a48;
  margin: 0;
}

.form-scroll {
  padding: 0 36px;
}

.section-title {
  font-family: 'Times New Roman', Times, serif;
  font-size: 15px;
  color: #5c4033;
  margin: 18px 0 12px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #bfa885;
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #4a3b2d;
  padding-bottom: 4px;
}

:deep(.el-input__wrapper) {
  background: #fffdf8;
  box-shadow: inset 0 0 0 1px #bfa885;
  border-radius: 2px;
}

:deep(.el-input__inner) {
  color: #3e2b1f;
}

:deep(.el-textarea__inner) {
  background: #fffdf8;
  box-shadow: inset 0 0 0 1px #bfa885;
  border-radius: 2px;
  color: #3e2b1f;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
  height: 44px;
  background: linear-gradient(180deg, #7a1e1e 0%, #5a1515 100%);
  border: 1px solid #3d0e0e;
  border-radius: 2px;
  font-size: 15px;
  letter-spacing: 4px;
}

.submit-btn:hover {
  background: linear-gradient(180deg, #8f2424 0%, #6b1818 100%);
}

.register-msg {
  min-height: 20px;
  margin-top: 14px;
  font-size: 13px;
  color: #2b5a2b;
  text-align: center;
  line-height: 1.5;
}

.register-msg.error {
  color: #7a1e1e;
}

.footer {
  text-align: center;
  padding: 16px 0 24px;
  margin-top: 8px;
}

.back-link {
  font-size: 13px;
  color: #6b5a48;
}

.back-link:hover {
  color: #7a1e1e;
}

.tab-switch {
  display: flex;
  margin: 0 36px 16px;
  border-bottom: 1px solid #d4c5a9;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  color: #8b7355;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab.active {
  color: #5c4033;
  font-weight: 600;
  border-bottom-color: #7a1e1e;
}
.tab:hover {
  color: #5c4033;
}

.query-result {
  margin-top: 16px;
}
.result-detail {
  margin-top: 12px;
  padding: 12px 16px;
  background: #f0f7eb;
  border: 1px solid #c3d9a5;
  border-radius: 4px;
}
.result-detail p {
  margin: 4px 0;
  font-size: 13px;
  color: #3e2b1f;
}
.result-detail .hint {
  margin-top: 8px;
  font-size: 12px;
  color: #856404;
}
</style>
