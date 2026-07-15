<template>
  <div class="point-mall">
    <div class="mall-header">
      <div>
        <div class="mall-title">积分商城</div>
        <div class="mall-subtitle">用学习积分兑换商品与权益</div>
      </div>
      <div class="balance-box">
        <div class="balance-label">我的积分</div>
        <div class="balance-value">{{ balance }}</div>
      </div>
    </div>

    <div v-if="visibleRules.length" class="mall-grid">
      <el-card v-for="rule in visibleRules" :key="rule.id" class="item-card" shadow="hover">
        <el-image v-if="isImageUrl(rule.itemIcon)" :src="rule.itemIcon" fit="cover" class="item-img"
                  :preview-src-list="[rule.itemIcon]" preview-teleported>
          <template #error>
            <div class="img-placeholder"><el-icon><Picture /></el-icon></div>
          </template>
        </el-image>
        <div v-else class="item-img img-placeholder">
          <el-icon><Picture /></el-icon>
        </div>
        <div class="item-name">
          {{ rule.itemName }}
          <el-tag v-if="rule.orgId != null" size="small" type="warning" effect="light">本机构专属</el-tag>
        </div>
        <div class="item-price">{{ rule.requiredCredit }} <span class="price-unit">积分</span></div>
        <div class="item-meta">
          <span :class="{ 'stock-low': lowStock(rule) }">{{ stockText(rule) }}</span>
          <span v-if="rule.perUserLimit > 0">每人限兑 {{ rule.perUserLimit }} 次</span>
        </div>
        <el-button
          class="exchange-btn"
          type="primary"
          :disabled="!!disabledReason(rule)"
          @click="confirmExchange(rule)"
        >
          {{ disabledReason(rule) || '立即兑换' }}
        </el-button>
      </el-card>
    </div>
    <el-empty v-else description="暂无可兑换的商品" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { getExchangeRules, exchangeItem } from '@/api/exchangeRule'
import { getProfile } from '@/api/profile'

const { currentUser } = useAuth()

const rules = ref([])
const balance = ref(currentUser.value?.balance ?? 0)

/** item_icon 支持本地上传路径(/api/files/...)和外链(http/https)，历史 icon_xxx 文案值按无图处理 */
function isImageUrl(icon) {
  return typeof icon === 'string' && (icon.startsWith('/') || icon.startsWith('http'))
}

const visibleRules = computed(() =>
  rules.value.filter(rule =>
    rule.isEnabled === 1 && (rule.orgId == null || rule.orgId === currentUser.value?.orgId)))

onMounted(async () => {
  await Promise.all([loadRules(), refreshBalance()])
})

async function loadRules() {
  try {
    rules.value = await getExchangeRules()
  } catch (error) {
    ElMessage.error(error.message || '加载商品失败')
  }
}

async function refreshBalance() {
  const id = currentUser.value?.id
  if (!id) return
  try {
    const profile = await getProfile(id)
    if (profile?.balance != null) {
      balance.value = profile.balance
      // 同步到全局用户状态，其他页面（钱包等）显示一致
      currentUser.value = { ...currentUser.value, balance: profile.balance }
      localStorage.setItem('cb_user', JSON.stringify(currentUser.value))
    }
  } catch (_) {
    // 拉取失败时保留本地缓存的余额
  }
}

function stockText(rule) {
  if (rule.stock == null) return '库存充足'
  if (rule.stock <= 0) return '已兑完'
  return rule.stock <= 10 ? `仅剩 ${rule.stock} 件` : `库存 ${rule.stock}`
}

function lowStock(rule) {
  return rule.stock != null && rule.stock <= 10
}

function disabledReason(rule) {
  if (rule.stock != null && rule.stock <= 0) return '已兑完'
  if (balance.value < rule.requiredCredit) return `还差 ${rule.requiredCredit - balance.value} 积分`
  return ''
}

async function confirmExchange(rule) {
  try {
    await ElMessageBox.confirm(
      `确认消耗 ${rule.requiredCredit} 积分兑换「${rule.itemName}」？`,
      '兑换确认',
      { confirmButtonText: '确认兑换', cancelButtonText: '再想想', type: 'warning' }
    )
  } catch (_) {
    return
  }
  try {
    await exchangeItem(rule.id)
    ElMessage.success(`兑换成功！「${rule.itemName}」已到账，请留意发放通知`)
    await Promise.all([loadRules(), refreshBalance()])
  } catch (error) {
    ElMessage.error(error.message || '兑换失败')
    // 失败也刷新一次，库存/余额可能已被别人改变
    await Promise.all([loadRules(), refreshBalance()])
  }
}
</script>

<style scoped>
.mall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 18px 24px;
  margin-bottom: 16px;
}

.mall-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.mall-subtitle {
  color: #868e96;
  font-size: 13px;
  margin-top: 4px;
}

.balance-box {
  text-align: right;
}

.balance-label {
  color: #868e96;
  font-size: 12px;
}

.balance-value {
  font-size: 28px;
  font-weight: 700;
  color: #9a6700;
  line-height: 1.2;
}

.mall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.item-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
}

.item-img {
  width: 100%;
  height: 130px;
  border-radius: 6px;
  overflow: hidden;
}

.img-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 130px;
  background: #f1f3f5;
  color: #ced4da;
  font-size: 34px;
  border-radius: 6px;
}

.item-name {
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.item-price {
  font-size: 22px;
  font-weight: 700;
  color: #9a6700;
}

.price-unit {
  font-size: 12px;
  font-weight: 400;
  color: #868e96;
}

.item-meta {
  display: flex;
  gap: 12px;
  color: #868e96;
  font-size: 12px;
}

.stock-low {
  color: #e8590c;
}

.exchange-btn {
  width: 100%;
  margin-top: 6px;
}
</style>
