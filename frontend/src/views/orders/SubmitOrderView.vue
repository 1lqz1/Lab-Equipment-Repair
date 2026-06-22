<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { listEquipment } from '@/api/equipment'
import { submitRepairOrder } from '@/api/repairOrder'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const equipment = ref([])
const error = ref('')
const form = reactive({
  equipmentId: '',
  faultDescription: '',
  urgency: 'NORMAL',
  location: '',
  contact: '',
})

async function loadEquipment() {
  const response = await listEquipment({ status: 'NORMAL' })
  equipment.value = response.data || []
}

async function submit() {
  error.value = ''
  try {
    const response = await submitRepairOrder({
      ...form,
      equipmentId: Number(form.equipmentId),
    })
    router.push(`/orders/${response.data.id}`)
  } catch (exception) {
    error.value = exception.message
  }
}

onMounted(loadEquipment)
</script>

<template>
  <PageHeader title="提交报修" description="选择故障设备并填写报修信息" />

  <form class="panel form-grid" @submit.prevent="submit">
    <label>
      报修设备
      <select v-model="form.equipmentId" required>
        <option value="">请选择设备</option>
        <option v-for="item in equipment" :key="item.id" :value="item.id">
          {{ item.code }} - {{ item.name }}
        </option>
      </select>
    </label>
    <label>
      紧急程度
      <select v-model="form.urgency">
        <option value="LOW">低</option>
        <option value="NORMAL">普通</option>
        <option value="HIGH">高</option>
        <option value="URGENT">紧急</option>
      </select>
    </label>
    <label>
      故障地点
      <input v-model="form.location" />
    </label>
    <label>
      联系方式
      <input v-model="form.contact" />
    </label>
    <label class="form-wide">
      故障描述
      <textarea v-model="form.faultDescription" required rows="5"></textarea>
    </label>
    <p v-if="error" class="form-error">{{ error }}</p>
    <div class="form-actions">
      <button class="primary-button" type="submit">提交工单</button>
    </div>
  </form>
</template>
