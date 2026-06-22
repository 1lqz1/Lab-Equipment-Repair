<script setup>
import { onMounted, ref } from 'vue'

import { listEquipment } from '@/api/equipment'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const equipment = ref([])

async function loadEquipment() {
  const response = await listEquipment()
  equipment.value = response.data || []
}

onMounted(loadEquipment)
</script>

<template>
  <PageHeader title="设备管理" description="维护实验室设备档案和维修状态" />

  <section class="panel">
    <table class="data-table">
      <thead>
        <tr>
          <th>设备编号</th>
          <th>设备名称</th>
          <th>类别</th>
          <th>实验室ID</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in equipment" :key="item.id">
          <td>{{ item.code }}</td>
          <td>{{ item.name }}</td>
          <td>{{ item.category || '-' }}</td>
          <td>{{ item.labId }}</td>
          <td><StatusTag :status="item.status" /></td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
