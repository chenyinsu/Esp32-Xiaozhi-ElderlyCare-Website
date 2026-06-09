export interface User {
  id: number
  username: string
  name: string
  phone: string
  role: 'ELDERLY' | 'COMMUNITY' | 'STAFF' | 'CHILD' | 'HOSPITAL'
isActive: boolean
createdAt: string
updatedAt: string
}

export interface LoginResponse {
userId: number
username: string
name: string
phone: string
role: string
token: string
deviceId?: number
}

export interface Device {
id: number
deviceId: string
deviceName: string
firmwareVersion?: string
status: 'ONLINE' | 'OFFLINE' | 'MAINTENANCE' | 'ERROR'
batteryLevel: number
wifiStrength: number
lastOnlineTime: string
createdAt: string
updatedAt: string
}

export interface Emergency {
id: number
emergencyType: string
emergencyLevel: string
status: 'PENDING' | 'HANDLING' | 'RESOLVED' | 'CLOSED' | 'FALSE_ALARM'
description: string
triggerSource?: string
triggerTime: string
deviceId: number
deviceName?: string
userId: number
userName?: string
handledById?: number
handledByName?: string
resolutionNote?: string
}

export interface Reminder {
id: number
title: string
content: string
reminderType: 'MEDICATION' | 'ALARM' | 'ACTIVITY' | 'APPOINTMENT' | 'CUSTOM'
repeatType: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'WORKDAYS' | 'WEEKENDS'
remindTime: string
isActive: boolean
isTaken?: boolean
doseAmount?: string
medicationName?: string
deviceId: number
deviceName?: string
userId: number
userName?: string
createdAt: string
updatedAt: string
}

export interface Chat {
id: number
deviceId: string
userId: number
content: string
type: 'text' | 'audio'
mood: string
sentiment: number
timestamp: string
}
