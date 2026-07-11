<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand">
        学分银行
        <small>{{ roleTitle }}</small>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        background-color="#2c3e50"
        text-color="#adb5bd"
        active-text-color="#fff"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main">
      <el-header class="topbar">
        <div class="page-title">{{ currentTitle }}</div>
        <div class="user-area">
          <div class="user-info">
            <div class="name">{{ currentUser?.realName }}</div>
            <div class="role">{{ ROLE_NAME[currentUser?.role] }}</div>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="avatar-wrap">
              <div class="avatar">{{ (currentUser?.realName || '?').charAt(0) }}</div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import {
  HomeFilled,
  UserFilled,
  ScaleToOriginal,
  WalletFilled,
  SwitchButton,
  OfficeBuilding,
  Avatar,
  Refresh,
  Medal,
  Tickets,
  Promotion
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { currentUser, ROLE_NAME, logout } = useAuth()

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => route.meta.title || '工作台')

const roleTitle = computed(() => {
  const titles = {
    admin: '系统管理员控制台',
    org_admin: '机构管理控制台',
    student: '学生中心',
    expert: '专家评审中心'
  }
  return titles[currentUser.value?.role] || ''
})

const menuItems = computed(() => {
  const menus = {
    admin: [
      { path: '/dashboard', title: '工作台', icon: HomeFilled },
      { path: '/users', title: '用户管理', icon: UserFilled },
      { path: '/organizations', title: '机构管理', icon: OfficeBuilding },
      { path: '/experts', title: '专家管理', icon: Avatar },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/exchange-rules', title: '转换规则', icon: Refresh },
      { path: '/cert-standards', title: '认证标准', icon: Medal },
      { path: '/applications', title: '业务流程', icon: Tickets },
      { path: '/campaigns', title: '平台活动管理', icon: Promotion },
      { path: '/transactions', title: '交易管理', icon: WalletFilled }
    ],
    org_admin: [
      { path: '/dashboard', title: '工作台', icon: HomeFilled },
      { path: '/users', title: '用户管理', icon: UserFilled },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/applications', title: '业务审核', icon: Tickets },
      { path: '/transactions', title: '积分流水', icon: WalletFilled }
    ],
    student: [
      { path: '/dashboard', title: '我的主页', icon: HomeFilled },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/campaigns/student', title: '参与活动', icon: Promotion },
      { path: '/transactions', title: '我的钱包', icon: WalletFilled }
    ],
    expert: [
      { path: '/dashboard', title: '我的主页', icon: HomeFilled },
      { path: '/applications', title: '项目评审', icon: Tickets },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/transactions', title: '项目流水', icon: WalletFilled }
    ]
  }
  return menus[currentUser.value?.role] || []
})

function handleCommand(command) {
  if (command === 'logout') {
    logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.sidebar {
  background: #2c3e50;
  color: #fff;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.brand {
  padding: 20px 16px;
  font-size: 17px;
  font-weight: 600;
  text-align: center;
  color: #fff;
  border-bottom: 1px solid #3a4a5c;
}

.brand small {
  display: block;
  font-size: 11px;
  color: #adb5bd;
  font-weight: normal;
  margin-top: 4px;
}

.menu {
  border-right: none;
}

.main {
  margin-left: 220px;
  min-height: 100vh;
}

.topbar {
  background: #fff;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.page-title {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  text-align: right;
  font-size: 13px;
}

.user-info .name {
  font-weight: 600;
  color: #2c3e50;
}

.user-info .role {
  color: #868e96;
  font-size: 12px;
}

.avatar-wrap {
  cursor: pointer;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #3b5bdb;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
}

.content {
  padding: 20px 24px;
  background: #f0f2f5;
}
</style>
