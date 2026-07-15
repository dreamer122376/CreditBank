<template>
  <div class="notifications-page">
    <div class="page-head">
      <div>
        <h1>通知中心</h1>
        <p>查看与你相关的业务进展和平台消息</p>
      </div>
      <div class="page-actions">
        <el-button v-if="canPublish" type="primary" @click="openPublishDialog">
          <el-icon><Promotion /></el-icon>
          发布通知
        </el-button>
        <el-button v-if="unreadCount > 0" type="primary" plain @click="readAll">
          <el-icon><CircleCheck /></el-icon>
          全部已读
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <div class="status-filter" role="tablist" aria-label="阅读状态">
        <button v-for="option in statusOptions" :key="option.value"
                :class="{ active: filters.readStatus === option.value }"
                @click="changeStatus(option.value)">
          {{ option.label }}
          <span v-if="option.value === 'UNREAD' && unreadCount">{{ unreadCount }}</span>
        </button>
      </div>
      <el-select v-model="filters.category" placeholder="全部类型" clearable style="width: 150px" @change="reload">
        <el-option label="系统消息" value="SYSTEM" />
        <el-option label="申请审批" value="APPLICATION" />
        <el-option label="积分变动" value="POINT" />
        <el-option label="积分商城" value="MALL" />
      </el-select>
    </div>

    <div class="message-list" v-loading="loading">
      <button v-for="item in items" :key="item.id" type="button"
              class="message-row" :class="{ unread: !item.readAt }"
              @click="openNotification(item)">
        <span class="message-icon" :class="categoryClass(item.category)">
          <el-icon :size="20"><component :is="categoryIcon(item.category)" /></el-icon>
        </span>
        <span class="message-body">
          <span class="message-title-line">
            <span class="message-title">{{ item.title }}</span>
            <span class="category-label">{{ categoryLabel(item.category) }}</span>
            <span v-if="!item.readAt" class="unread-mark">未读</span>
          </span>
          <span class="message-content">{{ item.content }}</span>
          <span class="message-time">{{ formatTime(item.createdAt) }}</span>
        </span>
        <el-icon v-if="item.actionPath" class="row-arrow"><ArrowRight /></el-icon>
      </button>
      <el-empty v-if="!loading && items.length === 0"
                :description="filters.readStatus === 'UNREAD' ? '没有未读消息' : '暂无通知'" />
    </div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      background
      @current-change="loadData"
    />

    <el-dialog v-model="publishVisible" title="发布通知" width="520px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="发布范围" required>
          <el-radio-group v-if="currentUser?.role === 'admin'" v-model="publishForm.scopeType">
            <el-radio-button value="ALL">全平台</el-radio-button>
            <el-radio-button value="ORG">指定机构</el-radio-button>
          </el-radio-group>
          <div v-else class="fixed-scope">
            <el-icon><OfficeBuilding /></el-icon>
            本机构全体用户
          </div>
        </el-form-item>

        <el-form-item v-if="currentUser?.role === 'admin' && publishForm.scopeType === 'ORG'"
                      label="接收机构" required>
          <el-select v-model="publishForm.orgId" placeholder="请选择机构" filterable style="width: 100%">
            <el-option v-for="org in organizationOptions" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="通知级别">
          <el-radio-group v-model="publishForm.level">
            <el-radio value="INFO">普通通知</el-radio>
            <el-radio value="WARNING">重要通知</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="通知标题" required>
          <el-input v-model="publishForm.title" maxlength="100" show-word-limit
                    placeholder="请输入简明的通知标题" />
        </el-form-item>

        <el-form-item label="通知内容" required>
          <el-input v-model="publishForm.content" type="textarea" :rows="6"
                    maxlength="500" show-word-limit resize="none"
                    placeholder="请输入需要告知用户的内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submitPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Bell,
  CircleCheck,
  Coin,
  DocumentChecked,
  OfficeBuilding,
  Promotion,
  ShoppingCart
} from '@element-plus/icons-vue'
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  publishNotification
} from '@/api/notification'
import { getOrganizations } from '@/api/organization'
import { useAuth } from '@/composables/useAuth'
import { useNotifications } from '@/composables/useNotifications'

const router = useRouter()
const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const filters = reactive({ readStatus: 'ALL', category: '' })
const publishVisible = ref(false)
const publishing = ref(false)
const organizations = ref([])
const publishForm = reactive({ scopeType: 'ALL', orgId: null, level: 'INFO', title: '', content: '' })
const { currentUser } = useAuth()
const { unreadCount, refreshUnreadCount } = useNotifications()
const canPublish = computed(() => ['admin', 'org_admin'].includes(currentUser.value?.role))
const organizationOptions = computed(() => organizations.value.filter(org => org.status === 1))

const statusOptions = [
  { label: '全部', value: 'ALL' },
  { label: '未读', value: 'UNREAD' },
  { label: '已读', value: 'READ' }
]

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const result = await getNotifications({
      page: page.value,
      size: pageSize,
      readStatus: filters.readStatus,
      category: filters.category || undefined
    })
    items.value = result?.records || []
    total.value = Number(result?.total || 0)
    unreadCount.value = Number(result?.unreadCount || 0)
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  loadData()
}

