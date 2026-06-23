<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

import {
  addRepairOrderRemark,
  cancelRepairOrder,
  getRepairOrder,
  listOrderLogs,
  transferRepairOrder,
} from '@/api/repairOrder'
import { listUsers } from '@/api/user'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePermission } from '@/hooks/usePermission'

const route = useRoute()
const { isManager, isReporter } = usePermission()
const order = ref(null)
const logs = ref([])
const repairers = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const message = ref('')

const cancelForm = reactive({
  remark: '',
})

const transferForm = reactive({
  repairerId: '',
  remark: '',
})

const remarkForm = reactive({
  remark: '',
})

const canCancel = computed(() => {
  if (!order.value) return false
  return !['CLOSED', 'CANCELLED', 'REJECTED'].includes(order.value.status) && (isManager.value || isReporter.value)
})

const canTransfer = computed(() => {
  if (!order.value) return false
  return isManager.value && ['ASSIGNED', 'IN_PROGRESS'].includes(order.value.status)
})

async function loadDetail() {
  loading.value = true
  error.value = ''
  const id = route.params.id
  try {
    const [orderResponse, logResponse] = await Promise.all([getRepairOrder(id), listOrderLogs(id)])
    order.value = orderResponse.data
    logs.value = logResponse.data || []
  } catch (exception) {
    error.value = exception.message || '工单详情加载失败'
  } finally {
    loading.value = false
  }
}

async function loadRepairers() {
  if (!isManager.value) {
    return
  }
  try {
    const response = await listUsers({ role: 'REPAIRER', status: 'ACTIVE' })
    repairers.value = response.data || []
  } catch {
    repairers.value = []
  }
}

async function runAction(action, successText) {
  actionLoading.value = true
  error.value = ''
  message.value = ''
  try {
    await action()
    message.value = successText
    await loadDetail()
  } catch (exception) {
    error.value = exception.message || '操作失败'
  } finally {
    actionLoading.value = false
  }
}

async function submitCancel() {
  await runAction(async () => {
    await cancelRepairOrder(order.value.id, { remark: cancelForm.remark.trim() || null })
    cancelForm.remark = ''
  }, '工单已取消')
}

async function submitTransfer() {
  await runAction(async () => {
    await transferRepairOrder(order.value.id, {
      repairerId: Number(transferForm.repairerId),
      remark: transferForm.remark.trim() || null,
    })
    transferForm.repairerId = ''
    transferForm.remark = ''
  }, '工单已转派')
}

async function submitRemark() {
  await runAction(async () => {
    await addRepairOrderRemark(order.value.id, { remark: remarkForm.remark.trim() || null })
    remarkForm.remark = ''
  }, '备注已追加')
}

async function loadPage() {
  await Promise.all([loadDetail(), loadRepairers()])
}

onMounted(loadPage)
</script>

<template>
  <PageHeader title="工单详情" description="查看工单信息、状态流转和横向操作记录" />

  <p v-if="error" class="form-error table-message">{{ error }}</p>
  <p v-if="message" class="form-success table-message">{{ message }}</p>

  <section v-if="loading" class="panel">
    <div class="table-state">正在加载工单详情...</div>
  </section>

  <section v-else-if="order" class="panel detail-grid">
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
      <span>实验室ID</span>
      <strong>{{ order.labId }}</strong>
    </div>
    <div>
      <span>报修人ID</span>
      <strong>{{ order.reporterId }}</strong>
    </div>
    <div>
      <span>维修人员ID</span>
      <strong>{{ order.assignedTo || '未分配' }}</strong>
    </div>
    <div>
      <span>紧急程度</span>
      <strong>{{ order.urgency }}</strong>
    </div>
    <div>
      <span>联系方式</span>
      <strong>{{ order.contact || '-' }}</strong>
    </div>
    <div class="detail-wide">
      <span>故障描述</span>
      <p>{{ order.faultDescription }}</p>
    </div>
  </section>

  <section v-if="order" class="panel">
    <h2>工单操作</h2>
    <form class="form-grid" @submit.prevent="submitRemark">
      <label class="form-wide">
        追加备注
        <textarea v-model="remarkForm.remark" rows="3" placeholder="补充沟通记录、现场情况或处理说明"></textarea>
      </label>
      <div class="form-actions">
        <button class="primary-button" type="submit" :disabled="actionLoading || !remarkForm.remark.trim()">
          {{ actionLoading ? '提交中...' : '追加备注' }}
        </button>
      </div>
    </form>

    <form v-if="canTransfer" class="form-grid order-action-form" @submit.prevent="submitTransfer">
      <label>
        转派维修人员
        <select v-model="transferForm.repairerId" required>
          <option value="">请选择维修人员</option>
          <option v-for="user in repairers" :key="user.id" :value="user.id">
            {{ user.realName }}（{{ user.username }}）
          </option>
        </select>
      </label>
      <label>
        转派说明
        <input v-model="transferForm.remark" placeholder="可选" />
      </label>
      <div class="form-actions">
        <button class="primary-button" type="submit" :disabled="actionLoading || !transferForm.repairerId">
          转派工单
        </button>
      </div>
    </form>

    <form v-if="canCancel" class="form-grid order-action-form" @submit.prevent="submitCancel">
      <label class="form-wide">
        取消原因
        <textarea v-model="cancelForm.remark" rows="2" placeholder="说明取消原因"></textarea>
      </label>
      <div class="form-actions">
        <button class="ghost-button danger-link" type="submit" :disabled="actionLoading">
          取消工单
        </button>
      </div>
    </form>
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
