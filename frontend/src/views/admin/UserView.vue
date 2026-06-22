<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { approveUser, createUser, disableUser, enableUser, listUsers, rejectUser } from '@/api/user'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const users = ref([])
const loading = ref(false)
const saving = ref(false)
const formError = ref('')
const formSuccess = ref('')
const listError = ref('')
const actionMessage = ref('')
const actionLoadingId = ref(null)
const filterStatus = ref('')
const form = reactive({
  username: '',
  password: '123456',
  realName: '',
  phone: '',
  role: 'REPORTER',
})

const filteredUsers = computed(() => {
  if (!filterStatus.value) {
    return users.value
  }
  return users.value.filter((user) => user.status === filterStatus.value)
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

function statusText(status) {
  const map = {
    PENDING: '待审核',
    ACTIVE: '启用',
    REJECTED: '已拒绝',
    DISABLED: '禁用',
  }
  return map[status] || status
}

function roleText(role) {
  const map = {
    ADMIN: '系统管理员',
    LAB_MANAGER: '实验室管理员',
    REPAIRER: '维修人员',
    REPORTER: '报修人员',
  }
  return map[role] || role
}

async function loadUsers() {
  loading.value = true
  listError.value = ''
  try {
    const response = await listUsers()
    users.value = response.data || []
  } catch (exception) {
    users.value = []
    listError.value = exception.message || '用户列表加载失败'
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  saving.value = true
  formError.value = ''
  formSuccess.value = ''
  actionMessage.value = ''
  try {
    await createUser(form)
    formSuccess.value = '用户已创建'
    form.username = ''
    form.password = '123456'
    form.realName = ''
    form.phone = ''
    form.role = 'REPORTER'
    await loadUsers()
  } catch (exception) {
    formError.value = exception.message || '用户创建失败'
  } finally {
    saving.value = false
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

onMounted(loadUsers)
</script>

<template>
  <PageHeader title="用户管理" description="创建系统账号，审核普通用户注册申请" />

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
    <h2>创建用户</h2>
    <form class="form-grid" @submit.prevent="submitCreate">
      <label>
        账号
        <input v-model="form.username" autocomplete="off" />
      </label>
      <label>
        初始密码
        <input v-model="form.password" type="password" autocomplete="new-password" />
      </label>
      <label>
        真实姓名
        <input v-model="form.realName" />
      </label>
      <label>
        联系电话
        <input v-model="form.phone" />
      </label>
      <label>
        角色
        <select v-model="form.role">
          <option value="ADMIN">系统管理员</option>
          <option value="LAB_MANAGER">实验室管理员</option>
          <option value="REPAIRER">维修人员</option>
          <option value="REPORTER">报修人员</option>
        </select>
      </label>
      <p v-if="formError" class="form-error">{{ formError }}</p>
      <p v-if="formSuccess" class="form-success">{{ formSuccess }}</p>
      <div class="form-actions">
        <button class="primary-button" type="submit" :disabled="saving">
          {{ saving ? '创建中...' : '创建用户' }}
        </button>
      </div>
    </form>
  </section>

  <section class="panel">
    <div class="table-toolbar">
      <div>
        <h2>用户列表</h2>
        <p>按状态筛选注册申请和系统账号</p>
      </div>
      <div class="toolbar-actions">
        <button class="ghost-button" type="button" :disabled="loading" @click="loadUsers">
          {{ loading ? '刷新中...' : '刷新列表' }}
        </button>
        <select v-model="filterStatus">
          <option value="">全部状态</option>
          <option value="PENDING">待审核</option>
          <option value="ACTIVE">启用</option>
          <option value="REJECTED">已拒绝</option>
          <option value="DISABLED">禁用</option>
        </select>
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
            <tr v-for="user in filteredUsers" :key="user.id">
              <td>{{ user.username }}</td>
              <td>{{ user.realName }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>{{ roleText(user.role) }}</td>
              <td>
                <span class="status-tag" :data-status="user.status">{{ statusText(user.status) }}</span>
              </td>
              <td>{{ user.createdAt || '-' }}</td>
              <td>
                <div class="row-actions">
                  <button
                    v-if="user.status === 'PENDING'"
                    class="link-button"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="runAction(approveUser, user, '用户已通过审核')"
                  >
                    {{ actionLoadingId === user.id ? '处理中' : '通过' }}
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
          <tr v-if="!loading && !listError && filteredUsers.length === 0">
            <td colspan="7">
              <div class="table-state">暂无符合条件的用户</div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
