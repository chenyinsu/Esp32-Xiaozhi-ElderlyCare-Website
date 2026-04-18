import { ref, onUnmounted } from 'vue'

interface WebSocketOptions {
url: string
onMessage?: (data: any) => void
  onOpen?: () => void
  onClose?: () => void
  onError?: (error: Event) => void
  reconnectInterval?: number
  maxReconnectAttempts?: number
}

export function useWebSocket(options: WebSocketOptions) {
  const {
    url,
    onMessage,
    onOpen,
    onClose,
    onError,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5
  } = options

  const connected = ref(false)
  const reconnectAttempts = ref(0)
  let ws: WebSocket | null = null
  let reconnectTimer: number | null = null

  const connect = () => {
    ws = new WebSocket(url)

    ws.onopen = () => {
      connected.value = true
      reconnectAttempts.value = 0
      onOpen?.()
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        onMessage?.(data)
      } catch {
        onMessage?.(event.data)
      }
    }

    ws.onclose = () => {
      connected.value = false
      onClose?.()

      if (reconnectAttempts.value < maxReconnectAttempts) {
        reconnectTimer = window.setTimeout(() => {
          reconnectAttempts.value++
          connect()
        }, reconnectInterval)
      }
    }

    ws.onerror = (error) => {
      onError?.(error)
    }
  }

  const send = (data: any) => {
    if (ws && connected.value) {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      ws.send(message)
    }
  }

  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.close()
      ws = null
    }
    connected.value = false
  }

  onUnmounted(() => {
    disconnect()
  })

  connect()

  return {
    connected,
    send,
    disconnect,
    reconnect: connect
  }
}
