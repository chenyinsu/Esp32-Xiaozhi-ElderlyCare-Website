<template>
  <div id="app">
    <nav v-if="showNav">
      <router-link to="/">首页</router-link> |
      <router-link to="/reminders">提醒管理</router-link> |
      <router-link to="/emergencies">紧急事件</router-link> |
      <router-link to="/chats">AI对话</router-link> |
      <router-link to="/reports">分析报告</router-link>
    </nav>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showNav = computed(() => route.name !== 'login')
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

nav {
  padding: 30px;
  text-align: center;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  margin: 0 10px;
}

nav a.router-link-exact-active {
  color: #42b983;
}
</style>
