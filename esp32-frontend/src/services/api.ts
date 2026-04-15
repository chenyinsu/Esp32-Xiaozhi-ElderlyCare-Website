import axios, { AxiosInstance, AxiosResponse } from 'axios'
import type {
  Reminder, ReminderDTO,
  Emergency, EmergencyDTO,
  Chat, ChatDTO,
  Report,
  ApiResponse
} from '@/types'

class ApiService {
  private axiosInstance: AxiosInstance

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: '/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    })
  }

  // 提醒相关API
  async getReminders(deviceId?: string, userId?: number): Promise<Reminder[]> {
    const params = new URLSearchParams()
    if (deviceId) params.append('deviceId', deviceId)
    if (userId) params.append('userId', userId.toString())

    const response: AxiosResponse<ApiResponse<Reminder[]>> = await this.axiosInstance.get(
      '/reminders',
      { params }
    )
    return response.data.data
  }

  async createReminder(reminder: ReminderDTO): Promise<Reminder> {
    const response: AxiosResponse<ApiResponse<Reminder>> = await this.axiosInstance.post(
      '/reminders',
      reminder
    )
    return response.data.data
  }

  async updateReminder(id: number, reminder: Partial<ReminderDTO>): Promise<Reminder> {
    const response: AxiosResponse<ApiResponse<Reminder>> = await this.axiosInstance.put(
      `/reminders/${id}`,
      reminder
    )
    return response.data.data
  }

  async deleteReminder(id: number): Promise<void> {
    await this.axiosInstance.delete(`/reminders/${id}`)
  }

  async triggerReminder(id: number): Promise<void> {
    await this.axiosInstance.post(`/reminders/${id}/trigger`)
  }

  // 紧急事件相关API
  async getEmergencies(deviceId?: string): Promise<Emergency[]> {
    const params = new URLSearchParams()
    if (deviceId) params.append('deviceId', deviceId)

    const response: AxiosResponse<ApiResponse<Emergency[]>> = await this.axiosInstance.get(
      '/emergencies',
      { params }
    )
    return response.data.data
  }

  async createEmergency(emergency: EmergencyDTO): Promise<Emergency> {
    const response: AxiosResponse<ApiResponse<Emergency>> = await this.axiosInstance.post(
      '/emergencies',
      emergency
    )
    return response.data.data
  }

  async updateEmergencyStatus(id: number, status: string): Promise<Emergency> {
    const response: AxiosResponse<ApiResponse<Emergency>> = await this.axiosInstance.put(
      `/emergencies/${id}/status`,
      null,
      { params: { status } }
    )
    return response.data.data
  }

  async notifyEmergency(
    id: number,
    notificationType: string,
    recipients: string,
    message: string
  ): Promise<void> {
    await this.axiosInstance.post(
      `/emergencies/${id}/notify`,
      null,
      {
        params: {
          notificationType,
          recipients,
          message
        }
      }
    )
  }

  // 聊天相关API
  async getChats(deviceId?: string, userId?: number): Promise<Chat[]> {
    const params = new URLSearchParams()
    if (deviceId) params.append('deviceId', deviceId)
    if (userId) params.append('userId', userId.toString())

    const response: AxiosResponse<ApiResponse<Chat[]>> = await this.axiosInstance.get(
      '/chats',
      { params }
    )
    return response.data.data
  }

  async createChat(chat: ChatDTO): Promise<Chat> {
    const response: AxiosResponse<ApiResponse<Chat>> = await this.axiosInstance.post(
      '/chats',
      chat
    )
    return response.data.data
  }

  // 报告相关API
  async getReports(deviceId?: string, userId?: number): Promise<Report[]> {
    const params = new URLSearchParams()
    if (deviceId) params.append('deviceId', deviceId)
    if (userId) params.append('userId', userId.toString())

    const response: AxiosResponse<ApiResponse<Report[]>> = await this.axiosInstance.get(
      '/reports/psychology',
      { params }
    )
    return response.data.data
  }
}

export default new ApiService()
