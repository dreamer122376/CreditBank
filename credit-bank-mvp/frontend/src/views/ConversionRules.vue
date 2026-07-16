<template>
  <div class="conversion-rules" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div v-for="item in statItems" :key="item.key"
           class="stat-card" :class="{ active: statFilter === item.key }"
           @click="toggleStat(item.key)">
        <div class="stat-icon" :style="{ background: item.bgColor }" v-html="item.icon"></div>
        <div class="stat-content">
          <div class="stat-value" :style="{ color: item.color }">{{ item.count }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <el-card class="rules-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">转换规则目录</span>
            <span class="card-subtitle">共 {{ filteredRules.length }} 条映射规则</span>
          </div>
          <div class="header-right">
            <el-tag v-if="statFilter" closable type="primary" effect="plain" @close="statFilter = null">
              {{ statItems.find(i => i.key === statFilter)?.label }}
            </el-tag>
            <el-button v-if="canCreate" type="primary" size="small" @click="openCreate">
              <el-icon><Plus /></el-icon> 新增规则
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="pagedRules" border :row-class-name="rowClassName" style="width: 100%;">
        <el-table-column label="序号" width="60" align="center">
          <template #default="{$index}">
            <span class="seq-no">{{ String((currentPage - 1) * pageSize + $index + 1).padStart(2, '0') }}</span>
          </template>
        </el-table-column>

        <!-- 原成果信息：左侧蓝色区块 -->
        <el-table-column label="原成果信息" min-width="320">
          <template #default="{ row }">
            <div class="achievement-block origin-block">
              <div class="block-label">
                <span class="label-dot origin-dot"></span>
                <span>SOURCE</span>
              </div>
              <div class="achievement-name" :title="row.originalName">{{ row.originalName }}</div>
              <div class="achievement-meta">
                <span class="org-tag" :title="row.originalOrgName">
                  <el-icon><OfficeBuilding /></el-icon>
                  {{ row.originalOrgName || '通用 / 不限机构' }}
                </span>
                <el-tag :type="getOriginalTypeTag(row.originalType)" size="small" effect="light" class="type-tag">
                  {{ row.originalType }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 转换桥梁：签名元素 -->
        <el-table-column label="转换" width="84" align="center">
          <template #default="{ row }">
            <div class="convert-bridge" :class="{ disabled: row.isEnabled !== 1 }">
              <div class="bridge-line"></div>
              <div class="bridge-arrow">
                <el-icon :size="22"><Right /></el-icon>
              </div>
              <div class="bridge-line"></div>
            </div>
          </template>
        </el-table-column>

        <!-- 转换后成果信息：右侧绿色区块 -->
        <el-table-column label="转换后成果信息" min-width="360">
          <template #default="{ row }">
            <div class="achievement-block converted-block">
              <div class="block-label">
                <span class="label-dot converted-dot"></span>
                <span>TARGET</span>
              </div>
              <div class="achievement-name converted-name" :title="row.convertedName">{{ row.convertedName }}</div>
              <div class="achievement-meta">
                <span class="org-tag" :title="row.convertedOrgName">
                  <el-icon><OfficeBuilding /></el-icon>
                  {{ row.convertedOrgName || '通用' }}
                </span>
                <!-- 转换后类型：使用 Tooltip + Popover 确保显示完全 -->
                <el-tooltip v-if="row.convertedType && row.convertedType.length > 8"
                            :content="row.convertedType" placement="top">
                  <el-popover v-if="row.creditRuleName" placement="top" :width="220" trigger="hover">
                    <template #reference>
                      <el-tag :type="getConvertedTypeTag(row.convertedType)" size="small" effect="dark" class="type-tag converted-type">
                        {{ truncateType(row.convertedType) }}
                      </el-tag>
                    </template>
                    <div class="popover-content">
                      <div class="popover-title">关联积分规则</div>
                      <div class="popover-rule-name">{{ row.creditRuleName }}</div>
                      <el-tag type="success" effect="light" class="popover-credit">+{{ row.creditValue }} 积分</el-tag>
                    </div>
                  </el-popover>
                  <el-tag v-else :type="getConvertedTypeTag(row.convertedType)" size="small" effect="dark" class="type-tag converted-type">
                    {{ truncateType(row.convertedType) }}
                  </el-tag>
                </el-tooltip>
                <template v-else>
                  <el-popover v-if="row.creditRuleName" placement="top" :width="220" trigger="hover">
                    <template #reference>
                      <el-tag :type="getConvertedTypeTag(row.convertedType)" size="small" effect="dark" class="type-tag converted-type">
                        {{ row.convertedType }}
                      </el-tag>
                    </template>
                    <div class="popover-content">
                      <div class="popover-title">关联积分规则</div>
                      <div class="popover-rule-name">{{ row.creditRuleName }}</div>
                      <el-tag type="success" effect="light" class="popover-credit">+{{ row.creditValue }} 积分</el-tag>
                    </div>
                  </el-popover>
                  <el-tag v-else :type="getConvertedTypeTag(row.convertedType)" size="small" effect="dark" class="type-tag converted-type">
                    {{ row.convertedType }}
                  </el-tag>
                </template>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="积分规则" width="150" align="center">
          <template #default="{ row }">
            <div v-if="row.creditRuleName" class="credit-rule-box">
              <div class="credit-rule-name" :title="row.creditRuleName">{{ row.creditRuleName }}</div>
              <el-tag type="success" effect="dark" size="small" class="credit-value-tag">
                +{{ row.creditValue }}
              </el-tag>
            </div>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>

        <el-table-column prop="isEnabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <div class="status-wrapper" :class="row.isEnabled === 1 ? 'status-active' : 'status-inactive'">
              <span class="status-dot"></span>
              <span>{{ row.isEnabled === 1 ? '启用' : '停用' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center" class-name="col-action" v-if="!readonly && currentUser">
          <template #default="{ row }">
            <template v-if="canOperate(row)">
              <div class="op-col">
                <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                <el-button :type="row.isEnabled === 1 ? 'warning' : 'success'" size="small" @click="toggle(row)">
                  {{ row.isEnabled === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
            <span v-else-if="row.convertedOrgId !== null" class="scope-tag">非本机构</span>
            <span v-else class="scope-tag">通用规则</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="filteredRules.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredRules.length"
          layout="total, sizes, prev, pager, next, jumper"
          small
          background
          @size-change="currentPage = 1" />
      </div>

      <el-empty v-if="!filteredRules.length" description="暂无符合条件的转换规则" :image-size="120">
        <el-button v-if="canCreate" type="primary" @click="openCreate">创建第一条规则</el-button>
      </el-empty>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑转换规则' : '新增转换规则'" width="620px" class="rule-dialog">
      <el-form :model="form" label-width="120px" class="rule-form">
        <div class="form-section origin-section">
          <div class="section-header">
            <span class="section-dot origin-dot"></span>
            <span class="section-title">原成果信息</span>
          </div>
          <el-form-item label="原成果名称" required>
            <el-input v-model="form.originalName" placeholder="例如：全国导游基础知识(李巧玲-智慧职教)" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="原成果机构" class="form-col">
              <el-select v-model="form.originalOrgId" placeholder="选择机构（可选）" style="width:100%;">
                <el-option label="不指定机构（通用）" :value="null" />
                <el-option v-for="o in organizations" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="原成果类型" required class="form-col">
              <el-input v-model="form.originalType" placeholder="例如：在线学习成果" />
            </el-form-item>
          </div>
        </div>

        <div class="form-arrow-divider">
          <div class="divider-line"></div>
          <el-icon :size="20" color="#3b82f6"><Switch /></el-icon>
          <div class="divider-line"></div>
        </div>

        <div class="form-section converted-section">
          <div class="section-header">
            <span class="section-dot converted-dot"></span>
            <span class="section-title">转换后成果信息</span>
          </div>
          <el-form-item label="转换后成果名称" required>
            <el-input v-model="form.convertedName" placeholder="例如：(0402114)导游基础知识" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="转换后机构" class="form-col">
              <el-select v-model="form.convertedOrgId" placeholder="选择机构（可选）" style="width:100%;">
                <el-option label="不指定机构（通用）" :value="null" />
                <el-option v-for="o in organizations" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="转换后类型" required class="form-col">
              <el-input v-model="form.convertedType" placeholder="例如：课程 / 校园APP开发优秀" />
            </el-form-item>
          </div>
        </div>

        <div class="form-section link-section">
          <div class="section-header">
            <span class="section-link-icon">🔗</span>
            <span class="section-title">关联设置</span>
          </div>
          <el-form-item label="关联积分规则">
            <el-select v-model="form.creditRuleId" placeholder="选择积分规则（审核通过自动加分）" style="width:100%;" filterable>
              <el-option label="不关联（仅记录转换）" :value="null" />
              <el-option v-for="cr in creditRules" :key="cr.id"
                         :label="`${cr.eventName}  (+${cr.creditValue} 积分)`"
                         :value="cr.id" />
            </el-select>
          </el-form-item>
          <div class="form-row">
            <el-form-item label="生效开始时间" class="form-col">
              <el-date-picker v-model="form.effectiveStart" type="datetime" placeholder="开始生效时间" style="width:100%;" />
            </el-form-item>
            <el-form-item label="生效结束时间" class="form-col">
              <el-date-picker v-model="form.effectiveEnd" type="datetime" placeholder="结束生效时间" style="width:100%;" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="规则状态" class="form-col">
              <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0"
                         active-text="启用" inactive-text="停用" />
            </el-form-item>
          </div>
          <el-form-item label="备注说明">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选：规则说明、适用范围等" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">
          <el-icon><Check /></el-icon> 保存规则
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Right, OfficeBuilding, Switch, Check } from '@element-plus/icons-vue'
import {
  getConversionRules,
  createConversionRule,
  updateConversionRule,
  deleteConversionRule,
  toggleConversionRule
} from '@/api/conversionRule'
import { getOrganizations } from '@/api/organization'
import { getRules } from '@/api/point'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

// 公开模式显式开关：为 true 时强制隐藏所有管理操作与管理数据加载，与全局登录态解耦
const props = defineProps({
  readonly: { type: Boolean, default: false }
})

const loading = ref(true)
const rules = ref([])
const organizations = ref([])
const creditRules = ref([])
const dialogVisible = ref(false)
const form = ref({})
const statFilter = ref(null)

// 分页：取消表格 max-height 后，通过前端分页限制单次渲染条数，滚动由页面外层统一管理
const currentPage = ref(1)
const pageSize = ref(10)

// 管理操作按钮显隐：公开模式一律不可创建；管理模式按角色判断
const canCreate = computed(() => {
  if (props.readonly) return false
  return currentUser.value?.role === 'admin' || currentUser.value?.role === 'org_admin'
})

// ============ 统计卡片 ============
const statItems = computed(() => ([
  {
    key: 'all',
    label: '全部规则',
    color: '#1e3a5f',
    bgColor: 'linear-gradient(135deg, #3b82f6 0%, #1e3a5f 100%)',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="8" y1="7" x2="16" y2="7"/><line x1="8" y1="11" x2="16" y2="11"/><line x1="8" y1="15" x2="12" y2="15"/></svg>`,
    count: rules.value.length
  },
  {
    key: 'course',
    label: '课程类',
    color: '#4f46e5',
    bgColor: 'linear-gradient(135deg, #818cf8 0%, #4f46e5 100%)',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>`,
    count: rules.value.filter(r => r.convertedType === '课程').length
  },
  {
    key: 'project',
    label: '项目/竞赛类',
    color: '#059669',
    bgColor: 'linear-gradient(135deg, #34d399 0%, #059669 100%)',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h2"/><path d="M18 9h2a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2h-2"/><path d="M12 17v6"/><path d="M8 23h8"/><path d="M7 2h10v4a5 5 0 0 1-10 0V2z"/><line x1="17" y1="3" x2="12" y2="9"/><line x1="7" y1="3" x2="12" y2="9"/></svg>`,
    count: rules.value.filter(r =>
      r.convertedType && (r.convertedType.includes('项目') || r.convertedType.includes('大赛') || r.convertedType.includes('优秀'))
    ).length
  },
  {
    key: 'disabled',
    label: '已停用',
    color: '#c0392b',
    bgColor: 'linear-gradient(135deg, #f87171 0%, #c0392b 100%)',
    icon: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>`,
    count: rules.value.filter(r => r.isEnabled !== 1).length
  }
]))

const filteredRules = computed(() => {
  if (!statFilter.value) return rules.value
  const list = rules.value
  switch (statFilter.value) {
    case 'all': return list
    case 'course': return list.filter(r => r.convertedType === '课程')
    case 'project': return list.filter(r =>
      r.convertedType && (r.convertedType.includes('项目') || r.convertedType.includes('大赛') || r.convertedType.includes('优秀'))
    )
    case 'disabled': return list.filter(r => r.isEnabled !== 1)
    default: return list
  }
})

// 分页切片：表格按 filteredRules 分页，避免一次性渲染大量行造成双滚动条
const pagedRules = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRules.value.slice(start, start + pageSize.value)
})

function toggleStat(key) {
  statFilter.value = statFilter.value === key ? null : key
  currentPage.value = 1
}

function rowClassName({ row }) {
  return row.isEnabled !== 1 ? 'row-disabled' : ''
}

// ============ 类型配色 ============
function getOriginalTypeTag(type) {
  const map = {
    '在线学习成果': 'primary',
    '实践成果': 'warning',
    '竞赛成果': 'danger'
  }
  return map[type] || 'info'
}

function getConvertedTypeTag(type) {
  if (!type) return 'info'
  if (type === '课程') return ''
  if (type.includes('项目') || type.includes('开发')) return 'warning'
  if (type.includes('大赛') || type.includes('竞赛') || type.includes('优秀')) return 'danger'
  if (type.includes('认证') || type.includes('证书')) return 'success'
  return 'primary'
}

function truncateType(type) {
  if (!type) return ''
  return type.length > 8 ? type.slice(0, 8) + '…' : type
}

// ============ 数据加载 ============
onMounted(async () => {
  loading.value = true
  try {
    // 公开只读模式：仅加载规则目录，不请求机构/积分规则等管理侧数据
    if (props.readonly) {
      await loadData()
    } else if (currentUser.value) {
      await Promise.all([loadData(), loadOrganizations(), loadCreditRules()])
    } else {
      await loadData()
    }
  } finally {
    loading.value = false
  }
})

async function loadData() {
  try {
    rules.value = await getConversionRules()
  } catch (e) {
    ElMessage.error(e.message || '加载数据失败')
  }
}

async function loadOrganizations() {
  try {
    organizations.value = await getOrganizations()
  } catch (_) {
    organizations.value = []
  }
}

async function loadCreditRules() {
  try {
    creditRules.value = await getRules(true)
  } catch (_) {
    creditRules.value = []
  }
}

// ============ 操作权限 ============
function canOperate(row) {
  if (currentUser.value?.role === 'admin') return true
  if (currentUser.value?.role === 'org_admin' && row.convertedOrgId !== null) {
    return row.convertedOrgId === currentUser.value.orgId
  }
  return false
}

// ============ CRUD ============
function openCreate() {
  if (props.readonly) return
  const now = new Date()
  const oneYearLater = new Date(now.getTime() + 365 * 24 * 60 * 60 * 1000)
  form.value = {
    originalName: '',
    originalOrgId: null,
    originalType: '',
    convertedName: '',
    convertedOrgId: null,
    convertedType: '',
    creditRuleId: null,
    isEnabled: 1,
    effectiveStart: now,
    effectiveEnd: oneYearLater,
    description: ''
  }
  dialogVisible.value = true
}

function openEdit(row) {
  if (props.readonly) return
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  if (props.readonly) return
  if (!form.value.originalName?.trim()) return ElMessage.warning('请输入原成果名称')
  if (!form.value.convertedName?.trim()) return ElMessage.warning('请输入转换后成果名称')
  if (!form.value.originalType?.trim()) return ElMessage.warning('请输入原成果类型')
  if (!form.value.convertedType?.trim()) return ElMessage.warning('请输入转换后成果类型')
  try {
    if (form.value.id) await updateConversionRule(form.value)
    else await createConversionRule(form.value)
    ElMessage.success('规则已保存')
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function toggle(row) {
  if (props.readonly) return
  try {
    await toggleConversionRule(row.id, row.isEnabled === 1 ? 0 : 1)
    ElMessage.success(row.isEnabled === 1 ? '已停用' : '已启用')
    await loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(row) {
  if (props.readonly) return
  try {
    await ElMessageBox.confirm(
      `确定删除规则「${row.convertedName}」？此操作不可恢复。`,
      '删除规则',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteConversionRule(row.id)
    ElMessage.success('规则已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}
</script>

<style scoped>
/* ============ 整体容器 ============ */
.conversion-rules {
  min-height: calc(100vh - 120px);
}

/* ============ 统计卡片 ============ */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: currentColor;
  opacity: 0.3;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(30, 58, 95, 0.08);
  border-color: #ced4da;
}

.stat-card.active {
  border-color: #3b5bdb;
  box-shadow: 0 0 0 3px rgba(59, 91, 219, 0.1);
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.15;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.stat-label {
  color: #6c757d;
  font-size: 12.5px;
  margin-top: 3px;
  font-weight: 500;
}

/* ============ 卡片头部 ============ */
.rules-card {
  border-radius: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e3a5f;
}

.card-subtitle {
  font-size: 12px;
  color: #868e96;
  background: #f1f3f5;
  padding: 2px 10px;
  border-radius: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* ============ 成果区块 ============ */
.achievement-block {
  padding: 10px 12px;
  border-radius: 8px;
  border-left: 3px solid transparent;
  position: relative;
}

.origin-block {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-left-color: #3b82f6;
}

.converted-block {
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  border-left-color: #10b981;
}

.block-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: #64748b;
  margin-bottom: 6px;
}

.label-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.origin-dot { background: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.18); }
.converted-dot { background: #10b981; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18); }

.achievement-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #1e3a5f;
  line-height: 1.45;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.converted-name {
  color: #065f46;
}

.achievement-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.org-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  color: #475569;
  background: rgba(255, 255, 255, 0.7);
  padding: 2px 8px;
  border-radius: 4px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-tag .el-icon { font-size: 12px; }

.type-tag {
  font-weight: 500;
}

.converted-type {
  letter-spacing: 0.3px;
}

/* ============ 转换桥梁：签名元素 ============ */
.convert-bridge {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  position: relative;
}

.bridge-line {
  flex: 1;
  max-width: 20px;
  height: 2px;
  background: linear-gradient(90deg, #3b82f6, #10b981);
  position: relative;
}

.bridge-line::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #fff;
  transform: translateY(-50%);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.4);
}

.bridge-line:first-child::after { left: 100%; transform: translate(-50%, -50%); }
.bridge-line:last-child::after { right: 100%; transform: translate(50%, -50%); box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.4); }

.bridge-arrow {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #10b981 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35);
  transition: transform 0.25s ease;
  z-index: 1;
}

.bridge-arrow:hover {
  transform: scale(1.08);
}

.convert-bridge.disabled .bridge-line {
  background: #e5e7eb;
}
.convert-bridge.disabled .bridge-arrow {
  background: linear-gradient(135deg, #d1d5db 0%, #9ca3af 100%);
  box-shadow: none;
}
.convert-bridge.disabled .bridge-line::after {
  box-shadow: 0 0 0 2px rgba(156, 163, 175, 0.3);
}

/* ============ 表格样式 ============ */
:deep(.el-table) {
  --el-table-border-color: #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table .cell) {
  padding: 8px 10px;
}

:deep(.el-table th.el-table__cell) {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #1e3a5f;
  font-weight: 600;
}

:deep(.row-disabled > td:not(.col-action)) {
  opacity: 0.55;
  background: #fafafa !important;
}

.seq-no {
  font-family: 'SF Mono', monospace;
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

/* ============ 积分规则列 ============ */
.credit-rule-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.credit-rule-name {
  font-size: 12px;
  color: #334155;
  font-weight: 500;
  text-align: center;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.credit-value-tag {
  font-weight: 700;
  letter-spacing: 0.5px;
}

.muted-text {
  color: #cbd5e1;
  font-size: 18px;
}

/* ============ 状态 ============ */
.status-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #ecfdf5;
  color: #059669;
}
.status-active .status-dot { background: #10b981; }

.status-inactive {
  background: #fef2f2;
  color: #dc2626;
}
.status-inactive .status-dot { background: #ef4444; }

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

/* ============ 操作列（.op-col 使用全局标准类，避免缩进问题） ============ */

.scope-tag {
  font-size: 11px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 3px 8px;
  border-radius: 4px;
}

/* ============ Popover ============ */
.popover-content {
  padding: 4px 2px;
}

.popover-title {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 6px;
}

.popover-rule-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e3a5f;
  margin-bottom: 8px;
  line-height: 1.4;
}

.popover-credit {
  font-weight: 600;
}

/* ============ 弹窗表单 ============ */
.rule-dialog :deep(.el-dialog) {
  border-radius: 14px;
  overflow: hidden;
}

.rule-form {
  padding: 0 8px;
}

.form-section {
  border-radius: 10px;
  padding: 16px 18px;
  margin-bottom: 8px;
}

.origin-section {
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border: 1px solid #bfdbfe;
}

.converted-section {
  background: linear-gradient(135deg, #ecfdf5 0%, #f8fafc 100%);
  border: 1px solid #a7f3d0;
}

.link-section {
  background: #fafafa;
  border: 1px dashed #cbd5e1;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.section-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.section-link-icon { font-size: 14px; }

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e3a5f;
}

.form-arrow-divider {
  display: flex;
  align-items: center;
  margin: 4px 0 8px;
  padding: 0 20px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, #e2e8f0 0%, #cbd5e1 50%, #e2e8f0 100%);
}

.form-arrow-divider .el-icon {
  margin: 0 12px;
  color: #94a3b8;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-row :deep(.el-form-item) {
  margin-bottom: 12px;
}

/* ============ 分页：置于卡片底部，滚动由页面外置滚动条统一处理 ============ */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 8px 4px;
  border-top: 1px solid #f1f3f5;
  margin-top: 4px;
}

/* ============ 响应式 ============ */
@media (max-width: 1280px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
