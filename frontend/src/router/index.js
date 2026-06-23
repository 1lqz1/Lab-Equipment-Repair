import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import MainLayout from '@/layouts/MainLayout.vue'
import DashboardView from '@/views/DashboardView.vue'
import EquipmentView from '@/views/equipment/EquipmentView.vue'
import LabView from '@/views/lab/LabView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import OrderDetailView from '@/views/orders/OrderDetailView.vue'
import OrderListView from '@/views/orders/OrderListView.vue'
import ProfileView from '@/views/ProfileView.vue'
import SubmitOrderView from '@/views/orders/SubmitOrderView.vue'
import RepairTaskView from '@/views/repair/RepairTaskView.vue'
import UserView from '@/views/admin/UserView.vue'
import DictView from '@/views/admin/DictView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { public: true },
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: DashboardView },
        { path: 'orders', name: 'orders', component: OrderListView },
        { path: 'orders/new', name: 'submit-order', component: SubmitOrderView, meta: { roles: ['ADMIN', 'REPORTER'] } },
        { path: 'orders/:id', name: 'order-detail', component: OrderDetailView },
        { path: 'repair/tasks', name: 'repair-tasks', component: RepairTaskView, meta: { roles: ['ADMIN', 'REPAIRER'] } },
        { path: 'equipment', name: 'equipment', component: EquipmentView },
        { path: 'labs', name: 'labs', component: LabView, meta: { roles: ['ADMIN', 'LAB_MANAGER'] } },
        { path: 'users', name: 'users', component: UserView, meta: { roles: ['ADMIN'] } },
        { path: 'dicts', name: 'dicts', component: DictView, meta: { roles: ['ADMIN'] } },
        { path: 'profile', name: 'profile', component: ProfileView },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (!to.meta.public && !authStore.isLoggedIn) {
    return '/login'
  }
  if (to.meta.public && authStore.isLoggedIn) {
    return '/dashboard'
  }
  if (to.meta.roles && !to.meta.roles.includes(authStore.role)) {
    return '/dashboard'
  }
  return true
})

export default router
