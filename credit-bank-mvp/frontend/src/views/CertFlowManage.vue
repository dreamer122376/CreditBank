<template>
  <div class="cert-flow-manage">
    <el-page-header @back="goBack" style="margin-bottom: 16px;">
      <template #title>
        <span>返回</span>
      </template>
      <template #content>
        <span class="page-title">审批流程管理</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div>
            <span class="standard-title">{{ standard.standardName }}</span>
            <el-tag size="small" type="info" style="margin-left: 8px;">v{{ standard.version }}</el-tag>
            <el-tag size="small" :type="standard.needManualAudit === 1 ? 'warning' : 'success'" style="margin-left: 4px;">
              {{ standard.needManualAudit === 1 ? '需人工审核' : '自动通过' }}
            </el-tag>
          </div>
          <div>
            <el-button size="small" @click="addNode">+ 添加节点</el-button>
            <el-button size="small" @click="goBack">返回列表</el-button>
          </div>
        </div>
      </template>

      <div v-if="standard.needManualAudit === 0" class="auto-pass-notice">
        <el-alert title="该认证标准为自动通过型，无需配置审批流程" type="info" :closable="false" show-icon />
      </div>

      <div v-else>
        <div class="flow-tip">
          <el-icon><InfoFilled /></el-icon>
          审批流程为链式结构：从第 1 步开始，每步通过后自动流转到下一步；最后一步通过即代表整个认证审批通过。
        </div>

        <el-table :data="flowNodes" border style="width: 100%;">
          <el-table-column label="步骤" width="70" align="center">
            <template #default="scope">
              <span class="step-num">{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="审核人" min-width="200">
            <template #default="scope">
              <el-select v-model="scope.row.auditorId" placeholder="选择审核人" filterable style="width: 100%;">
                <el-option
                  v-for="u in auditorOptions"
                  :key="u.id"
                  :label="`${u.realName}（${ROLE_NAME[u.role]}）`"
                  :value="u.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="流程走向" width="160" align="center">
            <template #default="scope">
              <el-tag v-if="scope.$index < flowNodes.length - 1" type="primary">
                → 下一步
              </el-tag>
              <el-tag v-else type="success">
                ✓ 审批通过
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button size="small" type="danger" link
                         :disabled="flowNodes.length <= 1"
                         @click="removeNode(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="flow-actions">
          <el-button type="primary" @click="saveFlow">保存流程</el-button>
          <el-button v-if="flowNodes.length > 0" @click="loadFlow">重新加载</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { getCertStandardById } from '@/api/certStandard'
import { getAuditFlow, saveAuditFlow } from '@/api/auditFlow'
import { getUsers } from '@/api/user'

const route = useRoute()
const router = useRouter()

const ROLE_NAME = {
  admin: '系统管理员',
  org_admin: '机构管理员',
  expert: '专家',
  student: '学生'
}

const standard = ref({})
const flowNodes = ref([])
const auditorOptions = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [std, users] = await Promise.all([
      getCertStandardById(route.params.id),
      getUsers()
    ])
    standard.value = std
    // 审核人候选：管理员、机构管理员、专家（学生不能作为审核人）
    auditorOptions.value = users.filter(u => ['admin', 'org_admin', 'expert'].includes(u.role) && u.status === 1)
    await loadFlow()
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
})

async function loadFlow() {
  try {
    flowNodes.value = await getAuditFlow(route.params.id)
    if (flowNodes.value.length === 0 && standard.value.needManualAudit === 1) {
      // 没有流程节点时自动添加一个空节点
      flowNodes.value = [{ auditorId: null }]
    }
  } catch (error) {
    ElMessage.error(error.message || '加载流程失败')
  }
}

function addNode() {
  flowNodes.value.push({ auditorId: null })
}

function removeNode(index) {
  flowNodes.value.splice(index, 1)
}

async function saveFlow() {
  try {
    // 校验
    for (let i = 0; i < flowNodes.value.length; i++) {
      if (!flowNodes.value[i].auditorId) {
        ElMessage.warning(`第 ${i + 1} 步未选择审核人`)
        return
      }
    }
    // 检查是否有重复审核人
    const auditorIds = flowNodes.value.map(n => n.auditorId)
    const uniqueIds = [...new Set(auditorIds)]
    if (uniqueIds.length < auditorIds.length) {
      await ElMessageBox.confirm('存在重复的审核人，同一审核人可能需要多次审批，确认继续？', '提示', {
        type: 'warning'
      })
    }
    await saveAuditFlow(route.params.id, flowNodes.value)
    ElMessage.success('流程保存成功')
    await loadFlow()
  } catch (error) {
    if (error !== 'cancel' && error?.message) {
      ElMessage.error(error.message)
    }
  }
}

function goBack() {
  router.push('/cert-standards')
}
</script>

<style scoped>
.page-title {
  font-weight: 600;
  font-size: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.standard-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.auto-pass-notice {
  padding: 8px 0;
}

.flow-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #868e96;
  font-size: 13px;
  margin-bottom: 16px;
  padding: 10px 14px;
  background: #f8f9fa;
  border-radius: 4px;
}

.step-num {
  font-weight: 600;
  color: #3b5bdb;
  font-size: 15px;
}

.flow-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
</style>
