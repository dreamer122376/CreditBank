<template>
  <div class="register-wrap">
    <div class="register-box">
      <div class="logo">
        <h1>机构入驻申请</h1>
        <p>Credit Bank Organization Registration</p>
      </div>

      <el-form :model="form" label-width="110px" class="register-form">
        <el-form-item label="机构名称" required>
          <el-input v-model="form.orgName" placeholder="请输入机构全称" />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="form.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="机构地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入机构地址" />
        </el-form-item>
        <el-button type="primary" size="large" class="btn-block" @click="handleSubmit" :loading="loading">提交申请</el-button>
        <p class="register-msg" v-if="msg">{{ msg }}</p>
      </el-form>

      <div class="back-login">
        <el-button link type="primary" @click="goLogin">已有账号？返回登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { submitApplication } from '@/api/application'

const router = useRouter()

const form = ref({
  orgName: '',
  contactPerson: '',
  contactPhone: '',
  address: ''
})

const loading = ref(false)
const msg = ref('')

function goLogin() {
  router.push('/login')
}

async function handleSubmit() {
  const { orgName, contactPerson, contactPhone } = form.value
  if (!orgName || !contactPerson || !contactPhone) {
    msg.value = '请填写所有必填项'
    return
  }

  loading.value = true
  msg.value = ''
  try {
    await submitApplication({
      bizType: 'ORG_REGISTER',
      formData: JSON.stringify({
        orgName,
        contactPerson,
        contactPhone,
        address: form.value.address
      })
    })
    loading.value = false
    msg.value = '提交成功，请等待平台管理员审核。审核通过后系统将自动创建机构管理员账号。'
    form.value = { orgName: '', contactPerson: '', contactPhone: '', address: '' }
  } catch (error) {
    loading.value = false
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
  background: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%);
}

.register-box {
  width: 480px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.logo {
  text-align: center;
  margin-bottom: 28px;
}

.logo h1 {
  font-size: 22px;
  color: #3b5bdb;
  margin-bottom: 6px;
}

.logo p {
  font-size: 13px;
  color: #868e96;
}

.register-form {
  width: 100%;
}

.btn-block {
  width: 100%;
  margin-top: 8px;
}

.register-msg {
  min-height: 20px;
  margin-top: 12px;
  font-size: 13px;
  color: #2b8a3e;
  text-align: center;
}

.back-login {
  text-align: center;
  margin-top: 16px;
}
</style>
