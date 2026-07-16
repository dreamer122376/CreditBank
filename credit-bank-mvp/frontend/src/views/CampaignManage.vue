<template>
  <div class="campaign-manage" v-loading="loading">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">平台活动管理</h1>
        <p class="page-subtitle">创建、编辑和管理平台运营活动</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">创建活动</el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" @change="loadData" size="small">
        <el-radio-button value="">全部活动</el-radio-button>
        <el-radio-button value="0">未开始</el-radio-button>
        <el-radio-button value="1">进行中</el-radio-button>
        <el-radio-button value="2">已结束</el-radio-button>
      </el-radio-group>
      <div class="filter-summary" v-if="total > 0">
        共 <strong>{{ total }}</strong> 个活动
      </div>
    </div>

    <!-- 卡片列表 -->
    <el-row :gutter="20">
      <el-col :span="8" :xs="24" :sm="12" :md="8" v-for="item in list" :key="item.id" class="card-col">
        <el-card class="campaign-card" shadow="hover" @click="goDetail(item.id)">
          <div class="card-cover" :style="coverStyle(item.coverImage)">
            <span v-if="!item.coverImage" class="cover-text">{{ item.title }}</span>
            <span v-if="item.multiplier > 1" class="badge-bonus">
              <el-icon><Lightning /></el-icon>
              {{ item.multiplier }}x 积分翻倍
            </span>
            <div class="cover-overlay"></div>
          </div>
          <div class="card-info">
            <div class="card-title" :title="item.title">{{ item.title }}</div>
            <div class="card-meta">
              <el-icon><Clock /></el-icon>
              <span>{{ fmt(item.startTime) }} ~ {{ fmt(item.endTime) }}</span>
            </div>
            <div class="card-desc" v-if="item.description">
              {{ item.description.substring(0, 60) }}{{ item.description.length > 60 ? '...' : '' }}
            </div>
            <div class="card-footer">
              <el-tag :type="statusType(item.status)" size="small" effect="light">{{ statusText(item.status) }}</el-tag>
              <div class="card-actions" @click.stop>
                <el-button v-if="item.status !== 2" size="small" :icon="Edit" @click="openEdit(item)">编辑</el-button>
                <el-button size="small" type="danger" plain :icon="Delete" @click="handleDelete(item)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <div v-if="!loading && list.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Collection /></el-icon>
      <div class="empty-title">暂无活动数据</div>
      <div class="empty-text">点击右上角“创建活动”按钮发布第一个平台活动</div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="loadData"
      />
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '创建活动'" width="640px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px" class="campaign-form">
        <el-form-item label="活动标题" required>
          <el-input v-model="form.title" maxlength="100" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动简介/详情描述" />
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="upload-row">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept="image/*"
            >
              <div class="upload-box" :class="{ 'has-preview': coverPreview }">
                <img v-if="coverPreview" :src="coverPreview" class="upload-preview" />
                <div v-else class="upload-placeholder">
                  <el-icon :size="28"><Picture /></el-icon>
                  <span>点击上传封面</span>
                </div>
              </div>
            </el-upload>
            <div class="upload-status">
              <el-icon v-if="uploading" class="is-loading"><Loading /></el-icon>
              <span v-else-if="form.coverImage" class="status-success">
                <el-icon><CircleCheck /></el-icon> 已上传
              </span>
              <span v-else class="status-hint">建议尺寸 800×450</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="活动图片">
          <div class="multi-upload">
            <div v-for="(img, idx) in form.images" :key="idx" class="mu-item">
              <img :src="img" class="mu-thumb" />
              <span class="mu-del" @click="removeImage(idx)">
                <el-icon><Close /></el-icon>
              </span>
            </div>
            <el-upload
              v-if="form.images.length < 6"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleMultiFile"
              accept="image/*"
            >
              <div class="mu-add">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
          </div>
          <div class="field-hint">最多6张，将在详情页轮播展示</div>
        </el-form-item>
        <el-form-item label="主办方">
          <el-input v-model="form.organizer" maxlength="100" placeholder="如：学分银行运营中心" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12" :xs="24" :sm="12">
            <el-form-item label="开始时间" required>
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width:100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24" :sm="12">
            <el-form-item label="结束时间" required>
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width:100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12" :xs="24" :sm="12">
            <el-form-item label="积分倍率">
              <el-input-number v-model="form.multiplier" :min="1.0" :max="10.0" :step="0.1" :precision="1" />
              <div class="field-hint">1.0=无积分奖励；&gt;1.0=活动期间积分翻倍</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">{{ isEdit ? '保存修改' : '确认创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Clock, Lightning,
  Picture, Loading, CircleCheck, Close, Collection
} from '@element-plus/icons-vue'
import { getCampaigns, createCampaign, updateCampaign, deleteCampaign, uploadImage, getCampaignDetail } from '@/api/campaign'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(9)
const filterStatus = ref('')
const coverPreview = ref('')

