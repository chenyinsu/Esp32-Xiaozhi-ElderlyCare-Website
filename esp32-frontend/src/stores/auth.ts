import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'
import { storage } from '@/utils/storage'
import type { User, LoginResponse } from '@/api/type'

export const useAuthStore = defineStore('auth', () => {
const user = ref<User | null>(storage.getUser())
const token = ref<string | null>(storage.getToken())

const isLoggedIn = computed(() => !!token.value)
const isAdmin = computed(() =>
user.value?.role === 'STAFF' || user.value?.role === 'COMMUNITY'
)

async function login(username: string, password: string): Promise<LoginResponse> {
    const res = await userApi.login({ username, password })
    token.value = res.token
    user.value = {
      id: res.userId,
      username: res.username,
      name: res.name,
      phone: res.phone,
      role: res.role,
      isActive: true,
      createdAt: '',
      updatedAt: ''
    }
    storage.setToken(res.token)
    storage.setUser(user.value)
    return res
  }

  async function logout(): Promise<void> {
    try {
      await userApi.logout()
    } finally {
      token.value = null
      user.value = null
      storage.clear()
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    login,
    logout
  }
})
