<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon size="36"><Document /></el-icon>
    </div>
    <div class="empty-text">{{ text }}</div>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'

defineProps({
  text: { type: String, default: '暂无数据' }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  color: #64748b;
}

.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: #475569;
}

.empty-text {
  font-size: 13px;
}
</style>
