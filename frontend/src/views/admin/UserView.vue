<script setup>
import { onMounted, ref } from 'vue'

import { listUsers } from '@/api/user'
import PageHeader from '@/components/PageHeader.vue'

const users = ref([])

async function loadUsers() {
  const response = await listUsers()
  users.value = response.data || []
}

onMounted(loadUsers)
</script>

<template>
  <PageHeader title="用户管理" description="查看系统用户及角色" />

  <section class="panel">
    <table class="data-table">
      <thead>
        <tr>
          <th>账号</th>
          <th>姓名</th>
          <th>联系电话</th>
          <th>角色</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.username }}</td>
          <td>{{ user.realName }}</td>
          <td>{{ user.phone || '-' }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.status }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
