<template>
  <div class="cert-standards">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>认证标准</span>
          <el-button type="primary" size="small" @click="openCreate">新增标准</el-button>
        </div>
      </template>
      <el-table :data="standards" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="standardName" label="认证名称" />
        <el-table-column prop="minCredit" label="最低积分" width="110">
          <template #default="scope">
            <span class="points">{{ scope.row.minCredit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="needExpertApprove" label="专家签字" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.needExpertApprove === 1 ? 'warning' : 'info'">
              {{ scope.row.needExpertApprove === 1 ? '需要' : '不需要' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validityDays" label="有效期(天)" width="110" />
        <el-table-column prop="isEnabled" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
              {{ scope.row.isEnabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.isEnabled === 1 ? 'danger' : 'success'"
                       @click="toggle(scope.row)">
              {{ scope.row.isEnabled === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="standards.length === 0" style="text-align: center; padding: 40px;">
        暂无认证标准
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标准' : '新增标准'" width="480px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="认证名称" required>
          <el-input v-model="form.standardName" placeholder="如：Java中级开发认证" />
        </el-form-item>
        <el-form-item label="最低积分" required>
          <el-input-number v-model="form.minCredit" :min="1" />
        </el-form-item>
        <el-form-item label="需要专家签字">
          <el-switch v-model="form.needExpertApprove" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="有效期(天)">
          <el-input-number v-model="form.validityDays" :min="1" />
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
  getCertStandards,
  createCertStandard,
  updateCertStandard,
  toggleCertStandard
} from '@/api/certStandard'

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

function openCreate() {
  form.value = { standardName: '', minCredit: 100, needExpertApprove: 0, validityDays: 365 }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
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
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.points {
  font-weight: 600;
  color: #0b7a4f;
}
</style>
