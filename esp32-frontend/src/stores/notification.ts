import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface Notification {
id: string
title: string
message: string
type: 'info' | 'success' | 'warning' | 'error'
timestamp: number
read: boolean
}

export const useNotificationStore = defineStore('notification', () => {
const notifications = ref<Notification[]>([])
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

function add(notification: Omit<Notification, 'id' | 'timestamp' | 'read'>) {
    const id = Date.now().toString() + Math.random().toString(36).substr(2, 9)
    notifications.value.unshift({
      ...notification,
      id,
      timestamp: Date.now(),
      read: false
    })
  }

  function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  function markAllAsRead() {
    notifications.value.forEach(n => n.read = true)
  }

  function remove(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  function clearAll() {
    notifications.value = []
  }

  // 快捷方法
  function info(title: string, message: string) {
    add({ title, message, type: 'info' })
  }

  function success(title: string, message: string) {
    add({ title, message, type: 'success' })
  }

  function warning(title: string, message: string) {
    add({ title, message, type: 'warning' })
  }

  function error(title: string, message: string) {
    add({ title, message, type: 'error' })
  }

  return {
    notifications,
    unreadCount,
    add,
    markAsRead,
    markAllAsRead,
    remove,
    clearAll,
    info,
    success,
    warning,
    error
  }
})
