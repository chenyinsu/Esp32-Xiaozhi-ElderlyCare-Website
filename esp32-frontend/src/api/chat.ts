import request from './request'

export interface Chat {
id: number
deviceId: string
userId: number
type: string
content: string
audioUrl?: string
mood: string
sentiment: number
keywordsJson?: string
timestamp: string
}

export interface ChatRequest {
deviceId?: string
userId?: number
type: string
content: string
audioUrl?: string
mood?: string
sentiment?: number
keywordsJson?: string
}

export interface ChatQueryRequest {
deviceId?: string
userId?: number
type?: string
keyword?: string
minSentiment?: number
maxSentiment?: number
startTime?: string
endTime?: string
page?: number
size?: number
}

export interface PageResponse<T> {
content: T[]
page: number
size: number
totalElements: number
totalPages: number
first: boolean
last: boolean
empty: boolean
}

export const chatApi = {
// 创建聊天记录
create(data: ChatRequest): Promise<Chat> {
    return request.post('/chats', data)
  },

  // 获取聊天记录详情
  getById(id: number): Promise<Chat> {
    return request.get(`/chats/${id}`)
  },

  // 获取设备的聊天记录
  getByDeviceId(deviceId: string): Promise<Chat[]> {
    return request.get(`/chats/device/${deviceId}`)
  },

  // 获取用户的聊天记录
  getByUserId(userId: number): Promise<Chat[]> {
    return request.get(`/chats/user/${userId}`)
  },

  // 分页查询聊天记录
  query(params: ChatQueryRequest): Promise<PageResponse<Chat>> {
    return request.post('/chats/query', params)
  },

  // 按时间范围查询
  getByTimeRange(startTime: string, endTime: string): Promise<Chat[]> {
    return request.get('/chats/time-range', {
      params: { startTime, endTime }
    })
  },

  // 删除聊天记录
  delete(id: number): Promise<void> {
    return request.delete(`/chats/${id}`)
  },

  // 删除设备的所有聊天记录
  deleteByDeviceId(deviceId: string): Promise<void> {
    return request.delete(`/chats/device/${deviceId}`)
  }
}
