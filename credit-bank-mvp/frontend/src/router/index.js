import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Accounts.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'account/:id',
        name: 'AccountDetail',
        component: () => import('@/views/AccountDetail.vue'),
        meta: { title: '用户详情' }
      },
      {
        path: 'rules',
        name: 'PointRules',
        component: () => import('@/views/PointRules.vue'),
        meta: { title: '积分规则' }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('@/views/Placeholder.vue'),
        meta: { title: '交易管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const { currentUser } = useAuth()
  if (to.path === '/login') {
    next()
  } else if (!currentUser.value) {
    next('/login')
  } else {
    next()
  }
})

export default router
