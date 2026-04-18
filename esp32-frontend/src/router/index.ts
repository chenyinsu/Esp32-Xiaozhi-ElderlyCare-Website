import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { storage } from '@/utils/storage'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('@/views/Device/index.vue'),
        meta: { title: '设备管理', requiresAuth: true }
      },
      {
        path: 'emergencies',
        name: 'Emergencies',
        component: () => import('@/views/Emergency/index.vue'),
        meta: { title: '紧急事件', requiresAuth: true }
      },
      {
        path: 'reminders',
        name: 'Reminders',
        component: () => import('@/views/Reminder/index.vue'),
        meta: { title: '提醒管理', requiresAuth: true }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/User/index.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['STAFF', 'COMMUNITY'] }
      },
      {
        path: 'chats',
        name: 'Chats',
        component: () => import('@/views/Chat/index.vue'),
        meta: { title: '聊天记录', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = storage.getToken()

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router