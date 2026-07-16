import { computed, ref } from 'vue'
import { getApplications } from '@/api/application'
import { getConversionApplications } from '@/api/conversionApplication'
import { useAuth } from './useAuth'

// 证书类业务类型：与 Applications.vue 保持一致
const CERT_BIZ_TYPES = ['CERT_APPLY', 'EXPERT_CERT']

const bizTodoCount = ref(0)    // 业务流程 - 待我审核
const certTodoCount = ref(0)   // 证书申请 - 待我审核
const convTodoCount = ref(0)   // 转换申请 - 待审核
let timer = null
let visibilityBound = false

// 按角色判断是否有审核权限：与 MainLayout 菜单权限保持一致
function hasAuditRole(user) {
  if (!user) return false
  if (String(user.username || user.id) === 'admin') return true
  return ['ORG_ADMIN', 'CENTER_REVIEWER', 'DEPT_REVIEWER', 'EXPERT'].includes(String(user.role))
}

async function refreshAuditTodos() {
  try {
    const { currentUser } = useAuth()
    if (!hasAuditRole(currentUser.value)) {
      bizTodoCount.value = 0
      certTodoCount.value = 0
      convTodoCount.value = 0
      return
    }
    const [apps, convApps] = await Promise.all([
      getApplications('REVIEWER', currentUser.value?.id),
      getConversionApplications()
    ])
    const appList = Array.isArray(apps?.records) ? apps.records : Array.isArray(apps) ? apps : []
    const convList = Array.isArray(convApps?.records) ? convApps.records : Array.isArray(convApps) ? convApps : []
    // 证书申请：CERT 类型 + canAudit
    certTodoCount.value = appList.filter(a => CERT_BIZ_TYPES.includes(a.bizType) && a.canAudit).length
    // 业务流程：非 CERT 类型 + canAudit
    bizTodoCount.value = appList.filter(a => !CERT_BIZ_TYPES.includes(a.bizType) && a.canAudit).length
    // 转换申请：status === 0
    convTodoCount.value = convList.filter(a => Number(a.status) === 0).length
  } catch (_) {
    // 徽标获取失败不影响主流程
  }
}

function onVisibilityChange() {
  if (document.visibilityState === 'visible') refreshAuditTodos()
}

function startAuditTodosPolling() {
  refreshAuditTodos()
  if (!timer) timer = window.setInterval(refreshAuditTodos, 30000)
  if (!visibilityBound) {
    document.addEventListener('visibilitychange', onVisibilityChange)
    visibilityBound = true
  }
}

function stopAuditTodosPolling() {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
  if (visibilityBound) {
    document.removeEventListener('visibilitychange', onVisibilityChange)
    visibilityBound = false
  }
}

function clearAuditTodos() {
  bizTodoCount.value = 0
  certTodoCount.value = 0
  convTodoCount.value = 0
}

// 导航栏汇总徽标
const auditTodoCount = computed(() =>
  bizTodoCount.value + certTodoCount.value + convTodoCount.value
)

export function useAuditTodos() {
  return {
    bizTodoCount,
    certTodoCount,
    convTodoCount,
    auditTodoCount,
    refreshAuditTodos,
    startAuditTodosPolling,
    stopAuditTodosPolling,
    clearAuditTodos
  }
}
