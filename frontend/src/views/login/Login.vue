<template>
  <div class="login-container elderly-theme">
    <el-card class="login-card">
      <div class="login-header">
        <h2 class="login-title">社区老年人健康管理服务平台</h2>
        <p class="login-subtitle">欢迎登录</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="loginForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-links">
        <router-link to="/register">还没有账号？立即注册</router-link>
        <router-link to="/forgot-password">忘记密码？</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { ROLE_HOME } from '@/router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  email: '',
  password: ''
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login({
        email: loginForm.email,
        password: loginForm.password
      })
      // 获取用户信息
      await userStore.getUserInfo()
      ElMessage.success('登录成功')
      // 根据角色跳转对应首页
      const roleCode = userStore.userInfo.roleCode
      const home = ROLE_HOME[roleCode] || '/'
      const redirect = route.query.redirect
      router.push(redirect && redirect !== '/login' ? redirect : home)
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a73e8 0%, #4a90d9 100%);
}

.login-card {
  width: 460px;
  padding: 20px 10px;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-title {
  margin: 0 0 8px;
  font-size: 26px;
  color: #1a73e8;
}

.login-subtitle {
  margin: 0;
  font-size: 16px;
  color: #909399;
}

.login-btn {
  width: 100%;
  font-size: 18px;
  letter-spacing: 4px;
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 15px;
}
</style>
