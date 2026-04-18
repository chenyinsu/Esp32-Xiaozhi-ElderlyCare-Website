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
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="36" icon="UserFilled" />
          <span class="username">{{ authStore.user?.name || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Fold, Expand, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentTitle = computed(() => route.meta.title || '')

const handleCommand = async (command: string) => {
  switch (command) {
    case 'logout':
      await authStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
    case 'profile':
      ElMessage.info('功能开发中')
      break
    case 'password':
      ElMessage.info('功能开发中')
      break
  }
}
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
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>
