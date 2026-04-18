import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export function useAuth() {
  const authStore = useAuthStore()

  const isLoggedIn = computed(() => !!authStore.token)
  const currentUser = computed(() => authStore.user)
  const isAdmin = computed(() =>
    authStore.user?.role === 'STAFF' || authStore.user?.role === 'COMMUNITY'
)

const hasPermission = (roles: string[]) => {
if (!authStore.user) return false
    return roles.includes(authStore.user.role)
  }

  return {
    isLoggedIn,
    currentUser,
    isAdmin,
    hasPermission,
    login: authStore.login,
    logout: authStore.logout
  }
}
