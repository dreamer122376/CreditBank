<template>
  <div class="experts">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>专家列表</span>
          <el-button type="primary" size="small" @click="openCreate">新增专家</el-button>
        </div>
      </template>
      <el-table :data="experts" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="登录账号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="expertField" label="擅长领域" />
        <el-table-column label="评审资质">
          <template #default="scope">
            <template v-if="(certMap[scope.row.id] || []).length">
              <el-tag v-for="cert in certMap[scope.row.id]" :key="cert.id"
                      class="cert-badge" effect="plain" size="small">
                <el-icon><Medal /></el-icon>
                {{ cert.fieldName }}
              </el-tag>
            </template>
            <span v-else class="no-cert">未认证</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="balance" label="积分余额" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 1" size="small" type="danger"
                       @click="changeStatus(scope.row, 0)">冻结</el-button>
            <el-button v-else size="small" type="success"
                       @click="changeStatus(scope.row, 1)">解冻</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="experts.length === 0" style="text-align: center; padding: 40px;">
        暂无专家
      </div>
    </el-card>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal } from '@element-plus/icons-vue'
import { getExperts, createExpert, updateExpert, changeExpertStatus } from '@/api/expert'
import { getAllCerts } from '@/api/expertCert'

const experts = ref([])
const certMap = ref({})
const dialogVisible = ref(false)
const form = ref({})

onMounted(loadData)

async function loadData() {
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
  }
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
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cert-badge {
  color: #9a6700;
  border-color: #d4a72c;
  background: #fff8e1;
  font-weight: 600;
  margin: 2px 4px 2px 0;
}

.no-cert {
  color: #adb5bd;
  font-size: 12px;
}
</style>
