<template>
  <div class="robot-control-page">
    <el-row :gutter="24">
      <el-col :span="16">
        <el-card class="glass-card control-panel">
          <template #header><span>🤖 机器人控制台</span></template>
          <div class="control-grid">
            <div class="direction-pad">
              <el-button class="dir-btn" @click="sendCommand('forward')" :loading="cmdLoading">
                <el-icon><CaretTop /></el-icon> 前进
              </el-button>
              <div class="mid-row">
                <el-button class="dir-btn" @click="sendCommand('left')"><el-icon><CaretLeft /></el-icon> 左转</el-button>
                <el-button class="dir-btn stop" type="danger" @click="sendCommand('stop')"><el-icon><Close /></el-icon> 停止</el-button>
                <el-button class="dir-btn" @click="sendCommand('right')">右转 <el-icon><CaretRight /></el-icon></el-button>
              </div>
              <el-button class="dir-btn" @click="sendCommand('backward')"><el-icon><CaretBottom /></el-icon> 后退</el-button>
            </div>
            <div class="action-buttons">
              <el-button @click="sendCommand('wave')" icon="HandWave">招手</el-button>
              <el-button @click="sendCommand('speak')" icon="Microphone">语音播报</el-button>
              <el-button @click="sendCommand('led_on')" icon="Sunny">开灯</el-button>
              <el-button @click="sendCommand('led_off')" icon="Moon">关灯</el-button>
              <el-button @click="sendCommand('get_status')" icon="RefreshRight">获取状态</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="glass-card status-card">
          <template #header><span>📡 设备状态</span></template>
          <div class="status-item">
            <span>连接状态</span>
            <el-tag :type="robotConnected ? 'success' : 'danger'">{{ robotConnected ? '在线' : '离线' }}</el-tag>
          </div>
          <div class="status-item">
            <span>电量</span>
            <el-progress :percentage="batteryLevel" :color="batteryColor" />
          </div>
          <div class="status-item">
            <span>最后指令</span>
            <code>{{ lastCommand }}</code>
          </div>
          <div class="status-item">
            <span>响应日志</span>
            <div class="log-box">{{ responseLog }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { robotApi } from '@/api/robot'  // 需要新建 robot API
import { CaretTop, CaretBottom, CaretLeft, CaretRight, Close } from '@element-plus/icons-vue'

const cmdLoading = ref(false)
const robotConnected = ref(true)   // 可通过心跳接口获取
const batteryLevel = ref(85)
const lastCommand = ref('')
const responseLog = ref('')

const batteryColor = computed(() => {
  if (batteryLevel.value <= 20) return '#f56c6c'
  if (batteryLevel.value <= 50) return '#e6a23c'
  return '#67c23a'
})

const sendCommand = async (cmd: string) => {
  cmdLoading.value = true
  lastCommand.value = cmd
  try {
    const res = await robotApi.control(cmd)
    responseLog.value = `[${new Date().toLocaleTimeString()}] ${cmd} -> ${res}`
    ElMessage.success(`指令 ${cmd} 已发送`)
  } catch (err) {
    responseLog.value = `[${new Date().toLocaleTimeString()}] ${cmd} 失败`
    ElMessage.error('指令发送失败')
  } finally {
    cmdLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.robot-control-page {
  .control-panel {
    .control-grid {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 24px;
      .direction-pad {
        text-align: center;
        .dir-btn {
          width: 100px;
          height: 60px;
          margin: 8px;
          font-size: 16px;
          border-radius: 24px;
          transition: 0.2s;
          &:active { transform: scale(0.96); }
        }
        .mid-row { display: flex; justify-content: center; gap: 8px; }
        .stop { background: #fef0f0; color: #f56c6c; border-color: #fbc4c4; }
      }
      .action-buttons {
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        gap: 12px;
      }
    }
  }
  .status-card {
    .status-item {
      margin-bottom: 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      .log-box {
        background: #f5f7fa;
        border-radius: 12px;
        padding: 8px;
        font-size: 12px;
        max-height: 120px;
        overflow-y: auto;
        width: 100%;
      }
    }
  }
}
</style>
