<template>
  <div class="project-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <el-button type="primary" size="small" @click="openCreate">+ 新增项目</el-button>
        </div>
      </template>
      <el-table :data="projects" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="项目ID" width="90" />
        <el-table-column prop="name" label="项目名称" min-width="160" />
        <el-table-column prop="description" label="项目描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="STATUS_TAG[scope.row.status] || 'info'">
              {{ STATUS_NAME[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="scope">{{ fmt(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">查看详情</el-button>
            <el-button size="small" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && projects.length === 0" style="text-align: center; padding: 40px;">
        暂无项目数据
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑项目' : '新增项目'" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.name" maxlength="100" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width:100%;">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责专家">
          <el-select v-model="form.expertId" placeholder="请选择专家（可选）" clearable style="width:100%;" filterable>
            <el-option v-for="ex in experts" :key="ex.id" :label="ex.realName + (ex.expertField ? '（' + ex.expertField + '）' : '')" :value="ex.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="项目详情" width="640px">
      <div v-loading="detailLoading">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="项目ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="项目描述">{{ detail.description || '—' }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">
            <el-tag :type="STATUS_TAG[detail.status] || 'info'">{{ STATUS_NAME[detail.status] || '未知' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ fmt(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ fmt(detail.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <div class="students-section">
          <div class="section-title">报名学生（{{ students.length }} 人）</div>
          <el-table :data="students" border size="small" style="width:100%;">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="realName" label="学生姓名" width="120" />
            <el-table-column prop="username" label="账号" width="140" />
            <el-table-column label="报名状态">
              <template #default="scope">
                <el-tag :type="studentStatusType(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="报名时间">
              <template #default="scope">{{ fmt(scope.row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="students.length === 0" style="text-align:center;padding:24px;color:#868e96;">暂无学生报名</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrgProjects, createProject, updateProject, deleteProject, getOrgProjectDetail } from '@/api/project'
import { getExperts } from '@/api/expert'

const STATUS_NAME = { 0: '未开始', 1: '进行中', 2: '已结束' }
const STATUS_TAG = { 0: 'warning', 1: 'success', 2: 'info' }

const loading = ref(false)
const submitting = ref(false)
const projects = ref([])
const experts = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({})
const students = ref([])

const form = reactive({
  id: null, name: '', description: '', status: 0, expertId: null
})

function resetForm() {
  form.id = null; form.name = ''; form.description = ''; form.status = 0; form.expertId = null
}

async function loadData() {
  loading.value = true
  try {
    projects.value = await getOrgProjects()
  } catch (e) {
    ElMessage.error('加载失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function loadExperts() {
  try {
    const res = await getExperts()
    experts.value = res.records || res || []
  } catch (e) { /* ignore */ }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  form.id = row.id
  form.name = row.name || ''
  form.description = row.description || ''
  form.status = row.status ?? 0
  form.expertId = row.expertId ?? null
  dialogVisible.value = true
}

async function save() {
  if (!form.name) { ElMessage.warning('请输入项目名称'); return }
  submitting.value = true
  try {
    const data = { ...form }
    if (form.id) {
      await updateProject(data)
    } else {
      await createProject(data)
    }
    ElMessage.success(form.id ? '修改成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除项目「' + row.name + '」吗？此操作不可恢复。', '确认删除', { type: 'warning' })
    await deleteProject(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = {}
  students.value = []
  try {
    const res = await getOrgProjectDetail(row.id)
    detail.value = res || {}
    const spList = res.studentProjects || []
    const stuList = res.students || []
    const stuMap = {}
    stuList.forEach(s => { stuMap[s.id] = s })
    students.value = spList.map(sp => {
      const s = stuMap[sp.studentId] || {}
      return {
        realName: s.realName || '—',
        username: s.username || '—',
        status: sp.status,
        createdAt: sp.createdAt
      }
    })
  } catch (e) {
    ElMessage.error('加载详情失败：' + (e.message || ''))
  } finally {
    detailLoading.value = false
  }
}

function studentStatusType(s) {
  if (s === '进行中') return 'success'
  if (s === '已完成') return 'info'
  return 'warning'
}

function fmt(t) {
  if (!t) return '—'
  const str = String(t)
  return str.length >= 16 ? str.substring(0, 16).replace('T', ' ') : str
}

onMounted(() => { loadData(); loadExperts() })
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.students-section {
  margin-top: 20px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
}
</style>
