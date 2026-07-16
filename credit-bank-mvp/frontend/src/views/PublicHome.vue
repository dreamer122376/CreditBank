<template>
  <div class="public-home">
    <header class="public-nav">
      <div class="nav-container">
        <div class="nav-left">
          <div class="nav-brand">
            <img src="/logo.jpg" alt="学分银行" class="nav-logo" />
            <span class="nav-title">终身学习学分银行</span>
          </div>
        </div>
        <div class="nav-center">
          <div class="nav-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="nav-tab"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>
        <!-- 导航右侧：根据登录态条件渲染，避免「未登录提示+管理员操作按钮」同屏 -->
        <div class="nav-right" v-if="!currentUser">
          <el-button type="primary" @click="goLogin">登录</el-button>
          <el-button @click="goOrgRegister">机构申请</el-button>
        </div>
        <div class="nav-right" v-else>
          <span class="welcome-text">
            你好，<b>{{ currentUser.realName || currentUser.username }}</b>
            <span class="role-chip">{{ ROLE_NAME[currentUser.role] || currentUser.role }}</span>
          </span>
          <el-button type="primary" @click="goDashboard">进入控制台</el-button>
          <el-button @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </header>

    <main class="public-content">
      <DashboardMap v-if="activeTab === 'dashboard'" />
      <PointMall v-else-if="activeTab === 'point-mall'" />
      <!-- 公开页面强制只读模式，确保不展示任何管理操作入口 -->
      <ConversionRules v-else-if="activeTab === 'conversion-rules'" :readonly="true" />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import DashboardMap from './DashboardMap.vue'
import PointMall from './PointMall.vue'
import ConversionRules from './ConversionRules.vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { currentUser, logout, ROLE_NAME } = useAuth()
const activeTab = ref('dashboard')

const tabs = [
  { key: 'point-mall', label: '积分商城' },
  { key: 'conversion-rules', label: '转换规则' },
  { key: 'dashboard', label: '数据大屏' }
]

onMounted(() => {
  // 从 query 读取目标 tab
  const tab = route.query.tab
  if (tab && tabs.some(t => t.key === tab)) {
    activeTab.value = tab
  }
  // 公开路由不保留过期/残留登录态：若 localStorage 有残留 admin 等，
  // 仍由组件层的 :readonly=true 兜底不显示操作按钮；
  // 同时 UI 顶部显示真实登录态，避免与页面内容产生矛盾。
})

function goLogin() {
  router.push('/login')
}

function goOrgRegister() {
  router.push('/org-register')
}

function goDashboard() {
  // 已登录用户点击"进入控制台"：跳转到 MainLayout 的默认首页（绝对路径 /dashboard，而非嵌套的 /app/dashboard）
  const role = currentUser.value?.role
  if (role === 'admin' || role === 'org_admin' || role === 'expert') {
    router.push('/dashboard')
  } else if (role === 'student') {
    router.push('/dashboard')
  } else {
    router.push('/dashboard')
  }
}

function handleLogout() {
  logout()
  ElMessage.success('已退出登录')
}
</script>

<style scoped>
.public-home {
  min-height: 100vh;
  background: #f0f2f5;
}

.public-nav {
  background: linear-gradient(135deg, #0F1B2D 0%, #1A2332 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1480px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  flex-shrink: 0;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
}

.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-tabs {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  padding: 4px;
}

.nav-tab {
  padding: 8px 24px;
  font-size: 14px;
  color: #adb5bd;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.nav-tab:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.nav-tab.active {
  color: #fff;
  background: linear-gradient(135deg, #C8914A 0%, #B8860B 100%);
  box-shadow: 0 2px 6px rgba(200, 145, 74, 0.3);
}

.nav-right {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
}

.nav-right .el-button {
  font-size: 13px;
  padding: 6px 16px;
  border-radius: 6px;
}

.nav-right .el-button--primary {
  background: linear-gradient(135deg, #C8914A 0%, #B8860B 100%);
  border-color: #B8860B;
}

.nav-right .el-button--primary:hover {
  background: linear-gradient(135deg, #DFB66A 0%, #C8914A 100%);
  border-color: #C8914A;
}

.welcome-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #e5e7eb;
  margin-right: 4px;
}
.welcome-text b {
  color: #fff;
  font-weight: 600;
}
.role-chip {
  display: inline-block;
  padding: 1px 8px;
  font-size: 11px;
  font-weight: 600;
  color: #C8914A;
  background: rgba(200, 145, 74, 0.12);
  border: 1px solid rgba(200, 145, 74, 0.35);
  border-radius: 10px;
}

.public-content {
  max-width: 1480px;
  margin: 0 auto;
  padding: 0;
}

@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
    height: 56px;
  }

  .nav-title {
    display: none;
  }

  .nav-tab {
    padding: 6px 16px;
    font-size: 13px;
  }

  .nav-right .el-button {
    padding: 4px 12px;
    font-size: 12px;
  }
}
</style>