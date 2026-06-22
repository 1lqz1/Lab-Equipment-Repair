<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const menus = computed(() => {
  const role = authStore.role
  const items = [
    { path: '/dashboard', label: '工作台', roles: ['ADMIN', 'LAB_MANAGER', 'REPAIRER', 'REPORTER'] },
    { path: '/orders', label: '工单列表', roles: ['ADMIN', 'LAB_MANAGER', 'REPAIRER', 'REPORTER'] },
    { path: '/orders/new', label: '提交报修', roles: ['ADMIN', 'REPORTER'] },
    { path: '/repair/tasks', label: '维修任务', roles: ['ADMIN', 'REPAIRER'] },
    { path: '/equipment', label: '设备管理', roles: ['ADMIN', 'LAB_MANAGER', 'REPORTER'] },
    { path: '/labs', label: '实验室管理', roles: ['ADMIN', 'LAB_MANAGER'] },
    { path: '/users', label: '用户管理', roles: ['ADMIN', 'LAB_MANAGER'] },
  ]
  return items.filter((item) => item.roles.includes(role))
})

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">LR</div>
        <div>
          <strong>设备报修系统</strong>
          <span>Lab Repair</span>
        </div>
      </div>

      <nav class="menu">
        <RouterLink v-for="item in menus" :key="item.path" :to="item.path">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <section class="main-area">
      <header class="topbar">
        <div>
          <strong>{{ authStore.user?.realName || authStore.user?.username }}</strong>
          <span>{{ authStore.role }}</span>
        </div>
        <button class="ghost-button" type="button" @click="logout">退出</button>
      </header>

      <main class="content">
        <RouterView />
      </main>
    </section>
  </div>
</template>
