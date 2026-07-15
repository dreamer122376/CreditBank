<template>
  <div class="exchange-rules" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分转换规则</span>
          <el-button type="primary" size="small" @click="openCreate">新增规则</el-button>
        </div>
      </template>
      <el-table :data="rules" border style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="80" align="center">
          <template #default="scope">
            <el-image v-if="isImageUrl(scope.row.itemIcon)"
                      :src="scope.row.itemIcon"
                      :preview-src-list="[scope.row.itemIcon]"
                      preview-teleported
                      fit="cover"
                      class="table-thumb" />
            <div v-else class="table-thumb thumb-empty">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="兑换品名称" />
        <el-table-column prop="requiredCredit" label="所需积分" width="110">
          <template #default="scope">
            <span class="points">{{ scope.row.requiredCredit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="perUserLimit" label="每人限兑" width="100" />
        <el-table-column prop="orgId" label="归属" width="140">
          <template #default="scope">
            <span v-if="scope.row.orgId" style="color: #409eff;">机构 {{ scope.row.orgId }}</span>
            <span v-else style="color: #909399;">全平台通用</span>
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
              {{ scope.row.isEnabled === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <template v-if="currentUser?.role !== 'org_admin' || scope.row.orgId != null">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" :type="scope.row.isEnabled === 1 ? 'danger' : 'success'"
                         @click="toggle(scope.row)">
                {{ scope.row.isEnabled === 1 ? '停用' : '启用' }}
              </el-button>
            </template>
            <span v-else style="color: #909399;">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="rules.length === 0" style="text-align: center; padding: 40px;">
        暂无转换规则
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑规则' : '新增规则'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="兑换品名称" required>
          <el-input v-model="form.itemName" placeholder="如：50元京东卡" />
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="icon-upload-box">
            <el-upload
              class="icon-uploader"
              action="/api/files/upload"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeIconUpload"
              :on-success="onIconSuccess"
              :on-error="onIconError"
            >
              <img v-if="isImageUrl(form.itemIcon)" :src="form.itemIcon" class="icon-preview" alt="商品图片" />
              <div v-else class="icon-uploader-empty">
                <el-icon><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </el-upload>
            <div class="icon-upload-side">
              <div class="upload-tip">JPG / PNG / GIF / WebP，不超过 5MB<br />学生积分商城的商品卡片将展示此图</div>
              <el-button v-if="isImageUrl(form.itemIcon)" size="small" type="danger" link @click="form.itemIcon = ''">
                移除图片
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="所需积分" required>
          <el-input-number v-model="form.requiredCredit" :min="1" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="每人限兑">
          <el-input-number v-model="form.perUserLimit" :min="1" />
        </el-form-item>
        <el-form-item v-if="currentUser?.role !== 'org_admin'" label="归属机构">
          <el-input-number v-model="form.orgId" :min="1" :precision="0" placeholder="留空为全平台通用" />
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
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Plus } from '@element-plus/icons-vue'
import {
  getExchangeRules,
  createExchangeRule,
  updateExchangeRule,
  toggleExchangeRule
} from '@/api/exchangeRule'
import { useAuth } from '@/composables/useAuth'

const { currentUser } = useAuth()

const rules = ref([])
const dialogVisible = ref(false)
const form = ref({})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('cb_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

/** item_icon 支持本地上传路径(/api/files/...)和外链(http/https)，历史 icon_xxx 文案值按无图处理 */
function isImageUrl(icon) {
  return typeof icon === 'string' && (icon.startsWith('/') || icon.startsWith('http'))
}

function beforeIconUpload(file) {
  const okType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!okType) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 格式')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 5MB')
    return false
  }
  return true
}

function onIconSuccess(response) {
  if (response.code === 200) {
    form.value.itemIcon = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

function onIconError(err) {
  ElMessage.error(err?.message || '图片上传失败，请重试')
}

onMounted(loadData)

async function loadData() {
  try {
    const allRules = await getExchangeRules()
    if (currentUser.value?.role === 'org_admin') {
      const orgId = currentUser.value.orgId
      rules.value = allRules.filter(r => r.orgId == null || r.orgId === orgId)
    } else {
      rules.value = allRules
    }
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

function openCreate() {
  form.value = { itemName: '', itemIcon: '', requiredCredit: 100, stock: 9999, perUserLimit: 1, orgId: null }
  if (currentUser.value?.role === 'org_admin') {
    form.value.orgId = currentUser.value.orgId
  }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (currentUser.value?.role === 'org_admin') {
      form.value.orgId = currentUser.value.orgId
    }
    if (form.value.id) {
      await updateExchangeRule(form.value)
    } else {
      await createExchangeRule(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function toggle(row) {
  try {
    await toggleExchangeRule(row.id)
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

.points {
  font-weight: 600;
  color: #0b7a4f;
}

.table-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  display: block;
  margin: 0 auto;
}

.thumb-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f3f5;
  color: #adb5bd;
  font-size: 18px;
}

.icon-upload-box {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.icon-uploader :deep(.el-upload) {
  border: 1px dashed #ced4da;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
}

.icon-uploader :deep(.el-upload:hover) {
  border-color: #3b5bdb;
}

.icon-preview {
  width: 96px;
  height: 96px;
  object-fit: cover;
  display: block;
}

.icon-uploader-empty {
  width: 96px;
  height: 96px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #868e96;
  font-size: 12px;
}

.icon-upload-side {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.upload-tip {
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
}
</style>
