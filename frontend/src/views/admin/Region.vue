<template>
  <div class="region-manage-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>辖区列表</span>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增辖区</el-button>
        </div>
      </template>

      <div class="table-wrap">
      <el-table v-loading="loading" :data="pagedData" border stripe height="100%" style="width: 100%">
        <el-table-column prop="regionName" label="辖区名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="elderlyCount" label="老人数" width="100" align="center" />
        <el-table-column prop="doctorCount" label="医护数" width="100" align="center" />
        <el-table-column label="操作" width="360" fixed="right" align="center">
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
            <el-button link type="warning" size="small" @click="openAssign(row)">分配用户</el-button>
            <el-button link type="info" size="small" @click="openUsers(row)">查看用户</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div class="pagination-wrap" v-if="tableData.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          :total="tableData.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- 新增/编辑辖区弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑辖区' : '新增辖区'"
      width="480px"
      @closed="clearForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="辖区名称" prop="regionName">
          <el-input v-model="form.regionName" placeholder="请输入辖区名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入辖区描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 分配用户弹窗 -->
    <el-dialog v-model="assignVisible" title="分配用户" width="480px" @closed="clearAssign">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="90px">
        <el-form-item label="辖区">
          <el-input :model-value="assignForm.regionName" disabled />
        </el-form-item>
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="assignForm.userId"
            filterable
            placeholder="请选择用户"
            style="--el-select-width: 100%; width: 100%"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="`${u.name}（${u.email || '-'}）`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="assignForm.userType">
            <el-radio :value="1">老人</el-radio>
            <el-radio :value="2">医护</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 查看辖区用户弹窗 -->
    <el-dialog v-model="usersVisible" title="辖区用户列表" width="720px" @closed="clearUsers">
      <div class="users-title">辖区：<b>{{ usersRegion.regionName }}</b></div>
      <el-table :data="regionUsers" border stripe size="small" style="width: 100%">
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleRemoveAssign(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="usersVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getList,
  add,
  update,
  remove,
  updateStatus,
  assign,
  removeAssign,
  getUsers
} from '@/api/region'
import { getUserPage } from '@/api/user'

const loading = ref(false)
const tableData = ref([])

// 客户端分页
const currentPage = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const loadList = async () => {
  loading.value = true
  try {
    const data = await getList()
    tableData.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
    currentPage.value = 1
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

// ===== 新增/编辑 =====
const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: undefined,
  regionName: '',
  description: ''
})

const formRules = {
  regionName: [{ required: true, message: '请输入辖区名称', trigger: 'blur' }]
}

const clearForm = () => {
  form.id = undefined
  form.regionName = ''
  form.description = ''
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
  form.regionName = row.regionName || ''
  form.description = row.description || ''
  formVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await update({ id: form.id, regionName: form.regionName, description: form.description })
        ElMessage.success('编辑成功')
      } else {
        await add({ regionName: form.regionName, description: form.description })
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

// ===== 删除 =====
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除辖区「${row.regionName}」吗？辖区下有关联用户时无法删除。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await remove(row.id)
      ElMessage.success('删除成功')
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

// ===== 启用/禁用 =====
const handleToggleStatus = (row) => {
  const target = row.status === 1 ? 0 : 1
  const action = target === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}辖区「${row.regionName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateStatus(row.id, { status: target })
      ElMessage.success(`${action}成功`)
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

// ===== 分配用户 =====
const assignVisible = ref(false)
const assignLoading = ref(false)
const assignFormRef = ref(null)
const userOptions = ref([])

const assignForm = reactive({
  regionId: undefined,
  regionName: '',
  userId: undefined,
  userType: 1
})

const assignRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

// 性别文本：0女1男
const genderText = (g) => {
  if (g === 1) return '男'
  if (g === 0) return '女'
  return '-'
}

const loadUserOptions = async () => {
  try {
    const data = await getUserPage({ pageNum: 1, pageSize: 200 })
    userOptions.value = data?.records || data?.list || []
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const clearAssign = () => {
  assignForm.regionId = undefined
  assignForm.regionName = ''
  assignForm.userId = undefined
  assignForm.userType = 1
  assignFormRef.value?.clearValidate()
}

const openAssign = async (row) => {
  assignForm.regionId = row.id
  assignForm.regionName = row.regionName
  if (userOptions.value.length === 0) {
    await loadUserOptions()
  }
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    assignLoading.value = true
    try {
      await assign({
        regionId: assignForm.regionId,
        userId: assignForm.userId,
        userType: assignForm.userType
      })
      ElMessage.success('分配成功')
      assignVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      assignLoading.value = false
    }
  })
}

// ===== 查看辖区用户 =====
const usersVisible = ref(false)
const usersRegion = reactive({ id: undefined, regionName: '' })
const regionUsers = ref([])

const clearUsers = () => {
  usersRegion.id = undefined
  usersRegion.regionName = ''
  regionUsers.value = []
}

const openUsers = async (row) => {
  usersRegion.id = row.id
  usersRegion.regionName = row.regionName
  usersVisible.value = true
  try {
    const data = await getUsers(row.id)
    regionUsers.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const handleRemoveAssign = (row) => {
  ElMessageBox.confirm(`确定要将用户「${row.name}」移出该辖区吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 后端 unassign 仅需 userId 和 regionId
      await removeAssign({
        regionId: usersRegion.id,
        userId: row.id
      })
      ElMessage.success('移除成功')
      const data = await getUsers(usersRegion.id)
      regionUsers.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.region-manage-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.table-card {
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
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

.users-title {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  flex-shrink: 0;
}
</style>
