<template>
  <div class="point-rules">
    <el-card>
      <template #header>
        <span>积分规则列表</span>
      </template>
      <el-table :data="rules" border style="width: 100%;">
        <el-table-column prop="id" label="规则ID" width="100" />
        <el-table-column prop="eventCode" label="事件编码" />
        <el-table-column prop="eventName" label="事件名称" />
        <el-table-column prop="creditValue" label="奖励积分">
          <template #default="scope">
            <span class="points">+{{ scope.row.creditValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scope" label="适用范围">
          <template #default="scope">
            <el-tag type="info">{{ getScopeName(scope.row.scope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
              {{ scope.row.isEnabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rules.length === 0" style="text-align: center; padding: 40px;">
        暂无积分规则
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRules } from '@/api/point'

const rules = ref([])

const SCOPE_NAME = {
  STUDENT: '学生',
  EXPERT: '专家',
  ALL: '全部'
}

function getScopeName(scope) {
  return SCOPE_NAME[scope] || scope
}

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    rules.value = await getRules()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}
</script>

<style scoped>
.points {
  font-weight: 600;
  color: #0b7a4f;
}
</style>
