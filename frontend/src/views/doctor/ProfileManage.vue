<template>
  <div class="profile-manage-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="辖区">
          <el-select v-model="queryParams.regionId" placeholder="全部辖区" clearable style="--el-select-width: 200px; width: 200px" @change="handleSearch">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.keyword" placeholder="老人姓名" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header"><span>健康档案列表</span></div>
      </template>
      <div class="table-wrap">
      <el-table v-loading="loading" :data="tableData" border stripe height="100%" style="width: 100%">
        <el-table-column prop="elderlyName" label="老人姓名" min-width="100" />
        <el-table-column prop="regionName" label="辖区" min-width="120" />
        <el-table-column prop="bloodType" label="血型" width="80" align="center" />
        <el-table-column label="身高/体重" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.height ? row.height + 'cm' : '-' }} / {{ row.weight ? row.weight + 'kg' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="emergencyContact" label="紧急联系人" min-width="110" show-overflow-tooltip />
        <el-table-column prop="emergencyPhone" label="紧急联系电话" width="140" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openView(row)">查看</el-button>
            <el-button link type="warning" size="small" @click="openEdit(row)">编辑</el-button>
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

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewVisible" title="档案详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderText(viewData.gender) }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ viewData.age }}</el-descriptions-item>
        <el-descriptions-item label="血型">{{ viewData.bloodType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高">{{ viewData.height ? viewData.height + ' cm' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ viewData.weight ? viewData.weight + ' kg' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ viewData.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ viewData.emergencyContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系电话">{{ viewData.emergencyPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="既往病史" :span="2">{{ viewData.medicalHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="2">{{ viewData.allergyHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家族病史" :span="2">{{ viewData.familyHistory || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑健康档案" width="640px" @closed="clearEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="110px">
        <el-form-item label="既往病史" prop="medicalHistory">
          <el-input v-model="editForm.medicalHistory" type="textarea" :rows="2" placeholder="请输入既往病史" />
        </el-form-item>
        <el-form-item label="过敏史" prop="allergyHistory">
          <el-input v-model="editForm.allergyHistory" type="textarea" :rows="2" placeholder="请输入过敏史" />
        </el-form-item>
        <el-form-item label="家族病史" prop="familyHistory">
          <el-input v-model="editForm.familyHistory" type="textarea" :rows="2" placeholder="请输入家族病史" />
        </el-form-item>
        <el-form-item label="血型" prop="bloodType">
          <el-select v-model="editForm.bloodType" placeholder="请选择血型" clearable style="--el-select-width: 100%; width: 100%">
            <el-option label="A型" value="A" />
            <el-option label="B型" value="B" />
            <el-option label="AB型" value="AB" />
            <el-option label="O型" value="O" />
          </el-select>
        </el-form-item>
        <el-form-item label="身高(cm)" prop="height">
          <el-input-number v-model="editForm.height" :min="0" :max="300" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="editForm.weight" :min="0" :max="500" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="紧急联系人" prop="emergencyContact">
          <el-input v-model="editForm.emergencyContact" placeholder="请输入紧急联系人姓名" />
        </el-form-item>
        <el-form-item label="紧急联系电话" prop="emergencyPhone">
          <el-input v-model="editForm.emergencyPhone" placeholder="请输入紧急联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getPage, getById, update } from '@/api/elderlyProfile'
import { getMyRegions } from '@/api/region'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const regionOptions = ref([])

const queryParams = reactive({
  regionId: '',
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

// 性别：0女1男
const genderText = (g) => {
  if (g === 1) return '男'
  if (g === 0) return '女'
  return '-'
}

const loadRegions = async () => {
  try {
    const data = await getMyRegions()
    const list = Array.isArray(data) ? data : (data?.records || [])
    regionOptions.value = list
  } catch (e) {}
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

const handleSearch = () => {
  queryParams.pageNum = 1
  loadList()
}

const handleReset = () => {
  queryParams.regionId = ''
  queryParams.keyword = ''
  queryParams.pageNum = 1
  loadList()
}

// 查看
const viewVisible = ref(false)
const viewData = reactive({})
const openView = async (row) => {
  try {
    const data = await getById(row.id)
    Object.assign(viewData, data || row)
    viewVisible.value = true
  } catch (e) {
    Object.assign(viewData, row)
    viewVisible.value = true
  }
}

// 编辑
const editVisible = ref(false)
const submitLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: undefined,
  medicalHistory: '',
  allergyHistory: '',
  familyHistory: '',
  bloodType: '',
  height: undefined,
  weight: undefined,
  emergencyContact: '',
  emergencyPhone: ''
})

const editRules = {
  emergencyPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const clearEditForm = () => {
  editForm.id = undefined
  editForm.medicalHistory = ''
  editForm.allergyHistory = ''
  editForm.familyHistory = ''
  editForm.bloodType = ''
  editForm.height = undefined
  editForm.weight = undefined
  editForm.emergencyContact = ''
  editForm.emergencyPhone = ''
  editFormRef.value?.clearValidate()
}

const openEdit = async (row) => {
  try {
    const data = await getById(row.id)
    const info = data || row
    editForm.id = info.id
    editForm.medicalHistory = info.medicalHistory || ''
    editForm.allergyHistory = info.allergyHistory || ''
    editForm.familyHistory = info.familyHistory || ''
    editForm.bloodType = info.bloodType || ''
    editForm.height = info.height
    editForm.weight = info.weight
    editForm.emergencyContact = info.emergencyContact || ''
    editForm.emergencyPhone = info.emergencyPhone || ''
  } catch (e) {
    editForm.id = row.id
  }
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

onMounted(() => {
  loadRegions()
  loadList()
})
</script>

<style scoped>
.profile-manage-page { display: flex; flex-direction: column; gap: 16px; height: calc(100vh - 100px); }
.search-card, .table-card { border-radius: 8px; }
.search-card { flex-shrink: 0; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; }
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
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; flex-shrink: 0; }
</style>