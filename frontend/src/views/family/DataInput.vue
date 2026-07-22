<template>
  <div class="family-data-input">
    <el-card shadow="never" class="selector-card">
      <template #header><div class="card-header"><el-icon><Connection /></el-icon><span>选择关联老人</span></div></template>
      <ElderlySelector v-model="elderlyId" @change="handleElderlyChange" />
    </el-card>

    <template v-if="elderlyId">
      <el-card shadow="never">
        <template #header><div class="card-header"><el-icon><EditPen /></el-icon><span>代录入健康数据</span></div></template>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="!elderlyId">
          <el-form-item label="数据类型" prop="dataType">
            <el-radio-group v-model="form.dataType" @change="handleTypeChange">
              <el-radio-button v-for="t in typeOptions" :key="t.value" :value="t.value">{{ t.label }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <template v-if="form.dataType === 'BLOOD_PRESSURE'">
            <el-form-item label="高压值" prop="valueHigh">
              <el-input-number v-model="form.valueHigh" :min="0" :max="300" placeholder="mmHg" />
              <span class="unit-text">mmHg</span>
            </el-form-item>
            <el-form-item label="低压值" prop="valueLow">
              <el-input-number v-model="form.valueLow" :min="0" :max="200" placeholder="mmHg" />
              <span class="unit-text">mmHg</span>
            </el-form-item>
          </template>
          <el-form-item v-else label="数值" prop="value">
            <el-input-number v-model="form.value" :min="0" :precision="2" :step="0.1" />
            <span class="unit-text">{{ currentUnit }}</span>
          </el-form-item>
          <el-form-item label="测量时间" prop="measureTime">
            <el-date-picker v-model="form.measureTime" type="datetime" placeholder="选择测量时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
    <el-card v-else shadow="never"><el-empty description="请先选择关联老人" /></el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue"
import { ElMessage } from "element-plus"
import ElderlySelector from "@/components/ElderlySelector.vue"
import { familyAdd } from "@/api/healthData"

const elderlyId = ref("")
const formRef = ref(null)
const submitLoading = ref(false)

const typeOptions = [
  { value: "BLOOD_PRESSURE", label: "血压" },
  { value: "BLOOD_SUGAR", label: "血糖" },
  { value: "HEART_RATE", label: "心率" },
  { value: "BLOOD_OXYGEN", label: "血氧" },
  { value: "TEMPERATURE", label: "体温" }
]

const unitMap = {
  BLOOD_SUGAR: "mmol/L",
  HEART_RATE: "bpm",
  BLOOD_OXYGEN: "%",
  TEMPERATURE: "℃"
}

const currentUnit = computed(() => unitMap[form.dataType] || "")

const form = reactive({
  dataType: "BLOOD_PRESSURE",
  valueHigh: undefined,
  valueLow: undefined,
  value: undefined,
  measureTime: ""
})

const rules = {
  dataType: [{ required: true, message: "请选择数据类型", trigger: "change" }],
  valueHigh: [{ required: true, message: "请输入高压值", trigger: "blur" }],
  valueLow: [{ required: true, message: "请输入低压值", trigger: "blur" }],
  value: [{ required: true, message: "请输入数值", trigger: "blur" }],
  measureTime: [{ required: true, message: "请选择测量时间", trigger: "change" }]
}

const handleElderlyChange = () => {
  handleReset()
}

const handleTypeChange = () => {
  form.valueHigh = undefined
  form.valueLow = undefined
  form.value = undefined
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!elderlyId.value) {
    ElMessage.warning("请先选择关联老人")
    return
  }
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      // 构造与HealthDataAddDTO一致的payload
      const payload = { dataType: form.dataType, measureTime: form.measureTime }
      if (form.dataType === "BLOOD_PRESSURE") {
        payload.valueHigh = form.valueHigh
        payload.valueLow = form.valueLow
      } else {
        payload.value = form.value
      }
      await familyAdd(elderlyId.value, payload)
      ElMessage.success("录入成功")
      handleReset()
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      submitLoading.value = false
    }
  })
}

const handleReset = () => {
  form.dataType = "BLOOD_PRESSURE"
  form.valueHigh = undefined
  form.valueLow = undefined
  form.value = undefined
  form.measureTime = ""
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.family-data-input { display: flex; flex-direction: column; gap: 16px; }
.selector-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
.unit-text { margin-left: 8px; color: #909399; }
</style>
