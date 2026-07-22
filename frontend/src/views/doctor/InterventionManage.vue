<template>
  <div class="intervention-manage-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>干预方案管理</span>
          <el-button type="primary" :icon="Refresh" @click="loadList">刷新</el-button>
        </div>
      </template>

      <div class="table-wrap">
      <el-table v-loading="loading" :data="tableData" border stripe height="100%" style="width: 100%">
        <el-table-column prop="userName" label="老人姓名" min-width="100" />
        <el-table-column prop="adviceTitle" label="建议标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="方案内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="cycle" label="执行周期" width="110" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" align="center" />
        <el-table-column prop="endDate" label="结束日期" width="120" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" link type="danger" size="small" @click="handleTerminate(row)">终止</el-button>
            <el-button link type="warning" size="small" @click="openFeedback(row)">反馈</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>

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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑干预方案" width="560px" @closed="clearEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="方案内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="3" placeholder="请输入方案内容" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="editForm.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="editForm.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="执行周期" prop="cycle">
          <el-input v-model="editForm.cycle" placeholder="如：每日/每周" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 反馈列表弹窗 -->
    <el-dialog v-model="feedbackVisible" title="干预方案反馈" width="760px">
      <el-table v-loading="feedbackLoading" :data="feedbackList" border stripe size="small">
        <el-table-column prop="feedbackTime" label="反馈时间" min-width="160" />
        <el-table-column prop="userName" label="反馈人" width="110" align="center" />
        <el-table-column label="是否执行" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isExecuted === 1 ? 'success' : 'info'" size="small">
              {{ row.isExecuted === 1 ? '已执行' : '未执行' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionFeeling" label="执行感受" min-width="180" show-overflow-tooltip />
        <el-table-column prop="abnormalSituation" label="异常情况" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!feedbackLoading && feedbackList.length === 0" description="暂无反馈记录" />
      <template #footer>
        <el-button @click="feedbackVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getPage, update, terminate, getFeedbacks } from '@/api/interventionPlan'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

// 状态：0已终止1进行中2已完成
const statusType = (s) => {
  const map = { 0: 'danger', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}

const statusText = (s) => {
  const map = { 0: '已终止', 1: '进行中', 2: '已完成' }
  return map[s] || '未知'
}

const loadList = async () => {
  loading.value = true
  try {
    const data = await getPage(queryParams)
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

// 编辑
const editVisible = ref(false)
const submitLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: undefined,
  content: '',
  startDate: '',
  endDate: '',
  cycle: ''
})

const editRules = {
  content: [{ required: true, message: '请输入方案内容', trigger: 'blur' }]
}

const clearEditForm = () => {
  editForm.id = undefined
  editForm.content = ''
  editForm.startDate = ''
  editForm.endDate = ''
  editForm.cycle = ''
  editFormRef.value?.clearValidate()
}

const openEdit = (row) => {
  editForm.id = row.id
  editForm.content = row.content || ''
  editForm.startDate = row.startDate || ''
  editForm.endDate = row.endDate || ''
  editForm.cycle = row.cycle || ''
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await update({ ...editForm })
      ElMessage.success('保存成功')
      editVisible.value = false
      loadList()
    } catch (e) {} finally {
      submitLoading.value = false
    }
  })
}

// 终止
const handleTerminate = (row) => {
  ElMessageBox.confirm(`确定要终止「${row.userName}」的干预方案吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await terminate(row.id)
      ElMessage.success('已终止')
      loadList()
    } catch (e) {}
  }).catch(() => {})
}

// 反馈
const feedbackVisible = ref(false)
const feedbackLoading = ref(false)
const feedbackList = ref([])

const openFeedback = async (row) => {
  feedbackVisible.value = true
  feedbackLoading.value = true
  feedbackList.value = []
  try {
    const data = await getFeedbacks(row.id)
    feedbackList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
  } catch (e) {} finally {
    feedbackLoading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.intervention-manage-page { display: flex; flex-direction: column; gap: 16px; height: calc(100vh - 100px); }
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
.card-header { display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; flex-shrink: 0; }
</style>