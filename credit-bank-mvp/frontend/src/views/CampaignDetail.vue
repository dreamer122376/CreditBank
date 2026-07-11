<template>
  <div class="campaign-detail" v-loading="loading">
    <template v-if="campaign">
      <!-- 封面 -->
      <div class="detail-cover" :style="campaign.coverImage ? { backgroundImage: 'url(' + campaign.coverImage + ')' } : {}">
        <span v-if="!campaign.coverImage">{{ campaign.title }}</span>
      </div>

      <!-- 图片轮播 -->
      <div class="detail-carousel" v-if="imageList.length > 0">
        <el-carousel :interval="4000" type="card" height="320px">
          <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
            <img :src="img" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 信息卡片 -->
      <el-card class="detail-card">
        <h2 class="detail-title">{{ campaign.title }}</h2>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🕐</span>
              <div><div class="meta-label">活动时间</div><div class="meta-val">{{ fmt(campaign.startTime) }} ~ {{ fmt(campaign.endTime) }}</div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🏛️</span>
              <div><div class="meta-label">主办方</div><div class="meta-val">{{ campaign.organizer || '未设置' }}</div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🎯</span>
              <div><div class="meta-label">积分倍率</div>
                <div class="meta-val" v-if="campaign.multiplier > 1" style="color:#f59f00;font-weight:700;">{{ campaign.multiplier }}x 积分翻倍</div>
                <div class="meta-val" v-else style="color:#868e96;">无积分奖励</div>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top:12px;">
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">🏷️</span>
              <div><div class="meta-label">状态</div><div class="meta-val"><el-tag :type="statusType(campaign.status)" size="small">{{ statusText(campaign.status) }}</el-tag></div></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="meta-item"><span class="meta-icon">📅</span>
              <div><div class="meta-label">创建时间</div><div class="meta-val">{{ fmt(campaign.createdAt) }}</div></div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 活动详情 -->
      <el-card class="detail-card">
        <template #header><h3 style="margin:0;">📝 活动详情</h3></template>
        <div class="detail-desc" v-if="campaign.description">
          <p v-for="(line, idx) in descLines" :key="idx">{{ line }}</p>
        </div>
        <div v-else style="color:#868e96;">暂无活动详情</div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="detail-actions">
        <el-button @click="goBack">← 返回列表</el-button>
        <template v-if="isAdmin">
          <el-button type="primary" @click="goEdit">编辑</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </template>
      </div>
    </template>
    <div v-else-if="!loading" style="text-align:center;padding:60px;color:#868e96;">❌ 活动不存在</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCampaignDetail, deleteCampaign } from '@/api/campaign'

const router = useRouter()
const route = useRoute()
const { currentUser } = useAuth()
const loading = ref(true)
const campaign = ref(null)

const isAdmin = computed(() => currentUser.value?.role === 'admin')

function parseImages(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  try { const arr = JSON.parse(val); return Array.isArray(arr) ? arr : [] } catch (e) { return [] }
}
const imageList = computed(() => parseImages(campaign.value?.images))

const descLines = computed(() => {
  if (!campaign.value?.description) return []
  return campaign.value.description.split('\n').filter(l => l.trim())
})

async function loadDetail() {
  loading.value = true
  try {
    campaign.value = await getCampaignDetail(route.params.id)
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push(isAdmin.value ? '/campaigns' : '/campaigns/student')
}
function goEdit() {
  router.push('/campaigns?edit=' + campaign.value.id)
}
async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？', '确认删除', { type: 'warning' })
    await deleteCampaign(campaign.value.id)
    ElMessage.success('已删除')
    router.push('/campaigns')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

function fmt(t) { if (!t) return ''; return t.length >= 16 ? t.substring(0, 16).replace('T', ' ') : t }
function statusType(s) { return s === 1 ? 'success' : s === 2 ? 'info' : '' }
function statusText(s) { return s === 1 ? '进行中' : s === 2 ? '已结束' : '未开始' }

onMounted(() => { loadDetail() })
</script>

<style scoped>
.detail-cover { height: 240px; background-size: cover; background-position: center; border-radius: 10px;
  background: linear-gradient(135deg, #3b5bdb 0%, #6c8ae4 100%);
  display: flex; align-items: center; justify-content: center; color: #fff;
  font-size: 22px; font-weight: 600; margin-bottom: 18px; }
.detail-card { margin-bottom: 18px; }
.detail-title { font-size: 20px; font-weight: 700; margin-bottom: 14px; color: #2c3e50; }
.meta-item { display: flex; align-items: flex-start; gap: 8px; }
.meta-icon { font-size: 18px; flex-shrink: 0; }
.meta-label { font-size: 12px; color: #868e96; }
.meta-val { font-size: 14px; color: #2c3e50; }
.detail-desc { line-height: 1.8; color: #495057; }
.detail-desc p { margin-bottom: 8px; }
.detail-actions { display: flex; gap: 10px; padding: 8px 0; }
</style>
