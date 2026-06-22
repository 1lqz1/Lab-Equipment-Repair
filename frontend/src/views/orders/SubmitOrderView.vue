<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { listEquipment } from '@/api/equipment'
import { submitRepairOrder } from '@/api/repairOrder'
import PageHeader from '@/components/PageHeader.vue'

const router = useRouter()
const equipment = ref([])
const equipmentLoading = ref(false)
const equipmentError = ref('')
const submitting = ref(false)
const error = ref('')
const form = reactive({
  equipmentId: '',
  faultDescription: '',
  urgency: 'NORMAL',
  location: '',
  contact: '',
})

async function loadEquipment() {
  equipmentLoading.value = true
  equipmentError.value = ''
  try {
    const response = await listEquipment({ status: 'NORMAL' })
    equipment.value = response.data || []
  } catch (exception) {
    equipment.value = []
    equipmentError.value = exception.message || '设备列表加载失败'
  } finally {
    equipmentLoading.value = false
  }
}

async function submit() {
  error.value = ''
  if (!form.equipmentId) {
    error.value = '请选择需要报修的设备'
    return
  }
  submitting.value = true
  try {
    const response = await submitRepairOrder({
      ...form,
      equipmentId: Number(form.equipmentId),
    })
    router.push(`/orders/${response.data.id}`)
  } catch (exception) {
    error.value = exception.message || '报修提交失败'
  } finally {
    submitting.value = false
  }
}

onMounted(loadEquipment)
</script>

<template>
  <PageHeader title="提交报修" description="选择故障设备并填写报修信息" />

  <section class="repair-notice">
    <div>
      <span>可报修设备</span>
      <strong>{{ equipment.length }}</strong>
    </div>
    <p>只显示状态为“正常”的设备。维修中、停用或报废设备不会进入报修下拉框。</p>
    <button class="ghost-button" type="button" :disabled="equipmentLoading" @click="loadEquipment">
      {{ equipmentLoading ? '刷新中...' : '刷新设备' }}
    </button>
  </section>

  <form class="panel form-grid" @submit.prevent="submit">
    <label>
      报修设备
      <select v-model="form.equipmentId" required :disabled="equipmentLoading || equipment.length === 0">
        <option value="">{{ equipmentLoading ? '正在加载设备...' : '请选择设备' }}</option>
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
    <p v-if="equipmentError" class="form-error">{{ equipmentError }}</p>
    <p v-else-if="!equipmentLoading && equipment.length === 0" class="form-error">
      暂无可报修设备，请先在设备管理中新增状态为“正常”的设备。
    </p>
    <p v-if="error" class="form-error">{{ error }}</p>
    <div class="form-actions">
      <button class="primary-button" type="submit" :disabled="submitting || equipmentLoading || equipment.length === 0">
        {{ submitting ? '提交中...' : '提交工单' }}
      </button>
    </div>
  </form>
</template>
