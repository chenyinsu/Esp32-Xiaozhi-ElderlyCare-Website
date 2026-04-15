import type {
  WebSocketMessage,
ReminderWebSocketMessage,
EmergencyWebSocketMessage
} from '@/types'

class WebSocketService {
private deviceSocket: WebSocket | null = null
private alertSocket: WebSocket | null = null
private deviceCallbacks: Map<string, Function[]> = new Map()
private alertCallbacks: Map<string, Function[]> = new Map()

// 连接到设备WebSocket
connectToDevice(deviceId: string): void {
    if (this.deviceSocket && this.deviceSocket.readyState === WebSocket.OPEN) {
      this.deviceSocket.close()
    }

    this.deviceSocket = new WebSocket(`ws://localhost:8080/ws/device/${deviceId}`)

    this.deviceSocket.onopen = () => {
      console.log(`设备 ${deviceId} WebSocket连接已建立`)
      this.emit('device:connected', deviceId)
    }

    this.deviceSocket.onmessage = (event) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data)
        this.handleDeviceMessage(message)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    this.deviceSocket.onerror = (error) => {
      console.error('设备WebSocket错误:', error)
      this.emit('device:error', error)
    }

    this.deviceSocket.onclose = () => {
      console.log('设备WebSocket连接已关闭')
      this.emit('device:disconnected')
    }
  }

  // 连接到告警WebSocket
  connectToAlerts(): void {
    if (this.alertSocket && this.alertSocket.readyState === WebSocket.OPEN) {
      this.alertSocket.close()
    }

    this.alertSocket = new WebSocket('ws://localhost:8080/ws/alerts')

    this.alertSocket.onopen = () => {
      console.log('告警WebSocket连接已建立')
      this.emit('alert:connected')
    }

    this.alertSocket.onmessage = (event) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data)
        this.handleAlertMessage(message)
      } catch (error) {
        console.error('解析告警WebSocket消息失败:', error)
      }
    }

    this.alertSocket.onerror = (error) => {
      console.error('告警WebSocket错误:', error)
      this.emit('alert:error', error)
    }

    this.alertSocket.onclose = () => {
      console.log('告警WebSocket连接已关闭')
      this.emit('alert:disconnected')
    }
  }

  // 处理设备消息
  private handleDeviceMessage(message: WebSocketMessage): void {
    switch (message.event) {
      case 'reminder':
        this.emit('reminder', message as ReminderWebSocketMessage)
        break
      case 'chat_response':
        this.emit('chat_response', message)
        break
      case 'connected':
        this.emit('device:connected', message.deviceId)
        break
    }
  }

  // 处理告警消息
  private handleAlertMessage(message: WebSocketMessage): void {
    if (message.event === 'emergency_alert') {
      this.emit('emergency_alert', message as EmergencyWebSocketMessage)
    }
  }

  // 发送消息到设备
  sendToDevice(message: any): void {
    if (this.deviceSocket && this.deviceSocket.readyState === WebSocket.OPEN) {
      this.deviceSocket.send(JSON.stringify(message))
    } else {
      console.error('设备WebSocket未连接')
    }
  }

  // 事件订阅
  on(event: string, callback: Function): void {
    if (event.startsWith('device:')) {
      if (!this.deviceCallbacks.has(event)) {
        this.deviceCallbacks.set(event, [])
      }
      this.deviceCallbacks.get(event)!.push(callback)
    } else {
      if (!this.alertCallbacks.has(event)) {
        this.alertCallbacks.set(event, [])
      }
      this.alertCallbacks.get(event)!.push(callback)
    }
  }

  // 事件触发
  private emit(event: string, ...args: any[]): void {
    let callbacks: Function[] = []

    if (event.startsWith('device:')) {
      callbacks = this.deviceCallbacks.get(event) || []
    } else {
      callbacks = this.alertCallbacks.get(event) || []
    }

    callbacks.forEach(callback => {
      try {
        callback(...args)
      } catch (error) {
        console.error(`执行回调 ${event} 失败:`, error)
      }
    })
  }

  // 断开连接
  disconnect(): void {
    if (this.deviceSocket) {
      this.deviceSocket.close()
      this.deviceSocket = null
    }
    if (this.alertSocket) {
      this.alertSocket.close()
      this.alertSocket = null
    }
    this.deviceCallbacks.clear()
    this.alertCallbacks.clear()
  }
}

export default new WebSocketService()
