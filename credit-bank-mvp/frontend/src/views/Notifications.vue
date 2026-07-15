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
        <el-button v-if="viewMode === 'RECEIVED' && unreadCount > 0" type="primary" plain @click="readAll">
          <el-icon><CircleCheck /></el-icon>
          全部已读
        </el-button>
      </div>
    </div>

    <div v-if="canPublish" class="management-tabs" role="tablist" aria-label="通知视图">
      <button :class="{ active: viewMode === 'RECEIVED' }" @click="changeView('RECEIVED')">我的通知</button>
      <button :class="{ active: viewMode === 'PUBLISHED' }" @click="changeView('PUBLISHED')">已发布</button>
    </div>

    <template v-if="viewMode === 'RECEIVED'">
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
        <div v-for="item in items" :key="item.id" role="button" tabindex="0"
             class="message-row" :class="{ unread: !item.readAt, important: item.level === 'WARNING' }"
             @click="openNotification(item)" @keydown.enter="openNotification(item)">
          <span class="message-icon" :class="categoryClass(item.category)">
            <el-icon :size="20"><component :is="categoryIcon(item.category)" /></el-icon>
          </span>
          <span class="message-body">
            <span class="message-title-line">
              <span class="message-title">{{ item.title }}</span>
              <span class="category-label">{{ categoryLabel(item.category) }}</span>
              <span v-if="item.level === 'WARNING'" class="important-mark">重要</span>
              <span v-if="!item.readAt" class="unread-mark">未读</span>
            </span>
            <span class="message-content">{{ item.content }}</span>
            <span class="message-meta-line">
              <span class="message-time">{{ formatTime(item.createdAt) }}</span>
              <span v-if="item.expiresAt">有效期至 {{ formatTime(item.expiresAt) }}</span>
            </span>
          </span>
          <div class="message-actions">
            <el-button v-if="item.level === 'WARNING' && !item.confirmedAt"
                       type="warning" size="small" @click.stop="confirmImportant(item)">
              确认收到
            </el-button>
            <span v-else-if="item.level === 'WARNING'" class="confirmed-text">
              <el-icon><CircleCheck /></el-icon> 已确认
            </span>
            <el-icon v-if="item.actionPath" class="row-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
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
    </template>

    <template v-else>
      <div class="filter-bar published-filter">
        <el-input v-model="publishedFilters.keyword" clearable placeholder="搜索标题或内容"
                  style="width: 260px" @keyup.enter="reloadPublished" @clear="reloadPublished" />
        <el-select v-model="publishedFilters.status" clearable placeholder="全部状态"
                   style="width: 140px" @change="reloadPublished">
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已撤回" value="REVOKED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </div>

      <el-table :data="publishedItems" v-loading="publishedLoading" border class="published-table">
        <el-table-column label="通知" min-width="250">
          <template #default="scope">
            <div class="published-title-line">
              <span>{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.level === 'WARNING'" type="warning" size="small">重要</el-tag>
            </div>
            <div class="published-summary">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="scopeName" label="发布范围" width="150" />
        <el-table-column label="触达情况" width="190">
          <template #default="scope">
            <div>{{ scope.row.readCount }} / {{ scope.row.recipientCount }} 已读</div>
            <div v-if="scope.row.level === 'WARNING'" class="confirm-stat">
              {{ scope.row.confirmedCount }} 人已确认
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="publishedStatusType(scope.row.status)" size="small">
              {{ publishedStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="showPublishedDetail(scope.row)">详情</el-button>
            <el-button v-if="scope.row.canRevoke" link type="danger" @click="revokePublished(scope.row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="publishedTotal > pageSize"
        v-model:current-page="publishedPage"
        :page-size="pageSize"
        :total="publishedTotal"
        layout="prev, pager, next"
        background
        @current-change="loadPublished"
      />
    </template>

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

        <el-form-item label="有效期（可选）">
          <el-date-picker v-model="publishForm.expiresAt" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm"
                          placeholder="不设置则长期有效" :disabled-date="disablePastDate"
                          style="width: 100%" />
          <div class="form-tip">到期后自动归档；未设置有效期的普通通知将在180天后归档。</div>
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

    <el-dialog v-model="detailVisible" title="通知发布详情" width="560px">
      <el-descriptions v-if="selectedPublished" :column="2" border>
        <el-descriptions-item label="发布范围">{{ selectedPublished.scopeName }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ selectedPublished.actorName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ publishedStatusLabel(selectedPublished.status) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatTime(selectedPublished.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="接收人数">{{ selectedPublished.recipientCount }}</el-descriptions-item>
        <el-descriptions-item label="已读人数">{{ selectedPublished.readCount }}</el-descriptions-item>
        <el-descriptions-item v-if="selectedPublished.level === 'WARNING'" label="确认人数">
          {{ selectedPublished.confirmedCount }}
        </el-descriptions-item>
        <el-descriptions-item label="有效期">
          {{ selectedPublished.expiresAt ? formatTime(selectedPublished.expiresAt) : '长期有效' }}
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="selectedPublished" class="detail-content">
        <div class="detail-title">{{ selectedPublished.title }}</div>
        <div>{{ selectedPublished.content }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  confirmNotification,
  getNotifications,
  getPublishedNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  publishNotification,
  revokeNotification
} from '@/api/notification'
import { getOrganizations } from '@/api/organization'
import { useAuth } from '@/composables/useAuth'
import { useNotifications } from '@/composables/useNotifications'

const router = useRouter()
const loading = ref(false)
const viewMode = ref('RECEIVED')
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const filters = reactive({ readStatus: 'ALL', category: '' })
const publishVisible = ref(false)
const publishing = ref(false)
const detailVisible = ref(false)
const selectedPublished = ref(null)
const organizations = ref([])
const publishForm = reactive({
  scopeType: 'ALL', orgId: null, level: 'INFO', title: '', content: '', expiresAt: null
})
const publishedItems = ref([])
const publishedLoading = ref(false)
const publishedPage = ref(1)
const publishedTotal = ref(0)
const publishedFilters = reactive({ status: '', keyword: '' })
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

function changeView(mode) {
  viewMode.value = mode
  if (mode === 'PUBLISHED') loadPublished()
}

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

async function confirmImportant(item) {
  await confirmNotification(item.id)
  ElMessage.success('已确认收到')
  await refreshUnreadCount()
  await loadData()
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
  publishForm.expiresAt = null
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
      content,
      expiresAt: publishForm.expiresAt
    })
    ElMessage.success(`通知已发布给 ${result.recipientCount} 人`)
    publishVisible.value = false
    await refreshUnreadCount()
    if (viewMode.value === 'PUBLISHED') await loadPublished()
    else await loadData()
  } finally {
    publishing.value = false
  }
}

async function loadPublished() {
  publishedLoading.value = true
  try {
    const result = await getPublishedNotifications({
      page: publishedPage.value,
      size: pageSize,
      status: publishedFilters.status || undefined,
      keyword: publishedFilters.keyword.trim() || undefined
    })
    publishedItems.value = result?.records || []
    publishedTotal.value = Number(result?.total || 0)
  } finally {
    publishedLoading.value = false
  }
}

function reloadPublished() {
  publishedPage.value = 1
  loadPublished()
}

function showPublishedDetail(item) {
  selectedPublished.value = item
  detailVisible.value = true
}

async function revokePublished(item) {
  await ElMessageBox.confirm(
    `撤回“${item.title}”后，接收人将无法继续查看，但发布记录会保留。`,
    '撤回通知',
    { type: 'warning', confirmButtonText: '确认撤回', cancelButtonText: '取消' }
  )
  await revokeNotification(item.id)
  ElMessage.success('通知已撤回')
  await loadPublished()
}

function publishedStatusLabel(status) {
  return { PUBLISHED: '已发布', REVOKED: '已撤回', ARCHIVED: '已归档' }[status] || status
}

function publishedStatusType(status) {
  return { PUBLISHED: 'success', REVOKED: 'danger', ARCHIVED: 'info' }[status] || 'info'
}

function disablePastDate(date) {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
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
.management-tabs {
  display: flex;
  gap: 24px;
  min-height: 46px;
  padding: 0 18px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid var(--cb-border);
  border-radius: var(--cb-radius-md);
}
.management-tabs button {
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--cb-muted);
  font-size: 13px;
  cursor: pointer;
}
.management-tabs button.active { color: var(--cb-primary); border-bottom-color: var(--cb-primary); font-weight: 600; }
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
  font-family: inherit;
}
.message-row:last-child { border-bottom: 0; }
.message-row:hover { background: var(--cb-bg-hover); }
.message-row.unread { background: #f5f7ff; }
.message-row.important { border-left: 3px solid var(--cb-warning); }
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
.important-mark { color: #9a6100; background: var(--cb-warning-glow); border-radius: 3px; padding: 1px 5px; font-size: 10px; }
.message-content { color: var(--cb-slate); font-size: 13px; line-height: 1.65; }
.message-time { color: var(--cb-muted); font-size: 11px; }
.message-meta-line { display: flex; gap: 12px; color: var(--cb-muted); font-size: 11px; flex-wrap: wrap; }
.message-actions { display: flex; align-items: center; gap: 10px; align-self: center; flex: 0 0 auto; }
.confirmed-text { color: var(--cb-success); font-size: 12px; display: flex; align-items: center; gap: 4px; }
.row-arrow { color: var(--cb-muted); align-self: center; flex: 0 0 auto; }
.published-filter { justify-content: flex-start; }
.published-table { width: 100%; }
.published-title-line { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--cb-charcoal); }
.published-summary { margin-top: 5px; color: var(--cb-muted); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.confirm-stat { color: var(--cb-warning); font-size: 11px; margin-top: 3px; }
.form-tip { margin-top: 6px; color: var(--cb-muted); font-size: 11px; line-height: 1.5; }
.detail-content { margin-top: 16px; padding: 16px; background: var(--cb-bg-hover); border-radius: var(--cb-radius-md); color: var(--cb-slate); line-height: 1.7; }
.detail-title { margin-bottom: 8px; color: var(--cb-charcoal); font-size: 15px; font-weight: 600; }
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
  .message-actions { align-self: flex-start; }
  .message-icon { width: 36px; height: 36px; flex-basis: 36px; }
}
</style>
