<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  listDictItems,
  listDicts,
  updateDict,
  updateDictItem,
} from '@/api/dict'
import PageHeader from '@/components/PageHeader.vue'

const dicts = ref([])
const items = ref([])
const selectedDict = ref(null)
const loading = ref(false)
const itemLoading = ref(false)
const savingDict = ref(false)
const savingItem = ref(false)
const error = ref('')
const message = ref('')
const editingDictId = ref(null)
const editingItemId = ref(null)

const filters = reactive({
  keyword: '',
})

const dictForm = reactive({
  dictCode: '',
  dictName: '',
  enabled: true,
  sortOrder: 0,
  remark: '',
})

const itemForm = reactive({
  itemValue: '',
  itemLabel: '',
  enabled: true,
  sortOrder: 0,
  remark: '',
})

const currentTitle = computed(() => selectedDict.value?.dictName || '请选择字典')

function resetDictForm() {
  editingDictId.value = null
  dictForm.dictCode = ''
  dictForm.dictName = ''
  dictForm.enabled = true
  dictForm.sortOrder = 0
  dictForm.remark = ''
}

function resetItemForm() {
  editingItemId.value = null
  itemForm.itemValue = ''
  itemForm.itemLabel = ''
  itemForm.enabled = true
  itemForm.sortOrder = 0
  itemForm.remark = ''
}

function editDict(dict) {
  editingDictId.value = dict.id
  dictForm.dictCode = dict.dictCode
  dictForm.dictName = dict.dictName
  dictForm.enabled = Boolean(dict.enabled)
  dictForm.sortOrder = dict.sortOrder || 0
  dictForm.remark = dict.remark || ''
}

function editItem(item) {
  editingItemId.value = item.id
  itemForm.itemValue = item.itemValue
  itemForm.itemLabel = item.itemLabel
  itemForm.enabled = Boolean(item.enabled)
  itemForm.sortOrder = item.sortOrder || 0
  itemForm.remark = item.remark || ''
}

async function loadDicts() {
  loading.value = true
  error.value = ''
  try {
    const response = await listDicts({ keyword: filters.keyword || undefined })
    dicts.value = response.data || []
    if (!selectedDict.value && dicts.value.length > 0) {
      await selectDict(dicts.value[0])
    }
  } catch (exception) {
    error.value = exception.message || '字典列表加载失败'
  } finally {
    loading.value = false
  }
}

async function selectDict(dict) {
  selectedDict.value = dict
  resetItemForm()
  await loadItems()
}

async function loadItems() {
  if (!selectedDict.value) {
    items.value = []
    return
  }
  itemLoading.value = true
  error.value = ''
  try {
    const response = await listDictItems(selectedDict.value.dictCode, { enabledOnly: false })
    items.value = response.data || []
  } catch (exception) {
    items.value = []
    error.value = exception.message || '字典项加载失败'
  } finally {
    itemLoading.value = false
  }
}

async function submitDict() {
  savingDict.value = true
  error.value = ''
  message.value = ''
  const payload = {
    dictCode: dictForm.dictCode.trim(),
    dictName: dictForm.dictName.trim(),
    enabled: dictForm.enabled,
    sortOrder: Number(dictForm.sortOrder) || 0,
    remark: dictForm.remark.trim() || null,
  }
  try {
    if (editingDictId.value) {
      await updateDict(editingDictId.value, payload)
      message.value = '字典已更新'
    } else {
      await createDict(payload)
      message.value = '字典已新增'
    }
    resetDictForm()
    selectedDict.value = null
    await loadDicts()
  } catch (exception) {
    error.value = exception.message || '字典保存失败'
  } finally {
    savingDict.value = false
  }
}

async function submitItem() {
  if (!selectedDict.value) {
    error.value = '请先选择字典'
    return
  }
  savingItem.value = true
  error.value = ''
  message.value = ''
  const payload = {
    itemValue: itemForm.itemValue.trim(),
    itemLabel: itemForm.itemLabel.trim(),
    enabled: itemForm.enabled,
    sortOrder: Number(itemForm.sortOrder) || 0,
    remark: itemForm.remark.trim() || null,
  }
  try {
    if (editingItemId.value) {
      await updateDictItem(editingItemId.value, payload)
      message.value = '字典项已更新'
    } else {
      await createDictItem(selectedDict.value.id, payload)
      message.value = '字典项已新增'
    }
    resetItemForm()
    await loadItems()
  } catch (exception) {
    error.value = exception.message || '字典项保存失败'
  } finally {
    savingItem.value = false
  }
}

async function removeDict(dict) {
  error.value = ''
  message.value = ''
  try {
    await deleteDict(dict.id)
    message.value = '字典已删除'
    if (selectedDict.value?.id === dict.id) {
      selectedDict.value = null
      items.value = []
    }
    await loadDicts()
  } catch (exception) {
    error.value = exception.message || '字典删除失败'
  }
}

