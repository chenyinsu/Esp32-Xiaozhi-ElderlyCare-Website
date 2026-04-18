import request from './request'
import type { Emergency, EmergencyStatistics, PageResponse, EmergencyStatus } from './types'

export const emergencyApi = {
// 获取紧急事件列表
getList(params?: any): Promise<PageResponse<Emergency>> {
    return request.post('/emergencies/query', params || { page: 1, size: 20 })
  },

  // 获取待处理事件
  getPending(): Promise<Emergency[]> {
    return request.get('/emergencies/pending')
  },

  // 获取事件详情
  getById(id: number): Promise<Emergency> {
    return request.get(`/emergencies/${id}`)
  },

  // 获取统计信息
  getStatistics(): Promise<EmergencyStatistics> {
    return request.get('/emergencies/statistics')
  },

  // 获取最近事件
  getRecent(hours: number = 24): Promise<Emergency[]> {
    return request.get('/emergencies/recent', { params: { hours } })
  },

  // 更新事件状态
  updateStatus(id: number, status: EmergencyStatus, handledById: number, resolutionNote?: string): Promise<Emergency> {
    return request.patch(`/emergencies/${id}/status`, { status, handledById, resolutionNote })
  },

  // 删除事件
  delete(id: number): Promise<void> {
    return request.delete(`/emergencies/${id}`)
  }
}
