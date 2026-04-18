// ==================== 通用类型 ====================
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data: T
  timestamp: string
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

// ==================== 用户类型 ====================
export type UserRole = 'ELDERLY' | 'COMMUNITY' | 'STAFF' | 'CHILD' | 'HOSPITAL'
export type UserGender = 'MALE' | 'FEMALE' | 'OTHER'

export interface User {
  id: number
  username: string
  name: string
  phone: string
  role: UserRole
  gender?: UserGender
  age?: number
  address?: string
  deviceId?: number
  deviceName?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface UserDetail extends User {
  idCard?: string
  emergencyPhone?: string
  medicalHistory?: string
  healthCondition?: string
  managedElders?: UserSummary[]
  emergencyContacts?: UserSummary[]
}

export interface UserSummary {
  id: number
  name: string
  phone: string
  role: UserRole
  deviceId?: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  userId: number
  username: string
  name: string
  phone: string
  role: UserRole
  token: string
  deviceId: number | null
}

export interface PasswordChangeRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface UserStatistics {
  totalUsers: number
  elderlyCount: number
  communityStaffCount: number
  staffCount: number
  childCount: number
  hospitalCount: number
  activeUsers: number
  inactiveUsers: number
  usersWithDevice: number
  usersByGender: Record<string, number>
  averageAgeByRole: Record<string, number>
}

// ==================== 设备类型 ====================
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'MAINTENANCE' | 'ERROR'
export type ButtonMode = 'SINGLE_CLICK' | 'DOUBLE_CLICK' | 'LONG_PRESS' | 'EMERGENCY'

export interface Device {
  id: number
  deviceId: string
  deviceName: string
  firmwareVersion?: string
  status: DeviceStatus
  lastOnlineTime?: string
  batteryLevel?: number
  volumeLevel: number
  hasCamera: boolean
  cameraUrl?: string
  button1Mode: ButtonMode
  button2Mode: ButtonMode
  button1Function: string
  button2Function: string
  userId?: number
  userName?: string
  latitude?: number
  longitude?: number
  wifiSsid?: string
  wifiStrength?: number
  createdAt: string
  updatedAt: string
}

export interface DeviceStatistics {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  maintenanceDevices: number
  errorDevices: number
  lowBatteryDevices: number
  unboundDevices: number
  cameraDevices: number
}

// ==================== 紧急事件类型 ====================
export type EmergencyType = 'BUTTON_PRESS' | 'FALL_DETECTION' | 'HEALTH_ABNORMAL' | 'NO_MOVEMENT' | 'OTHER'
export type EmergencyLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type EmergencyStatus = 'PENDING' | 'HANDLING' | 'RESOLVED' | 'CLOSED' | 'FALSE_ALARM'

export interface Emergency {
  id: number
  emergencyType: EmergencyType
  emergencyLevel: EmergencyLevel
  status: EmergencyStatus
  triggerTime: string
  triggerSource?: string
  description?: string
  deviceId: number
  deviceName: string
  userId: number
  userName: string
  handledById?: number
  handledByName?: string
  firstResponseTime?: string
  resolvedTime?: string
  closedTime?: string
  resolutionNote?: string
}

export interface EmergencyStatistics {
  totalEmergencies: number
  pendingCount: number
  handlingCount: number
  resolvedCount: number
  closedCount: number
  falseAlarmCount: number
  todayCount: number
  thisWeekCount: number
  averageResponseTime: number
  countByType: Record<string, number>
  countByLevel: Record<string, number>
}

// ==================== 提醒类型 ====================
export type ReminderType = 'MEDICATION' | 'ALARM' | 'ACTIVITY' | 'APPOINTMENT' | 'CUSTOM'
export type RepeatType = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'WORKDAYS' | 'WEEKENDS' | 'CUSTOM'
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY' | 'EVERYDAY'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface Reminder {
  id: number
  title: string
  content?: string
  voiceContent?: string
  reminderType: ReminderType
  repeatType: RepeatType
  repeatDays?: DayOfWeek[]
  remindTime: string
  startDate?: string
  endDate?: string
  isActive: boolean
  lastTriggered?: string
  nextTrigger?: string
  doseAmount?: string
  medicationName?: string
  isTaken: boolean
  takenTime?: string
  deviceId: number
  deviceName: string
  userId: number
  userName: string
  priority: Priority
}

export interface MedicationTracking {
  userId: number
  userName: string
  totalReminders: number
  takenCount: number
  missedCount: number
  adherenceRate: number
  dailyAdherence: Record<string, boolean>
}

// ==================== 仪表盘类型 ====================
export interface DashboardOverview {
  totalUsers: number
  totalDevices: number
  onlineDevices: number
  pendingEmergencies: number
  todayReminders: number
  unreadNotifications: number
  lowBatteryDevices: number
  todayChats: number
}

export interface RecentActivity {
  recentEmergencies: RecentEmergency[]
  recentReminders: RecentReminder[]
  recentChats: RecentChat[]
}

export interface RecentEmergency {
  id: number
  userName: string
  emergencyType: string
  emergencyLevel: string
  status: string
  triggerTime: string
}

export interface RecentReminder {
  id: number
  userName: string
  title: string
  isTaken: boolean
  remindTime: string
}

export interface RecentChat {
  id: number
  userName: string
  content: string
  mood: string
  timestamp: string
}