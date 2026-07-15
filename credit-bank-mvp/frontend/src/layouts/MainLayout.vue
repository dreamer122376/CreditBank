<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand">
        <div class="brand-mark" aria-hidden="true">
          <img src="/logo.jpg" alt="学分银行 Logo" width="40" height="40" />
        </div>
        <div class="brand-text">
          <div class="brand-name">学分银行</div>
          <div class="brand-role">
            <span class="brand-role-dot"></span>{{ roleTitle }}
          </div>
        </div>
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
          <NotificationBell />
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
      <!-- 冻结横幅 -->
      <div class="freeze-banner" v-if="isFrozen">
        <div class="freeze-banner-inner">
          <span class="freeze-icon">⚠️</span>
          <span class="freeze-text">
            您的账户已于 <strong>{{ fmt(currentUser?.frozenAt) }}</strong> 被冻结，
            若有问题请咨询系统管理员。
          </span>
          <el-button type="warning" size="small" @click="appealVisible = true">提交解冻申诉</el-button>
        </div>
      </div>

      <el-main class="content">
        <router-view />
      </el-main>

      <!-- 解冻申诉弹窗 -->
      <el-dialog v-model="appealVisible" title="提交解冻申诉" width="450px">
        <el-form>
          <el-form-item label="申诉理由" required>
            <el-input v-model="appealReason" type="textarea" :rows="4" placeholder="请说明解冻理由..." />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="appealVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAppeal" :loading="appealing">提交申诉</el-button>
        </template>
      </el-dialog>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { ElMessage } from 'element-plus'
import { submitUnfreezeAppeal } from '@/api/user'
import NotificationBell from '@/components/NotificationBell.vue'
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
  Promotion,
  Postcard,
  FolderOpened,
  Collection,
  Files,
  DataAnalysis,
  ShoppingCart,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { currentUser, ROLE_NAME, isFrozen, logout } = useAuth()

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

// 解冻申诉
const appealVisible = ref(false)
const appealReason = ref('')
const appealing = ref(false)

function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }

async function submitAppeal() {
  if (!appealReason.value.trim()) { ElMessage.warning('请填写申诉理由'); return }
  appealing.value = true
  try {
    await submitUnfreezeAppeal(appealReason.value.trim())
    ElMessage.success('解冻申诉已提交，请等待审核')
    appealVisible.value = false
    appealReason.value = ''
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    appealing.value = false
  }
}

const menuItems = computed(() => {
  const menus = {
    admin: [
      { path: '/dashboard', title: '工作台', icon: HomeFilled },
      { path: '/dashboard-map', title: '数据大屏', icon: DataAnalysis },
      { path: '/users', title: '用户管理', icon: UserFilled },
      { path: '/organizations', title: '机构管理', icon: OfficeBuilding },
      { path: '/experts', title: '专家管理', icon: Avatar },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/exchange-rules', title: '兑换规则', icon: Refresh },
      { path: '/conversion-rules', title: '转换规则管理', icon: ArrowRight },
      { path: '/cert-standards', title: '认证标准', icon: Medal },
      { path: '/applications', title: '审核管理', icon: Tickets },
      { path: '/campaigns', title: '平台活动管理', icon: Promotion },
      { path: '/projects/manage', title: '项目管理', icon: Files },
      { path: '/profile', title: '我的资料', icon: Postcard },
      { path: '/transactions', title: '交易管理', icon: WalletFilled },
      { path: '/op-logs', title: '操作日志', icon: Tickets }
    ],
    org_admin: [
      { path: '/dashboard', title: '工作台', icon: HomeFilled },
      { path: '/users', title: '用户管理', icon: UserFilled },
      { path: '/projects/manage', title: '项目管理', icon: Files },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/exchange-rules', title: '兑换规则', icon: Refresh },
      { path: '/conversion-rules', title: '转换规则管理', icon: ArrowRight },
      { path: '/cert-standards', title: '认证标准', icon: Medal },
      { path: '/applications', title: '业务审核', icon: Tickets },
      { path: '/profile', title: '我的资料', icon: Postcard },
      { path: '/transactions', title: '积分流水', icon: WalletFilled },
      { path: '/op-logs', title: '操作日志', icon: Tickets }
    ],
    student: [
      { path: '/dashboard', title: '我的主页', icon: HomeFilled },
      { path: '/projects', title: '项目报名', icon: Collection },
      { path: '/my-projects', title: '我的项目', icon: FolderOpened },
      { path: '/student-certs', title: '学生证书认证', icon: Medal },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/conversion-apply', title: '转换申请', icon: ArrowRight },
      { path: '/campaigns/student', title: '参与活动', icon: Promotion },
      { path: '/point-mall', title: '积分商城', icon: ShoppingCart },
      { path: '/profile', title: '我的资料', icon: Postcard },
      { path: '/transactions', title: '我的钱包', icon: WalletFilled }
    ],
    expert: [
      { path: '/dashboard', title: '我的主页', icon: HomeFilled },
      { path: '/applications', title: '项目评审', icon: Tickets },
      { path: '/my-certs', title: '我的资质', icon: Medal },
      { path: '/rules', title: '积分规则', icon: ScaleToOriginal },
      { path: '/profile', title: '我的资料', icon: Postcard },
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
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 18px;
  position: relative;
  border-bottom: 1px solid #3a4a5c;
}

/* 品牌区底部的金蓝渐变细线：唯一配饰，区分品牌与菜单 */
.brand::after {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(245, 159, 0, 0.45) 30%, rgba(59, 91, 219, 0.65) 70%, transparent 100%);
  pointer-events: none;
}

.brand-mark {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 13px;
  overflow: hidden;
  filter: drop-shadow(0 4px 10px rgba(59, 91, 219, 0.35));
  transition: transform 0.3s ease;
}

/* 图片填满容器，长宽一致，保持圆角裁剪 */
.brand-mark img {
  display: block;
  width: 40px;
  height: 40px;
  object-fit: cover;
}

.brand:hover .brand-mark {
  transform: translateY(-1px) scale(1.03);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

/* 中文字距即个性：呈现银行铭牌的正式感 */
.brand-name {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #fff;
  line-height: 1.2;
}

.brand-role {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10.5px;
  color: #adb5bd;
  letter-spacing: 0.12em;
  font-weight: 500;
}

.brand-role-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f59f00;
  box-shadow: 0 0 6px rgba(245, 159, 0, 0.6);
  flex-shrink: 0;
}

@media (prefers-reduced-motion: reduce) {
  .brand-mark,
  .brand:hover .brand-mark {
    transition: none;
    transform: none;
  }
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

.freeze-banner {
  background: linear-gradient(135deg, #fff3cd 0%, #ffe69c 100%);
  border-bottom: 2px solid #f59f00;
  padding: 0 24px;
}
.freeze-banner-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  max-width: 100%;
}
.freeze-icon { font-size: 18px; flex-shrink: 0; }
.freeze-text { flex: 1; font-size: 13px; color: #856404; }
.freeze-text strong { color: #d97706; }
</style>
