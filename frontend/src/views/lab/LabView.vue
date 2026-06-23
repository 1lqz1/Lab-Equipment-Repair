<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { createLab, deleteLab, disableLab, enableLab, queryLabs, updateLab } from '@/api/lab'
import PageHeader from '@/components/PageHeader.vue'
import { usePermission } from '@/hooks/usePermission'

const { isAdmin } = usePermission()
const labs = ref([])
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
})

const form = reactive({
  name: '',
  location: '',
  managerId: '',
  description: '',
})

const stats = computed(() => ({
  total: labs.value.length,
  active: labs.value.filter((lab) => lab.status !== 'DISABLED').length,
  disabled: labs.value.filter((lab) => lab.status === 'DISABLED').length,
}))

function statusText(status) {
  return status === 'DISABLED' ? '停用' : '启用'
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.location = ''
  form.managerId = ''
  form.description = ''
  formError.value = ''
}

function editLab(lab) {
  editingId.value = lab.id
  form.name = lab.name || ''
  form.location = lab.location || ''
  form.managerId = lab.managerId || ''
  form.description = lab.description || ''
  formError.value = ''
  message.value = ''
}

async function loadLabs() {
  loading.value = true
  listError.value = ''
  try {
    const response = await queryLabs({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
    })
    labs.value = response.data || []
  } catch (exception) {
    labs.value = []
    listError.value = exception.message || '实验室列表加载失败'
  } finally {
    loading.value = false
  }
}

async function submitLab() {
  saving.value = true
  formError.value = ''
  message.value = ''
  const payload = {
    name: form.name.trim(),
    location: form.location.trim() || null,
    managerId: form.managerId ? Number(form.managerId) : null,
    description: form.description.trim() || null,
  }
  try {
    if (editingId.value) {
      await updateLab(editingId.value, payload)
      message.value = '实验室信息已更新'
    } else {
      await createLab(payload)
      message.value = '实验室已新增'
    }
    resetForm()
    await loadLabs()
  } catch (exception) {
    formError.value = exception.message || '实验室保存失败'
  } finally {
    saving.value = false
  }
}

async function runRowAction(lab, action, successText) {
  actionLoadingId.value = lab.id
  listError.value = ''
  message.value = ''
  try {
    await action(lab.id)
    message.value = successText
    await loadLabs()
  } catch (exception) {
    listError.value = exception.message || '操作失败'
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(loadLabs)
</script>

<template>
  <PageHeader title="实验室管理" description="维护实验室分区、位置、负责人和启停状态" />

  <section class="admin-overview">
    <div>
      <span>实验室总数</span>
      <strong>{{ stats.total }}</strong>
    </div>
    <div>
      <span>启用中</span>
      <strong>{{ stats.active }}</strong>
    </div>
    <div>
      <span>已停用</span>
      <strong>{{ stats.disabled }}</strong>
    </div>
  </section>

  <section class="panel">
    <h2>{{ editingId ? '编辑实验室' : '新增实验室' }}</h2>
    <form class="form-grid" @submit.prevent="submitLab">
      <label>
        实验室名称
        <input v-model="form.name" required autocomplete="off" placeholder="例如 现代仪器分析实验室" />
      </label>
      <label>
        位置
        <input v-model="form.location" autocomplete="off" placeholder="例如 实验楼 A302" />
      </label>
      <label>
        管理员用户ID
        <input v-model="form.managerId" type="number" min="1" placeholder="可选" />
      </label>
      <label class="form-wide">
        说明
        <textarea v-model="form.description" rows="3" placeholder="实验室用途、注意事项等"></textarea>
      </label>
      <p v-if="formError" class="form-error">{{ formError }}</p>
      <p v-if="message" class="form-success">{{ message }}</p>
      <div class="form-actions">
        <button class="ghost-button" type="button" :disabled="saving" @click="resetForm">清空</button>
        <button class="primary-button" type="submit" :disabled="saving">
          {{ saving ? '保存中...' : editingId ? '保存修改' : '新增实验室' }}
        </button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="table-toolbar">
      <div>
        <h2>实验室列表</h2>
        <p>支持按名称、位置、说明搜索，并对实验室进行启停和删除。</p>
      </div>
      <div class="toolbar-actions">
        <input v-model="filters.keyword" placeholder="搜索实验室" @keyup.enter="loadLabs" />
        <select v-model="filters.status" @change="loadLabs">
          <option value="">全部状态</option>
          <option value="ACTIVE">启用</option>
          <option value="DISABLED">停用</option>
        </select>
        <button class="ghost-button" type="button" :disabled="loading" @click="loadLabs">
          {{ loading ? '刷新中...' : '查询' }}
        </button>
      </div>
    </div>

    <p v-if="listError" class="form-error table-message">{{ listError }}</p>

    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>位置</th>
            <th>管理员ID</th>
            <th>状态</th>
            <th>说明</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6">
              <div class="table-state">正在加载实验室列表...</div>
            </td>
          </tr>
          <template v-else>
            <tr v-for="lab in labs" :key="lab.id">
              <td>{{ lab.name }}</td>
              <td>{{ lab.location || '-' }}</td>
              <td>{{ lab.managerId || '-' }}</td>
              <td>
                <span class="status-tag" :data-status="lab.status || 'ACTIVE'">{{ statusText(lab.status) }}</span>
              </td>
              <td>{{ lab.description || '-' }}</td>
              <td>
                <div class="row-actions">
                  <button class="link-button" type="button" @click="editLab(lab)">编辑</button>
                  <button
                    v-if="lab.status === 'DISABLED'"
                    class="link-button"
                    type="button"
                    :disabled="actionLoadingId === lab.id"
                    @click="runRowAction(lab, enableLab, '实验室已启用')"
                  >
                    启用
                  </button>
                  <button
                    v-else
                    class="link-button danger-link"
                    type="button"
                    :disabled="actionLoadingId === lab.id"
                    @click="runRowAction(lab, disableLab, '实验室已停用')"
                  >
                    停用
                  </button>
                  <button
                    v-if="isAdmin"
                    class="link-button danger-link"
                    type="button"
                    :disabled="actionLoadingId === lab.id"
                    @click="runRowAction(lab, deleteLab, '实验室已删除')"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </template>
          <tr v-if="!loading && !listError && labs.length === 0">
            <td colspan="6">
              <div class="table-state">暂无符合条件的实验室</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
