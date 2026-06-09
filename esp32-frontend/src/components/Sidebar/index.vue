<template>
  <div class="sidebar-wrapper">
    <div class="logo-area">
      <div class="logo-icon">🤖</div>
      <div v-if="!isCollapse" class="logo-text">
        <span class="main">暖心伴</span>
        <span class="sub">NUANXINBAN</span>
      </div>
    </div>

    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      background-color="transparent"
      text-color="#e0e7ff"
      active-text-color="#fff"
      router
    >
      <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
      <el-menu-item index="/robot-control"><el-icon><Cpu /></el-icon><span>机器人控制</span></el-menu-item>
      <el-menu-item index="/devices"><el-icon><Monitor /></el-icon><span>设备管理</span></el-menu-item>
      <el-menu-item index="/emergencies"><el-icon><Warning /></el-icon><span>紧急事件</span><el-badge v-if="pendingCount > 0" :value="pendingCount" class="menu-badge" /></el-menu-item>
      <el-menu-item index="/reminders"><el-icon><Clock /></el-icon><span>提醒管理</span></el-menu-item>
      <el-menu-item index="/chats"><el-icon><ChatDotRound /></el-icon><span>聊天记录</span></el-menu-item>
      <el-menu-item v-if="authStore.isAdmin" index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
      <el-menu-item index="/profile"><el-icon><UserFilled /></el-icon><span>个人中心</span></el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { emergencyApi } from '@/api'
import { DataAnalysis, Monitor, Warning, Clock, ChatDotRound, User, UserFilled, Cpu } from '@element-plus/icons-vue'

defineProps<{ isCollapse: boolean }>()
const route = useRoute()
const authStore = useAuthStore()
const pendingCount = ref(0)

const activeMenu = computed(() => route.path)

const loadPending = async () => {
  try {
    const list = await emergencyApi.getPending()
    pendingCount.value = list.length
  } catch (e) {}
}
onMounted(() => { loadPending(); setInterval(loadPending, 60000) })
</script>

<style scoped lang="scss">
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0b2b3b 0%, #1a4a6f 100%);
  .logo-area {
    height: 70px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255,255,255,0.1);
    .logo-icon { font-size: 28px; }
    .logo-text {
      display: flex;
      flex-direction: column;
      .main { color: white; font-size: 18px; font-weight: bold; }
      .sub { color: rgba(255,255,255,0.6); font-size: 10px; letter-spacing: 2px; }
    }
  }
  .el-menu { border-right: none; flex: 1; }
  .menu-badge { margin-left: 8px; }
}
</style>
