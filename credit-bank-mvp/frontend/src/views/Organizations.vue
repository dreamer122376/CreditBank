<template>
  <div class="organizations" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>机构列表</span>
          <el-button type="primary" size="small" @click="openCreate">新增机构</el-button>
        </div>
      </template>
      <el-table :data="orgs" border style="width: 100%;">
        <el-table-column prop="id" label="机构ID" width="90" />
        <el-table-column prop="name" label="机构名称" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="creditPool" label="积分池余量" width="120">
          <template #default="scope">
            <span style="color: #409eff; font-weight: 500;">{{ scope.row.creditPool || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="STATUS_TAG[scope.row.status] || 'info'">
              {{ STATUS_NAME[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="拒绝原因">
          <template #default="scope">
            <span v-if="scope.row.status === 3 && scope.row.rejectReason" style="color: #909399;">
              {{ scope.row.rejectReason }}
            </span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <template v-if="scope.row.status === 0">
              <el-button size="small" type="success" @click="changeStatus(scope.row, 1)">审核通过</el-button>
              <el-button size="small" type="danger" @click="openReject(scope.row)">拒绝</el-button>
            </template>
            <el-button v-else-if="scope.row.status === 1" size="small" type="danger"
                       @click="changeStatus(scope.row, 2)">禁用</el-button>
            <el-button v-else-if="scope.row.status === 2" size="small" type="success"
                       @click="changeStatus(scope.row, 1)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="orgs.length === 0" style="text-align: center; padding: 40px;">
        暂无机构
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑机构' : '新增机构'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="机构名称" required>
          <el-input v-model="form.name" placeholder="如：XX大学继续教育学院" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditResultVisible" title="审核通过" width="420px">
      <div v-if="auditResult.created" style="text-align:center;padding:12px 0;">
        <el-alert type="success" :title="auditResult.message" :closable="false" show-icon />
        <div style="margin-top:20px;text-align:left;background:#f8f9fa;padding:16px;border-radius:8px;">
          <div style="margin-bottom:10px;"><strong>机构名称：</strong>{{ auditResult.organization?.name }}</div>
          <div style="margin-bottom:10px;"><strong>管理员账号：</strong>{{ auditResult.adminUsername }}</div>
          <div><strong>初始密码：</strong>{{ auditResult.adminPassword }}</div>
        </div>
        <p style="color:#f59f00;font-size:13px;margin-top:12px;">请妥善保管账号密码，关闭后无法再次查看明文密码。</p>
      </div>
      <div v-else style="text-align:center;padding:12px 0;">
        <el-alert type="info" :title="auditResult.message" :closable="false" show-icon />
      </div>
      <template #footer>
        <el-button type="primary" @click="auditResultVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝机构入驻" width="420px">
      <el-form :model="rejectForm" label-width="90px">
        <el-form-item label="机构名称">
          <span>{{ rejectForm.orgName }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3"
                    placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrganizations,
  createOrganization,
  updateOrganization,
  changeOrganizationStatus,
  rejectOrganization
} from '@/api/organization'

const auditResultVisible = ref(false)
const auditResult = ref({})
const adminAccount = ref({})
const rejectDialogVisible = ref(false)
const rejectForm = ref({ id: null, orgName: '', reason: '' })

const STATUS_NAME = { 0: '待审核', 1: '启用', 2: '禁用', 3: '已拒绝' }
const STATUS_TAG = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }

const loading = ref(true)
const orgs = ref([])
const dialogVisible = ref(false)
const form = ref({})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    orgs.value = await getOrganizations()
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { name: '', contactPerson: '', contactPhone: '', address: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (form.value.id) {
      await updateOrganization(form.value)
    } else {
      await createOrganization(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function changeStatus(row, status) {
  const action = status === 1 ? '启用' : '禁用'
  const tip = status === 1
    ? '启用后将解冻本机构所有用户，是否继续？'
    : '禁用后将联动冻结本机构所有用户，是否继续？'
  try {
    await ElMessageBox.confirm(tip, `确认${action}`, { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
    const res = await changeOrganizationStatus(row.id, status)
    const result = res.data || res
    if (status === 1 && row.status === 0) {
      auditResult.value = result
      auditResultVisible.value = true
    }
    ElMessage.success(result.message || '操作成功')
    await loadData()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || '操作失败')
  }
}

function openReject(row) {
  rejectForm.value = { id: row.id, orgName: row.name, reason: '' }
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectForm.value.reason || rejectForm.value.reason.trim() === '') {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await rejectOrganization(rejectForm.value.id, rejectForm.value.reason)
    ElMessage.success('已拒绝该机构入驻申请')
    rejectDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
