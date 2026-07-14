<template>
  <div class="point-rules">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分规则列表</span>
          <el-button v-if="currentUser?.role === 'admin'" type="primary" size="small" @click="openCreate">新增规则</el-button>
        </div>
      </template>
      <el-table :data="rules" border style="width: 100%;">
        <el-table-column prop="id" label="规则ID" width="100" />
        <el-table-column prop="eventName" label="事件名称" />
        <el-table-column prop="creditValue" label="奖励积分" width="120">
          <template #default="scope">
            <span class="points">+{{ scope.row.creditValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="projectId" label="关联项目" width="140">
          <template #default="scope">
            <el-tag v-if="scope.row.projectId" type="info">项目 #{{ scope.row.projectId }}</el-tag>
            <el-tag v-else type="info">通用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
              {{ scope.row.isEnabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="currentUser?.role === 'admin'" label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.isEnabled === 1 ? 'danger' : 'success'"
                       @click="toggle(scope.row)">
              {{ scope.row.isEnabled === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="warning" plain @click="handleAdjust(scope.row)">补差</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rules.length === 0" style="text-align: center; padding: 40px;">
        暂无积分规则
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑规则' : '新增规则'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="事件名称" required>
          <el-input v-model="form.eventName" placeholder="如：完成课程" />
        </el-form-item>
        <el-form-item label="奖励积分" required>
          <el-input-number v-model="form.creditValue" :min="1" />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="form.projectId" placeholder="选择项目（可选）" style="width:100%;">
            <el-option label="通用规则（不关联项目）" :value="null" />
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%;" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRules,
  createRule,
  updateRule,
  deleteRule,
  toggleRule,
  adjustRule,
  getProjects
} from '@/api/point'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const rules = ref([])
const projects = ref([])
const dialogVisible = ref(false)
const form = ref({})

onMounted(async () => {
  await loadData()
  await loadProjects()
})

async function loadData() {
  try {
    rules.value = await getRules()
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

async function loadProjects() {
  try {
    projects.value = await getProjects()
  } catch (error) {
    projects.value = []
  }
}

function openCreate() {
  const now = new Date()
  const oneYearLater = new Date(now.getTime() + 365 * 24 * 60 * 60 * 1000)
  form.value = { eventName: '', creditValue: 10, projectId: null, isEnabled: 1, startTime: now, endTime: oneYearLater }
  dialogVisible.value = true
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.eventName) {
    ElMessage.warning('请输入事件名称')
    return
  }
  if (!form.value.creditValue || form.value.creditValue <= 0) {
    ElMessage.warning('奖励积分必须大于0')
    return
  }
  try {
    if (form.value.id) {
      await updateRule(form.value)
    } else {
      await createRule(form.value)
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
    await toggleRule(row.id, newStatus)
    ElMessage.success('操作成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除规则「' + row.eventName + '」吗？此操作不可恢复。', '确认删除', { type: 'warning' })
    await deleteRule(row.id)
    ElMessage.success('已删除')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function handleAdjust(row) {
  try {
    await ElMessageBox.confirm(
      '确定对规则「' + row.eventName + '」执行补差吗？<br><br>' +
      '系统将检查该规则生效日期范围内已有的奖励流水，<br>' +
      '若金额与当前规则值不一致，将自动生成补差流水。<br><br>' +
      '<span style="color:#d93026;">此操作不可撤销，请确认规则积分值已设置正确。</span>',
      '确认补差',
      { type: 'warning', confirmButtonText: '执行补差', dangerouslyUseHTMLString: true }
    )
    const res = await adjustRule(row.id)
    const count = res.adjustedCount
    if (count > 0) {
      ElMessage.success('补差完成，共处理 ' + count + ' 条流水')
    } else {
      ElMessage.info('无需补差，所有流水均已同步')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '补差失败')
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

.points {
  font-weight: 600;
  color: #0b7a4f;
}
</style>