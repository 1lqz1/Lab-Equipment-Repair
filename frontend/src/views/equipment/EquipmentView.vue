<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { createEquipment, listEquipment } from '@/api/equipment'
import { listLabs } from '@/api/lab'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePermission } from '@/hooks/usePermission'

const { isManager: canManageEquipment } = usePermission()
const equipment = ref([])
const labs = ref([])
const loading = ref(false)
const saving = ref(false)
const listError = ref('')
const formError = ref('')
const formSuccess = ref('')

const filters = reactive({
  keyword: '',
  status: '',
  labId: '',
})

const form = reactive({
  code: '',
  name: '',
  category: '',
  labId: '',
  status: 'NORMAL',
  responsibleUserId: '',
  purchaseDate: '',
})

const labNameMap = computed(() => {
  return labs.value.reduce((map, lab) => {
    map[lab.id] = lab.name
    return map
  }, {})
})

const equipmentStats = computed(() => ({
  total: equipment.value.length,
  normal: equipment.value.filter((item) => item.status === 'NORMAL').length,
  repairing: equipment.value.filter((item) => item.status === 'REPAIRING').length,
  disabled: equipment.value.filter((item) => item.status === 'DISABLED').length,
}))

function labText(labId) {
  return labNameMap.value[labId] || `实验室 ${labId}`
}

async function loadEquipment() {
  loading.value = true
  listError.value = ''
  const params = {
    keyword: filters.keyword || undefined,
    status: filters.status || undefined,
    labId: filters.labId || undefined,
  }
  try {
    const response = await listEquipment(params)
    equipment.value = response.data || []
  } catch (exception) {
    equipment.value = []
    listError.value = exception.message || '设备列表加载失败'
  } finally {
    loading.value = false
  }
}

async function loadLabs() {
  try {
    const response = await listLabs()
    labs.value = response.data || []
    if (!form.labId && labs.value.length > 0) {
      form.labId = String(labs.value[0].id)
    }
  } catch (exception) {
    formError.value = exception.message || '实验室列表加载失败'
  }
}

function resetForm() {
  form.code = ''
  form.name = ''
  form.category = ''
  form.labId = labs.value[0]?.id ? String(labs.value[0].id) : ''
  form.status = 'NORMAL'
  form.responsibleUserId = ''
  form.purchaseDate = ''
}

async function submitCreate() {
  if (!canManageEquipment.value) {
    formError.value = '当前账号没有新增设备权限'
    return
  }
  saving.value = true
  formError.value = ''
  formSuccess.value = ''
  try {
    await createEquipment({
      code: form.code.trim(),
      name: form.name.trim(),
      category: form.category.trim() || null,
      labId: Number(form.labId),
      status: form.status,
      responsibleUserId: form.responsibleUserId ? Number(form.responsibleUserId) : null,
      purchaseDate: form.purchaseDate || null,
    })
    formSuccess.value = '设备已添加'
    resetForm()
    await loadEquipment()
  } catch (exception) {
    formError.value = exception.message || '设备添加失败'
  } finally {
    saving.value = false
  }
}

async function loadPage() {
  await loadLabs()
  await loadEquipment()
}

onMounted(loadPage)
</script>

<template>
  <PageHeader title="设备管理" description="维护实验室设备档案和维修状态" />

  <section class="admin-overview">
    <div>
      <span>设备总数</span>
      <strong>{{ equipmentStats.total }}</strong>
    </div>
    <div>
      <span>正常可报修</span>
      <strong>{{ equipmentStats.normal }}</strong>
    </div>
    <div>
      <span>维修中</span>
      <strong>{{ equipmentStats.repairing }}</strong>
    </div>
    <div>
      <span>停用</span>
      <strong>{{ equipmentStats.disabled }}</strong>
    </div>
  </section>

  <section v-if="canManageEquipment" class="panel">
    <h2>新增设备</h2>
    <form class="form-grid" @submit.prevent="submitCreate">
      <label>
        设备编号
        <input v-model="form.code" required placeholder="例如 EQ-LAB-001" autocomplete="off" />
      </label>
      <label>
        设备名称
        <input v-model="form.name" required placeholder="请输入设备名称" autocomplete="off" />
      </label>
      <label>
        设备类别
        <input v-model="form.category" placeholder="例如 显微分析、样品制备" autocomplete="off" />
      </label>
      <label>
        所属实验室
        <select v-model="form.labId" required :disabled="labs.length === 0">
          <option value="">请选择实验室</option>
          <option v-for="lab in labs" :key="lab.id" :value="lab.id">{{ lab.name }}</option>
        </select>
      </label>
      <label>
        初始状态
        <select v-model="form.status">
          <option value="NORMAL">正常</option>
          <option value="REPAIRING">维修中</option>
          <option value="DISABLED">停用</option>
          <option value="SCRAPPED">已报废</option>
        </select>
      </label>
      <label>
        负责人用户ID
        <input v-model="form.responsibleUserId" type="number" min="1" placeholder="可选" />
      </label>
      <label>
        购置日期
        <input v-model="form.purchaseDate" type="date" />
      </label>
      <p v-if="labs.length === 0" class="form-error">暂无实验室，请先创建实验室后再添加设备。</p>
      <p v-if="formError" class="form-error">{{ formError }}</p>
      <p v-if="formSuccess" class="form-success">{{ formSuccess }}</p>
      <div class="form-actions">
        <button class="primary-button" type="submit" :disabled="saving || labs.length === 0">
          {{ saving ? '添加中...' : '添加设备' }}
        </button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="table-toolbar">
      <div>
        <h2>设备列表</h2>
        <p>按编号、名称、实验室和状态筛选设备档案</p>
      </div>
      <div class="toolbar-actions equipment-filters">
        <input v-model="filters.keyword" placeholder="搜索编号或名称" @keyup.enter="loadEquipment" />
        <select v-model="filters.labId" @change="loadEquipment">
          <option value="">全部实验室</option>
          <option v-for="lab in labs" :key="lab.id" :value="lab.id">{{ lab.name }}</option>
        </select>
        <select v-model="filters.status" @change="loadEquipment">
          <option value="">全部状态</option>
          <option value="NORMAL">正常</option>
          <option value="REPAIRING">维修中</option>
          <option value="DISABLED">停用</option>
          <option value="SCRAPPED">已报废</option>
        </select>
        <button class="ghost-button" type="button" :disabled="loading" @click="loadEquipment">
          {{ loading ? '刷新中...' : '查询' }}
        </button>
      </div>
    </div>

    <p v-if="listError" class="form-error table-message">{{ listError }}</p>

    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>设备编号</th>
            <th>设备名称</th>
            <th>类别</th>
            <th>所属实验室</th>
            <th>负责人ID</th>
            <th>购置日期</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7">
              <div class="table-state">正在加载设备列表...</div>
            </td>
          </tr>
          <template v-else>
            <tr v-for="item in equipment" :key="item.id">
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.category || '-' }}</td>
              <td>{{ labText(item.labId) }}</td>
              <td>{{ item.responsibleUserId || '-' }}</td>
              <td>{{ item.purchaseDate || '-' }}</td>
              <td><StatusTag :status="item.status" /></td>
            </tr>
          </template>
          <tr v-if="!loading && !listError && equipment.length === 0">
            <td colspan="7">
              <div class="table-state">暂无符合条件的设备</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