async function removeItem(item) {
  error.value = ''
  message.value = ''
  try {
    await deleteDictItem(item.id)
    message.value = '字典项已删除'
    await loadItems()
  } catch (exception) {
    error.value = exception.message || '字典项删除失败'
  }
}

onMounted(loadDicts)
</script>

<template>
  <PageHeader title="字典管理" description="维护角色、状态、紧急程度、设备分类等基础选项" />

  <section class="panel">
    <h2>{{ editingDictId ? '编辑字典' : '新增字典' }}</h2>
    <form class="form-grid" @submit.prevent="submitDict">
      <label>
        字典编码
        <input v-model="dictForm.dictCode" required autocomplete="off" placeholder="例如 equipment_category" />
      </label>
      <label>
        字典名称
        <input v-model="dictForm.dictName" required autocomplete="off" placeholder="例如 设备分类" />
      </label>
      <label>
        排序
        <input v-model="dictForm.sortOrder" type="number" />
      </label>
      <label>
        是否启用
        <select v-model="dictForm.enabled">
          <option :value="true">启用</option>
          <option :value="false">停用</option>
        </select>
      </label>
      <label class="form-wide">
        备注
        <textarea v-model="dictForm.remark" rows="2"></textarea>
      </label>
      <div class="form-actions">
        <button class="ghost-button" type="button" @click="resetDictForm">清空</button>
        <button class="primary-button" type="submit" :disabled="savingDict">
          {{ savingDict ? '保存中...' : editingDictId ? '保存字典' : '新增字典' }}
        </button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="table-toolbar">
      <div>
        <h2>字典列表</h2>
        <p>点击字典行可查看和维护字典项。</p>
      </div>
      <div class="toolbar-actions">
        <input v-model="filters.keyword" placeholder="搜索字典" @keyup.enter="loadDicts" />
        <button class="ghost-button" type="button" :disabled="loading" @click="loadDicts">
          {{ loading ? '查询中...' : '查询' }}
        </button>
      </div>
    </div>
    <p v-if="error" class="form-error table-message">{{ error }}</p>
    <p v-if="message" class="form-success table-message">{{ message }}</p>
    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>编码</th>
            <th>名称</th>
            <th>状态</th>
            <th>排序</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dict in dicts" :key="dict.id" :class="{ 'is-selected': selectedDict?.id === dict.id }">
            <td>{{ dict.dictCode }}</td>
            <td>{{ dict.dictName }}</td>
            <td>{{ dict.enabled ? '启用' : '停用' }}</td>
            <td>{{ dict.sortOrder }}</td>
            <td>
              <div class="row-actions">
                <button class="link-button" type="button" @click="selectDict(dict)">字典项</button>
                <button class="link-button" type="button" @click="editDict(dict)">编辑</button>
                <button class="link-button danger-link" type="button" @click="removeDict(dict)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && dicts.length === 0">
            <td colspan="5">
              <div class="table-state">暂无字典</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <section class="panel">
    <h2>{{ currentTitle }} - 字典项</h2>
    <form class="form-grid" @submit.prevent="submitItem">
      <label>
        字典项值
        <input v-model="itemForm.itemValue" required :disabled="!selectedDict" autocomplete="off" />
      </label>
      <label>
        显示名称
        <input v-model="itemForm.itemLabel" required :disabled="!selectedDict" autocomplete="off" />
      </label>
      <label>
        排序
        <input v-model="itemForm.sortOrder" type="number" :disabled="!selectedDict" />
      </label>
      <label>
        是否启用
        <select v-model="itemForm.enabled" :disabled="!selectedDict">
          <option :value="true">启用</option>
          <option :value="false">停用</option>
        </select>
      </label>
      <label class="form-wide">
        备注
        <textarea v-model="itemForm.remark" rows="2" :disabled="!selectedDict"></textarea>
      </label>
      <div class="form-actions">
        <button class="ghost-button" type="button" @click="resetItemForm">清空</button>
        <button class="primary-button" type="submit" :disabled="savingItem || !selectedDict">
          {{ savingItem ? '保存中...' : editingItemId ? '保存字典项' : '新增字典项' }}
        </button>
      </div>
    </form>

    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>值</th>
            <th>显示名称</th>
            <th>状态</th>
            <th>排序</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="itemLoading">
            <td colspan="5">
              <div class="table-state">正在加载字典项...</div>
            </td>
          </tr>
          <template v-else>
            <tr v-for="item in items" :key="item.id">
              <td>{{ item.itemValue }}</td>
              <td>{{ item.itemLabel }}</td>
              <td>{{ item.enabled ? '启用' : '停用' }}</td>
              <td>{{ item.sortOrder }}</td>
              <td>
                <div class="row-actions">
                  <button class="link-button" type="button" @click="editItem(item)">编辑</button>
                  <button class="link-button danger-link" type="button" @click="removeItem(item)">删除</button>
                </div>
              </td>
            </tr>
          </template>
          <tr v-if="!itemLoading && selectedDict && items.length === 0">
            <td colspan="5">
              <div class="table-state">当前字典暂无字典项</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
