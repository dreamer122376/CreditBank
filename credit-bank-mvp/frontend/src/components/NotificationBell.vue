<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    trigger="hover"
    :open-delay="200"
    :close-delay="150"
    :width="380"
    :show-arrow="false"
    popper-class="notification-popper"
    @show="loadNotifications"
  >
    <template #reference>
      <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
        <button class="bell-button" type="button" aria-label="查看通知" title="" @click="goToCenter">
          <el-icon :size="20"><Bell /></el-icon>
        </button>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="panel-header">
        <div class="panel-title">通知</div>
        <el-button v-if="unreadCount > 0" link type="primary" @click="readAll">
          全部已读
        </el-button>
      </div>

      <div class="panel-tabs" role="tablist" aria-label="通知筛选">
        <button :class="{ active: readStatus === 'ALL' }" @click="switchTab('ALL')">全部</button>
        <button :class="{ active: readStatus === 'UNREAD' }" @click="switchTab('UNREAD')">
          未读<span v-if="unreadCount"> {{ unreadCount }}</span>
        </button>
      </div>

      <div class="notification-list" v-loading="loading">
        <button
          v-for="item in items"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.readAt, important: item.level === 'WARNING' }"
          type="button"
          @click="openNotification(item)"
        >
          <span class="category-icon" :class="categoryClass(item.category)">
            <el-icon><component :is="categoryIcon(item.category)" /></el-icon>
          </span>
          <span class="item-main">
            <span class="item-title-row">
              <span class="item-title">{{ item.title }}</span>
              <span v-if="item.level === 'WARNING' && !item.confirmedAt" class="confirm-needed">需确认</span>
              <span v-if="!item.readAt" class="unread-dot" aria-label="未读"></span>
            </span>
            <span class="item-content">{{ item.content }}</span>
            <span class="item-time">{{ relativeTime(item.createdAt) }}</span>
          </span>
        </button>
        <el-empty v-if="!loading && items.length === 0" :description="emptyText" :image-size="64" />
      </div>

      <button class="view-all" type="button" @click="goToCenter">
        查看全部通知
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
  </el-popover>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  Bell,
  CircleCheck,
  Coin,
  DocumentChecked,
  ShoppingCart
} from '@element-plus/icons-vue'
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead
} from '@/api/notification'
import { useNotifications } from '@/composables/useNotifications'

const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const items = ref([])
const readStatus = ref('ALL')
const {
  unreadCount,
  refreshUnreadCount,
  startNotificationPolling,
  stopNotificationPolling,
  clearNotificationState
} = useNotifications()

const emptyText = computed(() => readStatus.value === 'UNREAD' ? '没有未读消息' : '暂无通知')

onMounted(startNotificationPolling)
onUnmounted(() => {
  stopNotificationPolling()
  clearNotificationState()
})

async function loadNotifications() {
  loading.value = true
  try {
    const result = await getNotifications({ page: 1, size: 8, readStatus: readStatus.value })
    items.value = result?.records || []
    unreadCount.value = Number(result?.unreadCount || 0)
  } finally {
    loading.value = false
  }
}

async function switchTab(status) {
  readStatus.value = status
  await loadNotifications()
}

async function openNotification(item) {
  if (!item.readAt) {
    await markNotificationRead(item.id)
    item.readAt = new Date().toISOString()
    await refreshUnreadCount()
    if (readStatus.value === 'UNREAD') items.value = items.value.filter(row => row.id !== item.id)
  }
  if (item.actionPath) {
    visible.value = false
    await router.push(item.actionPath)
  }
}

async function readAll() {
  await markAllNotificationsRead()
  items.value = readStatus.value === 'UNREAD'
    ? []
    : items.value.map(item => ({ ...item, readAt: item.readAt || new Date().toISOString() }))
  await refreshUnreadCount()
}

function goToCenter() {
  visible.value = false
  router.push('/notifications')
}

function categoryIcon(category) {
  return {
    APPLICATION: DocumentChecked,
    POINT: Coin,
    MALL: ShoppingCart,
    SYSTEM: CircleCheck
  }[category] || Bell
}

function categoryClass(category) {
  return String(category || 'system').toLowerCase()
}

function relativeTime(value) {
  if (!value) return ''
  const date = new Date(String(value).replace(' ', 'T'))
  const seconds = Math.max(0, Math.floor((Date.now() - date.getTime()) / 1000))
  if (seconds < 60) return '刚刚'
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟前`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}小时前`
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}天前`
  return String(value).slice(0, 10)
}
</script>

<style scoped>
.bell-button {
  width: 40px;
  height: 40px;
  border: 0;
  background: transparent;
  color: var(--cb-slate);
  display: grid;
  place-items: center;
  cursor: pointer;
  border-radius: var(--cb-radius-md);
  transition: background var(--cb-transition-fast), color var(--cb-transition-fast);
}
.bell-button:hover,
.bell-button:focus-visible {
  background: var(--cb-bg-hover);
  color: var(--cb-primary);
  outline: none;
}
.notification-panel { margin: -12px; }
.panel-header {
  height: 52px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--cb-border);
}
.panel-title { font-size: 15px; font-weight: 600; color: var(--cb-charcoal); }
.panel-tabs {
  height: 42px;
  display: flex;
  gap: 20px;
  align-items: flex-end;
  padding: 0 16px;
  border-bottom: 1px solid var(--cb-border);
}
.panel-tabs button {
  height: 42px;
  padding: 0 2px;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--cb-muted);
  font-size: 13px;
  cursor: pointer;
}
.panel-tabs button.active { color: var(--cb-primary); border-bottom-color: var(--cb-primary); font-weight: 600; }
.notification-list { min-height: 180px; max-height: 388px; overflow-y: auto; }
.notification-item {
  width: 100%;
  min-height: 92px;
  padding: 14px 16px;
  border: 0;
  border-bottom: 1px solid var(--cb-border-light);
  background: #fff;
  display: flex;
  gap: 12px;
  text-align: left;
  cursor: pointer;
}
.notification-item:hover { background: var(--cb-bg-hover); }
.notification-item.unread { background: #f5f7ff; }
.notification-item.important { border-left: 3px solid var(--cb-warning); }
.category-icon {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--cb-primary-glow);
  color: var(--cb-primary);
}
.category-icon.point { background: var(--cb-warning-glow); color: #b26d00; }
.category-icon.mall { background: var(--cb-success-glow); color: var(--cb-success); }
.category-icon.application { background: rgba(23, 162, 184, 0.12); color: var(--cb-info); }
.item-main { min-width: 0; flex: 1; display: flex; flex-direction: column; gap: 4px; }
.item-title-row { display: flex; align-items: center; gap: 8px; }
.item-title { flex: 1; font-size: 13px; font-weight: 600; color: var(--cb-charcoal); }
.confirm-needed { color: #9a6100; background: var(--cb-warning-glow); padding: 1px 5px; border-radius: 3px; font-size: 10px; }
.unread-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--cb-primary); flex: 0 0 7px; }
.item-content {
  color: var(--cb-slate);
  font-size: 12px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-time { color: var(--cb-muted); font-size: 11px; }
.view-all {
  width: 100%;
  height: 44px;
  border: 0;
  background: #fff;
  color: var(--cb-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  font-size: 12px;
}
.view-all:hover { background: var(--cb-bg-hover); }
@media (max-width: 520px) {
  .notification-panel { width: min(380px, calc(100vw - 28px)); }
}
</style>
