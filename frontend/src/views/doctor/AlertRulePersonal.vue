<template>
  <div class="alert-rule-personal-page">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="辖区">
          <el-select v-model="regionId" placeholder="全部辖区" clearable style="--el-select-width: 180px; width: 180px" @change="onRegionChange">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="老人">
          <el-select v-model="elderlyId" placeholder="请选择老人" filterable style="--el-select-width: 220px; width: 220px" @change="loadRules">
            <el-option v-for="item in elderlyOptions" :key="item.userId" :label="item.elderlyName" :value="item.userId" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-empty v-if="!elderlyId" description="请先选择老人" />

    <template v-else>
      <el-card
        v-for="dt in dataTypes"
        :key="dt.code"
        shadow="never"
        class="rule-card"
        v-loading="loading && activeLoadingCode === dt.code"
      >
        <template #header>
          <div class="card-header">
            <span>{{ dt.name }}阈值</span>
            <div>
              <el-tag v-if="meta[dt.code].personal" type="warning" size="small" style="margin-right: 8px">个性化规则</el-tag>
              <el-tag v-else type="info" size="small" style="margin-right: 8px">全局规则（默认）</el-tag>
              <el-button
                v-if="meta[dt.code].personal"
                type="warning"
                plain
                size="small"
                @click="handleRestore(dt.code)"
              >恢复全局</el-button>
            </div>
          </div>
        </template>

        <el-form :model="forms[dt.code]" label-width="120px" inline>
          <!-- 血压：高压/低压上下限 -->
          <template v-if="dt.isBP">
            <el-form-item label="高压下限">
              <el-input-number v-model="forms[dt.code].minValueHigh" :min="0" :max="300" style="width: 160px" />
            </el-form-item>
            <el-form-item label="高压上限">
              <el-input-number v-model="forms[dt.code].maxValueHigh" :min="0" :max="300" style="width: 160px" />
            </el-form-item>
            <el-form-item label="低压下限">
              <el-input-number v-model="forms[dt.code].minValueLow" :min="0" :max="200" style="width: 160px" />
            </el-form-item>
            <el-form-item label="低压上限">
              <el-input-number v-model="forms[dt.code].maxValueLow" :min="0" :max="200" style="width: 160px" />
            </el-form-item>
          </template>
          <!-- 单值指标：上下限 -->
          <template v-else>
            <el-form-item label="下限">
              <el-input-number v-model="forms[dt.code].minValue" :min="0" style="width: 160px" />
            </el-form-item>
            <el-form-item label="上限">
              <el-input-number v-model="forms[dt.code].maxValue" :min="0" style="width: 160px" />
            </el-form-item>
          </template>
          <el-form-item label="预警等级">
            <el-select v-model="forms[dt.code].alertLevel" style="--el-select-width: 120px; width: 120px">
              <el-option label="低" :value="1" />
              <el-option label="中" :value="2" />
              <el-option label="高" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="规则名称">
            <el-input v-model="forms[dt.code].name" placeholder="规则名称" style="width: 200px" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="forms[dt.code].description" placeholder="规则描述" style="width: 240px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitCode === dt.code" @click="submitSave(dt.code)">保存个性化规则</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyRegions } from '@/api/region'
import { getPage as getProfilePage } from '@/api/elderlyProfile'
import { getPersonal, addPersonal, deletePersonal } from '@/api/alertRule'

const loading = ref(false)
const activeLoadingCode = ref('')
const submitCode = ref('')
const regionId = ref('')
const elderlyId = ref('')
const elderlyOptions = ref([])
const regionOptions = ref([])

// 数据类型（与后端 HealthDataType code 对应），血压使用高压/低压双值
const dataTypes = [
  { code: 'BLOOD_PRESSURE', name: '血压', isBP: true },
  { code: 'BLOOD_SUGAR', name: '血糖', isBP: false },
  { code: 'HEART_RATE', name: '心率', isBP: false },
  { code: 'BLOOD_OXYGEN', name: '血氧', isBP: false }
]

