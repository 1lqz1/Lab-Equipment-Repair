<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { approveUser, createUser, disableUser, enableUser, listUsers, rejectUser } from '@/api/user'
import PageHeader from '@/components/PageHeader.vue'

const users = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
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
  error.value = ''
  try {
    const response = await listUsers()
    users.value = response.data || []
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await createUser(form)
    success.value = '用户已创建'
    form.username = ''
    form.password = '123456'
    form.realName = ''
    form.phone = ''
    form.role = 'REPORTER'
    await loadUsers()
  } catch (exception) {
    error.value = exception.message
  } finally {
    saving.value = false
  }
}

async function runAction(action, user, message) {
  error.value = ''
  success.value = ''
  try {
    await action(user.id)
    success.value = message
    await loadUsers()
  } catch (exception) {
    error.value = exception.message
  }
}

onMounted(loadUsers)
</script>

<template>
  <PageHeader title="用户管理" description="创建系统账号，审核普通用户注册申请" />

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
      <p v-if="error" class="form-error">{{ error }}</p>
      <p v-if="success" class="form-success">{{ success }}</p>
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
      <select v-model="filterStatus">
        <option value="">全部状态</option>
        <option value="PENDING">待审核</option>
        <option value="ACTIVE">启用</option>
        <option value="REJECTED">已拒绝</option>
        <option value="DISABLED">禁用</option>
      </select>
    </div>

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
            <td colspan="7">加载中...</td>
          </tr>
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
                  @click="runAction(approveUser, user, '用户已通过审核')"
                >
                  通过
                </button>
                <button
                  v-if="user.status === 'PENDING'"
                  class="link-button danger-link"
                  type="button"
                  @click="runAction(rejectUser, user, '用户已拒绝')"
                >
                  拒绝
                </button>
                <button
                  v-if="user.status === 'ACTIVE'"
                  class="link-button danger-link"
                  type="button"
                  @click="runAction(disableUser, user, '用户已禁用')"
                >
                  禁用
                </button>
                <button
                  v-if="user.status === 'DISABLED' || user.status === 'REJECTED'"
                  class="link-button"
                  type="button"
                  @click="runAction(enableUser, user, '用户已启用')"
                >
                  启用
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && filteredUsers.length === 0">
            <td colspan="7">暂无用户</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
