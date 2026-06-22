import { computed } from 'vue'

import { useAuthStore } from '@/stores/auth'

export function usePermission() {
  const authStore = useAuthStore()

  const isAdmin = computed(() => authStore.role === 'ADMIN')
  const isManager = computed(() => ['ADMIN', 'LAB_MANAGER'].includes(authStore.role))
  const isRepairer = computed(() => ['ADMIN', 'REPAIRER'].includes(authStore.role))
  const isReporter = computed(() => ['ADMIN', 'REPORTER'].includes(authStore.role))

  function hasRole(roles) {
    return roles.includes(authStore.role)
  }

  return {
    isAdmin,
    isManager,
    isRepairer,
    isReporter,
    hasRole,
  }
}
