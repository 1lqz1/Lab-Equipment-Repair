<script setup>
import { onMounted, ref } from 'vue'

import { listRepairOrders } from '@/api/repairOrder'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const tasks = ref([])

async function loadTasks() {
  const response = await listRepairOrders({ assignedTo: authStore.user?.id })
  tasks.value = response.data || []
}

onMounted(loadTasks)
</script>

<template>
  <PageHeader title="维修任务" description="查看分配给当前维修人员的工单" />

  <section class="panel">
    <table class="data-table">
      <thead>
        <tr>
          <th>工单编号</th>
          <th>设备ID</th>
          <th>状态</th>
          <th>故障地点</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="task in tasks" :key="task.id">
          <td>{{ task.orderNo }}</td>
          <td>{{ task.equipmentId }}</td>
          <td><StatusTag :status="task.status" /></td>
          <td>{{ task.location || '-' }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
