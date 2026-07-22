<template>
  <div class="alert-handle-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>预警处理</span>
          <el-radio-group v-model="activeStatus" @change="handleTabChange">
            <el-radio-button :value="0">待处理</el-radio-button>
            <el-radio-button :value="1">已处理</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="table-wrap">
      <el-table v-loading="loading" :data="tableData" border stripe height="100%" style="width: 100%">
        <el-table-column prop="userName" label="老人姓名" min-width="100" />
        <el-table-column label="异常指标" min-width="100">
          <template #default="{ row }">{{ dataTypeText(row.dataType) }}</template>
        </el-table-column>
        <el-table-column prop="value" label="数值" width="110" align="center" />
        <el-table-column label="预警等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelType(row.alertLevel)" size="small">{{ levelText(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发生时间" min-width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'" size="small">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" link type="warning" size="small" @click="openHandle(row)">处理</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="预警详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="老人姓名">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="异常指标">{{ dataTypeText(detailData.dataType) }}</el-descriptions-item>
        <el-descriptions-item label="数值">{{ detailData.value }}</el-descriptions-item>
        <el-descriptions-item label="预警等级">{{ levelText(detailData.alertLevel) }}</el-descriptions-item>
        <el-descriptions-item label="发生时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 0 ? 'danger' : 'success'" size="small">
            {{ detailData.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detailData.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ detailData.handleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" :span="2">{{ detailData.handleOpinion || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理预警" width="520px" @closed="clearHandleForm">
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="老人姓名">
          <el-input :model-value="handleForm.userName" disabled />
        </el-form-item>
        <el-form-item label="异常指标">
          <el-input :model-value="dataTypeText(handleForm.dataType)" disabled />
        </el-form-item>
        <el-form-item label="数值">
          <el-input :model-value="handleForm.value" disabled />
        </el-form-item>
        <el-form-item label="处理意见" prop="handleOpinion">
          <el-input v-model="handleForm.handleOpinion" type="textarea" :rows="4" placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitHandle">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecordPage, getRecordById, handleRecord } from '@/api/alert'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeStatus = ref(0)

const queryParams = reactive({
  status: 0,
  pageNum: 1,
  pageSize: 10
})

// 数据类型映射（与后端 HealthDataType code 对应）
const dataTypeMap = {
  BLOOD_PRESSURE: '血压',
  BLOOD_SUGAR: '血糖',
  HEART_RATE: '心率',
  BLOOD_OXYGEN: '血氧',
  TEMPERATURE: '体温',
  STEPS: '步数'
}
const dataTypeText = (code) => dataTypeMap[code] || code || '-'

const levelType = (level) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[level] || 'info'
}

const levelText = (level) => {
  const map = { 1: '低', 2: '中', 3: '高' }
  return map[level] || '低'
}

const loadList = async () => {
  loading.value = true
  try {
    const data = await getRecordPage(queryParams)
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {} finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  queryParams.status = activeStatus.value
  queryParams.pageNum = 1
  loadList()
}

// 详情
const detailVisible = ref(false)
const detailData = reactive({})
const openDetail = async (row) => {
  try {
    const data = await getRecordById(row.id)
    Object.assign(detailData, data || row)
  } catch (e) {
    Object.assign(detailData, row)
  }
  detailVisible.value = true
}

// 处理
const handleVisible = ref(false)
const submitLoading = ref(false)
const handleFormRef = ref(null)
const handleForm = reactive({
  id: undefined,
  userName: '',
  dataType: '',
  value: '',
  handleOpinion: ''
})

const handleRules = {
  handleOpinion: [{ required: true, message: '请输入处理意见', trigger: 'blur' }]
}

const clearHandleForm = () => {
  handleForm.id = undefined
  handleForm.userName = ''
  handleForm.dataType = ''
  handleForm.value = ''
  handleForm.handleOpinion = ''
  handleFormRef.value?.clearValidate()
}

const openHandle = (row) => {
  handleForm.id = row.id
  handleForm.userName = row.userName
  handleForm.dataType = row.dataType
  handleForm.value = row.value
  handleForm.handleOpinion = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!handleFormRef.value) return
  await handleFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await handleRecord({ id: handleForm.id, handleOpinion: handleForm.handleOpinion })
      ElMessage.success('处理成功')
      handleVisible.value = false
      loadList()
    } catch (e) {} finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.alert-handle-page { display: flex; flex-direction: column; gap: 16px; height: calc(100vh - 100px); }
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