// 基础响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 用户类型
export interface User {
  id: number
  username: string
  email?: string
  phone?: string
  address?: string
  familyContacts?: string
  createdAt: string
}

// 设备类型
export interface Device {
  deviceId: string
  userId?: number
  deviceName?: string
  deviceType: string
  online: boolean
  lastSeen?: string
  ipAddress?: string
}

// 提醒类型
export interface Reminder {
  id: number
  deviceId: string
  userId?: number
  content: string
  reminderTime: string
  repeat: 'once' | 'daily' | 'weekly' | 'monthly'
  enabled: boolean
  note?: string
  createdAt: string
  nextTriggerTime?: string
  lastTriggered?: string
  status: 'pending' | 'triggered' | 'acknowledged'
  priority: 'low' | 'normal' | 'high'
}

// 紧急事件类型
export interface Emergency {
  id: number
  deviceId: string
  eventType: string
  level: 'low' | 'medium' | 'high' | 'critical'
  location?: string
  timestamp: string
  status: 'pending' | 'handling' | 'resolved'
  notified: boolean
  additionalInfo?: Record<string, any>
  resolvedAt?: string
}

// 聊天记录类型
export interface Chat {
  id: number
  deviceId?: string
  userId?: number
  type: 'text' | 'audio'
  content?: string
  audioUrl?: string
  answer?: string
  answerAudioUrl?: string
  analysis?: Record<string, any>
  timestamp: string
}

// 分析报告类型
export interface Report {
  id: number
  deviceId?: string
  userId?: number
  startDate: string
  endDate: string
  dataPoints: number
  moodAnalysis?: Record<string, any>
  cognitiveAnalysis?: Record<string, any>
  emotionalAnalysis?: Record<string, any>
  keyInsights?: string[]
  recommendations?: string[]
  generatedAt: string
}

// DTO类型
export interface ReminderDTO {
  userId?: number
  deviceId: string
  content: string
  reminderTime: string
  repeat?: 'once' | 'daily' | 'weekly' | 'monthly'
  enabled?: boolean
  note?: string
  priority?: 'low' | 'normal' | 'high'
}

export interface EmergencyDTO {
  deviceId: string
  eventType: string
  level: 'low' | 'medium' | 'high' | 'critical'
  location?: string
  timestamp?: string
  additionalInfo?: Record<string, any>
}

export interface ChatDTO {
  deviceId?: string
  userId?: number
  type?: 'text' | 'audio'
  content: string
  audioUrl?: string
  audioBase64?: string
}

// WebSocket消息类型
export interface WebSocketMessage {
  event: string
  [key: string]: any
}

export interface ReminderWebSocketMessage extends WebSocketMessage {
  event: 'reminder'
  reminderId: number
  content: string
  time: string
  priority: string
}

export interface EmergencyWebSocketMessage extends WebSocketMessage {
  event: 'emergency_alert'
  emergencyId: number
  deviceId: string
  level: string
  message: string
  timestamp: string
  videoUrl: string
  actions: string[]
}
