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
        <div class="nav-right">
          <el-button type="primary" @click="goLogin">登录</el-button>
          <el-button @click="goOrgRegister">机构申请</el-button>
        </div>
      </div>
    </header>

    <main class="public-content">
      <DashboardMap v-if="activeTab === 'dashboard'" />
      <PointMall v-else-if="activeTab === 'point-mall'" />
      <ConversionRules v-else-if="activeTab === 'conversion-rules'" />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import DashboardMap from './DashboardMap.vue'
import PointMall from './PointMall.vue'
import ConversionRules from './ConversionRules.vue'

const router = useRouter()
const route = useRoute()
const activeTab = ref('dashboard')

const tabs = [
  { key: 'point-mall', label: '积分商城' },
  { key: 'conversion-rules', label: '转换规则' },
  { key: 'dashboard', label: '数据大屏' }
]

onMounted(() => {
  const tab = route.query.tab
  if (tab && tabs.some(t => t.key === tab)) {
    activeTab.value = tab
  }
})

function goLogin() {
  router.push('/login')
}

function goOrgRegister() {
  router.push('/org-register')
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