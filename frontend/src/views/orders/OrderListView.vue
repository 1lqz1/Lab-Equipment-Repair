<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { listRepairOrders } from '@/api/repairOrder'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const router = useRouter()
const orders = ref([])

async function loadOrders() {
  const response = await listRepairOrders()
  orders.value = response.data || []
}

onMounted(loadOrders)
</script>

<template>
  <PageHeader title="工单列表" description="查询报修、派单、维修和验收状态">
    <RouterLink class="primary-button" to="/orders/new">提交报修</RouterLink>
  </PageHeader>

  <section class="panel">
    <table class="data-table">
      <thead>
        <tr>
          <th>工单编号</th>
          <th>设备ID</th>
          <th>实验室ID</th>
          <th>紧急程度</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="order in orders" :key="order.id">
          <td>{{ order.orderNo }}</td>
          <td>{{ order.equipmentId }}</td>
          <td>{{ order.labId }}</td>
          <td>{{ order.urgency }}</td>
          <td><StatusTag :status="order.status" /></td>
          <td>
            <button class="link-button" type="button" @click="router.push(`/orders/${order.id}`)">详情</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
