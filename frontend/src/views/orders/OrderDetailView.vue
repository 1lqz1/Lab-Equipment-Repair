<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import { getRepairOrder, listOrderLogs } from '@/api/repairOrder'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'

const route = useRoute()
const order = ref(null)
const logs = ref([])

async function loadDetail() {
  const id = route.params.id
  const [orderResponse, logResponse] = await Promise.all([getRepairOrder(id), listOrderLogs(id)])
  order.value = orderResponse.data
  logs.value = logResponse.data || []
}

onMounted(loadDetail)
</script>

<template>
  <PageHeader title="工单详情" description="查看工单信息和状态流转记录" />

  <section v-if="order" class="panel detail-grid">
    <div>
      <span>工单编号</span>
      <strong>{{ order.orderNo }}</strong>
    </div>
    <div>
      <span>状态</span>
      <StatusTag :status="order.status" />
    </div>
    <div>
      <span>设备ID</span>
      <strong>{{ order.equipmentId }}</strong>
    </div>
    <div>
      <span>紧急程度</span>
      <strong>{{ order.urgency }}</strong>
    </div>
    <div class="detail-wide">
      <span>故障描述</span>
      <p>{{ order.faultDescription }}</p>
    </div>
  </section>

  <section class="panel">
    <h2>流转日志</h2>
    <ol class="timeline">
      <li v-for="log in logs" :key="log.id">
        <strong>{{ log.operation }}</strong>
        <span>{{ log.fromStatus || '开始' }} → {{ log.toStatus }}</span>
        <p>{{ log.remark || '-' }}</p>
      </li>
    </ol>
  </section>
</template>