const form = reactive({
  title: '', description: '', coverImage: '', images: [], organizer: '',
  startTime: '', endTime: '', multiplier: 1.0
})

function resetForm() {
  form.title = ''; form.description = ''; form.coverImage = ''; form.images = []; form.organizer = ''
  form.startTime = ''; form.endTime = ''; form.multiplier = 1.0
  coverPreview.value = ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCampaigns(currentPage.value, pageSize.value, filterStatus.value)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false; editId.value = null; resetForm(); dialogVisible.value = true
}

function parseImages(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  try { const arr = JSON.parse(val); return Array.isArray(arr) ? arr : [] } catch (e) { return [] }
}

function openEdit(item) {
  isEdit.value = true; editId.value = item.id
  form.title = item.title || ''
  form.description = item.description || ''
  form.coverImage = item.coverImage || ''
  form.images = parseImages(item.images)
  form.organizer = item.organizer || ''
  form.startTime = item.startTime || ''
  form.endTime = item.endTime || ''
  form.multiplier = item.multiplier || 1.0
  coverPreview.value = item.coverImage || ''
  dialogVisible.value = true
}

function handleMultiFile(file) {
  const raw = file.raw || file
  if (!raw) return
  if (raw.size > 5 * 1024 * 1024) { ElMessage.warning('单张不超过5MB'); return }
  if (form.images.length >= 6) { ElMessage.warning('最多6张'); return }
  uploadImage(raw).then(url => {
    form.images.push(url)
  }).catch(e => {
    ElMessage.error('上传失败：' + (e.message || ''))
  })
}

function removeImage(idx) {
  form.images.splice(idx, 1)
}

function handleFileChange(file) {
  const raw = file.raw || file
  if (!raw) return
  if (raw.size > 5 * 1024 * 1024) { ElMessage.warning('文件不能超过5MB'); return }
  coverPreview.value = URL.createObjectURL(raw)
  uploading.value = true
  uploadImage(raw).then(url => {
    form.coverImage = url
    ElMessage.success('上传成功')
  }).catch(e => {
    ElMessage.error('上传失败：' + (e.message || ''))
  }).finally(() => { uploading.value = false })
}

async function submitForm() {
  if (!form.title) { ElMessage.warning('请输入活动标题'); return }
  if (!form.startTime) { ElMessage.warning('请选择开始时间'); return }
  if (!form.endTime) { ElMessage.warning('请选择结束时间'); return }
  submitting.value = true
  try {
    const data = { ...form, images: JSON.stringify(form.images) }
    if (isEdit.value) {
      await updateCampaign(editId.value, data)
    } else {
      await createCampaign(data)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确定要删除活动「' + item.title + '」吗？此操作不可恢复。', '确认删除', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
    await deleteCampaign(item.id)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

function goDetail(id) { router.push('/campaign/' + id) }

function coverStyle(url) {
  return url ? { backgroundImage: 'url(' + url + ')' } : { background: 'linear-gradient(135deg, var(--cb-primary) 0%, var(--cb-primary-light) 100%)' }
}

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

function statusType(s) { return s === 1 ? 'success' : s === 2 ? 'info' : 'warning' }
function statusText(s) { return s === 1 ? '进行中' : s === 2 ? '已结束' : '未开始' }

onMounted(async () => {
  await loadData()
  const editIdFromQuery = route.query.edit
  if (editIdFromQuery) {
    try {
      const campaign = await getCampaignDetail(Number(editIdFromQuery))
      if (campaign) openEdit(campaign)
    } catch (e) { /* ignore */ }
  }
})
</script>

<style scoped>
.campaign-manage {
  min-height: calc(100vh - 80px);
  background-color: var(--cb-bg);
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: var(--cb-text-2xl);
  font-weight: 700;
  color: var(--cb-ink);
  margin: 0;
}

.page-subtitle {
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
  margin: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 14px 18px;
  background: var(--cb-bg-card);
  border-radius: var(--cb-radius-md);
  border: 1px solid var(--cb-border);
}

.filter-summary {
  font-size: var(--cb-text-sm);
  color: var(--cb-slate);
}

.filter-summary strong {
  color: var(--cb-primary);
  font-weight: 600;
}

.card-col {
  margin-bottom: 20px;
}

.campaign-card {
  cursor: pointer;
  height: 100%;
  transition: transform var(--cb-transition-normal), box-shadow var(--cb-transition-normal);
}

.campaign-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--cb-shadow-lg);
}

.campaign-card :deep(.el-card__body) {
  padding: 0;
}

.card-cover {
  height: 170px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--cb-radius-lg) var(--cb-radius-lg) 0 0;
  overflow: hidden;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 40%, rgba(0, 0, 0, 0.45) 100%);
  pointer-events: none;
}

