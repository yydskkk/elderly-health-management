<template>
  <div class="role-manage-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增角色</el-button>
        </div>
      </template>

      <div class="table-wrap">
      <el-table v-loading="loading" :data="pagedData" border stripe height="100%" style="width: 100%">
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="是否内置" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isBuiltIn === 1 ? 'info' : 'success'">
              {{ row.isBuiltIn === 1 ? '内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="openPermission(row)">分配权限</el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.isBuiltIn === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="480px"
      @closed="clearForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="如：DOCTOR" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permVisible" title="分配权限" width="520px" @closed="clearPerm">
      <div class="perm-role-name">当前角色：<b>{{ permRole.roleName }}</b></div>
      <el-tree
        ref="treeRef"
        :data="permTree"
        :props="treeProps"
        node-key="id"
        show-checkbox
        check-strictly
        default-expand-all
        class="perm-tree"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getRoleList,
  addRole,
  editRole,
  deleteRole,
  getRolePermissions,
  assignPermissions
} from '@/api/role'
import { getPermissionTree } from '@/api/permission'

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
    const data = await getRoleList()
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
  roleCode: '',
  roleName: '',
  description: ''
})

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const clearForm = () => {
  form.id = undefined
  form.roleCode = ''
  form.roleName = ''
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
  form.roleCode = row.roleCode || ''
  form.roleName = row.roleName || ''
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
        // 编辑仅提交 RoleEditDTO 支持的字段（roleCode 不可编辑）
        await editRole({
          id: form.id,
          roleName: form.roleName,
          description: form.description
        })
        ElMessage.success('编辑成功')
      } else {
        await addRole({
          roleCode: form.roleCode,
          roleName: form.roleName,
          description: form.description
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

// ===== 删除 =====
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadList()
    } catch (e) {
      // 错误已由拦截器处理
    }
  }).catch(() => {})
}

// ===== 分配权限 =====
const permVisible = ref(false)
const permLoading = ref(false)
const treeRef = ref(null)
const permTree = ref([])
const permRole = reactive({ id: undefined, roleName: '' })
// 权限树字段映射：PermissionTreeVO 使用 permissionName 作为节点文本
const treeProps = { label: 'permissionName', children: 'children' }

const loadPermTree = async () => {
  try {
    const data = await getPermissionTree()
    permTree.value = Array.isArray(data) ? data : (data?.children || [])
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const openPermission = async (row) => {
  permRole.id = row.id
  permRole.roleName = row.roleName
  if (permTree.value.length === 0) {
    await loadPermTree()
  }
  permVisible.value = true
  try {
    // 后端返回的是权限ID数组 List<Long>
    const data = await getRolePermissions(row.id)
    const ids = Array.isArray(data)
      ? data.map((p) => (typeof p === 'object' ? p.id : p))
      : (data?.permissionIds || data?.ids || [])
    // 等待 tree 渲染完成
    setTimeout(() => {
      treeRef.value?.setCheckedKeys(ids)
    }, 50)
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const clearPerm = () => {
  permRole.id = undefined
  permRole.roleName = ''
  treeRef.value?.setCheckedKeys([])
}

const submitPermission = async () => {
  if (!treeRef.value) return
  permLoading.value = true
  try {
    const checked = treeRef.value.getCheckedKeys()
    const halfChecked = treeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checked, ...halfChecked]
    // 后端接收的是 List<Long>，直接传数组
    await assignPermissions(permRole.id, permissionIds)
    ElMessage.success('权限分配成功')
    permVisible.value = false
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    permLoading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.role-manage-page {
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

.perm-role-name {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.perm-tree {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  flex-shrink: 0;
}
</style>
