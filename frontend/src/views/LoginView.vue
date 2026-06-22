<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const form = reactive({
  username: '',
  password: '',
})

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await authStore.login(form)
    router.push('/dashboard')
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <span class="auth-kicker">Lab Equipment Repair</span>
        <strong>高校实验室设备报修管理系统</strong>
        <p>统一管理设备档案、报修工单、维修处理、验收归档与用户权限。</p>
        <div class="auth-flow" aria-label="业务流程">
          <span>报修提交</span>
          <span>审核派单</span>
          <span>维修处理</span>
          <span>验收归档</span>
        </div>
        <div class="auth-roles">
          <span>系统管理员</span>
          <span>实验室管理员</span>
          <span>维修人员</span>
          <span>报修人员</span>
        </div>
      </div>

      <form class="form-card auth-card" @submit.prevent="submit">
        <div class="auth-card-header">
          <span>账号登录</span>
          <h1>进入管理后台</h1>
          <p>默认演示账号：admin / 123456</p>
        </div>
        <label>
          账号
          <input v-model="form.username" autocomplete="username" placeholder="请输入登录账号" />
        </label>
        <label>
          密码
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入登录密码"
          />
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录系统' }}
        </button>
        <RouterLink class="form-link" to="/register">没有账号，申请注册</RouterLink>
      </form>
    </section>
  </main>
</template>
