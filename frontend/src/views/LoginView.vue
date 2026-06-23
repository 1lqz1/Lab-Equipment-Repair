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
    error.value = exception.message || '登录失败，可能是网线被猫咬断了，也可能是密码错了。'
  } finally {
    loading.value = false
  }
}

function selectDemoUser(username) {
  form.username = username
  form.password = '123456'
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <span class="auth-kicker">Lab Disaster Control Center</span>
        <strong>救命！设备又双叒叕坏了？</strong>
        <p>没关系，深呼吸。无论是示波器开始冒烟，还是恒温培养箱决定孵小鸡，登录这个系统，我们一起用玄学和科学拯救它们！</p>
        
        <div class="auth-flow" aria-label="玄学报修流程">
          <span>
            <strong>1. 痛哭上报</strong>
            <small>发誓真不是我弄坏的</small>
          </span>
          <span>
            <strong>2. 卑微派单</strong>
            <small>管理员大大求求快派人</small>
          </span>
          <span>
            <strong>3. 玄学维修</strong>
            <small>懂不懂“拍一拍”的含金量</small>
          </span>
          <span>
            <strong>4. 微笑验收</strong>
            <small>谢天谢地它又亮起来了</small>
          </span>
        </div>

        <div class="auth-note">
          <strong>⚠️ 报修人员温馨提示：</strong><br />
          如果设备冒烟，请第一时间切断电源，而不是发朋友圈。如果是软件闪退，请尝试“重启”，这能解决 99% 的玄学问题。
        </div>
      </div>

      <form class="form-card auth-card" @submit.prevent="submit">
        <div class="auth-card-header">
          <span>SECURE ACCESS</span>
          <h1>登录应急大厅</h1>
          <p>请认领您的神秘身份（点击一键填入演示账号）：</p>
        </div>

        <div class="auth-roles" style="margin-bottom: 20px;">
          <span title="点击填入" @click="selectDemoUser('admin')">
            <strong>系统管理员</strong><br/>
            <small>(掌握生杀大权)</small>
          </span>
          <span title="点击填入" @click="selectDemoUser('manager')">
            <strong>实验室掌门</strong><br/>
            <small>(日常唠叨“轻拿轻放”)</small>
          </span>
          <span title="点击填入" @click="selectDemoUser('repairer')">
            <strong>机修大师</strong><br/>
            <small>(包治百病，专业拍机器)</small>
          </span>
          <span title="点击填入" @click="selectDemoUser('reporter')">
            <strong>日常报修人</strong><br/>
            <small>(“我发誓我就摸了一下”)</small>
          </span>
        </div>

        <label>
          特工代号 (账号)
          <input v-model="form.username" autocomplete="username" placeholder="请输入登录账号 (如: admin)" required />
        </label>
        
        <label>
          暗号 (密码)
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入登录密码 (如: 123456)"
            required
          />
        </label>
        
        <p v-if="error" class="form-error">{{ error }}</p>
        
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '正在强行连接服务器...' : '召唤维修大师 🚀' }}
        </button>
        
        <RouterLink class="form-link" to="/register">没有身份？申请加入救机行动组</RouterLink>
      </form>
    </section>
  </main>
</template>

<style scoped>
.auth-flow span strong {
  display: block;
  font-size: 13px;
  color: var(--color-primary);
}
.auth-flow span small {
  display: block;
  font-size: 10px;
  color: var(--color-muted);
  margin-top: 4px;
}
.auth-roles span {
  display: block;
  text-align: center;
  flex: 1 1 45%;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 8px;
  background: rgba(223, 199, 147, 0.02);
  cursor: pointer;
  transition: all 0.2s ease;
}
.auth-roles span:hover {
  border-color: var(--color-primary);
  background: rgba(223, 199, 147, 0.06);
}
.auth-roles span strong {
  color: var(--color-primary);
  font-size: 12px;
}
.auth-roles span small {
  font-size: 10px;
  color: var(--color-muted);
}
</style>
