<template>
  <div class="user-manage-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="姓名/邮箱"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="queryParams.roleCode"
            placeholder="全部角色"
            clearable
            style="--el-select-width: 160px; width: 160px"
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="--el-select-width: 140px; width: 140px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增用户</el-button>
        </div>
      </template>

      <div class="table-wrap">
        <el-table
          v-loading="loading"
          :data="tableData"
          border
          stripe
          height="100%"
          style="width: 100%"
        >
          <el-table-column label="头像" width="70" align="center">
            <template #default="{ row }">
              <el-avatar :size="36" :src="row.avatar">
                {{ row.name ? row.name.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" min-width="100" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="角色" width="110" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.roleName" type="primary">{{ row.roleName }}</el-tag>
              <span v-else>{{ row.roleCode || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="性别" width="70" align="center">
            <template #default="{ row }">{{ genderText(row.gender) }}</template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="70" align="center" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="300" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" size="small" @click="openReset(row)">重置密码</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      @closed="clearForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item v-if="!isEdit" label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱（登录账号）" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择角色" style="--el-select-width: 100%; width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="0" :max="150" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="440px" @closed="clearPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="用户">
          <el-input :model-value="pwdForm.name" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="submitReset">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserPage, addUser, editUser, updateStatus, resetPassword, deleteUser } from '@/api/user'
import { getRoleList } from '@/api/role'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const roleOptions = ref([])

const queryParams = reactive({
  keyword: '',
  roleCode: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const genderText = (g) => {
  if (g === 1) return '男'
  if (g === 0) return '女'
  return '-'
}

const loadList = async () => {
  loading.value = true
  try {
    const data = await getUserPage(queryParams)
    tableData.value = data?.records || data?.list || []
    total.value = Number(data?.total) || 0
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadList()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.roleCode = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  loadList()
}

const loadRoles = async () => {
  try {
    const data = await getRoleList()
    const list = Array.isArray(data) ? data : (data?.records || data?.list || [])
    roleOptions.value = list.map((r) => ({
      code: r.code || r.roleCode,
      name: r.name || r.roleName
    }))
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// ===== 新增/编辑 =====
const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: undefined,
  email: '',
  password: '',
  name: '',
  roleCode: '',
  gender: 1,
  age: undefined,
  phone: ''
})

const formRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const clearForm = () => {
  form.id = undefined
  form.email = ''
  form.password = ''
  form.name = ''
  form.roleCode = ''
  form.gender = 1
  form.age = undefined
  form.phone = ''
  formRef.value?.clearValidate()
}

const openAdd = () => {
  isEdit.value = false
  clearForm()
  formVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.email = row.email || ''
  form.password = ''
  form.name = row.name || ''
  form.roleCode = row.roleCode || ''
  form.gender = row.gender === undefined ? 1 : row.gender
  form.age = row.age
  form.phone = row.phone || ''
  formVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        // 编辑提交 UserEditDTO 支持的字段（含角色）
        await editUser({
          id: form.id,
          name: form.name,
          roleCode: form.roleCode,
          gender: form.gender,
          age: form.age,
          phone: form.phone
        })
        ElMessage.success('编辑成功')
      } else {
        // 新增提交 UserAddDTO 支持的字段
        await addUser({
          email: form.email,
          password: form.password,
          name: form.name,
          roleCode: form.roleCode,
          gender: form.gender,
          age: form.age,
          phone: form.phone
        })
        ElMessage.success('新增成功')
      }
      formVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      submitLoading.value = false
    }
  })
}

// ===== 禁用/启用 =====
const handleToggleStatus = (row) => {
  const target = row.status === 1 ? 0 : 1
  const action = target === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}用户「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateStatus({ userId: row.id, status: target })
      ElMessage.success(`${action}成功`)
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

// ===== 删除 =====
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户「${row.name}」吗？删除后不可恢复。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

// ===== 重置密码 =====
const resetVisible = ref(false)
const resetLoading = ref(false)
const pwdFormRef = ref(null)

const pwdForm = reactive({
  id: undefined,
  name: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const clearPwdForm = () => {
  pwdForm.id = undefined
  pwdForm.name = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

const openReset = (row) => {
  pwdForm.id = row.id
  pwdForm.name = row.name
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  resetVisible.value = true
}

const submitReset = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    resetLoading.value = true
    try {
      await resetPassword({
        userId: pwdForm.id,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码重置成功')
      resetVisible.value = false
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      resetLoading.value = false
    }
  })
}

onMounted(() => {
  loadRoles()
  loadList()
})
</script>

<style scoped>
.user-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 100px);
}

.search-card {
  border-radius: 8px;
  flex-shrink: 0;
}

.table-card {
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.table-card :deep(.el-card__header) {
  flex-shrink: 0;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.table-wrap {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  flex-shrink: 0;
}
</style>
