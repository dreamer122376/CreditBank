<template>
  <div class="exchange-rules" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分转换规则</span>
          <el-button type="primary" size="small" @click="openCreate">新增规则</el-button>
        </div>
      </template>
      <el-table :data="rules" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="itemName" label="兑换品名称" />
        <el-table-column prop="requiredCredit" label="所需积分" width="110">
          <template #default="scope">
            <span class="points">{{ scope.row.requiredCredit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="perUserLimit" label="每人限兑" width="100" />
        <el-table-column prop="orgId" label="归属" width="140">
          <template #default="scope">
            <span v-if="scope.row.orgId" style="color: #409eff;">机构 {{ scope.row.orgId }}</span>
            <span v-else style="color: #909399;">全平台通用</span>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
              {{ scope.row.isEnabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <template v-if="currentUser?.role !== 'org_admin' || scope.row.orgId != null">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" :type="scope.row.isEnabled === 1 ? 'danger' : 'success'"
                         @click="toggle(scope.row)">
                {{ scope.row.isEnabled === 1 ? '停用' : '启用' }}
              </el-button>
            </template>
            <span v-else style="color: #909399;">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rules.length === 0" style="text-align: center; padding: 40px;">
        暂无转换规则
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑规则' : '新增规则'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="兑换品名称" required>
          <el-input v-model="form.itemName" placeholder="如：50元京东卡" />
        </el-form-item>
        <el-form-item label="所需积分" required>
          <el-input-number v-model="form.requiredCredit" :min="1" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="每人限兑">
          <el-input-number v-model="form.perUserLimit" :min="1" />
        </el-form-item>
        <el-form-item v-if="currentUser?.role !== 'org_admin'" label="归属机构">
          <el-input-number v-model="form.orgId" :min="1" :precision="0" placeholder="留空为全平台通用" />
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
  getExchangeRules,
  createExchangeRule,
  updateExchangeRule,
  toggleExchangeRule
} from '@/api/exchangeRule'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const rules = ref([])
const dialogVisible = ref(false)
const form = ref({})

onMounted(loadData)

async function loadData() {
  try {
    const allRules = await getExchangeRules()
    if (currentUser.value?.role === 'org_admin') {
      const orgId = currentUser.value.orgId
      rules.value = allRules.filter(r => r.orgId == null || r.orgId === orgId)
    } else {
      rules.value = allRules
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function openCreate() {
  form.value = { itemName: '', requiredCredit: 100, stock: 9999, perUserLimit: 1, orgId: null }
  if (currentUser.value?.role === 'org_admin') {
    form.value.orgId = currentUser.value.orgId
  }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (currentUser.value?.role === 'org_admin') {
      form.value.orgId = currentUser.value.orgId
    }
    if (form.value.id) {
      await updateExchangeRule(form.value)
    } else {
      await createExchangeRule(form.value)
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
    await toggleExchangeRule(row.id)
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
