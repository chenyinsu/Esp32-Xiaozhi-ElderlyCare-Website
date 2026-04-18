import request from './request'
import type { Reminder, MedicationTracking, PageResponse } from './types'

export const reminderApi = {
// 获取提醒列表
getList(params?: any): Promise<PageResponse<Reminder>> {
    return request.post('/reminders/query', params || { page: 1, size: 20 })
  },

  // 获取今日提醒
  getToday(): Promise<Reminder[]> {
    return request.get('/reminders/today')
  },

  // 获取用户提醒
  getByUser(userId: number): Promise<Reminder[]> {
    return request.get(`/reminders/user/${userId}`)
  },

  // 创��提醒
  create(data: Partial<Reminder>): Promise<Reminder> {
    return request.post('/reminders', data)
  },

  // 更新提醒
  update(id: number, data: Partial<Reminder>): Promise<Reminder> {
    return request.put(`/reminders/${id}`, data)
  },

  // 删除提醒
  delete(id: number): Promise<void> {
    return request.delete(`/reminders/${id}`)
  },

  // 标记已服用
  markAsTaken(id: number): Promise<Reminder> {
    return request.patch(`/reminders/${id}/taken`)
  },

  // 获取用药追踪
  getMedicationTracking(userId: number): Promise<MedicationTracking> {
    return request.get(`/reminders/medication-tracking/${userId}`)
  },

  // 触发提醒
  trigger(id: number): Promise<void> {
    return request.post(`/reminders/${id}/trigger`)
  }
}
