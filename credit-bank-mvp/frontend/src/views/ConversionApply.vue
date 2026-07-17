<template>
  <div class="conversion-apply" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>转换申请</span>
          <el-button v-if="currentUser?.role === 'student'" type="primary" size="small" @click="openApply">发起申请</el-button>
        </div>
      </template>
      <el-table :data="applications" border style="width: 100%;" size="small" :max-height="tableMaxHeight">
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="ruleName" label="转换规则" min-width="200" />
        <el-table-column prop="originalName" label="原成果名称" min-width="150" />
        <el-table-column prop="convertedName" label="转换后成果名称" min-width="150" />
        <el-table-column label="对应积分规则" min-width="160">
          <template #default="scope">
            <span v-if="scope.row.creditRuleName" class="credit-rule-name">
              {{ scope.row.creditRuleName }}
            </span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="对应分值" width="90" align="right">
          <template #default="scope">
            <span v-if="scope.row.creditValue != null" class="credit-value">
              +{{ scope.row.creditValue }}
            </span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="applyType" label="申请类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.applyType === 'RULE_CONVERT' ? 'primary' : 'warning'" size="small">
              {{ scope.row.applyType === 'RULE_CONVERT' ? '已有规则转换' : '新增规则申请' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="150">
          <template #default="scope">
            <span v-if="scope.row.rejectReason" class="reject-reason">{{ scope.row.rejectReason }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="证明材料" min-width="180">
          <template #default="scope">
            <div v-if="parseFormDataAtts(scope.row).length">
              <el-link
                v-for="(att, i) in parseFormDataAtts(scope.row)"
                :key="i"
                type="primary"
                :href="isAttachmentPreviewable(att) ? attachmentPreviewUrl(att) : attachmentDownloadUrl(att)"
                target="_blank"
                style="display:inline-flex;align-items:center;margin-right:10px;margin-bottom:4px;"
                size="small">
                <el-icon style="margin-right:3px;"><component :is="isAttachmentPreviewable(att) ? View : Paperclip" /></el-icon>
                {{ att.name?.slice(0, 10) }}{{ att.name?.length > 10 ? '…' : '' }}
              </el-link>
            </div>
            <span v-else style="color:#868e96;font-size:12px;">无附件</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="140">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button v-if="scope.row.status === 2" size="small" type="primary" @click="openApply(scope.row)">重新提交</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="applications.length === 0" style="text-align: center; padding: 40px;">
        暂无转换申请记录
      </div>
    </el-card>

    <el-card style="margin-top: 16px;">
      <template #header>
        <span>已有的转换规则</span>
      </template>
      <el-table :data="rules" border style="width: 100%;" size="small" :max-height="tableMaxHeight">
        <el-table-column prop="id" label="序号" width="60" />
        <el-table-column prop="originalName" label="原成果名称" min-width="180" />
        <el-table-column prop="originalOrgName" label="原成果机构" min-width="120" />
        <el-table-column prop="originalType" label="原成果类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.originalType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="转换" width="40">
          <template #default>
            <span class="arrow">➜</span>
          </template>
        </el-table-column>
        <el-table-column prop="convertedName" label="转换后成果名称" min-width="180" />
        <el-table-column prop="convertedOrgName" label="转换后成果机构" min-width="120" />
        <el-table-column label="对应积分规则" min-width="150">
          <template #default="scope">
            <span v-if="scope.row.creditRuleName" class="credit-rule-name">
              {{ scope.row.creditRuleName }}
            </span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="对应分值" width="90" align="right">
          <template #default="scope">
            <span v-if="scope.row.creditValue != null" class="credit-value">
              +{{ scope.row.creditValue }}
            </span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button v-if="scope.row.isEnabled === 1" size="small" type="primary" @click="applyByRule(scope.row)">申请转换</el-button>
            <span v-else style="color:#868e96;font-size:12px;">已停用</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rules.length === 0" style="text-align: center; padding: 40px;">
        暂无转换规则，您可以申请新增规则
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '提交申请' : '转换申请'" width="600px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="申请类型" required>
          <el-radio-group v-model="form.applyType">
            <el-radio :value="'RULE_CONVERT'">已有规则转换</el-radio>
            <el-radio :value="'RULE_ADD'">新增规则申请</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.applyType === 'RULE_CONVERT'" label="选择规则">
          <el-select v-model="form.ruleId" placeholder="请选择转换规则" style="width:100%;" @change="onRuleChange">
            <el-option v-for="r in availableRules" :key="r.id" :label="r.originalName + ' → ' + r.convertedName" :value="r.id" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">原成果信息</el-divider>
        <el-form-item label="原成果名称" required>
          <el-input v-model="form.originalName" placeholder="如：全国导游基础知识(李巧玲-智慧职教)" />
        </el-form-item>
        <el-form-item label="原成果机构">
          <el-select v-model="form.originalOrgId" placeholder="选择机构（可选）" style="width:100%;">
            <el-option label="不指定机构" :value="null" />
            <el-option v-for="o in organizations" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原成果类型" required>
          <el-input v-model="form.originalType" placeholder="如：在线学习成果" />
        </el-form-item>

        <el-divider content-position="left">转换后成果信息</el-divider>
        <el-form-item label="转换后成果名称" required>
          <el-input v-model="form.convertedName" placeholder="如：(0402114)导游基础知识" />
        </el-form-item>
        <el-form-item label="转换后成果机构">
          <el-select v-model="form.convertedOrgId" placeholder="选择机构（可选）" style="width:100%;">
            <el-option label="不指定机构" :value="null" />
            <el-option v-for="o in organizations" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <!-- RULE_CONVERT：已有关联转换规则，直接展示关联的积分规则名称（由后端覆盖 convertedType） -->
        <template v-if="form.applyType === 'RULE_CONVERT'">
          <el-form-item label="对应积分规则">
            <div v-if="form.creditRuleName" class="credit-rule-readonly">
              <span class="credit-rule-name">{{ form.creditRuleName }}</span>
              <span class="credit-value">+{{ form.creditValue }} 分</span>
            </div>
            <el-tag v-else type="info">请先选择上方的转换规则</el-tag>
          </el-form-item>
          <el-form-item label="转换后成果类型" required>
            <el-input v-model="form.convertedType" :disabled="!!form.ruleId" placeholder="选择转换规则后自动关联" />
          </el-form-item>
        </template>
        <!-- RULE_ADD：新增自定义转换，必须由学生显式选择一条积分规则（审核通过后按该规则加积分） -->
        <template v-if="form.applyType === 'RULE_ADD'">
          <el-form-item label="对应积分规则" required>
            <el-select v-model="form.creditRuleId" placeholder="请选择一条积分规则（审核通过后按此规则加分）" style="width:100%;" filterable @change="onCreditRuleChange">
              <el-option
                v-for="cr in availableCreditRules"
                :key="cr.id"
                :label="`${cr.eventName} (+${cr.creditValue}分)`"
                :value="cr.id">
                <div class="credit-rule-option">
                  <span class="cr-event-name">{{ cr.eventName }}</span>
                  <span class="cr-value">+{{ cr.creditValue }}</span>
                </div>
              </el-option>
            </el-select>
            <div v-if="form.creditRuleId" class="rule-pick-tip">
              转换审核通过后，系统将按所选积分规则自动发放积分。
            </div>
          </el-form-item>
          <el-form-item label="转换后成果类型" required>
            <el-input v-model="form.convertedType" placeholder="如：课程 / 竞赛 / 实践" />
          </el-form-item>
        </template>

        <el-form-item label="证明材料">
          <el-upload
            style="width: 100%;"
            action="/api/files/upload-attachment"
            name="file"
            :headers="uploadHeaders"
            :limit="5"
            :file-list="uploadList"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-remove="onUploadRemove"
            :on-error="onUploadError"
          >
            <el-button size="small">
              <el-icon><Paperclip /></el-icon>
              上传文件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word、图片，单个不超过 10MB，最多 5 个</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交申请</el-button>
      </template>
    </el-dialog>

    <el-card style="margin-top: 16px;">
      <template #header>
        <span>申请须知</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交申请">在规定时间内，通过学分银行信息平台，提交转换申请及证明材料。</el-descriptions-item>
        <el-descriptions-item label="审核认证">学分银行管理中心对材料进行审核，确认其真实性和有效性。</el-descriptions-item>
        <el-descriptions-item label="办理转换">审核通过后公布最终转换结果，根据积分规则自动发放积分。</el-descriptions-item>
        <el-descriptions-item label="时效性">各类转换申请均有严格的截止日期，逾期不予受理。</el-descriptions-item>
        <el-descriptions-item label="规则查询">已有的转换规则可在学分银行信息平台查询。如果您的成果不在列表中，可以申请新增转换规则。</el-descriptions-item>
        <el-descriptions-item label="诚信要求">提交虚假材料申请转换，一经查实将严肃处理。</el-descriptions-item>
        <el-descriptions-item label="转换期间">在正式公布转换结果前，您仍需照常参加已申请转换课程的教学活动。</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Paperclip, View } from '@element-plus/icons-vue'
import {
  getConversionApplications,
  submitConversionApplication
} from '@/api/conversionApplication'
import { getConversionRules } from '@/api/conversionRule'
import { getOrganizations } from '@/api/organization'
import { getRules as getCreditRules } from '@/api/point'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const loading = ref(true)
const applications = ref([])
const rules = ref([])
const organizations = ref([])
const creditRules = ref([]) // 所有可用的积分规则列表
const dialogVisible = ref(false)
const form = ref({})
const isEdit = ref(false)

// 证明材料：附件数组（持久化到 formData.attachments）+ upload 组件绑定的 file-list
const attachments = ref([])
const uploadList = ref([])

// 与 StudentCerts.vue 保持一致：直接从 localStorage 取 cb_token（useAuth 未暴露 getToken）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('cb_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 上传前置校验：扩展名+大小（完全对齐 StudentCerts 标准，保持一致）
const ALLOWED_EXTS = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.pdf', '.doc', '.docx']

function beforeUpload(file) {
  const ext = file.name.includes('.')
    ? file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
    : ''
  if (!ALLOWED_EXTS.includes(ext)) {
    ElMessage.warning('仅支持 PDF、Word、图片格式')
    return false
  }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('单个文件不能超过 10MB')
    return false
  }
  return true
}

// 上传成功：严格对齐 StudentCerts 的 onUploadSuccess 实现
// FileController.uploadAttachment 返回 Result<String>，其中 data 字段就是完整 URL "/api/files/download/{uuid}.ext"
function onUploadSuccess(response, file) {
  if (response && response.code === 200) {
    // 证明材料的 attachment 对象：{ uid, name, url } 三字段统一结构，与 StudentCerts 保持一致
    attachments.value.push({
      uid: file.uid,
      name: file.name,
      url: response.data
    })
  } else {
    ElMessage.error((response && response.message) || '上传失败')
    uploadList.value = uploadList.value.filter(item => item.uid !== file.uid)
  }
}

// 删除文件：按 uid 精确匹配删除（StudentCerts 同样实现）
function onUploadRemove(file) {
  attachments.value = attachments.value.filter(att => att.uid !== file.uid)
}

function onUploadError(err, file) {
  ElMessage.error(err?.message || '上传失败，请重试')
  uploadList.value = uploadList.value.filter(item => item.uid !== file.uid)
}

// 文件名提取：完全对齐 StudentCerts.fileNameFromUrl，同时兼容 "files/xxx" 旧前缀
function fileNameFromUrl(url) {
  if (!url) return ''
  const clean = String(url).split('?')[0]
  // 兼容旧格式 "files/abc.pdf" 和 "/api/files/download/abc.pdf" 以及纯 "abc.pdf"
  const stripPrefix = clean.replace(/^files\//, '')
  return stripPrefix.split('/').pop() || stripPrefix
}

// 附件下载链接：与 StudentCerts.downloadUrl 完全一致
function attachmentDownloadUrl(att) {
  if (!att?.url) return ''
  if (String(att.url).startsWith('http://') || String(att.url).startsWith('https://')) return att.url
  const name = encodeURIComponent(att.name || '附件')
  return `/api/files/download/${fileNameFromUrl(att.url)}?name=${name}`
}

// 附件预览链接：与 StudentCerts.previewUrl 完全一致
function attachmentPreviewUrl(att) {
  if (!att?.url) return ''
  const name = encodeURIComponent(att.name || '附件')
  return `/api/files/preview/${fileNameFromUrl(att.url)}?name=${name}`
}

// PDF/图片等浏览器可直接预览
function isAttachmentPreviewable(att) {
  return /\.(pdf|jpe?g|png|gif|webp)$/i.test(fileNameFromUrl(att?.url))
}

// 解析单条申请的证明材料：优先用 formData.attachments；降级用旧的 certificate_file 单文件
function parseFormDataAtts(row) {
  if (row?.formData) {
    try {
      const obj = typeof row.formData === 'string' ? JSON.parse(row.formData) : row.formData
      if (Array.isArray(obj?.attachments) && obj.attachments.length) {
        return obj.attachments
          .filter(a => a && typeof a.url === 'string')
          .map((a, i) => ({ ...a, uid: a.uid || `${a.url}-${i}` }))
      }
    } catch (_) { /* JSON 解析失败降级用旧字段 */ }
  }
  if (row?.certificateFile) {
    return [{ uid: `cert-${row.id}`, name: '证明材料', url: row.certificateFile }]
  }
  return []
}

// 为保持兼容：若有代码仍调用 getFileDownloadUrl，内部走附件下载链接逻辑
function getFileDownloadUrl(url) {
  return attachmentDownloadUrl({ url, name: '附件' })
}

const availableRules = computed(() => {
  return rules.value.filter(r => r.isEnabled === 1)
})

// 学生可选的积分规则：只展示已启用的（通用+本机构）
const availableCreditRules = computed(() => {
  const list = creditRules.value || []
  return list.filter(r => r.isEnabled === 1)
})

const tableMaxHeight = computed(() => {
  return Math.max(300, window.innerHeight - 400) + 'px'
})

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadApplications(),
      loadRules(),
      loadOrganizations(),
      loadCreditRules()
    ])
  } finally {
    loading.value = false
  }
})

