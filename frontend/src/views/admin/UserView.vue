<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import {
  approveUser,
  createUser,
  disableUser,
  enableUser,
  listUsers,
  rejectUser,
  resetUserPassword,
  updateUser,
} from '@/api/user'
import { listDictItems } from '@/api/dict'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const users = ref([])
const roles = ref([])
const statuses = ref([])
const loading = ref(false)
const saving = ref(false)
const formError = ref('')
const formSuccess = ref('')
const listError = ref('')
const actionMessage = ref('')
const actionLoadingId = ref(null)
const editingId = ref(null)

const filters = reactive({
  keyword: '',
  role: '',
  status: '',
})

const form = reactive({
  username: '',
  password: '123456',
  realName: '',
  phone: '',
  role: 'REPORTER',
  status: 'ACTIVE',
})

const passwordForm = reactive({
  userId: null,
  username: '',
  password: '123456',
})

const statusStats = computed(() => ({
  total: users.value.length,
  pending: users.value.filter((user) => user.status === 'PENDING').length,
  active: users.value.filter((user) => user.status === 'ACTIVE').length,
  disabled: users.value.filter((user) => user.status === 'DISABLED').length,
}))

function isCurrentUser(user) {
  return authStore.user?.id === user.id
}

function optionText(options, value) {
  const source = Array.isArray(options) ? options : options.value || []
  return source.find((item) => item.itemValue === value)?.itemLabel || value
}

function resetForm() {
  editingId.value = null
  form.username = ''
  form.password = '123456'
  form.realName = ''
  form.phone = ''
  form.role = 'REPORTER'
  form.status = 'ACTIVE'
  formError.value = ''
}

function editUser(user) {
  editingId.value = user.id
  form.username = user.username
  form.password = ''
  form.realName = user.realName || ''
  form.phone = user.phone || ''
  form.role = user.role || 'REPORTER'
  form.status = user.status || 'ACTIVE'
  formError.value = ''
  formSuccess.value = ''
}

function openResetPassword(user) {
  passwordForm.userId = user.id
  passwordForm.username = user.username
  passwordForm.password = '123456'
  listError.value = ''
  actionMessage.value = ''
}

function closeResetPassword() {
  passwordForm.userId = null
  passwordForm.username = ''
  passwordForm.password = '123456'
}

async function loadBasics() {
  try {
    const [roleResponse, statusResponse] = await Promise.all([
      listDictItems('user_role'),
      listDictItems('user_status'),
    ])
    roles.value = roleResponse.data || []
    statuses.value = statusResponse.data || []
  } catch (exception) {
    listError.value = exception.message || '用户选项加载失败'
  }
}

async function loadUsers() {
  loading.value = true
  listError.value = ''
  try {
    const response = await listUsers({
      keyword: filters.keyword || undefined,
      role: filters.role || undefined,
      status: filters.status || undefined,
    })
    users.value = response.data || []
  } catch (exception) {
    users.value = []
    listError.value = exception.message || '用户列表加载失败'
  } finally {
    loading.value = false
  }
}

async function submitSave() {
  saving.value = true
  formError.value = ''
  formSuccess.value = ''
  actionMessage.value = ''
  try {
    if (editingId.value) {
      await updateUser(editingId.value, {
        realName: form.realName.trim(),
        phone: form.phone.trim() || null,
        role: form.role,
        status: form.status,
      })
      formSuccess.value = '用户信息已更新'
    } else {
      await createUser({
        username: form.username.trim(),
        password: form.password,
        realName: form.realName.trim(),
        phone: form.phone.trim() || null,
        role: form.role,
      })
      formSuccess.value = '用户已创建'
    }
    resetForm()
    await loadUsers()
  } catch (exception) {
    formError.value = exception.message || '用户保存失败'
  } finally {
    saving.value = false
  }
}

async function submitResetPassword() {
  if (!passwordForm.userId) {
    return
  }
  actionLoadingId.value = passwordForm.userId
  listError.value = ''
  actionMessage.value = ''
  try {
    await resetUserPassword(passwordForm.userId, passwordForm.password)
    actionMessage.value = `账号 ${passwordForm.username} 的密码已重置`
    closeResetPassword()
  } catch (exception) {
    listError.value = exception.message || '重置密码失败'
  } finally {
    actionLoadingId.value = null
  }
}

async function runAction(action, user, message) {
  if (actionLoadingId.value) {
    return
  }
  formError.value = ''
  formSuccess.value = ''
  listError.value = ''
  actionMessage.value = ''
  actionLoadingId.value = user.id
  try {
    await action(user.id)
    actionMessage.value = message
    await loadUsers()
  } catch (exception) {
    listError.value = exception.message || '操作失败，请稍后重试'
  } finally {
    actionLoadingId.value = null
  }
}

async function loadPage() {
  await loadBasics()
  await loadUsers()
}

onMounted(loadPage)
</script>

