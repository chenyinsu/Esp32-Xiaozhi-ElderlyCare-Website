<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表和最近活动 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>紧急事件趋势</span>
          </template>
          <div class="chart-container">
            <v-chart :option="chartOption" autoresize />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="recent-card" shadow="hover">
          <template #header>
            <span>最近紧急事件</span>
            <el-button type="primary" link @click="goToEmergencies">查看更多</el-button>
          </template>
          <div class="recent-list">
            <div
              v-for="item in recentEmergencies"
              :key="item.id"
              class="recent-item"
              @click="goToEmergency(item.id)"
            >
              <div class="item-tag" :class="emergencyLevelMap[item.emergencyLevel]?.type">
                {{ emergencyLevelMap[item.emergencyLevel]?.text }}
              </div>
              <div class="item-content">
                <div class="item-title">{{ item.userName }}</div>
                <div class="item-desc">{{ item.emergencyType }}</div>
              </div>
              <div class="item-time">{{ formatRelativeTime(item.triggerTime) }}</div>
            </div>
            <el-empty v-if="!recentEmergencies.length" description="暂无紧急事件" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 在线设备和今日提醒 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="12">
        <el-card class="list-card" shadow="hover">
          <template #header>
            <span>在线设备</span>
            <el-tag type="success" style="margin-left: 10px;">{{ onlineDevices.length }}</el-tag>
          </template>
          <div class="device-list">
            <div v-for="device in onlineDevices.slice(0, 5)" :key="device.id" class="device-item">
              <div class="device-status">
                <span class="status-dot" :class="device.status === 'ONLINE' ? 'online' : 'offline'"></span>
              </div>
              <div class="device-info">
                <div class="device-name">{{ device.deviceName }}</div>
                <div class="device-id">{{ device.deviceId }}</div>
              </div>
              <div class="device-battery">
                <el-icon><Phone /></el-icon>
                {{ device.batteryLevel || '--' }}%
              </div>
            </div>
            <el-empty v-if="!onlineDevices.length" description="暂无在线设备" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="list-card" shadow="hover">
          <template #header>
            <span>今日提醒</span>
          </template>
          <div class="reminder-list">
            <div v-for="reminder in todayReminders" :key="reminder.id" class="reminder-item">
              <el-icon class="reminder-icon"><Clock /></el-icon>
              <div class="reminder-info">
                <div class="reminder-title">{{ reminder.title }}</div>
                <div class="reminder-user">{{ reminder.userName }}</div>
              </div>
              <el-tag :type="reminder.isTaken ? 'success' : 'warning'" size="small">
                {{ reminder.isTaken ? '已完成' : '待处理' }}
              </el-tag>
            </div>
            <el-empty v-if="!todayReminders.length" description="暂无今日提醒" />
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
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { User, Monitor, Warning, Clock, Phone } from '@element-plus/icons-vue'

// 注册 ECharts 组件
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const router = useRouter()

const overview = ref({
  totalUsers: 0,
  totalDevices: 0,
  onlineDevices: 0,
  pendingEmergencies: 0,
  todayReminders: 0
})

const onlineDevices = ref<any[]>([])
const todayReminders = ref<any[]>([])
const recentEmergencies = ref<any[]>([])

const stats = computed(() => [
  { 
    title: '总用户数', 
    value: overview.value.totalUsers, 
    icon: User, 
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
  },
  { 
    title: '在线设备', 
    value: overview.value.onlineDevices, 
    icon: Monitor, 
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' 
  },
  { 
    title: '待处理事件', 
    value: overview.value.pendingEmergencies, 
    icon: Warning, 
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' 
  },
  { 
    title: '今日提醒', 
    value: overview.value.todayReminders, 
    icon: Clock, 
    color: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' 
  }
])

const chartOption = computed(() => ({
  grid: { left: 50, right: 20, top: 30, bottom: 30 },
  xAxis: { 
    type: 'category', 
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] 
  },
  yAxis: { type: 'value' },
  series: [{
    data: [5, 3, 7, 2, 4, 1, 0],
    type: 'line',
    smooth: true,
    lineStyle: { color: '#409eff', width: 3 },
    areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
  }],
  tooltip: { trigger: 'axis' }
}))

const goToEmergencies = () => router.push('/emergencies')
const goToEmergency = (id: number) => router.push(`/emergencies?id=${id}`)

onMounted(async () => {
  try {
    const [overviewData, devices, reminders, recent] = await Promise.all([
      dashboardApi.getOverview().catch(() => null),
      deviceApi.getOnline().catch(() => []),
      reminderApi.getToday().catch(() => []),
      emergencyApi.getRecent(24).catch(() => [])
    ])

    if (overviewData) {
      overview.value = overviewData
    }
    onlineDevices.value = devices || []
    todayReminders.value = reminders || []
    recentEmergencies.value = (recent || []).slice(0, 5)
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-cards {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: #1a1f2e;
        }

        .stat-title {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .content-row {
    margin-bottom: 20px;
  }

  .chart-card {
    .chart-container {
      height: 300px;
    }
  }

  .recent-card {
    :deep(.el-card__header) {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .recent-list {
      max-height: 350px;
      overflow-y: auto;

      .recent-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;

        &:hover {
          background: #f5f7fa;
        }

        .item-tag {
          width: 50px;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
          text-align: center;

          &.danger { background: #fef0f0; color: #f56c6c; }
          &.warning { background: #fdf6ec; color: #e6a23c; }
          &.info { background: #f4f4f5; color: #909399; }
        }

        .item-content {
          flex: 1;
          margin-left: 12px;

          .item-title {
            font-weight: 500;
            color: #1a1f2e;
          }

          .item-desc {
            font-size: 12px;
            color: #909399;
          }
        }

        .item-time {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
    }
  }

  .device-list, .reminder-list {
    max-height: 300px;
    overflow-y: auto;

    .device-item, .reminder-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    .device-item {
      .device-status {
        margin-right: 12px;

        .status-dot {
          width: 10px;
          height: 10px;
          border-radius: 50%;
          display: inline-block;

          &.online { background: #67c23a; }
          &.offline { background: #909399; }
        }
      }

      .device-info {
        flex: 1;

        .device-name {
          font-weight: 500;
        }

        .device-id {
          font-size: 12px;
          color: #909399;
        }
      }

      .device-battery {
        color: #909399;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .reminder-item {
      .reminder-icon {
        font-size: 20px;
        color: #409eff;
        margin-right: 12px;
      }

      .reminder-info {
        flex: 1;

        .reminder-title {
          font-weight: 500;
        }

        .reminder-user {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>