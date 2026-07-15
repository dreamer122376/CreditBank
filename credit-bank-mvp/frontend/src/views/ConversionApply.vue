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
        <el-table-column prop="convertedType" label="转换后成果类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.convertedType }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '重新提交申请' : '转换申请'" width="600px">
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
        <el-form-item label="转换后成果类型" required>
          <el-input v-model="form.convertedType" placeholder="如：课程" />
        </el-form-item>

        <el-form-item label="证明材料">
          <el-input v-model="form.certificateFile" placeholder="上传证明材料文件路径" />
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
import {
  getConversionApplications,
  submitConversionApplication
} from '@/api/conversionApplication'
import { getConversionRules } from '@/api/conversionRule'
import { getOrganizations } from '@/api/organization'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const loading = ref(true)
const applications = ref([])
const rules = ref([])
const organizations = ref([])
const dialogVisible = ref(false)
const form = ref({})
const isEdit = ref(false)

const availableRules = computed(() => {
  return rules.value.filter(r => r.isEnabled === 1)
})

const tableMaxHeight = computed(() => {
  return Math.max(300, window.innerHeight - 400) + 'px'
})

onMounted(async () => {
  loading.value = true
  try {
    await loadApplications()
    await loadRules()
    await loadOrganizations()
  } finally {
    loading.value = false
  }
})

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

function openApply(row = null) {
  isEdit.value = row !== null
  if (row) {
    form.value = {
      id: row.id,
      ruleId: row.ruleId,
      applyType: row.applyType,
      originalName: row.originalName,
      originalOrgId: row.originalOrgId,
      originalType: row.originalType,
      convertedName: row.convertedName,
      convertedOrgId: row.convertedOrgId,
      convertedType: row.convertedType,
      certificateFile: row.certificateFile
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
      certificateFile: ''
    }
  }
  dialogVisible.value = true
}

function applyByRule(rule) {
  form.value = {
    ruleId: rule.id,
    applyType: 'RULE_CONVERT',
    originalName: rule.originalName,
    originalOrgId: rule.originalOrgId,
    originalType: rule.originalType,
    convertedName: rule.convertedName,
    convertedOrgId: rule.convertedOrgId,
    convertedType: rule.convertedType,
    certificateFile: ''
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
    form.value.convertedType = rule.convertedType
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
</style>