<template>
  <PageHeader title="用户管理" description="创建账号、审核注册申请、维护角色状态和重置密码" />

  <section class="admin-overview">
    <div>
      <span>账号总数</span>
      <strong>{{ statusStats.total }}</strong>
    </div>
    <div>
      <span>待审核</span>
      <strong>{{ statusStats.pending }}</strong>
    </div>
    <div>
      <span>启用中</span>
      <strong>{{ statusStats.active }}</strong>
    </div>
    <div>
      <span>已禁用</span>
      <strong>{{ statusStats.disabled }}</strong>
    </div>
  </section>

  <section class="panel">
    <h2>{{ editingId ? '编辑用户' : '创建用户' }}</h2>
    <form class="form-grid" @submit.prevent="submitSave">
      <label>
        账号
        <input v-model="form.username" :disabled="Boolean(editingId)" required autocomplete="off" />
      </label>
      <label v-if="!editingId">
        初始密码
        <input v-model="form.password" type="password" required autocomplete="new-password" />
      </label>
      <label>
        真实姓名
        <input v-model="form.realName" required />
      </label>
      <label>
        联系电话
        <input v-model="form.phone" />
      </label>
      <label>
        角色
        <select v-model="form.role">
          <option v-for="item in roles" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
        </select>
      </label>
      <label v-if="editingId">
        状态
        <select v-model="form.status">
          <option v-for="item in statuses" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
        </select>
      </label>
      <p v-if="formError" class="form-error">{{ formError }}</p>
      <p v-if="formSuccess" class="form-success">{{ formSuccess }}</p>
      <div class="form-actions">
        <button class="ghost-button" type="button" :disabled="saving" @click="resetForm">清空</button>
        <button class="primary-button" type="submit" :disabled="saving">
          {{ saving ? '保存中...' : editingId ? '保存修改' : '创建用户' }}
        </button>
      </div>
    </form>
  </section>

  <section v-if="passwordForm.userId" class="panel">
    <h2>重置密码</h2>
    <form class="form-grid" @submit.prevent="submitResetPassword">
      <label>
        账号
        <input :value="passwordForm.username" disabled />
      </label>
      <label>
        新密码
        <input v-model="passwordForm.password" type="password" required autocomplete="new-password" />
      </label>
      <div class="form-actions">
        <button class="ghost-button" type="button" @click="closeResetPassword">取消</button>
        <button class="primary-button" type="submit" :disabled="actionLoadingId === passwordForm.userId">
          {{ actionLoadingId === passwordForm.userId ? '重置中...' : '确认重置' }}
        </button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="table-toolbar">
      <div>
        <h2>用户列表</h2>
        <p>按账号、姓名、电话、角色和状态筛选系统账号。</p>
      </div>
      <div class="toolbar-actions">
        <input v-model="filters.keyword" placeholder="搜索用户" @keyup.enter="loadUsers" />
        <select v-model="filters.role" @change="loadUsers">
          <option value="">全部角色</option>
          <option v-for="item in roles" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
        </select>
        <select v-model="filters.status" @change="loadUsers">
          <option value="">全部状态</option>
          <option v-for="item in statuses" :key="item.id" :value="item.itemValue">{{ item.itemLabel }}</option>
        </select>
        <button class="ghost-button" type="button" :disabled="loading" @click="loadUsers">
          {{ loading ? '刷新中...' : '查询' }}
        </button>
      </div>
    </div>

    <p v-if="listError" class="form-error table-message">{{ listError }}</p>
    <p v-if="actionMessage" class="form-success table-message">{{ actionMessage }}</p>

    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>账号</th>
            <th>姓名</th>
            <th>联系电话</th>
            <th>角色</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7">
              <div class="table-state">正在加载用户列表...</div>
            </td>
          </tr>
          <template v-else>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.username }}</td>
              <td>{{ user.realName }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>{{ optionText(roles, user.role) }}</td>
              <td>
                <span class="status-tag" :data-status="user.status">{{ optionText(statuses, user.status) }}</span>
              </td>
              <td>{{ user.createdAt || '-' }}</td>
              <td>
                <div class="row-actions">
                  <button class="link-button" type="button" @click="editUser(user)">编辑</button>
                  <button class="link-button" type="button" @click="openResetPassword(user)">重置密码</button>
                  <button
                    v-if="user.status === 'PENDING'"
                    class="link-button"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="runAction(approveUser, user, '用户已通过审核')"
                  >
                    通过
                  </button>
                  <button
                    v-if="user.status === 'PENDING'"
                    class="link-button danger-link"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="runAction(rejectUser, user, '用户已拒绝')"
                  >
                    拒绝
                  </button>
                  <button
                    v-if="user.status === 'ACTIVE'"
                    class="link-button danger-link"
                    type="button"
                    :disabled="actionLoadingId === user.id || isCurrentUser(user)"
                    :title="isCurrentUser(user) ? '不能禁用当前登录账号' : ''"
                    @click="runAction(disableUser, user, '用户已禁用')"
                  >
                    {{ isCurrentUser(user) ? '当前账号' : '禁用' }}
                  </button>
                  <button
                    v-if="user.status === 'DISABLED' || user.status === 'REJECTED'"
                    class="link-button"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="runAction(enableUser, user, '用户已启用')"
                  >
                    启用
                  </button>
                </div>
              </td>
            </tr>
          </template>
          <tr v-if="!loading && !listError && users.length === 0">
            <td colspan="7">
              <div class="table-state">暂无符合条件的用户</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
