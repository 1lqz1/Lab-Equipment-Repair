<script setup>
import { onMounted, ref } from 'vue'

import { dashboardStats } from '@/api/statistics'
import DataCard from '@/components/DataCard.vue'
import PageHeader from '@/components/PageHeader.vue'

const stats = ref({
  submittedCount: 0,
  inProgressCount: 0,
  repairedCount: 0,
  closedCount: 0,
  equipmentCount: 0,
})

async function loadStats() {
  const response = await dashboardStats()
  stats.value = response.data || stats.value
}

onMounted(loadStats)
</script>

<template>
  <PageHeader title="工作台" description="查看当前报修流程和常用入口" />

  <section class="card-grid">
    <DataCard label="待审核工单" :value="stats.submittedCount" />
    <DataCard label="维修中工单" :value="stats.inProgressCount" />
    <DataCard label="待验收工单" :value="stats.repairedCount" />
    <DataCard label="已归档工单" :value="stats.closedCount" />
  </section>

  <section class="panel">
    <h2>流程概览</h2>
    <div class="workflow-line">
      <span>提交报修</span>
      <span>管理员审核</span>
      <span>维修人员处理</span>
      <span>验收归档</span>
    </div>
  </section>
</template>
