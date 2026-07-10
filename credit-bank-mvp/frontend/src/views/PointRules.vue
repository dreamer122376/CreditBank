<template>
  <div class="point-rules">
    <el-card>
      <template #header>
        <span>积分规则列表</span>
      </template>
      <el-table :data="rules" border style="width: 100%;">
        <el-table-column prop="id" label="规则ID" width="100" />
        <el-table-column prop="ruleCode" label="规则编码" />
        <el-table-column prop="ruleName" label="规则名称" />
        <el-table-column prop="points" label="奖励积分">
          <template #default="scope">
            <span class="points">+{{ scope.row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled === 1 ? 'success' : 'danger'">
              {{ scope.row.enabled === 1 ? '启用' : '停用' }}
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
