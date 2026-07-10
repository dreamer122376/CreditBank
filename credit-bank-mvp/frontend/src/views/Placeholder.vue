<template>
  <div class="placeholder">
    <el-card>
      <div class="func-placeholder">
        <div class="icon">🚧</div>
        <h2>{{ route.meta.title }}（功能开发中）</h2>
        <p>该功能页面尚在开发中，后续将在此实现具体业务逻辑。</p>
        <el-button type="primary" @click="goBack">← 返回工作台</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

function goBack() {
  router.push('/dashboard')
}
</script>

<style scoped>
.placeholder {
  padding: 0;
}

.func-placeholder {
  text-align: center;
  padding: 60px 20px;
}

.icon {
  font-size: 64px;
  color: #dee2e6;
  margin-bottom: 16px;
}

.func-placeholder h2 {
  font-size: 20px;
  color: #495057;
  margin-bottom: 10px;
}

.func-placeholder p {
  color: #868e96;
  margin-bottom: 24px;
}
</style>