function changeStatus(status) {
  filters.readStatus = status
  reload()
}

async function openNotification(item) {
  if (!item.readAt) {
    await markNotificationRead(item.id)
    await refreshUnreadCount()
  }
  if (item.actionPath) await router.push(item.actionPath)
  else await loadData()
}

async function readAll() {
  await markAllNotificationsRead()
  await refreshUnreadCount()
  await loadData()
}

async function openPublishDialog() {
  publishForm.scopeType = currentUser.value?.role === 'admin' ? 'ALL' : 'ORG'
  publishForm.orgId = null
  publishForm.level = 'INFO'
  publishForm.title = ''
  publishForm.content = ''
  if (currentUser.value?.role === 'admin' && organizations.value.length === 0) {
    organizations.value = await getOrganizations()
  }
  publishVisible.value = true
}

async function submitPublish() {
  const title = publishForm.title.trim()
  const content = publishForm.content.trim()
  if (!title) {
    ElMessage.warning('请输入通知标题')
    return
  }
  if (!content) {
    ElMessage.warning('请输入通知内容')
    return
  }
  if (currentUser.value?.role === 'admin' && publishForm.scopeType === 'ORG' && !publishForm.orgId) {
    ElMessage.warning('请选择接收机构')
    return
  }
  publishing.value = true
  try {
    const result = await publishNotification({
      scopeType: publishForm.scopeType,
      orgId: publishForm.scopeType === 'ORG' ? publishForm.orgId : null,
      level: publishForm.level,
      title,
      content
    })
    ElMessage.success(`通知已发布给 ${result.recipientCount} 人`)
    publishVisible.value = false
    await refreshUnreadCount()
    await loadData()
  } finally {
    publishing.value = false
  }
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

function categoryLabel(category) {
  return {
    APPLICATION: '申请审批',
    POINT: '积分变动',
    MALL: '积分商城',
    SYSTEM: '系统消息'
  }[category] || '通知'
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.notifications-page { max-width: 980px; margin: 0 auto; }
.page-head {
  min-height: 72px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}
.page-head h1 { margin: 0; font-size: 22px; color: var(--cb-charcoal); letter-spacing: 0; }
.page-head p { margin: 6px 0 0; color: var(--cb-muted); font-size: 13px; }
.page-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; justify-content: flex-end; }
.filter-bar {
  padding: 0 18px;
  min-height: 54px;
  justify-content: space-between;
}
.status-filter { display: flex; align-self: stretch; gap: 22px; }
.status-filter button {
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--cb-muted);
  font-size: 13px;
  cursor: pointer;
  padding: 0 2px;
}
.status-filter button.active { color: var(--cb-primary); border-bottom-color: var(--cb-primary); font-weight: 600; }
.status-filter button span {
  display: inline-grid;
  place-items: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  margin-left: 4px;
  border-radius: 9px;
  background: var(--cb-danger);
  color: #fff;
  font-size: 10px;
}
.message-list {
  min-height: 320px;
  background: #fff;
  border: 1px solid var(--cb-border);
  border-radius: var(--cb-radius-md);
  overflow: hidden;
}
.message-row {
  width: 100%;
  min-height: 112px;
  padding: 18px 20px;
  border: 0;
  border-bottom: 1px solid var(--cb-border-light);
  background: #fff;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  text-align: left;
  cursor: pointer;
}
.message-row:last-child { border-bottom: 0; }
.message-row:hover { background: var(--cb-bg-hover); }
.message-row.unread { background: #f5f7ff; }
.message-icon {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--cb-primary-glow);
  color: var(--cb-primary);
}
.message-icon.point { background: var(--cb-warning-glow); color: #b26d00; }
.message-icon.mall { background: var(--cb-success-glow); color: var(--cb-success); }
.message-icon.application { background: rgba(23, 162, 184, 0.12); color: var(--cb-info); }
.message-body { min-width: 0; flex: 1; display: flex; flex-direction: column; gap: 7px; }
.message-title-line { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.message-title { color: var(--cb-charcoal); font-size: 14px; font-weight: 600; }
.category-label { color: var(--cb-muted); font-size: 11px; }
.unread-mark { color: var(--cb-primary); background: var(--cb-primary-glow); border-radius: 3px; padding: 1px 5px; font-size: 10px; }
.message-content { color: var(--cb-slate); font-size: 13px; line-height: 1.65; }
.message-time { color: var(--cb-muted); font-size: 11px; }
.row-arrow { color: var(--cb-muted); align-self: center; flex: 0 0 auto; }
.fixed-scope {
  width: 100%;
  min-height: 38px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--cb-charcoal);
  background: var(--cb-bg-hover);
  border: 1px solid var(--cb-border);
  border-radius: var(--cb-radius-md);
}
@media (max-width: 768px) {
  .page-head { align-items: center; }
  .filter-bar { align-items: stretch; padding: 0 14px 12px; }
  .status-filter { min-height: 44px; }
  .message-row { padding: 15px 14px; }
  .message-icon { width: 36px; height: 36px; flex-basis: 36px; }
}
</style>
