import { createRouter, createWebHistory } from 'vue-router'
import { ElLoading } from 'element-plus'
import { useUserStore } from '@/store/user'

// 全局路由 Loading 实例（圆圈样式）
let loadingInstance = null
// 延迟显示定时器：避免快速切换时 loading 闪烁
let loadingTimer = null

// 各角色首页路径
export const ROLE_HOME = {
  ELDERLY: '/elderly/dashboard',
  FAMILY: '/family/dashboard',
  DOCTOR: '/doctor/dashboard',
  ADMIN: '/admin/dashboard'
}

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/Layout.vue'),
    redirect: '/login',
    children: [
      // ===== 老年用户页面 =====
      {
        path: 'elderly/dashboard',
        name: 'ElderlyDashboard',
        component: () => import('@/views/elderly/Dashboard.vue'),
        meta: { title: '工作台', requiresAuth: true, roles: ['ELDERLY'] }
      },
      {
        path: 'elderly/data-input',
        name: 'ElderlyDataInput',
        component: () => import('@/views/elderly/DataInput.vue'),
        meta: { title: '数据录入', requiresAuth: true, roles: ['ELDERLY'] }
      },
      {
        path: 'elderly/data-view',
        name: 'ElderlyDataView',
        component: () => import('@/views/elderly/DataView.vue'),
        meta: { title: '数据查看', requiresAuth: true, roles: ['ELDERLY'] }
      },
      {
        path: 'elderly/profile',
        name: 'ElderlyProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true, roles: ['ELDERLY'] }
      },
      {
        path: 'elderly/alert',
        name: 'ElderlyAlert',
        component: () => import('@/views/elderly/Alert.vue'),
        meta: { title: '预警通知', requiresAuth: true, roles: ['ELDERLY'] }
      },
      {
        path: 'elderly/advice',
        name: 'ElderlyAdvice',
        component: () => import('@/views/elderly/Advice.vue'),
        meta: { title: '健康建议', requiresAuth: true, roles: ['ELDERLY'] }
      },
      {
        path: 'elderly/intervention',
        name: 'ElderlyIntervention',
        component: () => import('@/views/elderly/Intervention.vue'),
        meta: { title: '干预方案', requiresAuth: true, roles: ['ELDERLY'] }
      },

      {
        path: 'elderly/health-profile',
        name: 'ElderlyHealthProfile',
        component: () => import('@/views/elderly/Profile.vue'),
        meta: { title: '健康档案', requiresAuth: true, roles: ['ELDERLY'] }
      },

      // ===== 家属页面 =====
      {
        path: 'family/dashboard',
        name: 'FamilyDashboard',
        component: () => import('@/views/family/Dashboard.vue'),
        meta: { title: '工作台', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/data-view',
        name: 'FamilyDataView',
        component: () => import('@/views/family/DataView.vue'),
        meta: { title: '数据查看', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/data-input',
        name: 'FamilyDataInput',
        component: () => import('@/views/family/DataInput.vue'),
        meta: { title: '数据录入', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/profile',
        name: 'FamilyProfile',
        component: () => import('@/views/family/Profile.vue'),
        meta: { title: '健康档案', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/user-profile',
        name: 'FamilyUserProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/alert',
        name: 'FamilyAlert',
        component: () => import('@/views/family/Alert.vue'),
        meta: { title: '预警通知', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/advice',
        name: 'FamilyAdvice',
        component: () => import('@/views/family/Advice.vue'),
        meta: { title: '健康建议', requiresAuth: true, roles: ['FAMILY'] }
      },
      {
        path: 'family/relation',
        name: 'FamilyRelation',
        component: () => import('@/views/family/Relation.vue'),
        meta: { title: '关联管理', requiresAuth: true, roles: ['FAMILY'] }
      },

      // ===== 医护人员页面 =====
      {
        path: 'doctor/dashboard',
        name: 'DoctorDashboard',
        component: () => import('@/views/doctor/Dashboard.vue'),
        meta: { title: '工作台', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/profile',
        name: 'DoctorProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/profile-manage',
        name: 'DoctorProfileManage',
        component: () => import('@/views/doctor/ProfileManage.vue'),
        meta: { title: '健康档案管理', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/data-analysis',
        name: 'DoctorDataAnalysis',
        component: () => import('@/views/doctor/DataAnalysis.vue'),
        meta: { title: '数据分析', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/alert-handle',
        name: 'DoctorAlertHandle',
        component: () => import('@/views/doctor/AlertHandle.vue'),
        meta: { title: '预警处理', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/advice-publish',
        name: 'DoctorAdvicePublish',
        component: () => import('@/views/doctor/AdvicePublish.vue'),
        meta: { title: '建议发布', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/intervention-manage',
        name: 'DoctorInterventionManage',
        component: () => import('@/views/doctor/InterventionManage.vue'),
        meta: { title: '干预方案管理', requiresAuth: true, roles: ['DOCTOR'] }
      },
      {
        path: 'doctor/alert-rule-personal',
        name: 'DoctorAlertRulePersonal',
        component: () => import('@/views/doctor/AlertRulePersonal.vue'),
        meta: { title: '个性化预警规则', requiresAuth: true, roles: ['DOCTOR'] }
      },

      // ===== 管理员页面 =====
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '工作台', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/user',
        name: 'AdminUser',
        component: () => import('@/views/admin/User.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/role',
        name: 'AdminRole',
        component: () => import('@/views/admin/Role.vue'),
        meta: { title: '角色管理', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/region',
        name: 'AdminRegion',
        component: () => import('@/views/admin/Region.vue'),
        meta: { title: '辖区管理', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/alert-rule',
        name: 'AdminAlertRule',
        component: () => import('@/views/admin/AlertRule.vue'),
        meta: { title: '预警规则', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '统计分析', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/log',
        name: 'AdminLog',
        component: () => import('@/views/admin/Log.vue'),
        meta: { title: '系统日志', requiresAuth: true, roles: ['ADMIN'] }
      },
      {
        path: 'admin/profile',
        name: 'AdminProfile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true, roles: ['ADMIN'] }
      }
    ]
  },

  // ===== 公共页面 =====
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/login/ForgotPassword.vue'),
    meta: { title: '忘记密码', requiresAuth: false }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 根据角色获取首页路径
function getHomeByRole(roleCode) {
  return ROLE_HOME[roleCode] || '/login'
}

// 启动路由 Loading（延迟 200ms 显示，避免快速切换闪烁）
function startLoading() {
  if (loadingTimer) clearTimeout(loadingTimer)
  loadingTimer = setTimeout(() => {
    // 根据角色决定 customClass，用于适老化样式差异化
    const userStore = useUserStore()
    const roleCode = userStore.userInfo?.roleCode
    let customClass = 'route-loading'
    if (roleCode === 'ELDERLY') {
      customClass = 'route-loading elderly-loading'
    } else if (roleCode === 'FAMILY') {
      customClass = 'route-loading warm-loading'
    }
    loadingInstance = ElLoading.service({
      lock: true,
      text: '正在加载，请稍候...',
      background: 'rgba(255, 255, 255, 0.7)',
      customClass
    })
  }, 200)
}

// 关闭路由 Loading
function closeLoading() {
  if (loadingTimer) {
    clearTimeout(loadingTimer)
    loadingTimer = null
  }
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

// 全局前置守卫
router.beforeEach((to, from, next) => {
  startLoading()
  const userStore = useUserStore()
  const token = userStore.token

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 社区老年人健康管理服务平台`
  }

  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    // 已登录访问登录/注册页 → 跳转对应角色首页
    if (token && (to.path === '/login' || to.path === '/register')) {
      const roleCode = userStore.userInfo.roleCode
      next(getHomeByRole(roleCode))
      return
    }
    next()
    return
  }

  // 需要认证但未登录 → 跳转登录页
  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录，校验角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    const roleCode = userStore.userInfo.roleCode
    if (!to.meta.roles.includes(roleCode)) {
      // 无权访问 → 跳转对应角色首页
      next(getHomeByRole(roleCode))
      return
    }
  }

  next()
})

// 全局后置守卫：关闭 Loading
router.afterEach(() => {
  closeLoading()
})

// 路由错误处理：也需关闭 Loading，避免卡死
router.onError(() => {
  closeLoading()
})

export default router
