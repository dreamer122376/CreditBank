<template>
  <div class="stat-card" :class="[`accent-${color}`]">
    <div class="stat-icon">
      <el-icon size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-content">
      <div class="stat-value">
        <CountTo :value="value" :duration="1200" />
      </div>
      <div class="stat-label">{{ label }}</div>
    </div>
    <div v-if="trend !== undefined" class="stat-trend" :class="trend >= 0 ? 'up' : 'down'">
      <el-icon size="12"><ArrowUp v-if="trend >= 0" /><ArrowDown v-else /></el-icon>
      <span>{{ Math.abs(trend) }}%</span>
    </div>
  </div>
</template>

<script setup>
import CountTo from './CountTo.vue'

defineProps({
  icon: { type: [String, Object], default: 'DataLine' },
  label: { type: String, required: true },
  value: { type: Number, default: 0 },
  color: { type: String, default: 'blue' },
  trend: { type: Number, default: undefined }
})
</script>

<style scoped>
.stat-card {
  position: relative;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--accent-color, #3b82f6);
  opacity: 0.8;
}

.stat-card:hover {
  transform: translateY(-4px);
  background: rgba(30, 41, 59, 0.75);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
  border-color: rgba(148, 163, 184, 0.18);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.12);
  color: #60a5fa;
  flex-shrink: 0;
}

.accent-blue { --accent-color: #3b82f6; }
.accent-blue .stat-icon { background: rgba(59, 130, 246, 0.12); color: #60a5fa; }

.accent-green { --accent-color: #10b981; }
.accent-green .stat-icon { background: rgba(16, 185, 129, 0.12); color: #34d399; }

.accent-orange { --accent-color: #f59e0b; }
.accent-orange .stat-icon { background: rgba(245, 158, 11, 0.12); color: #fbbf24; }

.accent-purple { --accent-color: #8b5cf6; }
.accent-purple .stat-icon { background: rgba(139, 92, 246, 0.12); color: #a78bfa; }

.accent-red { --accent-color: #ef4444; }
.accent-red .stat-icon { background: rgba(239, 68, 68, 0.12); color: #f87171; }

.accent-cyan { --accent-color: #06b6d4; }
.accent-cyan .stat-icon { background: rgba(6, 182, 212, 0.12); color: #22d3ee; }

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #f1f5f9;
  line-height: 1.1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.stat-trend.up {
  background: rgba(16, 185, 129, 0.12);
  color: #34d399;
}

.stat-trend.down {
  background: rgba(239, 68, 68, 0.12);
  color: #f87171;
}
</style>
