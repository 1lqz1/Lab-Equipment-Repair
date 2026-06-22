<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    default: 0,
  },
  tone: {
    type: String,
    default: 'blue',
  },
})
</script>

<template>
  <section class="data-card" :data-tone="tone">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
  </section>
</template>
