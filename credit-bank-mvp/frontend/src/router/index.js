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
        path: 'users/op-logs',
        name: 'UserOpLogs',
        component: () => import('@/views/UserOpLog.vue'),
        meta: { title: '操作日志' }
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
        component: () => import('@/views/Transactions.vue'),
        meta: { title: '交易管理' }
      },
      {
        path: 'organizations',
        name: 'Organizations',
        component: () => import('@/views/Organizations.vue'),
        meta: { title: '机构管理' }
      },
      {
        path: 'experts',
        name: 'Experts',
        component: () => import('@/views/Experts.vue'),
        meta: { title: '专家管理' }
      },
      {
        path: 'exchange-rules',
        name: 'ExchangeRules',
        component: () => import('@/views/ExchangeRules.vue'),
        meta: { title: '转换规则' }
      },
      {
        path: 'cert-standards',
        name: 'CertStandards',
        component: () => import('@/views/CertStandards.vue'),
        meta: { title: '认证标准' }
      },
      {
        path: 'cert-standards/:id/requirement',
        name: 'CertRequirement',
        component: () => import('@/views/CertRequirement.vue'),
        meta: { title: '执行标准文件' }
      },
      {
        path: 'cert-standards/:id/flow',
        name: 'CertFlowManage',
        component: () => import('@/views/CertFlowManage.vue'),
        meta: { title: '审批流程管理' }
      },
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/Applications.vue'),
        meta: { title: '业务流程' }
      },
      {
        path: 'cert-applications',
        name: 'CertApplications',
        component: () => import('@/views/CertApplications.vue'),
        meta: { title: '证书申请审核' }
      },
      {
        path: 'campaigns',
        name: 'CampaignManage',
        component: () => import('@/views/CampaignManage.vue'),
        meta: { title: '平台活动管理' }
      },
      {
        path: 'campaigns/student',
        name: 'CampaignStudent',
        component: () => import('@/views/CampaignStudent.vue'),
        meta: { title: '参与活动' }
      },
      {
        path: 'campaign/:id',
        name: 'CampaignDetail',
        component: () => import('@/views/CampaignDetail.vue'),
        meta: { title: '活动详情' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '我的资料' }
      },
      {
        path: 'my-certs',
        name: 'ExpertCerts',
        component: () => import('@/views/ExpertCerts.vue'),
        meta: { title: '我的资质' }
      },
      {
        path: 'student-certs',
        name: 'StudentCerts',
        component: () => import('@/views/StudentCerts.vue'),
        meta: { title: '学生证书认证' }
      },
      {
        path: 'my-certificates',
        name: 'MyCertificates',
        component: () => import('@/views/StudentCerts.vue'),
        meta: { title: '我的证书' }
      },
      {
        path: 'student-certificate/:id',
        name: 'StudentCertificate',
        component: () => import('@/views/StudentCertificate.vue'),
        meta: { title: '证书详情' }
      },
      {
        path: 'projects',
        name: 'ProjectStudent',
        component: () => import('@/views/ProjectStudent.vue'),
        meta: { title: '项目报名' }
      },
      {
        path: 'project/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/ProjectDetail.vue'),
        meta: { title: '项目详情' }
      },
      {
        path: 'my-projects',
        name: 'MyProjects',
        component: () => import('@/views/MyProjects.vue'),
        meta: { title: '我的项目' }
      },
      {
        path: 'projects',
        name: 'ProjectManage',
        component: () => import('@/views/ProjectManage.vue'),
        meta: { title: '项目管理' }
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
