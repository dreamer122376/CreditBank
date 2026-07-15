<template>
  <div class="cert-standards">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>认证标准管理</span>
          <el-button v-if="canCreate" type="primary" size="small" @click="openCreate">新增标准</el-button>
        </div>
      </template>
      <el-table :data="standards" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="认证名称" min-width="180">
          <template #default="scope">
            <span class="standard-name">{{ scope.row.standardName }}</span>
            <el-tag size="small" type="info" style="margin-left: 6px;">v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orgId" label="归属机构" width="130">
          <template #default="scope">
            <template v-if="scope.row.orgId != null">
              {{ scope.row.orgName }}
            </template>
            <span class="org-name" style="color: #67c23a;">全平台通用</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetRole" label="适用人员" width="110">
          <template #default="scope">
            <el-tag :type="roleTagType(scope.row.targetRole)">
              {{ ROLE_NAME[scope.row.targetRole] || scope.row.targetRole }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行标准" width="110" align="center">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="goRequirement(scope.row)">
              <el-icon><Document /></el-icon> 查看文件
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="审批流程" width="120" align="center">
          <template #default="scope">
            <el-button size="small" link type="warning"
                       :disabled="scope.row.needManualAudit === 0 || !canOperate(scope.row)"
                       @click="goFlowManage(scope.row)">
              <el-icon><Connection /></el-icon>
              {{ scope.row.needManualAudit === 0 ? '无需审核' : `管理流程(${scope.row.flowStepCount || 0})` }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140" align="center">
          <template #default="scope">
            <div class="status-toggle">
              <span :class="['toggle-label', scope.row.isEnabled === 0 ? 'active-danger' : '', scope.row.isEnabled === 1 ? 'inactive' : '']">停用</span>
              <span :class="['toggle-label', scope.row.isEnabled === 1 ? 'active' : '', scope.row.isEnabled === 0 ? 'inactive' : '']">启用</span>
              <el-switch v-model="scope.row.isEnabled"
                         :active-value="1"
                         :inactive-value="0"
                         :disabled="!canOperate(scope.row)"
                         @change="toggle(scope.row)" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <template v-if="canOperate(scope.row)">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            </template>
            <span v-else style="color:#868e96;font-size:12px;">不可操作</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="standards.length === 0" style="text-align: center; padding: 40px;">
        暂无认证标准
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标准' : '新增标准'" width="560px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="认证名称" required>
          <el-input v-model="form.standardName" placeholder="如：计算机能力认证" />
        </el-form-item>
        <el-form-item label="版本号" required>
          <el-input v-model="form.version" placeholder="如：1.0" style="width: 160px;" />
        </el-form-item>
        <el-form-item v-if="isAdmin" label="归属机构ID">
          <el-input-number v-model="form.orgId" :min="1" controls-position="right"
                           placeholder="留空=平台通用" style="width: 200px;" />
          <span class="form-hint">留空表示平台通用认证</span>
        </el-form-item>
        <el-form-item label="适用人员" required>
          <el-select v-model="form.targetRole" placeholder="选择适用对象" style="width: 200px;">
            <el-option label="学生" value="student" />
            <el-option v-if="isAdmin" label="专家" value="expert" />
            <el-option v-if="isAdmin" label="机构" value="org_admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证要求表述">
          <el-input v-model="form.requirementText" type="textarea" :rows="6"
                    placeholder="输入认证要求的执行标准正文（支持换行）" />
        </el-form-item>
        <el-form-item label="需要人工审核">
          <el-switch v-model="form.needManualAudit" :active-value="1" :inactive-value="0" />
          <span class="form-hint">关闭后申请提交即自动通过，无需配置审批流程</span>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Connection } from '@element-plus/icons-vue'
import {
  getCertStandards,
  createCertStandard,
  updateCertStandard,
  toggleCertStandard
} from '@/api/certStandard'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { currentUser } = useAuth()

const ROLE_NAME = {
  admin: '系统管理员',
  org_admin: '机构',
  expert: '专家',
  student: '学生'
}

const isAdmin = computed(() => currentUser.value?.role === 'admin')
const isOrgAdmin = computed(() => currentUser.value?.role === 'org_admin')
const canCreate = computed(() => isAdmin.value || isOrgAdmin.value)

const standards = ref([])
const dialogVisible = ref(false)
const form = ref({})

onMounted(loadData)

async function loadData() {
  try {
    standards.value = await getCertStandards()
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function canOperate(row) {
  if (isAdmin.value) return true
  if (isOrgAdmin.value && row.orgId != null) {
    return row.orgId === currentUser.value.orgId
  }
  return false
}

function roleTagType(role) {
  const map = { student: 'success', expert: 'warning', org_admin: 'primary' }
  return map[role] || 'info'
}

function openCreate() {
  form.value = {
    standardName: '',
    version: '1.0',
    orgId: isAdmin.value ? null : (currentUser.value?.orgId || null),
    targetRole: 'student',
    requirementText: '',
    needManualAudit: 1
  }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (!form.value.standardName?.trim()) {
      ElMessage.warning('认证名称不能为空')
      return
    }
    if (!form.value.targetRole) {
      ElMessage.warning('请选择适用人员')
      return
    }
    if (form.value.id) {
      await updateCertStandard(form.value)
    } else {
      await createCertStandard(form.value)
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
    await toggleCertStandard(row.id)
    ElMessage.success('操作成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

function goRequirement(row) {
  router.push(`/cert-standards/${row.id}/requirement`)
}

function goFlowManage(row) {
  router.push(`/cert-standards/${row.id}/flow`)
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.standard-name {
  font-weight: 600;
  color: #2c3e50;
}

.org-name {
  font-size: 11px;
  color: #868e96;
  margin-top: 2px;
}

.form-hint {
  color: #868e96;
  font-size: 12px;
  margin-left: 8px;
}

.status-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.toggle-label {
  font-size: 13px;
  color: #c0c4cc;
  transition: color 0.2s;
}

.toggle-label.active {
  color: #67c23a;
  font-weight: 600;
}

.toggle-label.active-danger {
  color: #f56c6c;
  font-weight: 600;
}

.toggle-label.inactive {
  color: #c0c4cc;
  font-weight: normal;
}
</style>
