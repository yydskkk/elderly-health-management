<template>
  <div class="register-container elderly-theme">
    <el-card class="register-card">
      <div class="register-header">
        <h2 class="register-title">注册账号</h2>
        <p class="register-subtitle">社区老年人健康管理服务平台</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-radio-group v-model="registerForm.roleCode">
            <el-radio value="ELDERLY">老年用户</el-radio>
            <el-radio value="FAMILY">家属</el-radio>
            <el-radio value="DOCTOR">医护人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="registerForm.code" placeholder="请输入验证码" :prefix-icon="Key" />
            <el-button type="primary" plain :disabled="codeDisabled" @click="handleSendCode">
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-links">
        <router-link to="/login">已有账号？返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, User, Phone, Key } from '@element-plus/icons-vue'
import { register, sendCode } from '@/api/auth'

const router = useRouter()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
  roleCode: 'ELDERLY',
  code: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// ===== 验证码倒计时 =====
const codeDisabled = ref(false)
const codeButtonText = ref('发送验证码')
let countdown = 60
let timer = null

const startCountdown = () => {
  codeDisabled.value = true
  codeButtonText.value = `${countdown}秒后重发`
  timer = setInterval(() => {
    countdown--
    if (countdown <= 0) {
      clearInterval(timer)
      timer = null
      countdown = 60
      codeDisabled.value = false
      codeButtonText.value = '发送验证码'
    } else {
      codeButtonText.value = `${countdown}秒后重发`
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    await sendCode({ email: registerForm.email, purpose: 'REGISTER' })
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({
        email: registerForm.email,
        password: registerForm.password,
        confirmPassword: registerForm.confirmPassword,
        name: registerForm.name,
        phone: registerForm.phone,
        roleCode: registerForm.roleCode,
        code: registerForm.code
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a73e8 0%, #4a90d9 100%);
  padding: 20px 0;
}

.register-card {
  width: 500px;
  padding: 20px 10px;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-title {
  margin: 0 0 8px;
  font-size: 26px;
  color: #1a73e8;
}

.register-subtitle {
  margin: 0;
  font-size: 16px;
  color: #909399;
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-row .el-input {
  flex: 1;
}

.register-btn {
  width: 100%;
  font-size: 18px;
  letter-spacing: 4px;
}

.register-links {
  text-align: center;
  margin-top: 12px;
  font-size: 15px;
}
</style>
