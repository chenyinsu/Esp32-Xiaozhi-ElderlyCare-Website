<template>
  <div class="dashboard">
    <!-- 统计卡片 玻璃态 + 图标动画 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.title">
        <el-card class="glass-card stat-card" shadow="hover">
          <div class="stat-icon" :style="{ background: stat.gradient }"><el-icon :size="28"><component :is="stat.icon" /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 + 最近紧急事件 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card class="glass-card">
          <template #header><span>📈 紧急事件趋势（近7天）</span></template>
          <v-chart :option="chartOption" autoresize style="height: 320px;" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="glass-card">
          <template #header><span>🚨 待处理紧急事件</span><el-button text type="primary" @click="goToEmergencies">全部</el-button></template>
          <div class="emergency-list">
            <div v-for="e in pendingEmergencies.slice(0,4)" :key="e.id" class="emergency-item" @click="goToEmergency(e.id)">
              <div class="level-tag" :class="e.emergencyLevel.toLowerCase()">{{ emergencyLevelMap[e.emergencyLevel]?.text }}</div>
              <div class="info"><strong>{{ e.userName }}</strong><br/><span>{{ e.emergencyType }}</span></div>
              <div class="time">{{ formatRelativeTime(e.triggerTime) }}</div>
            </div>
            <el-empty v-if="!pendingEmergencies.length" description="暂无待处理事件" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 在线设备 & 今日提醒 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card class="glass-card">
          <template #header><span>🟢 在线设备</span><el-tag type="success">{{ onlineDevices.length }}</el-tag></template>
          <div class="device-row" v-for="d in onlineDevices.slice(0,5)" :key="d.id">
            <span>{{ d.deviceName }}</span><span>⚡{{ d.batteryLevel ?? '?' }}%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="glass-card">
          <template #header><span>⏰ 今日提醒</span></template>
          <div class="reminder-row" v-for="r in todayReminders.slice(0,5)" :key="r.id">
            <span>{{ r.title }}</span><el-tag :type="r.isTaken?'success':'warning'" size="small">{{ r.isTaken?'已完成':'待处理' }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi, deviceApi, emergencyApi, reminderApi } from '@/api'
import { formatRelativeTime, emergencyLevelMap } from '@/utils/format'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const router = useRouter()
const overview = ref({ totalUsers:0, onlineDevices:0, pendingEmergencies:0, todayReminders:0 })
const onlineDevices = ref([])
const todayReminders = ref([])
const pendingEmergencies = ref([])

const stats = computed(() => [
  { title:'总用户', value:overview.value.totalUsers, icon:'User', gradient:'linear-gradient(135deg,#667eea,#764ba2)' },
  { title:'在线设备', value:overview.value.onlineDevices, icon:'Monitor', gradient:'linear-gradient(135deg,#43e97b,#38f9d7)' },
  { title:'待处理事件', value:overview.value.pendingEmergencies, icon:'Warning', gradient:'linear-gradient(135deg,#fa709a,#fee140)' },
  { title:'今日提醒', value:overview.value.todayReminders, icon:'Clock', gradient:'linear-gradient(135deg,#a18cd1,#fbc2eb)' }
])

const chartOption = computed(() => ({
  xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五','周六','周日'] },
  yAxis: { type: 'value' },
  series: [{ data: [5,8,3,7,4,2,1], type: 'line', smooth: true, areaStyle: { opacity: 0.2 }, lineStyle: { width: 3, color: '#409eff' } }],
  tooltip: { trigger: 'axis' }
}))

const loadData = async () => {
  const [over, devs, reminders, pending] = await Promise.all([
    dashboardApi.getOverview().catch(()=>null), deviceApi.getOnline(), reminderApi.getToday(), emergencyApi.getPending()
  ])
  if(over) overview.value = over
  onlineDevices.value = devs || []
  todayReminders.value = reminders || []
  pendingEmergencies.value = pending || []
}
onMounted(loadData)
const goToEmergencies = () => router.push('/emergencies')
const goToEmergency = (id) => router.push(`/emergencies?id=${id}`)
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card { display: flex; align-items: center; gap: 16px; padding: 12px; }
  .stat-icon { width: 54px; height: 54px; border-radius: 18px; display: flex; align-items: center; justify-content: center; color: white; }
  .stat-value { font-size: 28px; font-weight: 700; }
  .emergency-list, .device-row, .reminder-row { .emergency-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #eee; cursor: pointer; } }
}
</style>
