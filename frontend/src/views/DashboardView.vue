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

  <section class="dashboard-hero">
    <div>
      <span>今日工作概览</span>
      <h2>集中掌握设备维修进度与待处理事项</h2>
      <p>优先处理待审核与维修中工单，减少设备停用时间，保障实验室教学与科研使用。</p>
      <div class="hero-actions">
        <RouterLink class="primary-button" to="/orders">查看工单</RouterLink>
        <RouterLink class="ghost-button ghost-on-dark" to="/equipment">设备档案</RouterLink>
      </div>
    </div>
    <div class="hero-metric">
      <span>设备总数</span>
      <strong>{{ stats.equipmentCount }}</strong>
    </div>
  </section>

  <section class="card-grid">
    <DataCard label="待审核工单" :value="stats.submittedCount" tone="blue" />
    <DataCard label="维修中工单" :value="stats.inProgressCount" tone="amber" />
    <DataCard label="待验收工单" :value="stats.repairedCount" tone="teal" />
    <DataCard label="已归档工单" :value="stats.closedCount" tone="green" />
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
