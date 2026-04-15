import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiService from '@/services/api'
import wsService from '@/services/websocket'
import type { Reminder, ReminderDTO } from '@/types'

export const useReminderStore = defineStore('reminder', () => {
// 状态
const reminders = ref<Reminder[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// 计算属性
const pendingReminders = computed(() =>
reminders.value.filter(r => r.status === 'pending')
)

const highPriorityReminders = computed(() =>
reminders.value.filter(r => r.priority === 'high')
)

const todayReminders = computed(() => {
const today = new Date().toISOString().split('T')[0]
return reminders.value.filter(r =>
      r.reminderTime.startsWith(today) && r.enabled
)
})

// 动作
const fetchReminders = async (deviceId?: string, userId?: number) => {
loading.value = true
error.value = null

try {
reminders.value = await apiService.getReminders(deviceId, userId)
} catch (err: any) {
error.value = err.message || '获取提醒失败'
console.error('获取提醒失败:', err)
    } finally {
      loading.value = false
    }
  }

  const createReminder = async (reminderData: ReminderDTO) => {
    loading.value = true
    error.value = null

    try {
      const newReminder = await apiService.createReminder(reminderData)
      reminders.value.push(newReminder)
      return newReminder
    } catch (err: any) {
      error.value = err.message || '创建提醒失败'
      console.error('创建提醒失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateReminder = async (id: number, reminderData: Partial<ReminderDTO>) => {
    loading.value = true
    error.value = null

    try {
      const updatedReminder = await apiService.updateReminder(id, reminderData)
      const index = reminders.value.findIndex(r => r.id === id)
      if (index !== -1) {
        reminders.value[index] = updatedReminder
      }
      return updatedReminder
    } catch (err: any) {
      error.value = err.message || '更新提醒失败'
      console.error('更新提醒失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteReminder = async (id: number) => {
    loading.value = true
    error.value = null

    try {
      await apiService.deleteReminder(id)
      reminders.value = reminders.value.filter(r => r.id !== id)
    } catch (err: any) {
      error.value = err.message || '删除提醒失败'
      console.error('删除提醒失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const triggerReminder = async (id: number) => {
    try {
      await apiService.triggerReminder(id)
      const index = reminders.value.findIndex(r => r.id === id)
      if (index !== -1) {
        reminders.value[index].status = 'triggered'
        reminders.value[index].lastTriggered = new Date().toISOString()
      }
    } catch (err: any) {
      error.value = err.message || '触发提醒失败'
      console.error('触发提醒失败:', err)
      throw err
    }
  }

  // 初始化WebSocket监听
  const initWebSocket = () => {
    wsService.on('reminder', (message: any) => {
      console.log('收到提醒WebSocket消息:', message)
      // 可以在这里触发UI通知
    })
  }

  return {
    // 状态
    reminders,
    loading,
    error,

    // 计算属性
    pendingReminders,
    highPriorityReminders,
    todayReminders,

    // 动作
    fetchReminders,
    createReminder,
    updateReminder,
    deleteReminder,
    triggerReminder,
    initWebSocket
  }
})
