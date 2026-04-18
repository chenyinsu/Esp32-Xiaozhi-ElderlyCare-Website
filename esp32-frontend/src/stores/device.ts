import { defineStore } from 'pinia'
import { ref } from 'vue'
import { deviceApi } from '@/api'
import type { Device } from '@/api/type'

export const useDeviceStore = defineStore('device', () => {
const devices = ref<Device[]>([])
const onlineDevices = ref<Device[]>([])
const currentDevice = ref<Device | null>(null)
const loading = ref(false)

async function fetchDevices(params?: any) {
    loading.value = true
    try {
      const res = await deviceApi.getList(params)
      devices.value = res.content
      return res
    } finally {
      loading.value = false
    }
  }

  async function fetchOnlineDevices() {
    loading.value = true
    try {
      onlineDevices.value = await deviceApi.getOnline()
      return onlineDevices.value
    } finally {
      loading.value = false
    }
  }

  async function fetchDeviceById(id: number) {
    loading.value = true
    try {
      currentDevice.value = await deviceApi.getById(id)
      return currentDevice.value
    } finally {
      loading.value = false
    }
  }

  async function createDevice(data: Partial<Device>) {
    const device = await deviceApi.create(data)
    await fetchDevices()
    return device
  }

  async function updateDevice(id: number, data: Partial<Device>) {
    const device = await deviceApi.update(id, data)
    await fetchDevices()
    return device
  }

  async function deleteDevice(id: number) {
    await deviceApi.delete(id)
    await fetchDevices()
  }

  return {
    devices,
    onlineDevices,
    currentDevice,
    loading,
    fetchDevices,
    fetchOnlineDevices,
    fetchDeviceById,
    createDevice,
    updateDevice,
    deleteDevice
  }
})
