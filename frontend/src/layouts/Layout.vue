<template>
  <el-container class="layout-container" :class="{ 'elderly-theme': isElderly, 'warm-theme': isWarmTheme }">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
        <span class="system-title">社区老年人健康管理服务平台</span>
      </div>

      <div class="header-right">
        <!-- 未读预警数量徽章 -->
        <el-badge :value="alertStore.unreadCount" :hidden="alertStore.unreadCount === 0" :max="99" class="alert-badge">
          <el-icon class="header-icon" @click="goAlert">
            <Bell />
          </el-icon>
        </el-badge>

        <!-- 用户头像与下拉菜单 -->
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="36" :src="userStore.userInfo.avatar">
              {{ avatarText }}
            </el-avatar>
            <span class="user-name">{{ userStore.userInfo.name || '用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人信息
              </el-dropdown-item>
              <el-dropdown-item command="password">
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="layout-body">
      <!-- 侧边菜单栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :router="true"
          class="side-menu"
        >
          <el-menu-item
            v-for="item in menus"
            :key="item.path"
            :index="item.path"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="440px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitPassword">确认</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAlertStore } from '@/store/alert'
import { changePassword } from '@/api/profile'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const alertStore = useAlertStore()

const isCollapse = ref(false)

// 是否老年用户（适老化）
const isElderly = computed(() => userStore.userInfo.roleCode === 'ELDERLY')

// 是否使用暖橙色主题（老年用户、家属）
const isWarmTheme = computed(() => ['ELDERLY', 'FAMILY'].includes(userStore.userInfo.roleCode))

// 头像文字
const avatarText = computed(() => {
  const name = userStore.userInfo.name
  return name ? name.charAt(0).toUpperCase() : 'U'
})

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 各角色菜单配置
const menuConfig = {
  ELDERLY: [
    { path: '/elderly/dashboard', title: '工作台', icon: 'HomeFilled' },
    { path: '/elderly/data-input', title: '数据录入', icon: 'EditPen' },
    { path: '/elderly/data-view', title: '数据查看', icon: 'DataLine' },
    { path: '/elderly/health-profile', title: '健康档案', icon: 'Files' },
    { path: '/elderly/alert', title: '预警通知', icon: 'Bell' },
    { path: '/elderly/advice', title: '健康建议', icon: 'ChatDotRound' },
    { path: '/elderly/profile', title: '个人信息', icon: 'User' }
  ],
  FAMILY: [
    { path: '/family/dashboard', title: '工作台', icon: 'HomeFilled' },
    { path: '/family/data-view', title: '数据查看', icon: 'DataLine' },
    { path: '/family/data-input', title: '数据录入', icon: 'EditPen' },
    { path: '/family/profile', title: '健康档案', icon: 'Files' },
    { path: '/family/alert', title: '预警通知', icon: 'Bell' },
    { path: '/family/advice', title: '健康建议', icon: 'ChatDotRound' },
    { path: '/family/relation', title: '关联管理', icon: 'Connection' },
    { path: '/family/user-profile', title: '个人信息', icon: 'User' }
  ],
  DOCTOR: [
    { path: '/doctor/dashboard', title: '工作台', icon: 'HomeFilled' },
    { path: '/doctor/data-analysis', title: '数据分析', icon: 'TrendCharts' },
    { path: '/doctor/alert-handle', title: '预警处理', icon: 'Bell' },
    { path: '/doctor/profile-manage', title: '健康档案管理', icon: 'Files' },
    { path: '/doctor/advice-publish', title: '建议发布', icon: 'ChatDotRound' },
    { path: '/doctor/intervention-manage', title: '干预方案管理', icon: 'Document' },
    { path: '/doctor/alert-rule-personal', title: '个性化预警规则', icon: 'SetUp' },
    { path: '/doctor/profile', title: '个人信息', icon: 'User' }
  ],
  ADMIN: [
    { path: '/admin/dashboard', title: '工作台', icon: 'HomeFilled' },
    { path: '/admin/user', title: '用户管理', icon: 'UserFilled' },
    { path: '/admin/role', title: '角色管理', icon: 'User' },
    { path: '/admin/region', title: '辖区管理', icon: 'Location' },
    { path: '/admin/alert-rule', title: '预警规则', icon: 'Bell' },
    { path: '/admin/statistics', title: '统计分析', icon: 'TrendCharts' },
    { path: '/admin/log', title: '系统日志', icon: 'Document' },
    { path: '/admin/profile', title: '个人信息', icon: 'User' }
  ]
}

// 当前角色菜单
const menus = computed(() => {
  const roleCode = userStore.userInfo.roleCode
  return menuConfig[roleCode] || []
})

// 跳转预警页
const goAlert = () => {
  const roleCode = userStore.userInfo.roleCode
  const alertPath = {
    ELDERLY: '/elderly/alert',
    FAMILY: '/family/alert',
    DOCTOR: '/doctor/alert-handle',
    ADMIN: '/admin/alert-rule'
  }
  router.push(alertPath[roleCode] || '/')
}

// 下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    goProfile()
  } else if (command === 'password') {
    passwordVisible.value = true
  } else if (command === 'logout') {
    handleLogout()
  }
}

// 跳转个人信息
const goProfile = () => {
  const roleCode = userStore.userInfo.roleCode
  const profilePath = {
    ELDERLY: '/elderly/profile',
    FAMILY: '/family/user-profile',
    DOCTOR: '/doctor/profile',
    ADMIN: '/admin/profile'
  }
  router.push(profilePath[roleCode] || '/')
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    alertStore.resetUnread()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

// ===== 修改密码 =====
const passwordVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const submitPassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    passwordLoading.value = true
    try {
      await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      passwordVisible.value = false
      userStore.logout()
      router.push('/login')
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      passwordLoading.value = false
    }
  })
}

// 初始化：获取用户信息与未读预警数
onMounted(async () => {
  if (!userStore.userInfo.id) {
    try {
      await userStore.getUserInfo()
    } catch (e) {
      // 获取失败由拦截器处理
    }
  }
  alertStore.getUnreadCount()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--theme-header-bg);
  color: #fff;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
  transition: background-color 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 22px;
  cursor: pointer;
  color: #fff;
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.alert-badge {
  line-height: 1;
}

.header-icon {
  font-size: 22px;
  color: #fff;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
}

.user-name {
  font-size: 16px;
}

.layout-body {
  height: calc(100vh - 60px);
}

.layout-aside {
  background-color: var(--theme-aside-bg);
  transition: width 0.3s, background-color 0.3s ease;
  overflow-x: hidden;
}

.side-menu {
  border-right: none;
  height: 100%;
  background-color: var(--theme-aside-bg) !important;
}

.side-menu .el-menu-item,
.side-menu .el-sub-menu__title {
  color: var(--theme-menu-text);
}

.side-menu .el-menu-item.is-active,
.side-menu .el-sub-menu__title.is-active {
  color: var(--theme-menu-active);
}

.side-menu .el-menu-item:hover,
.side-menu .el-sub-menu__title:hover {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.side-menu:not(.el-menu--collapse) {
  width: 220px;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 适老化：放大顶部标题与用户名 */
.elderly-theme .system-title {
  font-size: 22px;
}

.elderly-theme .user-name {
  font-size: 18px;
}

/* 暖橙色主题：放大标题（家属也应用，保持与老年用户一致的暖色调体验） */
.warm-theme .system-title {
  font-size: 22px;
}

.warm-theme .user-name {
  font-size: 18px;
}
</style>
