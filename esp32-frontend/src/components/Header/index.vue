<template>
  <div class="header-wrapper">
    <div class="left">
      <el-icon class="toggle-icon" @click="$emit('toggleSidebar')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="right">
      <!-- 通知中心 -->
      <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
        <el-icon :size="20" class="notification-icon">
          <Bell />
        </el-icon>
      </el-badge>

      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="36" icon="UserFilled" />
          <span class="username">{{ authStore.user?.name || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Fold, Expand, ArrowDown, Bell,
  User, Lock, SwitchButton
} from '@element-plus/icons-vue'

defineProps<{
  isCollapse: boolean
}>()

defineEmits<{
  toggleSidebar: []
}>()

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const currentTitle = computed(() => route.meta.title || '')
const unreadCount = computed(() => notificationStore.unreadCount)

const handleCommand = async (command: string) => {
  switch (command) {
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          type: 'warning'
        })
        await authStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch {
        // 用户取消退出
      }
      break
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      router.push('/profile')
      break
  }
}

// 轮询通知
let timer: number | null = null
onMounted(() => {
  timer = window.setInterval(() => {
    // 可在此调用API获取未读通知数
  }, 30000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.header-wrapper {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .left {
    display: flex;
    align-items: center;
    gap: 16px;

    .toggle-icon {
      font-size: 20px;
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }
  }

  .right {
    display: flex;
    align-items: center;
    gap: 20px;

    .notification-badge {
      cursor: pointer;

      .notification-icon {
        color: #606266;

        &:hover {
          color: #409eff;
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background 0.2s;

      &:hover {
        background: #f5f7fa;
      }

      .username {
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 14px;
        color: #1a1f2e;
      }
    }
  }
}
</style>
