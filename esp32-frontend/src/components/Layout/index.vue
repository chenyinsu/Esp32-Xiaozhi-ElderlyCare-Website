<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <Sidebar :is-collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="header">
        <Header @toggle-sidebar="isCollapse = !isCollapse" />
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '../Sidebar/index.vue'
import Header from '../Header/index.vue'

const isCollapse = ref(false)
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;

  .sidebar {
    background: linear-gradient(180deg, #1a1f2e 0%, #0f131c 100%);
    transition: width 0.3s;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  }

  .header {
    height: 60px;
    padding: 0 20px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    display: flex;
    align-items: center;
  }

  .main {
    background: #f5f7fa;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>
