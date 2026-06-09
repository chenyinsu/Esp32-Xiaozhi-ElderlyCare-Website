<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧个人信息 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="user-avatar-section">
            <el-avatar :size="80" icon="UserFilled" />
            <h3>{{ userInfo.name }}</h3>
            <el-tag :type="getRoleType(userInfo.role)">
              {{ userRoleMap[userInfo.role] }}
            </el-tag>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ userInfo.gender === 'MALE' ? '男' : userInfo.gender === 'FEMALE' ? '女' : '其他' }}
            </el-descriptions-item>
            <el-descriptions-item label="年龄">{{ userInfo.age || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ userInfo.address || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="紧急电话">{{ userInfo.emergencyPhone || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createdAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧功能区域 -->
      <el-col :span="16">
        <!-- 修改密码 -->
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form :model="passwordForm" label-width="100px" style="max-width: 500px;">
            <el-form-item label="旧密码">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入旧密码"
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少6位）"
              />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="handleChangePassword"
                :loading="passwordLoading"
                :disabled="!canChangePassword"
              >
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 我的设备 -->
        <el-card shadow="hover" class="section-card" v-if="myDevice">
          <template #header>
            <span>我的设备</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备名称">{{ myDevice.deviceName }}</el-descriptions-item>
            <el-descriptions-item label="设备ID">{{ myDevice.deviceId }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="deviceStatusMap[myDevice.status]?.type">
                {{ deviceStatusMap[myDevice.status]?.text }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="电量">
              <el-progress
                :percentage="myDevice.batteryLevel || 0"
                :color="getBatteryColor(myDevice.batteryLevel)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="固件版本">{{ myDevice.firmwareVersion || '--' }}</el-descriptions-item>
            <el-descriptions-item label="最后在线">
              {{ myDevice.lastOnlineTime ? formatDate(myDevice.lastOnlineTime) : '--' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card v-else shadow="hover" class="section-card">
          <template #header>
            <span>我的设备</span>
          </template>
          <el-empty description="暂未绑定设备" />
        </el-card>

        <!-- 最近活动 -->
        <el-card shadow="hover" class="section-card">
          <template #header>
            <span>最近通知</span>
          </template>
          <div class="notification-list">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              class="notification-item"
              :class="{ unread: !notification.isRead }"
            >
              <div class="notif-icon">
                <el-icon :size="20">
                  <Warning v-if="notification.level === 'DANGER' || notification.level === 'CRITICAL'" />
                  <Bell v-else-if="notification.level === 'WARNING'" />
                  <InfoFilled v-else />
                </el-icon>
              </div>
              <div class="notif-content">
                <div class="notif-title">{{ notification.title }}</div>
                <div class="notif-text">{{ notification.content }}</div>
                <div class="notif-time">{{ formatRelativeTime(notification.createdAt) }}</div>
              </div>
            </div>
            <el-empty v-if="notifications.length === 0" description="暂无通知" />
          </div>
        </el-card>

        <!-- 用药追踪 -->
        <el-card shadow="hover" class="section-card" v-if="medicationTracking">
          <template #header>
            <span>用药追踪</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value success">{{ medicationTracking.takenCount }}</div>
                <div class="stat-label">已服用</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value danger">{{ medicationTracking.missedCount }}</div>
                <div class="stat-label">未服用</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value primary">{{ (medicationTracking.adherenceRate * 100).toFixed(0) }}%</div>
                <div class="stat-label">依从率</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Bell, InfoFilled } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { formatDate, formatRelativeTime, deviceStatusMap, userRoleMap } from '@/utils/format'
import type { UserDetail } from '@/api/type'

const authStore = useAuthStore()

const userInfo = ref<UserDetail>({
  id: 0,
  username: '',
  name: '',
  phone: '',
  role: 'STAFF',
  gender: undefined,
  age: undefined,
  address: '',
  deviceId: undefined,
  deviceName: '',
  isActive: true,
  createdAt: '',
  updatedAt: ''
})

const myDevice = ref<any>(null)
const notifications = ref<any[]>([])
const medicationTracking = ref<any>(null)

const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const canChangePassword = computed(() => {
  return passwordForm.oldPassword &&
         passwordForm.newPassword &&
         passwordForm.confirmPassword &&
         passwordForm.newPassword.length >= 6 &&
         passwordForm.newPassword === passwordForm.confirmPassword
})

const getRoleType = (role: string) => {
  const typeMap: Record<string, string> = {
    ELDERLY: 'warning',
    COMMUNITY: 'success',
    STAFF: 'primary',
    CHILD: 'info',
    HOSPITAL: 'danger'
  }
  return typeMap[role] || 'info'
}

const getBatteryColor = (level: number) => {
  if (!level) return '#909399'
  if (level <= 20) return '#f56c6c'
  if (level <= 50) return '#e6a23c'
  return '#67c23a'
}

const loadProfile = async () => {
  try {
    const profile = await userApi.getProfile()
    userInfo.value = profile

    // 如果有绑定的设备，加载设备信息
    if (profile.deviceId) {
      try {
        const { deviceApi } = await import('@/api')
        myDevice.value = await deviceApi.getById(profile.deviceId)
      } catch (e) {
        console.log('加载设备信息失败')
      }
    }

    // 加载用药追踪（如果是老年人）
    if (profile.role === 'ELDERLY') {
      try {
        const { reminderApi } = await import('@/api')
        medicationTracking.value = await reminderApi.getMedicationTracking(profile.id)
      } catch (e) {
        console.log('加载用药追踪失败')
      }
    }
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

const loadNotifications = async () => {
  // 这里需要根据实际API加载通知
  // 暂时使用模拟数据
  notifications.value = []
}

const handleChangePassword = async () => {
  if (!canChangePassword.value) {
    ElMessage.warning('请正确填写密码信息')
    return
  }

  passwordLoading.value = true
  try {
    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')

    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''

    // 退出登录
    setTimeout(() => {
      authStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (error: any) {
    ElMessage.error(error?.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadNotifications()
})
</script>

<style scoped lang="scss">
.profile-page {
  .user-avatar-section {
    text-align: center;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 20px;

    h3 {
      margin: 12px 0 8px;
      color: #1a1f2e;
    }
  }

  .section-card {
    margin-bottom: 20px;
  }

  .notification-list {
    .notification-item {
      display: flex;
      align-items: flex-start;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &.unread {
        background: #f0f9ff;
        margin: 0 -20px;
        padding: 12px 20px;
      }

      .notif-icon {
        margin-right: 12px;
        color: #409eff;
      }

      .notif-content {
        flex: 1;

        .notif-title {
          font-weight: 500;
          color: #1a1f2e;
        }

        .notif-text {
          font-size: 13px;
          color: #606266;
          margin: 4px 0;
        }

        .notif-time {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
    }
  }

  .stat-item {
    text-align: center;
    padding: 20px;

    .stat-value {
      font-size: 32px;
      font-weight: bold;

      &.success { color: #67c23a; }
      &.danger { color: #f56c6c; }
      &.primary { color: #409eff; }
    }

    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-top: 8px;
    }
  }
}
</style>
