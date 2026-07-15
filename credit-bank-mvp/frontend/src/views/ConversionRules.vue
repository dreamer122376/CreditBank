<template>
  <div class="conversion-rules" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>转换规则列表</span>
          <el-button v-if="currentUser?.role === 'admin' || currentUser?.role === 'org_admin'" type="primary" size="small" @click="openCreate">新增规则</el-button>
        </div>
      </template>
      <el-table :data="rules" border style="width: 100%;" size="small" :max-height="tableMaxHeight">
        <el-table-column prop="id" label="序号" width="60" />
        <el-table-column prop="originalName" label="原成果名称" min-width="180">
          <template #default="scope">
            <span class="name-cell">{{ scope.row.originalName }}</span>
          </template>
        </el-table-column>
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
        <el-table-column prop="convertedName" label="转换后成果名称" min-width="180">
          <template #default="scope">
            <span class="name-cell">{{ scope.row.convertedName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="convertedOrgName" label="转换后成果机构" min-width="120" />
        <el-table-column prop="convertedType" label="转换后成果类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.convertedType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联积分" width="100">
          <template #default="scope">
            <span v-if="scope.row.creditRuleName" class="credit-info">
              {{ scope.row.creditRuleName }} <el-tag type="success" size="small">+{{ scope.row.creditValue }}</el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="70">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.isEnabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <template v-if="canOperate(scope.row)">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
                <el-button size="small" :type="scope.row.isEnabled === 1 ? 'warning' : 'success'"
                           @click="toggle(scope.row)">
                  {{ scope.row.isEnabled === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
            <span v-else-if="scope.row.convertedOrgId !== null" style="color:#868e96;font-size:12px;">非本机构</span>
            <span v-else style="color:#868e96;font-size:12px;">通用规则</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rules.length === 0" style="text-align: center; padding: 40px;">
        暂无转换规则
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑转换规则' : '新增转换规则'" width="560px">
      <el-form :model="form" label-width="110px">
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

        <el-divider content-position="left">关联设置</el-divider>
        <el-form-item label="关联积分规则">
          <el-select v-model="form.creditRuleId" placeholder="选择积分规则（可选）" style="width:100%;">
            <el-option label="不关联" :value="null" />
            <el-option v-for="cr in creditRules" :key="cr.id" :label="cr.eventName + ' (+' + cr.creditValue + ')' " :value="cr.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效开始时间">
          <el-date-picker v-model="form.effectiveStart" type="datetime" placeholder="选择开始时间" style="width:100%;" />
        </el-form-item>
        <el-form-item label="生效结束时间">
          <el-date-picker v-model="form.effectiveEnd" type="datetime" placeholder="选择结束时间" style="width:100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="规则描述/备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConversionRules,
  createConversionRule,
  updateConversionRule,
  deleteConversionRule,
  toggleConversionRule
} from '@/api/conversionRule'
import { getOrganizations } from '@/api/organization'
import { getRules } from '@/api/point'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const loading = ref(true)
const rules = ref([])
const organizations = ref([])
const creditRules = ref([])
const dialogVisible = ref(false)
const form = ref({})

const tableMaxHeight = computed(() => {
  return Math.max(400, window.innerHeight - 280) + 'px'
})

onMounted(async () => {
  loading.value = true
  try {
    await loadData()
    await loadOrganizations()
    await loadCreditRules()
  } finally {
    loading.value = false
  }
})

async function loadData() {
  try {
    rules.value = await getConversionRules()
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

async function loadOrganizations() {
  try {
    organizations.value = await getOrganizations()
  } catch (error) {
    organizations.value = []
  }
}

async function loadCreditRules() {
  try {
    creditRules.value = await getRules(true)
  } catch (error) {
    creditRules.value = []
  }
}

function canOperate(row) {
  if (currentUser.value?.role === 'admin') {
    return true
  }
  if (currentUser.value?.role === 'org_admin' && row.convertedOrgId !== null) {
    return row.convertedOrgId === currentUser.value.orgId
  }
  return false
}

function openCreate() {
  const now = new Date()
  const oneYearLater = new Date(now.getTime() + 365 * 24 * 60 * 60 * 1000)
  form.value = {
    originalName: '',
    originalOrgId: null,
    originalType: '',
    convertedName: '',
    convertedOrgId: null,
    convertedType: '',
    creditRuleId: null,
    isEnabled: 1,
    effectiveStart: now,
    effectiveEnd: oneYearLater,
    description: ''
  }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
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
    if (form.value.id) {
      await updateConversionRule(form.value)
    } else {
      await createConversionRule(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function toggle(row) {
  try {
    const newStatus = row.isEnabled === 1 ? 0 : 1
    await toggleConversionRule(row.id, newStatus)
    ElMessage.success('操作成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除转换规则吗？此操作不可恢复。', '确认删除', { type: 'warning' })
    await deleteConversionRule(row.id)
    ElMessage.success('已删除')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.name-cell {
  white-space: normal;
  word-break: break-all;
  line-height: 1.4;
}

.arrow {
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}

.credit-info {
  font-size: 12px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 3px;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  flex-shrink: 0;
  padding: 3px 6px;
  font-size: 11px;
}
</style>