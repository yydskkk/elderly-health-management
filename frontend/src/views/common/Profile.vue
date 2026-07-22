<template>
  <div class="profile-page">
    <el-row :gutter="20" class="profile-row">
      <!-- 左侧：个人信息展示 -->
      <el-col :xs="24" :md="7">
        <el-card class="info-card" shadow="never">
          <div class="avatar-area">
            <el-avatar :size="100" :src="userStore.userInfo.avatar">
              {{ avatarText }}
            </el-avatar>
            <h3 class="user-name">{{ userStore.userInfo.name || '未设置' }}</h3>
            <el-tag type="primary" size="large">{{ userStore.userInfo.roleName || userStore.userInfo.roleCode || '用户' }}</el-tag>
          </div>
          <el-divider />
          <ul class="info-list">
            <li>
              <el-icon class="info-icon"><Message /></el-icon>
              <span class="label">邮箱</span>
              <span class="value">{{ userStore.userInfo.email || '-' }}</span>
            </li>
            <li>
              <el-icon class="info-icon"><User /></el-icon>
              <span class="label">性别</span>
              <span class="value">{{ genderText }}</span>
            </li>
            <li>
              <el-icon class="info-icon"><Calendar /></el-icon>
              <span class="label">年龄</span>
              <span class="value">{{ userStore.userInfo.age || '-' }}</span>
            </li>
            <li>
              <el-icon class="info-icon"><Phone /></el-icon>
              <span class="label">手机号</span>
              <span class="value">{{ userStore.userInfo.phone || '-' }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>

      <!-- 右侧：编辑表单 -->
      <el-col :xs="24" :md="17">
        <el-card class="edit-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><EditPen /></el-icon>
              <span>编辑个人信息</span>
            </div>
          </template>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item label="姓名" prop="name">
              <el-input v-model="profileForm.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" disabled />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="profileForm.age" :min="0" :max="150" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="头像地址" prop="avatar">
              <el-input v-model="profileForm.avatar" placeholder="请输入头像图片地址" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSave">保存修改</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="warning" plain @click="passwordVisible = true">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getProfile, updateProfile, changePassword } from '@/api/profile'

const userStore = useUserStore()

const profileFormRef = ref(null)
const loading = ref(false)

const profileForm = reactive({
  name: '',
  email: '',
  gender: 1,
  age: undefined,
  phone: '',
  avatar: ''
})

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const avatarText = computed(() => {
  const name = userStore.userInfo.name
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const genderText = computed(() => {
  const g = userStore.userInfo.gender
  if (g === 1 || g === '1') return '男'
  if (g === 0 || g === '0') return '女'
  return '-'
})

// 加载个人信息
const loadProfile = async () => {
  try {
    const data = await getProfile()
    const info = data || userStore.userInfo
    profileForm.name = info.name || ''
    profileForm.email = info.email || ''
    profileForm.gender = info.gender != null ? info.gender : 1
    profileForm.age = info.age
    profileForm.phone = info.phone || ''
    profileForm.avatar = info.avatar || ''
    userStore.updateUserInfo(info)
  } catch (e) {
    // 使用 store 中的信息兜底
    const info = userStore.userInfo
    profileForm.name = info.name || ''
    profileForm.email = info.email || ''
    profileForm.gender = info.gender != null ? info.gender : 1
    profileForm.age = info.age
    profileForm.phone = info.phone || ''
    profileForm.avatar = info.avatar || ''
  }
}

const handleSave = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await updateProfile({
        name: profileForm.name,
        gender: profileForm.gender,
        age: profileForm.age,
        phone: profileForm.phone,
        avatar: profileForm.avatar
      })
      ElMessage.success('保存成功')
      userStore.updateUserInfo({
        name: profileForm.name,
        gender: profileForm.gender,
        age: profileForm.age,
        phone: profileForm.phone,
        avatar: profileForm.avatar
      })
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      loading.value = false
    }
  })
}

const handleReset = () => {
  loadProfile()
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
      ElMessage.success('密码修改成功')
      passwordVisible.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      passwordLoading.value = false
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  width: 100%;
}

.profile-row {
  align-items: stretch;
}

.profile-row .el-col {
  display: flex;
}

.info-card,
.edit-card {
  width: 100%;
  border-radius: 12px;
}

.info-card {
  text-align: center;
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 12px 0 8px;
}

.user-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
  text-align: left;
}

.info-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 4px;
  font-size: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.info-list li:last-child {
  border-bottom: none;
}

.info-icon {
  font-size: 18px;
  color: #409EFF;
  flex-shrink: 0;
}

.info-list .label {
  color: #909399;
  width: 56px;
  flex-shrink: 0;
}

.info-list .value {
  flex: 1;
  color: #303133;
  font-weight: 500;
  word-break: break-all;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}
</style>
