import { ref } from 'vue'
import { getUnreadNotificationCount } from '@/api/notification'

const unreadCount = ref(0)
let timer = null
let visibilityBound = false

async function refreshUnreadCount() {
  try {
    const result = await getUnreadNotificationCount()
    unreadCount.value = Number(result?.unreadCount || 0)
  } catch (_) {
    // 顶栏角标失败不打断当前页面业务。
  }
}

function onVisibilityChange() {
  if (document.visibilityState === 'visible') refreshUnreadCount()
}

function startNotificationPolling() {
  refreshUnreadCount()
  if (!timer) timer = window.setInterval(refreshUnreadCount, 30000)
  if (!visibilityBound) {
    document.addEventListener('visibilitychange', onVisibilityChange)
    visibilityBound = true
  }
}

function stopNotificationPolling() {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
  if (visibilityBound) {
    document.removeEventListener('visibilitychange', onVisibilityChange)
    visibilityBound = false
  }
}

function clearNotificationState() {
  unreadCount.value = 0
}

export function useNotifications() {
  return {
    unreadCount,
    refreshUnreadCount,
    startNotificationPolling,
    stopNotificationPolling,
    clearNotificationState
  }
}
