<template>
  <div class="expert-certs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的评审资质</span>
          <el-button type="primary" size="small" @click="openApply">申请领域认证</el-button>
        </div>
      </template>

      <div v-if="certs.length > 0" class="badge-wall">
        <el-tag v-for="cert in certs" :key="cert.id" class="cert-badge" effect="plain">
          <el-icon><Medal /></el-icon>
          {{ cert.fieldName }}
        </el-tag>
      </div>
      <div v-else class="empty-tip">
        暂无评审资质。通过领域认证后，你才能被指派评审对应认证标准的申请。
      </div>
    </el-card>

    <el-card style="margin-top: 16px;">
      <template #header>
        <span>认证申请记录</span>
      </template>
      <el-table :data="myApplies" border style="width: 100%;">
        <el-table-column prop="id" label="申请单号" width="90" />
        <el-table-column label="申请领域" width="160">
          <template #default="scope">{{ parseForm(scope.row).fieldName || '—' }}</template>
        </el-table-column>
        <el-table-column label="证明材料" min-width="200">
          <template #default="scope">
            <template v-if="attachmentsOf(scope.row).length">
              <div v-for="(att, i) in attachmentsOf(scope.row)" :key="i" class="att-row">
                <span class="att-name">{{ att.name }}</span>
                <el-link v-if="isPreviewable(att)" :href="previewUrl(att)" target="_blank"
                         type="primary">预览</el-link>
                <el-link :href="downloadUrl(att)" type="primary">下载</el-link>
              </div>
            </template>
            <span v-else class="empty-tip">未提供</span>
          </template>
        </el-table-column>
        <el-table-column label="审批进度" width="110">
          <template #default="scope">
            <span v-if="scope.row.flowSteps && scope.row.flowSteps.length">
              {{ doneSteps(scope.row) }}/{{ scope.row.flowSteps.length }} 步
            </span>
            <span v-else class="empty-tip">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.statusType">{{ scope.row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" />
      </el-table>
      <div v-if="myApplies.length === 0" style="text-align: center; padding: 24px; color: #868e96;">
        暂无申请记录
      </div>
    </el-card>

    <!-- 申请认证对话框 -->
    <el-dialog v-model="applyVisible" title="申请领域认证" width="580px">
      <el-form :model="applyForm" label-width="100px" label-position="left">
        <el-form-item label="认证标准" required>
          <el-select v-model="applyForm.certStandardId" placeholder="选择要申请的认证标准"
                     style="width: 100%;" @change="loadFlowPreview">
            <el-option v-for="s in applicableStandards" :key="s.id"
                       :label="s.standardName" :value="s.id" />
          </el-select>
          <div v-if="selectedStandard" class="standard-hint">{{ standardDesc(selectedStandard) }}</div>
        </el-form-item>
        <el-form-item v-if="flowPreview.length" label="审核流程">
          <div class="flow-preview">
            <span v-for="(n, i) in flowPreview" :key="n.id" class="flow-node">
              第{{ i + 1 }}步 {{ n.auditorName || ('用户#' + n.auditorId) }}
              <el-icon v-if="i < flowPreview.length - 1" class="flow-arrow"><Right /></el-icon>
            </span>
          </div>
        </el-form-item>
        <el-form-item label="领域名称">
          <el-input v-model="applyForm.fieldName" placeholder="默认使用认证标准名称" />
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="applyForm.reason" type="textarea" :rows="3"
                    placeholder="说明你的相关背景与资历" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            style="width: 100%;"
            action="/api/files/upload-attachment"
            name="file"
            :limit="5"
            :file-list="uploadList"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-remove="onUploadRemove"
            :on-error="onUploadError"
          >
            <el-button size="small">
              <el-icon><Paperclip /></el-icon>&nbsp;上传文件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word、图片，单个不超过 10MB，最多 5 个（论文、获奖证书等）</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, Paperclip, Right } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getCertsByExpert } from '@/api/expertCert'
import { getCertStandards } from '@/api/certStandard'
import { getAuditFlow } from '@/api/auditFlow'
import { getApplications, submitApplication } from '@/api/application'

const { currentUser } = useAuth()

const certs = ref([])
const standards = ref([])
const myApplies = ref([])
const applyVisible = ref(false)
const applyForm = ref({ certStandardId: null, fieldName: '', reason: '', attachments: [] })
const uploadList = ref([])
const flowPreview = ref([])

const ALLOWED_EXTS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.pdf', '.doc', '.docx']

// 只列适用对象为专家的标准，且已持证的不再出现
const applicableStandards = computed(() => {
  const held = new Set(certs.value.map(c => c.certStandardId))
  return standards.value.filter(s =>
    s.isEnabled === 1 && s.targetRole === 'expert' && !held.has(s.id))
})

const selectedStandard = computed(() =>
  standards.value.find(s => s.id === applyForm.value.certStandardId))

onMounted(loadAll)

