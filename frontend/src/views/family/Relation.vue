<template>
  <div class="family-relation">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我的关联" name="my" />
        <el-tab-pane label="发起关联" name="apply" />
        <el-tab-pane label="待确认关联" name="pending" />
      </el-tabs>

      <!-- 我的关联 -->
      <div v-if="activeTab === 'my'">
        <el-table v-loading="myLoading" :data="myPagedData" border stripe max-height="calc(100vh - 280px)">
          <el-table-column label="老人姓名" min-width="120">
            <template #default="{ row }">{{ row.elderlyName || row.name || "-" }}</template>
          </el-table-column>
          <el-table-column label="邮箱" min-width="180">
            <template #default="{ row }">{{ row.elderlyEmail || row.email || "-" }}</template>
          </el-table-column>
          <el-table-column label="关系" width="120" align="center">
            <template #default="{ row }"><el-tag>{{ row.relationType || row.relation || "-" }}</el-tag></template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }"><el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="关联时间" min-width="160" />
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="handleUnlink(row)">解除关联</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="myList.length > 0">
          <el-pagination
            v-model:current-page="myCurrentPage"
            v-model:page-size="myPageSize"
            :page-sizes="[5, 10, 20]"
            :total="myList.length"
            layout="total, sizes, prev, pager, next, jumper"
            background
          />
        </div>
      </div>

      <!-- 发起关联 -->
      <div v-if="activeTab === 'apply'">
        <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px" style="max-width: 500px">
          <el-form-item label="老人邮箱" prop="elderlyEmail">
            <el-input v-model="applyForm.elderlyEmail" placeholder="请输入老人邮箱" />
          </el-form-item>
          <el-form-item label="关系类型" prop="relationType">
            <el-select v-model="applyForm.relationType" placeholder="请选择关系类型" style="--el-select-width: 100%; width: 100%">
              <el-option v-for="r in relationOptions" :key="r" :label="r" :value="r" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="applyLoading" @click="handleApply">提交申请</el-button>
            <el-button @click="resetApplyForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 待确认关联 -->
      <div v-if="activeTab === 'pending'">
        <el-table v-loading="pendingLoading" :data="pendingPagedData" border stripe max-height="calc(100vh - 280px)">
          <el-table-column label="老人姓名" min-width="120">
            <template #default="{ row }">{{ row.elderlyName || row.name || "-" }}</template>
          </el-table-column>
          <el-table-column label="邮箱" min-width="180">
            <template #default="{ row }">{{ row.elderlyEmail || row.email || "-" }}</template>
          </el-table-column>
          <el-table-column label="关系" width="120" align="center">
            <template #default="{ row }"><el-tag>{{ row.relationType || row.relation || "-" }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" min-width="160" />
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="success" size="small" @click="handleConfirm(row)">确认</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="pendingList.length > 0">
          <el-pagination
            v-model:current-page="pendingCurrentPage"
            v-model:page-size="pendingPageSize"
            :page-sizes="[5, 10, 20]"
            :total="pendingList.length"
            layout="total, sizes, prev, pager, next, jumper"
            background
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { getList, apply, confirm, reject, unlink } from "@/api/relation"

const activeTab = ref("my")
const myLoading = ref(false)
const pendingLoading = ref(false)
const myList = ref([])
const pendingList = ref([])

// 客户端分页 - 我的关联
const myCurrentPage = ref(1)
const myPageSize = ref(10)
const myPagedData = computed(() => {
  const start = (myCurrentPage.value - 1) * myPageSize.value
  return myList.value.slice(start, start + myPageSize.value)
})

// 客户端分页 - 待确认关联
const pendingCurrentPage = ref(1)
const pendingPageSize = ref(10)
const pendingPagedData = computed(() => {
  const start = (pendingCurrentPage.value - 1) * pendingPageSize.value
  return pendingList.value.slice(start, start + pendingPageSize.value)
})

const relationOptions = ["子女", "配偶", "父母", "兄弟姐妹", "其他"]

// RelationVO: status 0待确认1已关联2已拒绝
const statusText = (val) => {
  if (val === 0) return "待确认"
  if (val === 1) return "已关联"
  if (val === 2) return "已拒绝"
  return "-"
}
const statusTagType = (val) => {
  if (val === 0) return "warning"
  if (val === 1) return "success"
  if (val === 2) return "info"
  return "primary"
}

const loadMyList = async () => {
  myLoading.value = true
  try {
    // 查询已确认的关联（status=1），返回RelationVO含完整关联信息
    const data = await getList({ status: 1 })
    myList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
    myCurrentPage.value = 1
  } catch (e) {
    myList.value = []
  } finally {
    myLoading.value = false
  }
}

const loadPendingList = async () => {
  pendingLoading.value = true
  try {
    // 查询待确认的关联（status=0）
    const data = await getList({ status: 0 })
    pendingList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
    pendingCurrentPage.value = 1
  } catch (e) {
    pendingList.value = []
  } finally {
    pendingLoading.value = false
  }
}

const handleTabChange = () => {
  if (activeTab.value === "my") loadMyList()
  if (activeTab.value === "pending") loadPendingList()
}

// 发起关联
const applyFormRef = ref(null)
const applyLoading = ref(false)
const applyForm = reactive({ elderlyEmail: "", relationType: "" })
const applyRules = {
  elderlyEmail: [{ required: true, message: "请输入老人邮箱", trigger: "blur" }, { type: "email", message: "请输入正确的邮箱", trigger: "blur" }],
  relationType: [{ required: true, message: "请选择关系类型", trigger: "change" }]
}

const handleApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    applyLoading.value = true
    try { await apply({ elderlyEmail: applyForm.elderlyEmail, relationType: applyForm.relationType }); ElMessage.success("申请已提交，等待对方确认"); resetApplyForm() }
    catch (e) {} finally { applyLoading.value = false }
  })
}

const resetApplyForm = () => { applyForm.elderlyEmail = ""; applyForm.relationType = ""; applyFormRef.value?.clearValidate() }

// 确认/拒绝
const handleConfirm = async (row) => {
  try { await confirm(row.id); ElMessage.success("已确认关联"); loadPendingList() }
  catch (e) {}
}

const handleReject = (row) => {
  ElMessageBox.confirm("确定要拒绝该关联申请吗？", "提示", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" })
    .then(async () => { try { await reject(row.id); ElMessage.success("已拒绝关联"); loadPendingList() } catch (e) {} })
    .catch(() => {})
}

// 解除关联
const handleUnlink = (row) => {
  ElMessageBox.confirm(`确定要解除与「${row.elderlyName || row.name || "该老人"}」的关联吗？`, "提示", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" })
    .then(async () => { try { await unlink(row.id); ElMessage.success("已解除关联"); loadMyList() } catch (e) {} })
    .catch(() => {})
}

onMounted(() => { loadMyList() })
</script>

<style scoped>
.family-relation { display: flex; flex-direction: column; gap: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }
</style>
