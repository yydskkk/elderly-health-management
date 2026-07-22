<template>
  <div class="forgot-container elderly-theme">
    <el-card class="forgot-card">
      <div class="forgot-header">
        <h2 class="forgot-title">重置密码</h2>
        <p class="forgot-subtitle">社区老年人健康管理服务平台</p>
      </div>

      <el-form
        ref="forgotFormRef"
        :model="forgotForm"
        :rules="forgotRules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="forgotForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-row">
            <el-input v-model="forgotForm.code" placeholder="请输入验证码" :prefix-icon="Key" />
            <el-button type="primary" plain :disabled="codeDisabled" @click="handleSendCode">
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="forgot-btn" :loading="loading" @click="handleReset">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="forgot-links">
        <router-link to="/login">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import { forgotPassword, sendCode } from '@/api/auth'

const router = useRouter()

const forgotFormRef = ref(null)
const loading = ref(false)

const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
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
  if (!forgotForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    await sendCode({ email: forgotForm.email, purpose: 'FORGET_PASSWORD' })
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const handleReset = async () => {
  if (!forgotFormRef.value) return
  await forgotFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await forgotPassword({
        email: forgotForm.email,
        code: forgotForm.code,
        newPassword: forgotForm.newPassword,
        confirmPassword: forgotForm.confirmPassword
      })
      ElMessage.success('密码重置成功，请登录')
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
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a73e8 0%, #4a90d9 100%);
}

.forgot-card {
  width: 460px;
  padding: 20px 10px;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.forgot-header {
  text-align: center;
  margin-bottom: 24px;
}

.forgot-title {
  margin: 0 0 8px;
  font-size: 26px;
  color: #1a73e8;
}

.forgot-subtitle {
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

.forgot-btn {
  width: 100%;
  font-size: 18px;
  letter-spacing: 4px;
}

.forgot-links {
  text-align: center;
  margin-top: 12px;
  font-size: 15px;
}
</style>
