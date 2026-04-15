<template>
  <div class="home">
    <header class="header">
      <h1>ESP32-S3老年人健康助手</h1>
      <p>社区工作人员远程监控与管理平台</p>
    </header>

    <div class="dashboard">
      <div class="stats-grid">
        <!-- 今日提醒统计 -->
        <div class="stat-card">
          <div class="stat-icon">⏰</div>
          <div class="stat-content">
            <h3>今日提醒</h3>
            <p class="stat-number">{{ reminderStore.todayReminders.length }}</p>
            <p class="stat-desc">待处理</p>
          </div>
        </div>

        <!-- 紧急事件统计 -->
        <div class="stat-card critical" v-if="emergencyStore.criticalEmergencies.length > 0">
          <div class="stat-icon">🚨</div>
          <div class="stat-content">
            <h3>紧急事件</h3>
            <p class="stat-number">{{ emergencyStore.criticalEmergencies.length }}</p>
            <p class="stat-desc">需立即处理</p>
          </div>
        </div>

        <!-- 设备状态 -->
        <div class="stat-card">
          <div class="stat-icon">📱</div>
          <div class="stat-content">
            <h3>设备状态</h3>
            <p class="stat-status" :class="{ online: deviceOnline }">
              {{ deviceOnline ? '在线' : '离线' }}
            </p>
            <p class="stat-desc">esp32-01</p>
          </div>
        </div>

        <!-- AI对话统计 -->
        <div class="stat-card">
          <div class="stat-icon">💬</div>
          <div class="stat-content">
            <h3>今日对话</h3>
            <p class="stat-number">{{ todayChats }}</p>
            <p class="stat-desc">次</p>
          </div>
        </div>
      </div>

      <!-- 实时告警 -->
      <div class="alerts-section" v-if="emergencyStore.recentAlerts.length > 0">
        <h2>实时告警</h2>
        <div class="alerts-container">
          <div
            v-for="alert in emergencyStore.recentAlerts"
            :key="alert.id"
            class="alert-item"
            :class="alert.level"
            @click="handleAlertClick(alert)"
          >
            <div class="alert-header">
              <span class="alert-level">{{ formatLevel(alert.level) }}</span>
              <span class="alert-time">{{ formatTime(alert.timestamp) }}</span>
            </div>
            <div class="alert-message">{{ alert.message }}</div>
            <div class="alert-actions">
              <button @click.stop="viewDetails(alert)">查看详情</button>
              <button @click.stop="dismissAlert(alert.id)">忽略</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 今日提醒 -->
      <div class="reminders-section">
        <h2>今日提醒</h2>
        <div class="reminders-container">
          <div
            v-for="reminder in reminderStore.todayReminders"
            :key="reminder.id"
            class="reminder-item"
            :class="reminder.priority"
          >
            <div class="reminder-time">
              {{ formatReminderTime(reminder.reminderTime) }}
            </div>
            <div class="reminder-content">
              <h4>{{ reminder.content }}</h4>
              <p v-if="reminder.note">备注: {{ reminder.note }}</p>
            </div>
            <div class="reminder-status">
              <span :class="reminder.status">{{ formatStatus(reminder.status) }}</span>
            </div>
            <div class="reminder-actions">
              <button @click="triggerReminder(reminder.id)">立即触发</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 快速操作 -->
      <div class="quick-actions">
        <h2>快速操作</h2>
        <div class="actions-grid">
          <button class="action-btn" @click="$router.push('/reminders')">
            <span class="action-icon">➕</span>
            <span>添加提醒</span>
          </button>
          <button class="action-btn" @click="startChat">
            <span class="action-icon">💬</span>
            <span>开始对话</span>
          </button>
          <button class="action-btn" @click="viewReports">
            <span class="action-icon">📊</span>
            <span>查看报告</span>
          </button>
          <button class="action-btn" @click="viewVideo">
            <span class="action-icon">📹</span>
            <span>查看监控</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReminderStore } from '@/stores/reminder'
import { useEmergencyStore } from '@/stores/emergency'
import wsService from '@/services/websocket'

const router = useRouter()
const reminderStore = useReminderStore()
const emergencyStore = useEmergencyStore()

// 响应式数据
const deviceOnline = ref(true)
const todayChats = ref(0)