// 每个数据类型的可编辑表单（字段与 PersonalAlertRuleAddDTO 对齐）
const forms = reactive({})
// 每个数据类型的元信息：personal 是否个性化、ruleId 个性化规则ID
const meta = reactive({})
dataTypes.forEach((dt) => {
  forms[dt.code] = {
    name: '',
    minValue: undefined,
    maxValue: undefined,
    minValueHigh: undefined,
    maxValueHigh: undefined,
    minValueLow: undefined,
    maxValueLow: undefined,
    alertLevel: 2,
    description: ''
  }
  meta[dt.code] = { personal: false, ruleId: null }
})

const loadRegions = async () => {
  try {
    const data = await getMyRegions()
    const list = Array.isArray(data) ? data : (data?.records || [])
    regionOptions.value = list
  } catch (e) {}
}

const onRegionChange = async () => {
  elderlyId.value = ''
  await loadElderly()
}

const loadElderly = async () => {
  try {
    const params = { pageNum: 1, pageSize: 200 }
    if (regionId.value) params.regionId = regionId.value
    const data = await getProfilePage(params)
    const list = data?.records || []
    // 个性化规则以老人用户ID为维度
    elderlyOptions.value = list.map((e) => ({ userId: e.userId, elderlyName: e.elderlyName }))
  } catch (e) {}
}

// 重置表单为空
const resetForms = () => {
  dataTypes.forEach((dt) => {
    forms[dt.code].name = ''
    forms[dt.code].minValue = undefined
    forms[dt.code].maxValue = undefined
    forms[dt.code].minValueHigh = undefined
    forms[dt.code].maxValueHigh = undefined
    forms[dt.code].minValueLow = undefined
    forms[dt.code].maxValueLow = undefined
    forms[dt.code].alertLevel = 2
    forms[dt.code].description = ''
    meta[dt.code].personal = false
    meta[dt.code].ruleId = null
  })
}

// 用后端返回的规则填充表单
const fillForm = (code, rule) => {
  const f = forms[code]
  f.name = rule.name || ''
  f.minValue = rule.minValue
  f.maxValue = rule.maxValue
  f.minValueHigh = rule.minValueHigh
  f.maxValueHigh = rule.maxValueHigh
  f.minValueLow = rule.minValueLow
  f.maxValueLow = rule.maxValueLow
  f.alertLevel = rule.alertLevel ?? 2
  f.description = rule.description || ''
  meta[code].personal = !!rule.personal
  meta[code].ruleId = rule.personal ? rule.id : null
}

const loadRules = async () => {
  if (!elderlyId.value) {
    resetForms()
    return
  }
  loading.value = true
  activeLoadingCode.value = ''
  try {
    resetForms()
    // 后端 listPersonal：返回个性化规则 + 未个性化的全局规则
    const data = await getPersonal(elderlyId.value)
    const list = Array.isArray(data) ? data : []
    list.forEach((rule) => {
      if (forms[rule.dataType]) {
        fillForm(rule.dataType, rule)
      }
    })
  } catch (e) {
    resetForms()
  } finally {
    loading.value = false
  }
}

const submitSave = async (code) => {
  if (!elderlyId.value) {
    ElMessage.warning('请先选择老人')
    return
  }
  submitCode.value = code
  try {
    const f = forms[code]
    // 构造 PersonalAlertRuleAddDTO
    const payload = {
      userId: elderlyId.value,
      dataType: code,
      name: f.name,
      minValue: f.minValue,
      maxValue: f.maxValue,
      minValueHigh: f.minValueHigh,
      maxValueHigh: f.maxValueHigh,
      minValueLow: f.minValueLow,
      maxValueLow: f.maxValueLow,
      alertLevel: f.alertLevel,
      description: f.description
    }
    await addPersonal(payload)
    ElMessage.success('保存成功')
    await loadRules()
  } catch (e) {} finally {
    submitCode.value = ''
  }
}

const handleRestore = (code) => {
  ElMessageBox.confirm('确定要恢复为全局规则吗？将删除当前个性化规则。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (!meta[code].ruleId) {
      ElMessage.warning('当前无个性化规则')
      return
    }
    try {
      await deletePersonal(meta[code].ruleId)
      ElMessage.success('已恢复为全局规则')
      await loadRules()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(async () => {
  await loadRegions()
  await loadElderly()
})
</script>

<style scoped>
.alert-rule-personal-page { display: flex; flex-direction: column; gap: 16px; }
.filter-card, .rule-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; }
</style>
