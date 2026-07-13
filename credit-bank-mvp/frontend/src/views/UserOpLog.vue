<template>
  <div class="op-logs">
    <div class="toolbar">
      <span class="toolbar-title">📝 操作日志</span>
      <el-button @click="$router.push('/dashboard')">← 返回工作台</el-button>
    </div>

    <el-card>
      <el-form :model="query" inline class="filter-form">
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 140px">
            <el-option label="用户管理" value="USER" />
            <el-option label="平台活动" value="CAMPAIGN" />
            <el-option label="项目管理" value="PROJECT" />
            <el-option label="积分" value="POINT" />
            <el-option label="报名" value="ENROLL" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="query.action" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="冻结账户" value="FREEZE" />
            <el-option label="解冻账户" value="UNFREEZE" />
            <el-option label="批量冻结" value="BATCH_FREEZE" />
            <el-option label="批量解冻" value="BATCH_UNFREEZE" />
            <el-option label="重置密码" value="RESET_PW" />
            <el-option label="编辑信息" value="UPDATE" />
            <el-option label="创建" value="CREATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="项目审核" value="PROJECT_AUDIT" />
            <el-option label="项目下架" value="PROJECT_OFFLINE" />
            <el-option label="活动报名" value="CAMPAIGN_ENROLL" />
            <el-option label="活动取消" value="CAMPAIGN_LEAVE" />
            <el-option label="项目报名" value="PROJECT_ENROLL" />
            <el-option label="项目取消" value="PROJECT_LEAVE" />
            <el-option label="获得积分" value="EARN" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="操作人/目标用户/详情" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="query.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="module" label="模块" width="100">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ moduleText(scope.row.module) }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="操作时间" width="165">
          <template #default="scope">{{ fmt(scope.row.createdAt) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && logs.length === 0" style="text-align:center;padding:40px;color:#868e96;">
        📭 暂无操作日志
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          v-model:current-page="query.page"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOpLogs } from '@/api/user'

const loading = ref(false)
const logs = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10,
  module: '',
  action: '',
  keyword: '',
  dateRange: []
})

const MODULE_MAP = {
  USER: '用户管理',
  CAMPAIGN: '平台活动',
  PROJECT: '项目管理',
  POINT: '积分',
  ENROLL: '报名'
}

const ACTION_MAP = {
  FREEZE: { text: '冻结账户', type: 'warning' },
  UNFREEZE: { text: '解冻账户', type: 'success' },
  RESET_PW: { text: '重置密码', type: 'danger' },
  BATCH_FREEZE: { text: '批量冻结', type: 'warning' },
  BATCH_UNFREEZE: { text: '批量解冻', type: 'success' },
  UPDATE: { text: '编辑信息', type: 'info' },
  CREATE: { text: '创建', type: 'success' },
  DELETE: { text: '删除', type: 'danger' },
  PROJECT_AUDIT: { text: '项目审核', type: 'primary' },
  PROJECT_OFFLINE: { text: '项目下架', type: 'warning' },
  CAMPAIGN_ENROLL: { text: '活动报名', type: 'success' },
  CAMPAIGN_LEAVE: { text: '活动取消', type: 'info' },
  PROJECT_ENROLL: { text: '项目报名', type: 'success' },
  PROJECT_LEAVE: { text: '项目取消', type: 'info' },
  EARN: { text: '获得积分', type: 'success' }
}

function moduleText(m) { return MODULE_MAP[m] || m || '—' }
function actionText(a) { return ACTION_MAP[a]?.text || a }
function actionType(a) { return ACTION_MAP[a]?.type || 'info' }
function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }

onMounted(() => { loadData() })

function handleSearch() {
  query.page = 1
  loadData()
}

function handleReset() {
  query.page = 1
  query.module = ''
  query.action = ''
  query.keyword = ''
  query.dateRange = []
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      module: query.module,
      action: query.action,
      keyword: query.keyword
    }
    if (query.dateRange && query.dateRange.length === 2) {
      params.startTime = query.dateRange[0]
      params.endTime = query.dateRange[1]
    }
    const res = await getOpLogs(params)
    logs.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-title { font-size: 16px; font-weight: 600; color: #2c3e50; }
.filter-form { margin-bottom: 16px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
