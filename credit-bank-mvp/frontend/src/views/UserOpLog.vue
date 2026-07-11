<template>
  <div class="op-logs">
    <div class="toolbar">
      <span class="toolbar-title">📝 用户操作日志</span>
      <el-button @click="$router.push('/users')">← 返回用户列表</el-button>
    </div>

    <el-card>
      <el-table :data="logs" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="targetUserName" label="目标用户" width="120">
          <template #default="scope">
            <span v-if="scope.row.targetUserName">{{ scope.row.targetUserName }}</span>
            <span v-else style="color:#adb5bd;">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="130">
          <template #default="scope">
            <el-tag :type="actionType(scope.row.action)" size="small">{{ actionText(scope.row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="200" />
        <el-table-column prop="createdAt" label="操作时间" width="160">
          <template #default="scope">{{ fmt(scope.row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && logs.length === 0" style="text-align:center;padding:40px;color:#868e96;">
        📭 暂无操作日志
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination background layout="prev, pager, next" :total="total"
          :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOpLogs } from '@/api/user'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const ACTION_MAP = {
  FREEZE: { text: '冻结账户', type: 'warning' },
  UNFREEZE: { text: '解冻账户', type: 'success' },
  RESET_PW: { text: '重置密码', type: 'danger' },
  BATCH_FREEZE: { text: '批量冻结', type: 'warning' },
  BATCH_UNFREEZE: { text: '批量解冻', type: 'success' },
  UPDATE: { text: '编辑信息', type: 'info' }
}

function actionText(a) { return ACTION_MAP[a]?.text || a }
function actionType(a) { return ACTION_MAP[a]?.type || 'info' }
function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const res = await getOpLogs(currentPage.value, pageSize.value)
    logs.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载操作日志失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-title { font-size: 16px; font-weight: 600; color: #2c3e50; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
