<template>
  <span class="count-to">{{ formattedValue }}</span>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'

const props = defineProps({
  value: { type: Number, default: 0 },
  duration: { type: Number, default: 1200 },
  decimals: { type: Number, default: 0 },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
  separator: { type: String, default: ',' },
  startOnMount: { type: Boolean, default: true }
})

const displayValue = ref(0)
let rafId = null
let startTime = null
let fromValue = 0
let toValue = 0

const formattedValue = computed(() => {
  const fixed = displayValue.value.toFixed(props.decimals)
  const parts = fixed.split('.')
  const integer = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, props.separator)
  return props.prefix + integer + (parts[1] ? '.' + parts[1] : '') + props.suffix
})

function easeOutQuart(t) {
  return 1 - Math.pow(1 - t, 4)
}

function animate(timestamp) {
  if (!startTime) startTime = timestamp
  const progress = Math.min((timestamp - startTime) / props.duration, 1)
  displayValue.value = fromValue + (toValue - fromValue) * easeOutQuart(progress)
  if (progress < 1) {
    rafId = requestAnimationFrame(animate)
  } else {
    displayValue.value = toValue
  }
}

function start() {
  if (rafId) cancelAnimationFrame(rafId)
  fromValue = displayValue.value
  toValue = props.value
  startTime = null
  rafId = requestAnimationFrame(animate)
}

watch(() => props.value, (newVal, oldVal) => {
  if (newVal !== oldVal) start()
})

onMounted(() => {
  if (props.startOnMount) {
    displayValue.value = 0
    start()
  }
})
</script>

<style scoped>
.count-to {
  font-variant-numeric: tabular-nums;
}
</style>
