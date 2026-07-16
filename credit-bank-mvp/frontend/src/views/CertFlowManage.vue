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
          <span>
            审批流程为链式结构：每步通过后自动流转到下一步，最后一步通过即整体通过。
            <template v-if="requireExpertCert">专家候选人已过滤为持有本标准有效评审资质者。</template>
          </span>
        </div>

        <div class="flow-canvas">
          <template v-for="(node, index) in flowNodes" :key="index">
            <div class="flow-card" :class="{ 'flow-card-empty': !node.auditorId }">
              <div class="flow-card-head">
                <span class="step-num">第 {{ index + 1 }} 步</span>
                <el-button size="small" type="danger" link
                           :disabled="flowNodes.length <= 1"
                           @click="removeNode(index)">
                  删除
                </el-button>
              </div>
              <el-select v-model="node.auditorId" placeholder="选择审核人" filterable style="width: 100%;">
                <el-option
                  v-for="u in auditorOptions"
                  :key="u.id"
                  :label="auditorLabel(u)"
                  :value="u.id"
                  :disabled="u.disabled"
                />
              </el-select>
            </div>
            <el-icon class="flow-arrow"><Right /></el-icon>
          </template>

          <div class="flow-card flow-card-add" @click="addNode">
            <el-icon><Plus /></el-icon>
            <span>添加节点</span>
          </div>

          <el-icon class="flow-arrow"><Right /></el-icon>
          <div class="flow-end">
            <el-icon><CircleCheck /></el-icon>
            审批通过
          </div>
        </div>

        <div class="flow-actions">
          <el-button type="primary" @click="saveFlow">保存流程</el-button>
          <el-button v-if="flowNodes.length > 0" @click="loadFlow">重新加载</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, InfoFilled, Plus, Right } from '@element-plus/icons-vue'
import { getCertStandardById } from '@/api/certStandard'
import { getAuditFlow, saveAuditFlow } from '@/api/auditFlow'
import { getAuditorCandidates } from '@/api/user'
import { getCertifiedExperts } from '@/api/expertCert'

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
const allUsers = ref([])
const certifiedExpertIds = ref(new Set())
const loading = ref(false)

/** 学生认证类标准：专家审核人必须持有本标准的有效评审资质 */
const requireExpertCert = computed(() => standard.value.targetRole === 'student')

const auditorOptions = computed(() => {
  // 管理员/机构管理员/专家均可选，不再按专家资质过滤
  return allUsers.value
    .filter(u => ['admin', 'org_admin', 'expert'].includes(u.role) && u.status === 1)
  // 原逻辑：学生认证类标准下，只保留持证专家和已选中专家
  // const referenced = new Set(flowNodes.value.map(n => n.auditorId).filter(Boolean))
  // .filter(u => u.role !== 'expert' || !requireExpertCert.value
  //   || certifiedExpertIds.value.has(u.id) || referenced.has(u.id))
  // .map(u => ({
  //   ...u,
  //   disabled: u.role === 'expert' && requireExpertCert.value && !certifiedExpertIds.value.has(u.id)
  // }))
})

onMounted(async () => {
  loading.value = true
  try {
    const [std, users] = await Promise.all([
      getCertStandardById(route.params.id),
      getAuditorCandidates()
    ])
    standard.value = std
    allUsers.value = users
    if (std.targetRole === 'student') {
      const certified = await getCertifiedExperts(std.id)
      certifiedExpertIds.value = new Set(certified.map(u => u.id))
    }
    await loadFlow()
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
})

function auditorLabel(u) {
  const base = `${u.realName}（${ROLE_NAME[u.role]}）`
  // if (u.role === 'expert' && requireExpertCert.value && !certifiedExpertIds.value.has(u.id)) {
  //   return `${base} - 已无本标准资质`
  // }
  return base
}

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
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
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

.flow-canvas {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 18px 6px;
}

.flow-card {
  width: 220px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 10px 12px 14px;
  background: #fff;
  transition: box-shadow 0.2s;
}

.flow-card:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.flow-card-empty {
  border-style: dashed;
  border-color: #adb5bd;
}

.flow-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.step-num {
  font-weight: 600;
  color: #3b5bdb;
  font-size: 14px;
}

.flow-card-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 130px;
  min-height: 78px;
  border-style: dashed;
  color: #868e96;
  cursor: pointer;
  user-select: none;
}

.flow-card-add:hover {
  color: #3b5bdb;
  border-color: #3b5bdb;
}

.flow-arrow {
  color: #adb5bd;
  font-size: 18px;
  flex-shrink: 0;
}

.flow-end {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #2f9e44;
  font-weight: 600;
  background: #ebfbee;
  border: 1px solid #b2f2bb;
  border-radius: 8px;
  padding: 10px 16px;
}

.flow-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
</style>