async function loadAll() {
  const id = currentUser.value?.id
  if (!id) return
  try {
    certs.value = await getCertsByExpert(id)
    standards.value = await getCertStandards()
    const applies = await getApplications(currentUser.value?.role, id)
    myApplies.value = applies.filter(
      a => a.bizType === 'EXPERT_CERT' && a.applicantId === id)
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function standardDesc(s) {
  const audit = s.needManualAudit === 1
    ? `需人工审核${s.flowStepCount ? `（${s.flowStepCount} 级流程）` : ''}`
    : '达标自动通过'
  const header = `${s.standardName} v${s.version || '1.0'} · ${audit}`
  return s.requirementText ? `${header}\n${s.requirementText}` : header
}

async function loadFlowPreview() {
  flowPreview.value = []
  const s = selectedStandard.value
  if (!s || s.needManualAudit !== 1) return
  try {
    flowPreview.value = await getAuditFlow(s.id)
  } catch (e) {
    flowPreview.value = []
  }
}

function openApply() {
  applyForm.value = { certStandardId: null, fieldName: '', reason: '', attachments: [] }
  uploadList.value = []
  flowPreview.value = []
  applyVisible.value = true
}

function beforeUpload(file) {
  const ext = file.name.includes('.')
    ? file.name.substring(file.name.lastIndexOf('.')).toLowerCase() : ''
  if (!ALLOWED_EXTS.includes(ext)) {
    ElMessage.warning('仅支持 PDF、Word、图片格式')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('单个文件不能超过 10MB')
    return false
  }
  return true
}

function onUploadSuccess(response, file) {
  if (response.code === 200) {
    // 用 uid 精确标识，避免同名文件误删
    applyForm.value.attachments.push({ uid: file.uid, name: file.name, url: response.data })
  } else {
    ElMessage.error(response.message || '上传失败')
    // 只移除失败文件，已上传成功的保持不变
    uploadList.value = uploadList.value.filter(f => f.uid !== file.uid)
  }
}

function onUploadRemove(file) {
  // 用 uid 精确删除：即使多个文件同名，也只删当前移除的那个
  applyForm.value.attachments = applyForm.value.attachments.filter(a => a.uid !== file.uid)
}

function onUploadError(err, file) {
  // 服务器端校验失败（如超10MB/格式不对）：只移除失败文件，保留已上传好的
  ElMessage.error(err?.message || '上传失败，请重试')
  uploadList.value = uploadList.value.filter(f => f.uid !== file.uid)
}

async function submitApply() {
  if (!applyForm.value.certStandardId) {
    ElMessage.warning('请选择认证标准')
    return
  }
  if (!applyForm.value.reason.trim()) {
    ElMessage.warning('请填写申请理由')
    return
  }
  try {
    await submitApplication({
      bizType: 'EXPERT_CERT',
      applicantId: currentUser.value.id,
      formData: JSON.stringify({
        certStandardId: applyForm.value.certStandardId,
        fieldName: applyForm.value.fieldName.trim(),
        reason: applyForm.value.reason.trim(),
        attachments: applyForm.value.attachments
      })
    })
    ElMessage.success('申请已提交')
    applyVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function parseForm(row) {
  try {
    return row.formData ? JSON.parse(row.formData) : {}
  } catch (e) {
    return {}
  }
}

function attachmentsOf(row) {
  const atts = parseForm(row).attachments
  return Array.isArray(atts) ? atts : []
}

function doneSteps(row) {
  return row.flowSteps.filter(s => s.state === 'done').length
}

function fileNameFromUrl(url) {
  return (url || '').split('?')[0].split('/').pop()
}

function isPreviewable(att) {
  return /\.(pdf|jpe?g|png|gif|webp)$/i.test(fileNameFromUrl(att.url))
}

function previewUrl(att) {
  return `/api/files/preview/${fileNameFromUrl(att.url)}?name=${encodeURIComponent(att.name)}`
}

function downloadUrl(att) {
  return `/api/files/download/${fileNameFromUrl(att.url)}?name=${encodeURIComponent(att.name)}`
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.badge-wall {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cert-badge {
  color: #9a6700;
  border-color: #d4a72c;
  background: #fff8e1;
  font-weight: 600;
  padding: 0 12px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.empty-tip {
  color: #868e96;
  font-size: 13px;
}

.upload-tip {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}

.standard-hint {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 6px;
  background: #f8f9fa;
  border-radius: 4px;
  padding: 6px 10px;
  width: 100%;
  white-space: pre-line;
  max-height: 140px;
  overflow-y: auto;
}

.flow-preview {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: 13px;
  color: #495057;
}

.flow-node {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f1f3f5;
  border-radius: 4px;
  padding: 3px 10px;
}

.flow-arrow {
  color: #adb5bd;
}

.att-row {
  display: flex;
  align-items: center;
  gap: 10px;
  line-height: 1.8;
}

.att-name {
  color: #495057;
}
</style>
