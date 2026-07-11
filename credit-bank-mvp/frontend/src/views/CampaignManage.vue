<template>
  <div class="campaign-manage">
    <div class="toolbar">
      <div class="filter-tabs">
        <el-radio-group v-model="filterStatus" @change="loadData" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">未开始</el-radio-button>
          <el-radio-button value="1">进行中</el-radio-button>
          <el-radio-button value="2">已结束</el-radio-button>
        </el-radio-group>
      </div>
      <el-button type="primary" @click="openCreate">+ 创建活动</el-button>
    </div>

    <!-- 卡片列表 -->
    <el-row :gutter="18" v-loading="loading">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom: 18px;">
        <el-card class="campaign-card" shadow="hover" @click="goDetail(item.id)">
          <div class="card-cover" :style="coverStyle(item.coverImage)">
            <span v-if="!item.coverImage" class="cover-text">{{ item.title }}</span>
            <span v-if="item.multiplier > 1" class="badge-bonus">{{ item.multiplier }}x 积分翻倍</span>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-meta">🕐 {{ fmt(item.startTime) }} ~ {{ fmt(item.endTime) }}</div>
            <div class="card-footer">
              <el-tag :type="statusType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
              <div class="card-actions" @click.stop>
                <el-button size="small" @click="openEdit(item)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="handleDelete(item)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="!loading && list.length === 0" style="text-align:center;padding:60px;color:#868e96;">📭 暂无活动数据</div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadData" />
    </div>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '创建活动'" width="620px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
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
              <div class="upload-box">
                <img v-if="coverPreview" :src="coverPreview" class="upload-preview" />
                <div v-else class="upload-placeholder">
                  <span style="font-size:28px;">🖼️</span>
                  <span>点击上传</span>
                </div>
              </div>
            </el-upload>
            <span v-if="uploading">上传中...</span>
            <span v-else-if="form.coverImage" style="color:#0f6e56;">✅ 已上传</span>
          </div>
        </el-form-item>
        <el-form-item label="活动图片">
          <div class="multi-upload">
            <div v-for="(img, idx) in form.images" :key="idx" class="mu-item">
              <img :src="img" class="mu-thumb" />
              <span class="mu-del" @click="removeImage(idx)">✕</span>
            </div>
            <el-upload
              v-if="form.images.length < 6"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleMultiFile"
              accept="image/*"
            >
              <div class="mu-add">+</div>
            </el-upload>
          </div>
          <div class="field-hint">最多6张，将在详情页轮播展示</div>
        </el-form-item>
        <el-form-item label="主办方">
          <el-input v-model="form.organizer" maxlength="100" placeholder="如：学分银行运营中心" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" required>
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" required>
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="积分倍率">
              <el-input-number v-model="form.multiplier" :min="1.0" :max="10.0" :step="0.1" :precision="1" />
              <div class="field-hint">1.0=无积分奖励；>1.0=活动期间积分翻倍</div>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCampaigns, createCampaign, updateCampaign, deleteCampaign, uploadImage } from '@/api/campaign'

const router = useRouter()
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
    await ElMessageBox.confirm('确定要删除活动「' + item.title + '」吗？此操作不可恢复。', '确认删除', { type: 'warning' })
    await deleteCampaign(item.id)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

function goDetail(id) { router.push('/campaign/' + id) }

function coverStyle(url) {
  return url ? { backgroundImage: 'url(' + url + ')' } : { background: 'linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%)' }
}

function fmt(t) {
  if (!t) return ''
  return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t
}

function statusType(s) { return s === 1 ? 'success' : s === 2 ? 'info' : '' }
function statusText(s) { return s === 1 ? '进行中' : s === 2 ? '已结束' : '未开始' }

onMounted(() => { loadData() })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.campaign-card { cursor: pointer; overflow: hidden; }
.campaign-card:hover { transform: translateY(-2px); }
.card-cover { height: 140px; background-size: cover; background-position: center; position: relative;
  display: flex; align-items: center; justify-content: center; border-radius: 6px; margin: -20px -20px 0; }
.cover-text { color: #fff; font-size: 16px; font-weight: 600; padding: 16px; text-align: center; text-shadow: 0 1px 3px rgba(0,0,0,0.3); }
.badge-bonus { position: absolute; top: 8px; right: 8px; background: #f59f00; color: #fff;
  padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.card-info { margin-top: 12px; }
.card-title { font-size: 15px; font-weight: 600; color: #2c3e50; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-meta { font-size: 12px; color: #868e96; margin-bottom: 8px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 8px; border-top: 1px solid #f0f0f0; }
.card-actions { display: flex; gap: 6px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
.upload-row { display: flex; align-items: center; gap: 12px; }
.upload-box { width: 160px; height: 100px; border: 2px dashed #dee2e6; border-radius: 8px; cursor: pointer; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.upload-box:hover { border-color: #3b5bdb; }
.upload-placeholder { text-align: center; color: #868e96; font-size: 12px; }
.upload-preview { width: 100%; height: 100%; object-fit: cover; }
.field-hint { font-size: 11px; color: #868e96; margin-top: 2px; line-height: 1.4; }

.multi-upload { display: flex; flex-wrap: wrap; gap: 8px; align-items: flex-start; }
.mu-item { width: 80px; height: 80px; border-radius: 6px; overflow: hidden; position: relative; border: 1px solid #e9ecef; }
.mu-thumb { width: 100%; height: 100%; object-fit: cover; }
.mu-del { position: absolute; top: 2px; right: 2px; width: 18px; height: 18px; border-radius: 50%; background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.mu-del:hover { background: #e03131; }
.mu-add { width: 80px; height: 80px; border: 2px dashed #dee2e6; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 28px; color: #adb5bd; cursor: pointer; }
.mu-add:hover { border-color: #3b5bdb; color: #3b5bdb; }
</style>
