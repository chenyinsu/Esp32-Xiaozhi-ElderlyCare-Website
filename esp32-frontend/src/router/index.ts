import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '首页 - ESP32健康助手' }
    },
    {
      path: '/reminders',
      name: 'reminders',
      component: () => import('../views/ReminderView.vue'),
      meta: { title: '提醒管理' }
    },
    {
      path: '/emergencies',
      name: 'emergencies',
      component: () => import('../views/EmergencyView.vue'),
      meta: { title: '紧急事件' }
    },
    {
      path: '/chats',
      name: 'chats',
      component: () => import('../views/ChatView.vue'),
      meta: { title: 'AI对话' }
    },
    {
      path: '/reports',
      name: 'reports',
      component: () => import('../views/ReportView.vue'),
      meta: { title: '分析报告' }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/SettingsView.vue'),
      meta: { title: '系统设置' }
    }
  ]
})

// 路由守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title as string || 'ESP32健康助手'
  next()
})

export default router