async function loadCreditRules() {
  try {
    creditRules.value = await getCreditRules(true)
  } catch (error) {
    creditRules.value = []
  }
}

async function loadApplications() {
  try {
    applications.value = await getConversionApplications()
  } catch (error) {
    ElMessage.error(error.message || '加载申请记录失败')
  }
}

async function loadRules() {
  try {
    rules.value = await getConversionRules(true)
  } catch (error) {
    rules.value = []
  }
}

async function loadOrganizations() {
  try {
    organizations.value = await getOrganizations()
  } catch (error) {
    organizations.value = []
  }
}

function getStatusType(status) {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

function getStatusText(status) {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已驳回'
    default: return '未知'
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化申请弹窗：清空/回填附件 + creditRuleId/Name/Value
function openApply(row = null) {
  attachments.value = []
  uploadList.value = []
  isEdit.value = row !== null
  if (row) {
    // 重新提交时：RULE_CONVERT 模式下 convertedType 仍然优先展示中文 creditRuleName（避免英文 event_code）
    // RULE_ADD 则保留原 convertedType（自己填的"课程/竞赛"等）
    const fallbackType = row.applyType === 'RULE_CONVERT'
      ? (row.creditRuleName || row.convertedType)
      : row.convertedType
    form.value = {
      id: row.id,
      ruleId: row.ruleId,
      applyType: row.applyType,
      originalName: row.originalName,
      originalOrgId: row.originalOrgId,
      originalType: row.originalType,
      convertedName: row.convertedName,
      convertedOrgId: row.convertedOrgId,
      convertedType: fallbackType,
      certificateFile: row.certificateFile || '',
      creditRuleId: row.creditRuleId ?? null,
      creditRuleName: row.creditRuleName ?? '',
      creditValue: row.creditValue ?? null
    }
    // 重新提交时：把历史附件解析出来回填，保证和 StudentCerts 结构一致（uid/name/url 三字段）
    const existAtts = parseFormDataAtts(row)
    if (existAtts.length) {
      attachments.value = existAtts.map((a, i) => ({
        uid: a.uid || `${row.id}-att-${i}`,
        name: a.name || `附件${i + 1}`,
        url: a.url
      }))
      // 回填 el-upload 的 file-list 初始值
      uploadList.value = attachments.value.map(a => ({
        name: a.name,
        url: attachmentDownloadUrl(a),
        status: 'success',
        uid: a.uid
      }))
    }
  } else {
    form.value = {
      ruleId: null,
      applyType: 'RULE_CONVERT',
      originalName: '',
      originalOrgId: null,
      originalType: '',
      convertedName: '',
      convertedOrgId: null,
      convertedType: '',
      certificateFile: '',
      creditRuleId: null,
      creditRuleName: '',
      creditValue: null
    }
  }
  dialogVisible.value = true
}

function applyByRule(rule) {
  attachments.value = []
  uploadList.value = []
  // RULE_CONVERT 模式下，表单里的 convertedType 展示给学生看：直接用对应积分规则中文名（creditRuleName），
  // 避免学生看到英文 event_code（如 COURSE_COMPLETE）。后端在 submit() 里会覆盖为正确的 event_code，不影响持久化。
  const displayType = rule.creditRuleName || rule.convertedType
  form.value = {
    ruleId: rule.id,
    applyType: 'RULE_CONVERT',
    originalName: rule.originalName,
    originalOrgId: rule.originalOrgId,
    originalType: rule.originalType,
    convertedName: rule.convertedName,
    convertedOrgId: rule.convertedOrgId,
    convertedType: displayType,
    certificateFile: '',
    creditRuleId: rule.creditRuleId ?? null,
    creditRuleName: rule.creditRuleName ?? '',
    creditValue: rule.creditValue ?? null
  }
  dialogVisible.value = true
}

function onRuleChange(ruleId) {
  const rule = rules.value.find(r => r.id === ruleId)
  if (rule) {
    form.value.originalName = rule.originalName
    form.value.originalOrgId = rule.originalOrgId
    form.value.originalType = rule.originalType
    form.value.convertedName = rule.convertedName
    form.value.convertedOrgId = rule.convertedOrgId
    // 同上：展示层填中文，避免英文 event_code
    form.value.convertedType = rule.creditRuleName || rule.convertedType
    form.value.creditRuleId = rule.creditRuleId ?? null
    form.value.creditRuleName = rule.creditRuleName ?? ''
    form.value.creditValue = rule.creditValue ?? null
  } else {
    form.value.creditRuleId = null
    form.value.creditRuleName = ''
    form.value.creditValue = null
  }
}

// RULE_ADD：学生选中积分规则后，提示对应分值（convertedType 仍留给学生手动填，因为是成果类型名，如"课程/竞赛"）
function onCreditRuleChange(creditRuleId) {
  const cr = creditRules.value.find(x => x.id === creditRuleId)
  if (cr) {
    form.value.creditRuleName = cr.eventName || ''
    form.value.creditValue = cr.creditValue ?? null
  } else {
    form.value.creditRuleName = ''
    form.value.creditValue = null
  }
}

async function submit() {
  if (!form.value.applyType) {
    ElMessage.warning('请选择申请类型')
    return
  }
  if (form.value.applyType === 'RULE_CONVERT' && !form.value.ruleId) {
    ElMessage.warning('请选择转换规则')
    return
  }
  // 方案A：RULE_ADD（新增自定义转换）必须显式选择一条积分规则，审核通过后按该规则加积分
  if (form.value.applyType === 'RULE_ADD' && !form.value.creditRuleId) {
    ElMessage.warning('新增规则申请必须选择一条对应的积分规则（用于审核通过后自动加分）')
    return
  }
  if (!form.value.originalName) {
    ElMessage.warning('请输入原成果名称')
    return
  }
  if (!form.value.convertedName) {
    ElMessage.warning('请输入转换后成果名称')
    return
  }
  if (!form.value.originalType) {
    ElMessage.warning('请输入原成果类型')
    return
  }
  if (!form.value.convertedType) {
    ElMessage.warning('请输入转换后成果类型')
    return
  }
  try {
    const submitData = { ...form.value }
    delete submitData.id
    // 只提交数据库持久化字段，去掉非持久化展示字段
    delete submitData.creditRuleName
    delete submitData.creditValue
    // 附件：存到 formData JSON（多文件标准格式）
    submitData.formData = JSON.stringify({ attachments: attachments.value })
    // 兼容旧接口：第一个附件 URL 回写到 certificateFile，避免历史单文件逻辑断链
    submitData.certificateFile = attachments.value.length ? attachments.value[0].url : ''
    await submitConversionApplication(submitData)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.arrow {
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}

.reject-reason {
  color: #f56c6c;
  font-size: 12px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
  line-height: 1.5;
}

.muted {
  color: #adb5bd;
  font-size: 12px;
}

/* 积分规则名称+分值的展示样式 */
.credit-rule-name {
  color: #1e3a5f;
  font-weight: 500;
  margin-right: 6px;
}

.credit-value {
  color: #e8590c;
  font-weight: 700;
}

/* 只读展示：已有转换规则关联的积分规则 */
.credit-rule-readonly {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
}

/* 下拉选项：显示事件名+分值 */
.credit-rule-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.credit-rule-option .cr-event-name {
  color: #333;
}

.credit-rule-option .cr-value {
  color: #e8590c;
  font-weight: 600;
  margin-left: 12px;
}

.rule-pick-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #5c940d;
  background: #f4fce3;
  padding: 4px 8px;
  border-radius: 4px;
}
</style>