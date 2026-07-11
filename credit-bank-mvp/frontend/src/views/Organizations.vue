<template>
  <div class="organizations">
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="STATUS_TAG[scope.row.status] || 'info'">
              {{ STATUS_NAME[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 0" size="small" type="success"
                       @click="changeStatus(scope.row, 1)">审核通过</el-button>
            <el-button v-else-if="scope.row.status === 1" size="small" type="danger"
                       @click="changeStatus(scope.row, 2)">禁用</el-button>
            <el-button v-else size="small" type="success"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getOrganizations,
  createOrganization,
  updateOrganization,
  changeOrganizationStatus
} from '@/api/organization'

const STATUS_NAME = { 0: '待审核', 1: '启用', 2: '禁用' }
const STATUS_TAG = { 0: 'warning', 1: 'success', 2: 'danger' }

const orgs = ref([])
const dialogVisible = ref(false)
const form = ref({})

onMounted(loadData)

async function loadData() {
  try {
    orgs.value = await getOrganizations()
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
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
  try {
    await changeOrganizationStatus(row.id, status)
    ElMessage.success('操作成功')
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
