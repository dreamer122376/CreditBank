<template>
  <div class="experts" v-loading="loading">
    <div class="toolbar">
      <div class="toolbar-left">
        <span class="page-title">专家列表</span>
        <span class="page-subtitle">共 {{ filteredExperts.length }} 位专家</span>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="keyword"
          placeholder="搜索姓名 / 账号 / 领域"
          clearable
          style="width: 220px;"
          :prefix-icon="Search"
        />
        <el-button type="primary" @click="openCreate">新增专家</el-button>
      </div>
    </div>

    <div v-if="filteredExperts.length" class="card-grid">
      <el-card v-for="expert in filteredExperts" :key="expert.id" class="expert-card" shadow="hover">
        <div class="expert-head">
          <el-avatar :size="44" class="expert-avatar">{{ avatarText(expert) }}</el-avatar>
          <div class="expert-title">
            <div class="expert-name">
              {{ expert.realName || expert.username }}
              <el-tag size="small" :type="expert.status === 1 ? 'success' : 'danger'" effect="light">
                {{ expert.status === 1 ? '正常' : '冻结' }}
              </el-tag>
            </div>
            <div class="expert-sub">{{ expert.username }}<template v-if="expert.phone"> · {{ expert.phone }}</template></div>
          </div>
        </div>

        <div class="expert-badges">
          <template v-if="(certMap[expert.id] || []).length">
            <el-tag v-for="cert in certMap[expert.id]" :key="cert.id" class="cert-badge" effect="plain" size="small">
              <el-icon><Medal /></el-icon>
              {{ cert.fieldName }}
            </el-tag>
          </template>
          <span v-else class="no-cert">暂无评审资质</span>
        </div>

        <div class="expert-actions">
          <el-button size="small" @click="openEdit(expert)">编辑</el-button>
          <el-button size="small" type="warning" plain @click="openCertManage(expert)">资质管理</el-button>
          <el-button v-if="expert.status === 1" size="small" type="danger" plain
                     @click="changeStatus(expert, 0)">冻结</el-button>
          <el-button v-else size="small" type="success" plain
                     @click="changeStatus(expert, 1)">解冻</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else :description="keyword ? '没有匹配的专家' : '暂无专家'" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑专家' : '新增专家'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item v-if="!form.id" label="登录账号" required>
          <el-input v-model="form.username" placeholder="手机号或邮箱" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" required>
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="form.expertField" placeholder="如：计算机科学" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="certManageVisible" :title="`资质管理 - ${certExpert?.realName || certExpert?.username || ''}`" width="720px">
      <el-table v-loading="certLoading" :data="expertCerts" border style="width: 100%;">
        <el-table-column prop="fieldName" label="认证领域" min-width="140" />
        <el-table-column label="发证时间" width="110">
          <template #default="{ row }">{{ formatDate(row.issuedAt) }}</template>
        </el-table-column>
        <el-table-column label="有效期至" width="110">
          <template #default="{ row }">{{ formatDate(row.validUntil) || '长期' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="certStatus(row).type" size="small">{{ certStatus(row).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="撤销信息" min-width="160">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <div class="revoke-info">{{ row.revokeReason || '-' }}</div>
              <div class="revoke-time">{{ formatDate(row.revokedAt) }}</div>
            </template>
            <span v-else class="no-cert">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="danger" link @click="openRevoke(row)">
              撤销
            </el-button>
            <span v-else class="no-cert">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!certLoading && expertCerts.length === 0" description="该专家暂无资质记录" :image-size="70" />
      <template #footer>
        <el-button @click="certManageVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="revokeVisible" title="撤销评审资质" width="440px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 14px;"
                :title="`将撤销「${revokeTarget?.fieldName || ''}」评审资质，撤销后该专家不能再评审对应标准`" />
      <el-form label-width="80px">
        <el-form-item label="撤销原因" required>
          <el-input v-model="revokeReason" type="textarea" :rows="3" placeholder="请填写撤销原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="revokeVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRevoke">确认撤销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Medal, Search } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getExperts, createExpert, updateExpert, changeExpertStatus } from '@/api/expert'
