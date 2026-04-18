<template>
  <div class="sidebar-wrapper">
    <div class="logo">
      <span class="logo-text-main">暖心伴</span>
      <span v-if="!isCollapse" class="logo-text-sub">NUANXINBAN</span>
    </div>
    
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      background-color="transparent"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataAnalysis /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      
      <el-menu-item index="/devices">
        <el-icon><Monitor /></el-icon>
        <span>设备管理</span>
      </el-menu-item>
      
      <el-menu-item index="/emergencies">
        <el-icon><Warning /></el-icon>
        <span>紧急事件</span>
        <el-badge v-if="pendingCount > 0" :value="pendingCount" class="menu-badge" />
      </el-menu-item>
      
      <el-menu-item index="/reminders">
        <el-icon><Clock /></el-icon>
        <span>提醒管理</span>
      </el-menu-item>
      
      <el-menu-item index="/chats">
        <el-icon><ChatDotRound /></el-icon>
        <span>聊天记录</span>
      </el-menu-item>
      
      <el-menu-item v-if="authStore.isAdmin" index="/users">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { emergencyApi } from '@/api'
import {
  DataAnalysis, Monitor, Warning, Clock, ChatDotRound, User
} from '@element-plus/icons-vue'

defineProps<{ isCollapse: boolean }>()

const route = useRoute()
const authStore = useAuthStore()
const pendingCount = ref(0)

const activeMenu = computed(() => route.path)

onMounted(async () => {
  try {
    const pending = await emergencyApi.getPending()
    pendingCount.value = pending.length
  } catch (e) {
    console.error('Failed to fetch pending emergencies', e)
  }
})
</script>

<style scoped lang="scss">
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .logo {
    height: 60px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    .logo-text-main {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      white-space: nowrap;
    }
    
    .logo-text-sub {
      color: rgba(255, 255, 255, 0.7);
      font-size: 10px;
      letter-spacing: 2px;
      white-space: nowrap;
    }
  }
  
  .el-menu {
    flex: 1;
    border-right: none;
    
    .menu-badge {
      margin-left: 8px;
    }
  }
}
</style>