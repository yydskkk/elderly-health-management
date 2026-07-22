<template>
  <div class="data-input-page">
    <el-card shadow="never" class="input-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="24"><EditPen /></el-icon>
          <span>录入健康数据</span>
        </div>
      </template>

      <!-- 数据类型快捷选择 -->
      <div class="type-section">
        <div class="section-title">请选择数据类型</div>
        <el-row :gutter="16">
          <el-col :xs="12" :sm="6" v-for="t in typeList" :key="t.key">
            <div
              class="type-card"
              :class="{ active: form.type === t.key }"
              :style="form.type === t.key ? { borderColor: t.color, background: t.bg } : {}"
              @click="selectType(t.key)"
            >
              <div class="type-icon" :style="{ color: t.color }">
                <el-icon :size="40"><component :is="t.icon" /></el-icon>
              </div>
              <div class="type-name">{{ t.label }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <!-- 录入表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="left"
        class="input-form"
        v-if="form.type"
      >
        <el-form-item :label="currentType.label + '数值'" required>
          <template v-if="form.type === 'BLOOD_PRESSURE'">
            <div class="bp-inputs">
              <el-input
                v-model="form.highValue"
                placeholder="高压"
                size="large"
                clearable
                class="bp-input"
              >
                <template #prepend>高压</template>
                <template #append>mmHg</template>
              </el-input>
              <span class="bp-sep">/</span>
              <el-input
                v-model="form.lowValue"
                placeholder="低压"
                size="large"
                clearable
                class="bp-input"
              >
                <template #prepend>低压</template>
                <template #append>mmHg</template>
              </el-input>
            </div>
          </template>
          <template v-else>
            <el-input
              v-model="form.value"
              :placeholder="'请输入' + currentType.label + '数值'"
              size="large"
              clearable
            >
              <template #append>{{ currentType.unit }}</template>
            </el-input>
          </template>
          <div class="range-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ currentType.range }}</span>
          </div>
        </el-form-item>

        <el-form-item label="测量时间" prop="measureTime">
          <div class="time-row">
            <el-date-picker
              v-model="form.measureTime"
              type="datetime"
              placeholder="选择测量时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              size="large"
              class="time-picker"
            />
            <el-button type="success" size="large" @click="setNow">
              <el-icon><Clock /></el-icon>现在
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>提交数据
          </el-button>
          <el-button size="large" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-empty v-else description="请先选择要录入的数据类型" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { add as addHealthData } from '@/api/healthData'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

// 数据类型配置
const typeList = [
  { key: 'BLOOD_PRESSURE', label: '血压', icon: 'BloodPressureIcon', color: '#409EFF', bg: '#ecf5ff', unit: 'mmHg', range: '正常范围：高压 90-140 mmHg，低压 60-90 mmHg' },
  { key: 'BLOOD_SUGAR', label: '血糖', icon: 'BloodSugarIcon', color: '#67C23A', bg: '#f0f9eb', unit: 'mmol/L', range: '正常范围：空腹 3.9-6.1 mmol/L' },
  { key: 'HEART_RATE', label: '心率', icon: 'HeartRateIcon', color: '#F56C6C', bg: '#fef0f0', unit: '次/分', range: '正常范围：60-100 次/分' },
  { key: 'BLOOD_OXYGEN', label: '血氧', icon: 'BloodOxygenIcon', color: '#9C27B0', bg: '#f3e8f9', unit: '%', range: '正常范围：95-100 %' }
]

const form = reactive({
  type: 'BLOOD_PRESSURE',
  value: '',
  highValue: '',
  lowValue: '',
  measureTime: ''
})

const currentType = computed(() => typeList.find((t) => t.key === form.type) || typeList[0])

const rules = {
  measureTime: [{ required: true, message: '请选择测量时间', trigger: 'change' }]
}

// 当前时间字符串
const nowStr = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
}

const setNow = () => {
  form.measureTime = nowStr()
}

const selectType = (key) => {
  form.type = key
  form.value = ''
  form.highValue = ''
  form.lowValue = ''
}

const handleReset = () => {
  form.value = ''
  form.highValue = ''
  form.lowValue = ''
  form.measureTime = nowStr()
}

// 校验数值
const validateValues = () => {
  if (form.type === 'BLOOD_PRESSURE') {
    if (form.highValue === '' || form.lowValue === '') {
      ElMessage.warning('请输入高压和低压数值')
      return false
    }
    if (isNaN(Number(form.highValue)) || isNaN(Number(form.lowValue))) {
      ElMessage.warning('血压数值必须为数字')
      return false
    }
    return true
  }
  if (form.value === '') {
    ElMessage.warning('请输入' + currentType.value.label + '数值')
    return false
  }
  if (isNaN(Number(form.value))) {
    ElMessage.warning('数值必须为数字')
    return false
  }
  return true
}

// 提交健康数据（字段对齐后端 HealthDataAddDTO：dataType/valueHigh/valueLow/value/measureTime）
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!validateValues()) return
    submitting.value = true
    try {
      const payload = { dataType: form.type, measureTime: form.measureTime }
      if (form.type === 'BLOOD_PRESSURE') {
        payload.valueHigh = Number(form.highValue)
        payload.valueLow = Number(form.lowValue)
      } else {
        payload.value = Number(form.value)
      }
      await addHealthData(payload)
      ElMessage.success('健康数据提交成功！')
      ElMessageBox.confirm('数据已成功记录，是否继续录入？', '提交成功', {
        confirmButtonText: '继续录入',
        cancelButtonText: '查看趋势',
        type: 'success',
        distinguishCancelAndClose: true
      }).then(() => {
        handleReset()
      }).catch((action) => {
        if (action === 'cancel') {
          router.push('/elderly/data-view')
        }
      })
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  form.measureTime = nowStr()
})
</script>
<style scoped>
.data-input-page {
  max-width: 900px;
  margin: 0 auto;
}

.input-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 8px;
  border: 2px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 16px;
  background: #fff;
}

.type-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.type-card.active {
  border-width: 2px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.type-icon {
  margin-bottom: 10px;
}

.type-name {
  font-size: 19px;
  font-weight: 600;
  color: #303133;
}

.input-form {
  margin-top: 8px;
}

.bp-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.bp-input {
  flex: 1;
  min-width: 180px;
}

.bp-sep {
  font-size: 24px;
  color: #909399;
  font-weight: 700;
}

.range-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 15px;
  color: #e6a23c;
  line-height: 1.5;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.time-picker {
  flex: 1;
  min-width: 240px;
}

.submit-btn {
  min-width: 180px;
  font-size: 20px;
  font-weight: 600;
}

/* 适老化：放大输入与按钮 */
.elderly-theme .type-name {
  font-size: 21px;
}

.elderly-theme .section-title {
  font-size: 20px;
}

.elderly-theme .range-tip {
  font-size: 17px;
}

.elderly-theme .submit-btn {
  min-width: 220px;
  font-size: 22px;
}

.elderly-theme .bp-sep {
  font-size: 28px;
}
</style>