<script setup>
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { registerApi } from '@/api/auth'

const loading = ref(false)
const error = ref('')
const success = ref('')
const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
})

async function submit() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await registerApi(form)
    success.value = '注册申请已提交，请等待管理员审核'
    form.username = ''
    form.password = ''
    form.realName = ''
    form.phone = ''
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
        <strong>账号注册</strong>
        <span>普通用户注册后需要管理员审核，通过后才能登录系统</span>
      </div>

      <form class="form-card" @submit.prevent="submit">
        <h1>注册报修账号</h1>
        <label>
          账号
          <input v-model="form.username" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="form.password" type="password" autocomplete="new-password" />
        </label>
        <label>
          真实姓名
          <input v-model="form.realName" autocomplete="name" />
        </label>
        <label>
          联系电话
          <input v-model="form.phone" autocomplete="tel" />
        </label>
        <p v-if="error" class="form-error">{{ error }}</p>
        <p v-if="success" class="form-success">{{ success }}</p>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '提交中...' : '提交注册' }}
        </button>
        <RouterLink class="form-link" to="/login">已有账号，返回登录</RouterLink>
      </form>
    </section>
  </main>
</template>
