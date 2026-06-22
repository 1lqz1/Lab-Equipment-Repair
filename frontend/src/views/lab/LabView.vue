<script setup>
import { onMounted, ref } from 'vue'

import { listLabs } from '@/api/lab'
import PageHeader from '@/components/PageHeader.vue'

const labs = ref([])

async function loadLabs() {
  const response = await listLabs()
  labs.value = response.data || []
}

onMounted(loadLabs)
</script>

<template>
  <PageHeader title="实验室管理" description="维护实验室名称、位置和负责人" />

  <section class="panel">
    <table class="data-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>位置</th>
          <th>管理员ID</th>
          <th>说明</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="lab in labs" :key="lab.id">
          <td>{{ lab.name }}</td>
          <td>{{ lab.location || '-' }}</td>
          <td>{{ lab.managerId || '-' }}</td>
          <td>{{ lab.description || '-' }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
