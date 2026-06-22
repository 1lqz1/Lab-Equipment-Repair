<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { getProfile, updateProfile, uploadAvatar } from '@/api/profile'
import PageHeader from '@/components/PageHeader.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const uploading = ref(false)
const error = ref('')
const success = ref('')
const form = reactive({
  realName: '',
  phone: '',
})

const avatarUrl = computed(() => {
  const path = authStore.user?.avatarPath
  if (!path) {
    return ''
  }
  return path.startsWith('http') ? path : `http://localhost:8080${path}`
})

async function loadProfile() {
  const response = await getProfile()
  authStore.setUser(response.data)
  form.realName = response.data.realName || ''
  form.phone = response.data.phone || ''
}

async function submit() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const response = await updateProfile(form)
    authStore.setUser(response.data)
    success.value = '个人资料已保存'
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function onAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }
  uploading.value = true
  error.value = ''
  success.value = ''
  try {
    const response = await uploadAvatar(file)
    authStore.setUser(response.data)
    success.value = '头像已更新'
  } catch (exception) {
    error.value = exception.message
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

onMounted(loadProfile)
</script>

<template>
  <PageHeader title="个人资料" description="查看和维护当前账号信息" />

  <section class="panel profile-layout">
    <div class="avatar-block">
      <div class="avatar-preview">
        <img v-if="avatarUrl" :src="avatarUrl" alt="用户头像" />
        <span v-else>{{ authStore.user?.realName?.[0] || authStore.user?.username?.[0] || 'U' }}</span>
      </div>
      <label class="upload-button">
        {{ uploading ? '上传中...' : '上传头像' }}
        <input type="file" accept="image/png,image/jpeg,image/webp" :disabled="uploading" @change="onAvatarChange" />
      </label>
    </div>

    <form class="form-grid profile-form" @submit.prevent="submit">
      <label>
        账号
        <input :value="authStore.user?.username" disabled />
      </label>
      <label>
        角色
        <input :value="authStore.user?.role" disabled />
      </label>
      <label>
        状态
        <input :value="authStore.user?.status" disabled />
      </label>
      <label>
        真实姓名
        <input v-model="form.realName" />
      </label>
      <label>
        联系电话
        <input v-model="form.phone" />
      </label>
      <p v-if="error" class="form-error">{{ error }}</p>
      <p v-if="success" class="form-success">{{ success }}</p>
      <div class="form-actions">
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '保存中...' : '保存资料' }}
        </button>
      </div>
    </form>
  </section>
</template>
