<template>
  <div class="profile-page">
    <!-- 基本信息卡片 -->
    <el-card shadow="never" class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><User /></el-icon>
          <span>基本信息</span>
          <el-tag type="info" class="readonly-tag" size="large">只读</el-tag>
        </div>
      </template>
      <el-descriptions :column="3" border size="large">
        <el-descriptions-item label="姓名">{{ profile.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ profile.age != null ? profile.age + ' 岁' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ profile.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="血型">{{ profile.bloodType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">{{ profile.emergencyContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系电话">{{ profile.emergencyPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="居住地址">{{ profile.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ profile.idCard || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 身体指标卡片 -->
    <el-card shadow="never" class="info-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><DataAnalysis /></el-icon>
          <span>身体指标</span>
        </div>
      </template>
      <el-descriptions :column="3" border size="large">
        <el-descriptions-item label="身高">{{ profile.height != null ? profile.height + ' cm' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ profile.weight != null ? profile.weight + ' kg' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="BMI">{{ bmiText }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 病史信息卡片 -->
    <el-card shadow="never" class="info-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><Files /></el-icon>
          <span>病史信息</span>
        </div>
      </template>
      <div class="history-block">
        <div class="history-item">
          <div class="history-label"><el-icon><FirstAidKit /></el-icon>既往病史</div>
          <div class="history-content">{{ profile.medicalHistory || '暂无' }}</div>
        </div>
        <el-divider />
        <div class="history-item">
          <div class="history-label"><el-icon><Warning /></el-icon>过敏史</div>
          <div class="history-content">{{ profile.allergyHistory || '暂无' }}</div>
        </div>
        <el-divider />
        <div class="history-item">
          <div class="history-label"><el-icon><Connection /></el-icon>家族病史</div>
          <div class="history-content">{{ profile.familyHistory || '暂无' }}</div>
        </div>
      </div>
    </el-card>

    <el-alert
      title="健康档案由医护人员维护，如有变更请联系您的家庭医生或社区医护。"
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMine } from '@/api/elderlyProfile'

const loading = ref(false)
const profile = ref({})

// 性别：后端 ElderlyProfileVO 字段 gender，0女1男
const genderText = computed(() => {
  const g = profile.value.gender
  if (g === 1 || g === '1') return '男'
  if (g === 0 || g === '0') return '女'
  return '-'
})

const bmiText = computed(() => {
  const h = profile.value.height
  const w = profile.value.weight
  if (!h || !w) return '-'
  const heightM = Number(h) / 100
  if (!heightM) return '-'
  const bmi = (Number(w) / (heightM * heightM)).toFixed(1)
  return bmi
})

const loadProfile = async () => {
  loading.value = true
  try {
    const data = await getMine()
    profile.value = data || {}
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>
<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1000px;
  margin: 0 auto;
}

.info-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.readonly-tag {
  margin-left: auto;
}

.history-block {
  padding: 4px 0;
}

.history-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.history-label {
  flex-shrink: 0;
  width: 120px;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.history-content {
  flex: 1;
  font-size: 16px;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
}

.tip-alert {
  border-radius: 10px;
}

/* 适老化 */
.elderly-theme .card-header {
  font-size: 20px;
}

.elderly-theme .history-label {
  font-size: 18px;
  width: 140px;
}

.elderly-theme .history-content {
  font-size: 18px;
}
</style>