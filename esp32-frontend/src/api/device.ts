import request from './request'
import type { Device, DeviceStatistics, PageResponse } from './types'

export const deviceApi = {
// 获取设备列表
getList(params?: any): Promise<PageResponse<Device>> {
    return request.post('/devices/query', params || { page: 1, size: 20 })
  },

  // 获取在线设备
  getOnline(): Promise<Device[]> {
    return request.get('/devices/online')
  },

  // 获取设备详情
  getById(id: number): Promise<Device> {
    return request.get(`/devices/${id}`)
  },

  // 根据设备ID获取
  getByDeviceId(deviceId: string): Promise<Device> {
    return request.get(`/devices/device-id/${deviceId}`)
  },

  // 创建设备
  create(data: Partial<Device>): Promise<Device> {
    return request.post('/devices', data)
  },

  // 更新设备
  update(id: number, data: Partial<Device>): Promise<Device> {
    return request.put(`/devices/${id}`, data)
  },

  // 删除设备
  delete(id: number): Promise<void> {
    return request.delete(`/devices/${id}`)
  },

  // 获取设备统计
  getStatistics(): Promise<DeviceStatistics> {
    return request.get('/devices/statistics')
  },

  // 获取低电量设备
  getLowBattery(threshold: number = 20): Promise<Device[]> {
    return request.get('/devices/low-battery', { params: { threshold } })
  },

  // 绑定设备到用户
  bindToUser(deviceId: number, userId: number): Promise<Device> {
    return request.post(`/devices/${deviceId}/bind/${userId}`)
  },

  // 解绑设备
  unbind(deviceId: number): Promise<Device> {
    return request.post(`/devices/${deviceId}/unbind`)
  }
}
