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
        <strong>高校实验室设备报修管理系统</strong>
        <span>统一报修、派单、维修、验收和归档</span>
      </div>

      <form class="form-card" @submit.prevent="submit">
        <h1>登录</h1>
        <label>
          账号
          <input v-model="form.username" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="form.password" type="password" autocomplete="current-password" />
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录系统' }}
        </button>
      </form>
    </section>
  </main>
</template>
