<template>
  <div class="alert-rule-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>预警规则列表</span>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增规则</el-button>
        </div>
      </template>

      <div class="table-wrap">
      <el-table v-loading="loading" :data="pagedData" border stripe height="100%" style="width: 100%">
        <el-table-column label="指标类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag>{{ dataTypeText(row.dataType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="规则名称" min-width="160" />
        <el-table-column label="阈值范围" min-width="180">
          <template #default="{ row }">
            <span>{{ thresholdText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.alertLevel)">{{ levelText(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
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

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      width="560px"
      @closed="clearForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="form.dataType" placeholder="请选择数据类型" style="--el-select-width: 100%; width: 100%">
            <el-option
              v-for="item in dataTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item v-if="isBloodPressure" label="收缩压阈值" prop="minValueHigh">
          <div class="threshold-range">
            <el-input-number
              v-model="form.minValueHigh"
              :min="0"
              :max="300"
              placeholder="最小值"
              style="flex: 1"
            />
            <span class="range-sep">~</span>
            <el-input-number
              v-model="form.maxValueHigh"
              :min="0"
              :max="300"
              placeholder="最大值"
              style="flex: 1"
            />
</div>
        </el-form-item>
        <el-form-item v-if="isBloodPressure" label="舒张压阈值" prop="minValueLow">
          <div class="threshold-range">
            <el-input-number
              v-model="form.minValueLow"
              :min="0"
              :max="200"
              placeholder="最小值"
              style="flex: 1"
            />
            <span class="range-sep">~</span>
            <el-input-number
              v-model="form.maxValueLow"
              :min="0"
              :max="200"
              placeholder="最大值"
              style="flex: 1"
            />
</div>
        </el-form-item>
        <el-form-item v-if="!isBloodPressure" label="阈值范围" prop="minValue">
          <div class="threshold-range">
            <el-input-number
              v-model="form.minValue"
              :min="0"
              placeholder="最小值"
              style="flex: 1"
            />
            <span class="range-sep">~</span>
            <el-input-number
              v-model="form.maxValue"
              :min="0"
              placeholder="最大值"
              style="flex: 1"
            />
</div>
        </el-form-item>
        <el-form-item label="预警等级" prop="alertLevel">
          <el-select v-model="form.alertLevel" placeholder="请选择预警等级" style="--el-select-width: 100%; width: 100%">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入规则描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getList, add, update, remove, updateStatus } from '@/api/alertRule'

const loading = ref(false)
const tableData = ref([])

// 客户端分页
const currentPage = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const dataTypeOptions = [
  { value: 'HEART_RATE', label: '心率' },
  { value: 'BLOOD_PRESSURE', label: '血压' },
  { value: 'BLOOD_OXYGEN', label: '血氧' },
  { value: 'BLOOD_SUGAR', label: '血糖' },
  { value: 'TEMPERATURE', label: '体温' },
  { value: 'STEPS', label: '步数' }
]

const dataTypeText = (val) => {
  const item = dataTypeOptions.find((d) => d.value === val)
  return item ? item.label : (val || '-')
}

const levelText = (val) => {
  if (val === 1) return '低'
  if (val === 2) return '中'
  if (val === 3) return '高'
  return '-'
}

const levelTagType = (val) => {
  if (val === 3) return 'danger'
  if (val === 2) return 'warning'
  return 'info'
}

const thresholdText = (row) => {
  if (row.dataType === 'BLOOD_PRESSURE') {
    return `收缩压 ${formatNum(row.minValueHigh)}~${formatNum(row.maxValueHigh)}，舒张压 ${formatNum(row.minValueLow)}~${formatNum(row.maxValueLow)}`
  }
  return `${formatNum(row.minValue)}~${formatNum(row.maxValue)}`
}

const formatNum = (val) => (val === undefined || val === null ? '-' : val)

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
  dataType: '',
  name: '',
  minValue: undefined,
  maxValue: undefined,
  minValueHigh: undefined,
  maxValueHigh: undefined,
  minValueLow: undefined,
  maxValueLow: undefined,
  alertLevel: 2,
  description: '',
  status: 1
})

const isBloodPressure = computed(() => form.dataType === 'BLOOD_PRESSURE')

const formRules = {
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  alertLevel: [{ required: true, message: '请选择预警等级', trigger: 'change' }]
}

const clearForm = () => {
  form.id = undefined
  form.dataType = ''
  form.name = ''
  form.minValue = undefined
  form.maxValue = undefined
  form.minValueHigh = undefined
  form.maxValueHigh = undefined
  form.minValueLow = undefined
  form.maxValueLow = undefined
  form.alertLevel = 2
  form.description = ''
  form.status = 1
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
  form.dataType = row.dataType || ''
  form.name = row.name || ''
  form.minValue = row.minValue
  form.maxValue = row.maxValue
  form.minValueHigh = row.minValueHigh
  form.maxValueHigh = row.maxValueHigh
  form.minValueLow = row.minValueLow
  form.maxValueLow = row.maxValueLow
  form.alertLevel = row.alertLevel || 2
  form.description = row.description || ''
  form.status = row.status === undefined ? 1 : row.status
  formVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { ...form }
      // 血压类型清除单值字段，其他类型清除血压字段
      if (isBloodPressure.value) {
        delete payload.minValue
        delete payload.maxValue
      } else {
        delete payload.minValueHigh
        delete payload.maxValueHigh
        delete payload.minValueLow
        delete payload.maxValueLow
      }
      if (isEdit.value) {
        await update(payload)
        ElMessage.success('编辑成功')
      } else {
        await add(payload)
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

// ===== 启用/禁用 =====
const handleToggleStatus = async (row, val) => {
  const target = val ? 1 : 0
  try {
    await updateStatus(row.id, { status: target })
    ElMessage.success(target === 1 ? '已启用' : '已禁用')
    loadList()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// ===== 删除 =====
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除规则「${row.name}」吗？`, '提示', {
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

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.alert-rule-page {
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

.threshold-range {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.range-sep {
  color: #909399;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  flex-shrink: 0;
}
</style>