import { getAllCerts, getCertsByExpert, revokeExpertCert } from '@/api/expertCert'

const { currentUser } = useAuth()

const loading = ref(true)
const experts = ref([])
const certMap = ref({})
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({})

const certManageVisible = ref(false)
const certExpert = ref(null)
const expertCerts = ref([])
const certLoading = ref(false)
const revokeVisible = ref(false)
const revokeTarget = ref(null)
const revokeReason = ref('')

const filteredExperts = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return experts.value
  return experts.value.filter(e =>
    [e.realName, e.username, e.expertField, e.phone].some(v => v && String(v).toLowerCase().includes(kw)))
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    experts.value = await getExperts()
    const certs = await getAllCerts()
    const map = {}
    for (const cert of certs) {
      (map[cert.expertId] = map[cert.expertId] || []).push(cert)
    }
    certMap.value = map
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

function avatarText(expert) {
  const name = expert.realName || expert.username || '专'
  return name.slice(0, 1)
}

function openCreate() {
  form.value = { username: '', password: '', realName: '', expertField: '', phone: '', email: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (form.value.id) {
      await updateExpert(form.value)
    } else {
      await createExpert(form.value)
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
    await changeExpertStatus(row.id, status)
    ElMessage.success('操作成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// ---- 资质管理 ----

async function openCertManage(expert) {
  certExpert.value = expert
  certManageVisible.value = true
  await loadExpertCerts()
}

async function loadExpertCerts() {
  certLoading.value = true
  try {
    expertCerts.value = await getCertsByExpert(certExpert.value.id)
  } catch (error) {
    ElMessage.error(error.message || '加载资质记录失败')
  } finally {
    certLoading.value = false
  }
}

function certStatus(row) {
  if (row.status === 0) return { text: '已撤销', type: 'danger' }
  if (row.validUntil && new Date(row.validUntil) <= new Date()) return { text: '已过期', type: 'info' }
  return { text: '有效', type: 'success' }
}

function openRevoke(row) {
  revokeTarget.value = row
  revokeReason.value = ''
  revokeVisible.value = true
}

async function confirmRevoke() {
  if (!revokeReason.value.trim()) {
    ElMessage.warning('请填写撤销原因')
    return
  }
  try {
    const result = await revokeExpertCert(revokeTarget.value.id, currentUser.value?.role, revokeReason.value.trim())
    revokeVisible.value = false
    ElMessage.success('资质已撤销')
    if (result?.warning) {
      await ElMessageBox.alert(result.warning, '审批流程提醒', { type: 'warning', confirmButtonText: '知道了' })
    }
    await Promise.all([loadExpertCerts(), loadData()])
  } catch (error) {
    ElMessage.error(error.message || '撤销失败')
  }
}

function formatDate(time) {
  return time ? String(time).slice(0, 10) : ''
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.page-subtitle {
  color: #868e96;
  font-size: 13px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 14px;
}

.expert-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.expert-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.expert-avatar {
  background: #edf2ff;
  color: #3b5bdb;
  font-weight: 600;
  flex-shrink: 0;
}

.expert-name {
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.expert-sub {
  color: #868e96;
  font-size: 12px;
  margin-top: 2px;
}

.expert-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
}

.cert-badge {
  color: #9a6700;
  border-color: #d4a72c;
  background: #fff8e1;
  font-weight: 600;
}

.no-cert {
  color: #adb5bd;
  font-size: 12px;
}

.expert-meta {
  color: #495057;
  font-size: 13px;
}

.expert-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
  padding-top: 4px;
  border-top: 1px solid #f1f3f5;
}

.revoke-info {
  color: #c0392b;
  font-size: 12px;
  line-height: 1.5;
}

.revoke-time {
  color: #adb5bd;
  font-size: 12px;
}
</style>
