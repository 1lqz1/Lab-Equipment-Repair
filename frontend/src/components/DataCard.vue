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
})
</script>

<template>
  <section class="data-card">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
  </section>
</template>
