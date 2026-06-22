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
        <span class="auth-kicker">Account Application</span>
        <strong>普通用户注册</strong>
        <p>注册申请会进入待审核状态，管理员审核通过后才能登录系统并提交报修。</p>
        <div class="auth-flow" aria-label="注册流程">
          <span>填写资料</span>
          <span>提交申请</span>
          <span>管理员审核</span>
          <span>启用账号</span>
        </div>
        <div class="auth-note">
          管理员创建的四类用户可直接启用；普通用户自助注册只创建报修人员账号。
        </div>
      </div>

      <form class="form-card auth-card" @submit.prevent="submit">
        <div class="auth-card-header">
          <span>注册申请</span>
          <h1>创建报修账号</h1>
          <p>请填写真实资料，便于后续工单联系和审核。</p>
        </div>
        <label>
          账号
          <input v-model="form.username" autocomplete="username" placeholder="设置登录账号" />
        </label>
        <label>
          密码
          <input
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            placeholder="至少 6 位密码"
          />
        </label>
        <label>
          真实姓名
          <input v-model="form.realName" autocomplete="name" placeholder="请输入真实姓名" />
        </label>
        <label>
          联系电话
          <input v-model="form.phone" autocomplete="tel" placeholder="请输入联系电话" />
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
