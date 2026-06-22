<script setup>
const props = defineProps({
  status: {
    type: String,
    default: '',
  },
})

const statusText = {
  SUBMITTED: '待审核',
  REJECTED: '已退回',
  ASSIGNED: '已派单',
  IN_PROGRESS: '维修中',
  REPAIRED: '待验收',
  ACCEPTED: '已验收',
  CLOSED: '已归档',
  CANCELLED: '已取消',
  NORMAL: '正常',
  REPAIRING: '维修中',
  SCRAPPED: '已报废',
  DISABLED: '停用',
}
</script>

<template>
  <span class="status-tag" :data-status="props.status">
    {{ statusText[props.status] || props.status || '-' }}
  </span>
</template>
