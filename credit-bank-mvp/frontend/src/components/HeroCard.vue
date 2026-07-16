<template>
  <div class="hero-card" :class="[`accent-${data.color || 'blue'}`]" @click="$emit('click', data.path)">
    <div class="hero-main">
      <div class="hero-icon">
        <el-icon size="32"><component :is="data.icon" /></el-icon>
      </div>
      <div class="hero-content">
        <div class="hero-label">{{ data.label }}</div>
        <div class="hero-value">
          <CountTo :value="data.value || 0" :duration="1500" />
        </div>
        <div class="hero-desc">{{ data.desc }}</div>
      </div>
    </div>
    <div class="hero-action">
      <span>{{ data.action || '查看' }}</span>
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup>
import CountTo from './CountTo.vue'
import { ArrowRight } from '@element-plus/icons-vue'

defineProps({
  role: String,
  data: { type: Object, required: true }
})

defineEmits(['click'])
</script>

<style scoped>
.hero-card {
  background: rgba(30, 41, 59, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 20px;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.35s ease;
  position: relative;
  overflow: hidden;
}

.hero-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6px;
  background: var(--accent-color, #3b82f6);
}

.hero-card::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent-color, #3b82f6) 0%, transparent 70%);
  opacity: 0.08;
  transition: opacity 0.35s ease;
}

.hero-card:hover {
  transform: translateY(-3px);
  background: rgba(30, 41, 59, 0.8);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.28);
  border-color: rgba(148, 163, 184, 0.2);
}

.hero-card:hover::after {
  opacity: 0.15;
}

.accent-blue { --accent-color: #3b82f6; }
.accent-green { --accent-color: #10b981; }
.accent-orange { --accent-color: #f59e0b; }
.accent-purple { --accent-color: #8b5cf6; }
.accent-red { --accent-color: #ef4444; }
.accent-cyan { --accent-color: #06b6d4; }

.hero-main {
  display: flex;
  align-items: center;
  gap: 24px;
  z-index: 1;
}

.hero-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.12);
  color: #60a5fa;
  flex-shrink: 0;
}

.accent-blue .hero-icon { background: rgba(59, 130, 246, 0.12); color: #60a5fa; }
.accent-green .hero-icon { background: rgba(16, 185, 129, 0.12); color: #34d399; }
.accent-orange .hero-icon { background: rgba(245, 158, 11, 0.12); color: #fbbf24; }
.accent-purple .hero-icon { background: rgba(139, 92, 246, 0.12); color: #a78bfa; }
.accent-red .hero-icon { background: rgba(239, 68, 68, 0.12); color: #f87171; }
.accent-cyan .hero-icon { background: rgba(6, 182, 212, 0.12); color: #22d3ee; }

.hero-label {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.hero-value {
  font-size: 48px;
  font-weight: 800;
  color: #f8fafc;
  line-height: 1.1;
  letter-spacing: -2px;
  margin-bottom: 8px;
}

.hero-desc {
  font-size: 13px;
  color: #64748b;
}

.hero-action {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 10px;
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
  font-size: 13px;
  font-weight: 600;
  z-index: 1;
  transition: all 0.2s ease;
}

.accent-blue .hero-action { background: rgba(59, 130, 246, 0.1); color: #60a5fa; }
.accent-green .hero-action { background: rgba(16, 185, 129, 0.1); color: #34d399; }
.accent-orange .hero-action { background: rgba(245, 158, 11, 0.1); color: #fbbf24; }
.accent-purple .hero-action { background: rgba(139, 92, 246, 0.1); color: #a78bfa; }
.accent-red .hero-action { background: rgba(239, 68, 68, 0.1); color: #f87171; }
.accent-cyan .hero-action { background: rgba(6, 182, 212, 0.1); color: #22d3ee; }

.hero-card:hover .hero-action {
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 22px;
  }
  .hero-value {
    font-size: 36px;
  }
}
</style>