// 计算属性
const formatLevel = (level: string) => {
  const levels: Record<string, string> = {
    critical: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return levels[level] || level
}

const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatReminderTime = (time: string) => {
  return time.split('T')[1]?.substring(0, 5) || time.substring(0, 5)
}

const formatStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '待触发',
    triggered: '已触发',
    acknowledged: '已确认'
  }
  return statusMap[status] || status
}

// 方法
const handleAlertClick = (alert: any) => {
  router.push({
    name: 'emergencies',
    query: { highlight: alert.emergencyId }
  })
}

const viewDetails = (alert: any) => {
  // 查看详情逻辑
  console.log('查看告警详情:', alert)
}

const dismissAlert = (id: number) => {
  emergencyStore.removeAlert(id)
}

const triggerReminder = async (id: number) => {
  try {
    await reminderStore.triggerReminder(id)
  } catch (error) {
    console.error('触发提醒失败:', error)
  }
}

const startChat = () => {
  router.push('/chats')
}

const viewReports = () => {
  router.push('/reports')
}

const viewVideo = () => {
  window.open('http://localhost:8080/video/esp32-01/live', '_blank')
}

// 生命周期
onMounted(() => {
  // 初始化数据
  reminderStore.fetchReminders('esp32-01')
  emergencyStore.fetchEmergencies('esp32-01')

  // 初始化WebSocket
  wsService.connectToAlerts()
  reminderStore.initWebSocket()
  emergencyStore.initWebSocket()

  // 请求通知权限
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission()
  }
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.header p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.critical {
  border-left: 5px solid #ff4757;
  background: #ffeaea;
}

.stat-icon {
  font-size: 3rem;
}

.stat-content h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 1.1rem;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  margin: 5px 0;
  color: #667eea;
}

.stat-status {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 5px 0;
}

.stat-status.online {
  color: #2ed573;
}

.stat-status.offline {
  color: #ff4757;
}

.stat-desc {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.alerts-section,
.reminders-section,
.quick-actions {
  background: white;
  border-radius: 10px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.alerts-section h2,
.reminders-section h2,
.quick-actions h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

.alerts-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.alert-item {
  border-left: 4px solid;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.alert-item:hover {
  transform: translateX(5px);
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.1);
}

.alert-item.critical {
  border-left-color: #ff4757;
  background: #ffeaea;
}

.alert-item.high {
  border-left-color: #ffa502;
  background: #fff3e0;
}

.alert-item.medium {
  border-left-color: #2ed573;
  background: #e8f8f0;
}

.alert-item.low {
  border-left-color: #3742fa;
  background: #e8f4ff;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.alert-level {
  font-weight: bold;
  text-transform: uppercase;
  font-size: 0.9rem;
}

.alert-time {
  color: #666;
  font-size: 0.9rem;
}

.alert-message {
  margin-bottom: 10px;
  font-size: 1.1rem;
}

.alert-actions {
  display: flex;
  gap: 10px;
}

.alert-actions button {
  padding: 5px 15px;
  border: none;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.alert-actions button:hover {
  background: #f0f0f0;
}

.reminders-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reminder-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  border-radius: 8px;
  background: #f8f9fa;
  transition: background 0.3s ease;
}

.reminder-item:hover {
  background: #e9ecef;
}

.reminder-item.high {
  border-left: 4px solid #ff4757;
}

.reminder-item.normal {
  border-left: 4px solid #2ed573;
}

.reminder-item.low {
  border-left: 4px solid #3742fa;
}

.reminder-time {
  min-width: 80px;
  text-align: center;
  font-weight: bold;
  color: #667eea;
  font-size: 1.2rem;
}

.reminder-content {
  flex: 1;
}

.reminder-content h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.reminder-content p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.reminder-status span {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
}

.reminder-status .pending {
  background: #ffeaa7;
  color: #e17055;
}

.reminder-status .triggered {
  background: #a29bfe;
  color: white;
}

.reminder-status .acknowledged {
  background: #55efc4;
  color: white;
}

.reminder-actions button {
  padding: 5px 15px;
  border: 1px solid #667eea;
  border-radius: 4px;
  background: white;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reminder-actions button:hover {
  background: #667eea;
  color: white;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  border: 2px dashed #ddd;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  border-color: #667eea;
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.2);
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.action-btn span:last-child {
  font-size: 1.1rem;
  color: #333;
  font-weight: bold;
}
</style>
