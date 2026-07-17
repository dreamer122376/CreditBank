import { computed, ref } from 'vue'
import { getApplications } from '@/api/application'
import { getConversionApplications } from '@/api/conversionApplication'
import { getPendingAuditEnrollments } from '@/api/project'
import { useAuth } from './useAuth'

// 证书类业务类型：与 Applications.vue 保持一致
const CERT_BIZ_TYPES = ['CERT_APPLY', 'EXPERT_CERT']

const bizTodoCount = ref(0)    // 业务流程 - 待我审核
const certTodoCount = ref(0)   // 证书申请 - 待我审核
const convTodoCount = ref(0)   // 转换申请 - 待审核
const enrollmentTodoCount = ref(0) // 报名审核 - 待审核
let timer = null
let visibilityBound = false

// 按角色判断是否有审核权限：与 useAuth.ROLE_NAME 的 key（全小写）保持一致
function hasAuditRole(user) {
  if (!user || !user.role) return false
  const role = String(user.role).toLowerCase()
  // 与 MainLayout 菜单权限一致：admin / org_admin / expert 均有审核入口
  return ['admin', 'org_admin', 'expert'].includes(role)
}

async function refreshAuditTodos() {
  try {
    const { currentUser } = useAuth()
    if (!hasAuditRole(currentUser.value)) {
      bizTodoCount.value = 0
      certTodoCount.value = 0
      convTodoCount.value = 0
      enrollmentTodoCount.value = 0
      return
    }
    // 专家角色不获取转换申请数据
    const isExpert = currentUser.value?.role === 'expert'
    // 与 Applications.vue loadData 调用口径完全一致：传实际 role + userId
    const [apps, convApps, enrollApps] = await Promise.all([
      getApplications(currentUser.value?.role, currentUser.value?.id),
      isExpert ? Promise.resolve([]) : getConversionApplications(),
      getPendingAuditEnrollments()
    ])
    const appList = Array.isArray(apps?.records) ? apps.records : Array.isArray(apps) ? apps : []
    const convList = Array.isArray(convApps?.records) ? convApps.records : Array.isArray(convApps) ? convApps : []
    const enrollList = Array.isArray(enrollApps) ? enrollApps : []
    // 证书申请：CERT 类型 + canAudit
    certTodoCount.value = appList.filter(a => CERT_BIZ_TYPES.includes(a.bizType) && a.canAudit).length
    // 业务流程：非 CERT 类型 + canAudit
    bizTodoCount.value = appList.filter(a => !CERT_BIZ_TYPES.includes(a.bizType) && a.canAudit).length
    // 转换申请：专家不计入（也不获取数据），其他角色取 status === 0
    convTodoCount.value = isExpert ? 0 : convList.filter(a => Number(a.status) === 0).length
    // 报名审核：status === '待审核'
    enrollmentTodoCount.value = enrollList.filter(e => e.status === '待审核').length
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
  enrollmentTodoCount.value = 0
}

// 导航栏汇总徽标（专家不计入转换申请待办数）
const auditTodoCount = computed(() => {
  const { currentUser } = useAuth()
  const isExpert = currentUser.value?.role === 'expert'
  return bizTodoCount.value + certTodoCount.value + (isExpert ? 0 : convTodoCount.value) + enrollmentTodoCount.value
})

export function useAuditTodos() {
  return {
    bizTodoCount,
    certTodoCount,
    convTodoCount,
    enrollmentTodoCount,
    auditTodoCount,
    refreshAuditTodos,
    startAuditTodosPolling,
    stopAuditTodosPolling,
    clearAuditTodos
  }
}
