import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiService from '@/services/api'
import wsService from '@/services/websocket'
import type { Emergency, EmergencyDTO } from '@/types'

export const useEmergencyStore = defineStore('emergency', () => {
// 状态
const emergencies = ref<Emergency[]>([])
const alerts = ref<any[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// 计算属性
const pendingEmergencies = computed(() =>
emergencies.value.filter(e => e.status === 'pending')
)

const criticalEmergencies = computed(() =>
emergencies.value.filter(e => e.level === 'critical')
)

const recentAlerts = computed(() =>
alerts.value.slice(0, 10) // 最近10条告警
)

// 动作
const fetchEmergencies = async (deviceId?: string) => {
loading.value = true
error.value = null

try {
emergencies.value = await apiService.getEmergencies(deviceId)
} catch (err: any) {
error.value = err.message || '获取紧急事件失败'
console.error('获取紧急事件失败:', err)
    } finally {
      loading.value = false
    }
  }

  const createEmergency = async (emergencyData: EmergencyDTO) => {
    loading.value = true
    error.value = null

    try {
      const newEmergency = await apiService.createEmergency(emergencyData)
      emergencies.value.unshift(newEmergency)
      return newEmergency
    } catch (err: any) {
      error.value = err.message || '创建紧急事件失败'
      console.error('创建紧急事件失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateEmergencyStatus = async (id: number, status: string) => {
    loading.value = true
    error.value = null

    try {
      const updatedEmergency = await apiService.updateEmergencyStatus(id, status)
      const index = emergencies.value.findIndex(e => e.id === id)
      if (index !== -1) {
        emergencies.value[index] = updatedEmergency
      }
      return updatedEmergency
    } catch (err: any) {
      error.value = err.message || '更新紧急事件状态失败'
      console.error('更新紧急事件状态失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const notifyEmergency = async (
    id: number,
    notificationType: string,
    recipients: string,
    message: string
  ) => {
    loading.value = true
    error.value = null

    try {
      await apiService.notifyEmergency(id, notificationType, recipients, message)

      const index = emergencies.value.findIndex(e => e.id === id)
      if (index !== -1) {
        emergencies.value[index].notified = true
      }
    } catch (err: any) {
      error.value = err.message || '发送通知失败'
      console.error('发送通知失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const addAlert = (alert: any) => {
    alerts.value.unshift({
      ...alert,
      id: Date.now(),
      timestamp: new Date().toISOString()
    })
  }

  const removeAlert = (id: number) => {
    alerts.value = alerts.value.filter(alert => alert.id !== id)
  }

  // 初始化WebSocket监听
  const initWebSocket = () => {
    wsService.on('emergency_alert', (message: any) => {
      console.log('收到紧急告警:', message)
      addAlert(message)

      // 显示浏览器通知
      if (Notification.permission === 'granted') {
        new Notification('紧急告警', {
          body: message.message,
          icon: '/favicon.ico'
        })
      }
    })
  }

  return {
    // 状态
    emergencies,
    alerts,
    loading,
    error,

    // 计算属性
    pendingEmergencies,
    criticalEmergencies,
    recentAlerts,

    // 动作
    fetchEmergencies,
    createEmergency,
    updateEmergencyStatus,
    notifyEmergency,
    addAlert,
    removeAlert,
    initWebSocket
  }
})
