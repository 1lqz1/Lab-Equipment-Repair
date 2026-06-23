<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const menus = computed(() => {
  const role = authStore.role
  const items = [
    { path: '/dashboard', label: '工作台', icon: '台', roles: ['ADMIN', 'LAB_MANAGER', 'REPAIRER', 'REPORTER'] },
    { path: '/orders', label: '工单列表', icon: '单', roles: ['ADMIN', 'LAB_MANAGER', 'REPAIRER', 'REPORTER'] },
    { path: '/orders/new', label: '提交报修', icon: '报', roles: ['ADMIN', 'REPORTER'] },
    { path: '/repair/tasks', label: '维修任务', icon: '修', roles: ['ADMIN', 'REPAIRER'] },
    { path: '/equipment', label: '设备管理', icon: '设', roles: ['ADMIN', 'LAB_MANAGER', 'REPORTER'] },
    { path: '/labs', label: '实验室管理', icon: '室', roles: ['ADMIN', 'LAB_MANAGER'] },
    { path: '/users', label: '用户管理', icon: '员', roles: ['ADMIN'] },
    { path: '/options', label: '系统选项', icon: '选', roles: ['ADMIN'] },
    { path: '/profile', label: '个人资料', icon: '我', roles: ['ADMIN', 'LAB_MANAGER', 'REPAIRER', 'REPORTER'] },
  ]
  return items.filter((item) => item.roles.includes(role))
})

const userInitial = computed(() => {
  const name = authStore.user?.realName || authStore.user?.username || 'U'
  return name.slice(0, 1).toUpperCase()
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
          <span>Lab Operations</span>
        </div>
      </div>

      <nav class="menu">
        <RouterLink v-for="item in menus" :key="item.path" :to="item.path">
          <span>{{ item.icon }}</span>
          <strong>{{ item.label }}</strong>
        </RouterLink>
      </nav>

      <div class="sidebar-status">
        <span>当前权限</span>
        <strong>{{ authStore.role || '未登录' }}</strong>
      </div>
    </aside>

    <section class="main-area">
      <header class="topbar">
        <div class="topbar-title">
          <span>实验室设备报修管理系统</span>
          <strong>统一运维工作台</strong>
        </div>
        <div class="topbar-user">
          <div class="user-avatar">{{ userInitial }}</div>
          <div>
            <strong>{{ authStore.user?.realName || authStore.user?.username }}</strong>
            <span>{{ authStore.role }}</span>
          </div>
          <button class="ghost-button" type="button" @click="logout">退出</button>
        </div>
      </header>

      <main class="content">
        <RouterView />
      </main>
    </section>
  </div>
</template>
