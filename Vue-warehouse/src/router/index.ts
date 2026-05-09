import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: 'lc-depot',
          name: 'lc-depot',
          component: () => import('../views/LCDepot.vue'),
          meta: { title: '良仓管理' }
        },
        {
          path: 'yc-depot',
          name: 'yc-depot',
          component: () => import('../views/YCDepot.vue'),
          meta: { title: '翼仓管理' }
        },
        {
          path: 'analytics',
          name: 'analytics',
          component: () => import('../views/Analytics.vue'),
          meta: { title: '数据统计' }
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('../views/Settings.vue'),
          meta: { title: '系统设置' }
        },
        {
          path: 'us-time',
          name: 'us-time',
          component: () => import('../views/USTime.vue'),
          meta: { title: '美国时区' }
        }
      ]
    }
  ]
})

export default router