.cover-text {
  color: #fff;
  font-size: var(--cb-text-xl);
  font-weight: 600;
  padding: 16px;
  text-align: center;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 1;
}

.badge-bonus {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: var(--cb-warning);
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: var(--cb-text-xs);
  font-weight: 600;
  z-index: 2;
  box-shadow: var(--cb-shadow-sm);
}

.card-info {
  padding: 18px;
}

.card-title {
  font-size: var(--cb-text-lg);
  font-weight: 600;
  color: var(--cb-charcoal);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
  margin-bottom: 10px;
}

.card-meta .el-icon {
  color: var(--cb-primary-light);
}

.card-desc {
  font-size: var(--cb-text-sm);
  color: var(--cb-slate);
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--cb-border-light);
}

.card-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--cb-muted);
  background: var(--cb-bg-card);
  border-radius: var(--cb-radius-lg);
  border: 1px dashed var(--cb-border);
}

.empty-state .empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
  color: var(--cb-muted);
  opacity: 0.6;
}

.empty-state .empty-title {
  font-size: var(--cb-text-lg);
  font-weight: 600;
  color: var(--cb-slate);
  margin-bottom: 8px;
}

.empty-state .empty-text {
  font-size: var(--cb-text-sm);
  color: var(--cb-muted);
}

.campaign-form .el-input-number {
  width: 100%;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.upload-box {
  width: 180px;
  height: 110px;
  border: 2px dashed var(--cb-border);
  border-radius: var(--cb-radius-md);
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color var(--cb-transition-fast);
  background: var(--cb-bg);
}

.upload-box:hover {
  border-color: var(--cb-primary);
}

.upload-box.has-preview {
  border-style: solid;
  border-color: var(--cb-border);
}

.upload-placeholder {
  text-align: center;
  color: var(--cb-muted);
  font-size: var(--cb-text-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--cb-text-sm);
  color: var(--cb-slate);
}

.status-success {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--cb-success);
  font-weight: 500;
}

.status-hint {
  color: var(--cb-muted);
}

.field-hint {
  font-size: var(--cb-text-xs);
  color: var(--cb-muted);
  margin-top: 4px;
  line-height: 1.4;
}

.multi-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}

.mu-item {
  width: 88px;
  height: 88px;
  border-radius: var(--cb-radius-md);
  overflow: hidden;
  position: relative;
  border: 1px solid var(--cb-border);
}

.mu-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mu-del {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background var(--cb-transition-fast);
}

.mu-del:hover {
  background: var(--cb-danger);
}

.mu-add {
  width: 88px;
  height: 88px;
  border: 2px dashed var(--cb-border);
  border-radius: var(--cb-radius-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--cb-muted);
  cursor: pointer;
  transition: all var(--cb-transition-fast);
}

.mu-add:hover {
  border-color: var(--cb-primary);
  color: var(--cb-primary);
  background: var(--cb-primary-glow);
}

@media (max-width: 768px) {
  .campaign-manage {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .card-actions {
    flex-wrap: wrap;
  }
}
</style>
