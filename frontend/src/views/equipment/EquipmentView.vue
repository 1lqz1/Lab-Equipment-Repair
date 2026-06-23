<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import {
  createEquipment,
  deleteEquipment,
  listEquipment,
  updateEquipment,
  updateEquipmentStatus,
} from '@/api/equipment'
import { listDictItems } from '@/api/dict'
import { listLabs } from '@/api/lab'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { usePermission } from '@/hooks/usePermission'

const { isManager: canManageEquipment } = usePermission()
const equipment = ref([])
const labs = ref([])
const categories = ref([])
const statuses = ref([])
const loading = ref(false)
const saving = ref(false)
const actionLoadingId = ref(null)
const listError = ref('')
const formError = ref('')
const message = ref('')
const editingId = ref(null)

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

function optionLabel(options, value) {
  return options.find((item) => item.itemValue === value)?.itemLabel || value
}

function resetForm() {
  editingId.value = null
  form.code = ''
  form.name = ''
  form.category = ''
  form.labId = labs.value[0]?.id ? String(labs.value[0].id) : ''
  form.status = 'NORMAL'
  form.responsibleUserId = ''
  form.purchaseDate = ''
  formError.value = ''
}

function editEquipment(item) {
  editingId.value = item.id
  form.code = item.code || ''
  form.name = item.name || ''
  form.category = item.category || ''
  form.labId = item.labId ? String(item.labId) : ''
  form.status = item.status || 'NORMAL'
  form.responsibleUserId = item.responsibleUserId || ''
  form.purchaseDate = item.purchaseDate || ''
  formError.value = ''
  message.value = ''
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

async function loadBasics() {
  try {
    const [labResponse, categoryResponse, statusResponse] = await Promise.all([
      listLabs(),
      listDictItems('equipment_category'),
      listDictItems('equipment_status'),
    ])
    labs.value = labResponse.data || []
    categories.value = categoryResponse.data || []
    statuses.value = statusResponse.data || []
    if (!form.labId && labs.value.length > 0) {
      form.labId = String(labs.value[0].id)
    }
  } catch (exception) {
    formError.value = exception.message || '基础数据加载失败'
  }
}

async function submitSave() {
  if (!canManageEquipment.value) {
    formError.value = '当前账号没有设备维护权限'
    return
  }
  saving.value = true
  formError.value = ''
  message.value = ''
  try {
    const payload = {
      code: form.code.trim(),
      name: form.name.trim(),
      category: form.category || null,
      labId: Number(form.labId),
      status: form.status,
      responsibleUserId: form.responsibleUserId ? Number(form.responsibleUserId) : null,
      purchaseDate: form.purchaseDate || null,
    }
    if (editingId.value) {
      await updateEquipment(editingId.value, payload)
      message.value = '设备信息已更新'
    } else {
      await createEquipment(payload)
      message.value = '设备已添加'
    }
    resetForm()
    await loadEquipment()
  } catch (exception) {
    formError.value = exception.message || '设备保存失败'
  } finally {
    saving.value = false
  }
}

async function runRowAction(item, action, successText) {
  actionLoadingId.value = item.id
  listError.value = ''
  message.value = ''
  try {
    await action(item)
    message.value = successText
    await loadEquipment()
  } catch (exception) {
    listError.value = exception.message || '操作失败'
  } finally {
    actionLoadingId.value = null
  }
}

async function loadPage() {
  await loadBasics()
  await loadEquipment()
}

onMounted(loadPage)
</script>

<template>
  <PageHeader title="设备管理" description="维护实验室设备档案、分类、归属和资产状态" />

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
    <h2>{{ editingId ? '编辑设备' : '新增设备' }}</h2>
    <form class="form-grid" @submit.prevent="submitSave">
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
        <select v-model="form.category">
          <option value="">未分类</option>
          <option v-for="item in categories" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
        </select>
      </label>
      <label>
        所属实验室
        <select v-model="form.labId" required :disabled="labs.length === 0">
          <option value="">请选择实验室</option>
          <option v-for="lab in labs" :key="lab.id" :value="lab.id">{{ lab.name }}</option>
        </select>
      </label>
      <label>
        设备状态
        <select v-model="form.status">
          <option v-for="item in statuses" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
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
      <p v-if="message" class="form-success">{{ message }}</p>
      <div class="form-actions">
        <button class="ghost-button" type="button" :disabled="saving" @click="resetForm">清空</button>
        <button class="primary-button" type="submit" :disabled="saving || labs.length === 0">
          {{ saving ? '保存中...' : editingId ? '保存修改' : '添加设备' }}
        </button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="table-toolbar">
      <div>
        <h2>设备列表</h2>
        <p>按编号、名称、实验室和状态筛选设备档案。</p>
      </div>
      <div class="toolbar-actions equipment-filters">
        <input v-model="filters.keyword" placeholder="搜索编号或名称" @keyup.enter="loadEquipment" />
        <select v-model="filters.labId" @change="loadEquipment">
          <option value="">全部实验室</option>
          <option v-for="lab in labs" :key="lab.id" :value="lab.id">{{ lab.name }}</option>
        </select>
        <select v-model="filters.status" @change="loadEquipment">
          <option value="">全部状态</option>
          <option v-for="item in statuses" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
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
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8">
              <div class="table-state">正在加载设备列表...</div>
            </td>
          </tr>
          <template v-else>
            <tr v-for="item in equipment" :key="item.id">
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ optionLabel(categories, item.category) || '-' }}</td>
              <td>{{ labText(item.labId) }}</td>
              <td>{{ item.responsibleUserId || '-' }}</td>
              <td>{{ item.purchaseDate || '-' }}</td>
              <td><StatusTag :status="item.status" /></td>
              <td>
                <div v-if="canManageEquipment" class="row-actions">
                  <button class="link-button" type="button" @click="editEquipment(item)">编辑</button>
                  <button
                    class="link-button"
                    type="button"
                    :disabled="actionLoadingId === item.id || item.status === 'DISABLED'"
                    @click="runRowAction(item, (row) => updateEquipmentStatus(row.id, 'DISABLED'), '设备已停用')"
                  >
                    停用
                  </button>
                  <button
                    class="link-button"
                    type="button"
                    :disabled="actionLoadingId === item.id || item.status === 'NORMAL'"
                    @click="runRowAction(item, (row) => updateEquipmentStatus(row.id, 'NORMAL'), '设备已恢复正常')"
                  >
                    恢复
                  </button>
                  <button
                    class="link-button danger-link"
                    type="button"
                    :disabled="actionLoadingId === item.id || item.status === 'SCRAPPED'"
                    @click="runRowAction(item, (row) => updateEquipmentStatus(row.id, 'SCRAPPED'), '设备已报废')"
                  >
                    报废
                  </button>
                  <button
                    class="link-button danger-link"
                    type="button"
                    :disabled="actionLoadingId === item.id"
                    @click="runRowAction(item, (row) => deleteEquipment(row.id), '设备已删除')"
                  >
                    删除
                  </button>
                </div>
                <span v-else>-</span>
              </td>
            </tr>
          </template>
          <tr v-if="!loading && !listError && equipment.length === 0">
            <td colspan="8">
              <div class="table-state">暂无符合条件的设备</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
