<template>
  <div class="family-dashboard">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-inner">
        <el-avatar :size="64" :src="userStore.userInfo.avatar">{{ avatarText }}</el-avatar>
        <div class="welcome-text">
          <h2 class="welcome-title">{{ greeting }},{{ userStore.userInfo.name || "家属" }}</h2>
          <p class="welcome-sub">欢迎使用社区老年人健康管理服务平台</p>
        </div>
      </div>
    </el-card>
    <el-card shadow="never" class="selector-card">
      <template #header><div class="card-header"><el-icon><Connection /></el-icon><span>选择关联老人</span></div></template>
      <ElderlySelector v-model="elderlyId" @change="handleElderlyChange" @load="handleElderlyLoad" />
    </el-card>
    <el-card v-if="loaded && !hasElderly" shadow="never"><el-empty description="您还未关联任何老人"><el-button type="primary" @click="router.push('/family/relation')">去关联老人</el-button></el-empty></el-card>
    <template v-if="hasElderly">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6"><el-card shadow="hover" :body-style="{ padding: '20px' }"><div class="stat-card-inner"><div class="stat-icon" style="background: #409EFF"><el-icon :size="26"><Bell /></el-icon></div><div><div class="stat-value">{{ unreadCount }}</div><div class="stat-label">未读预警</div></div></div></el-card></el-col>
        <el-col :xs="12" :sm="6"><el-card shadow="hover" :body-style="{ padding: '20px' }"><div class="stat-card-inner"><div class="stat-icon" style="background: #67C23A"><el-icon :size="26"><DataLine /></el-icon></div><div><div class="stat-value">{{ recentDataCount }}</div><div class="stat-label">近期数据</div></div></div></el-card></el-col>
        <el-col :xs="12" :sm="6"><el-card shadow="hover" :body-style="{ padding: '20px' }"><div class="stat-card-inner"><div class="stat-icon" style="background: #E6A23C"><el-icon :size="26"><ChatDotRound /></el-icon></div><div><div class="stat-value">{{ adviceCount }}</div><div class="stat-label">健康建议</div></div></div></el-card></el-col>
        <el-col :xs="12" :sm="6"><el-card shadow="hover" :body-style="{ padding: '20px' }"><div class="stat-card-inner"><div class="stat-icon" style="background: #F56C6C"><el-icon :size="26"><Document /></el-icon></div><div><div class="stat-value">{{ interventionCount }}</div><div class="stat-label">干预方案</div></div></div></el-card></el-col>
      </el-row>
      <el-card shadow="never"><template #header><div class="card-header"><el-icon><DataLine /></el-icon><span>最近健康数据摘要</span><el-button link type="primary" @click="router.push('/family/data-view')">查看更多</el-button></div></template>
        <el-table v-loading="dataLoading" :data="recentData" border stripe size="small">
          <el-table-column label="数据类型" width="120" align="center"><template #default="{ row }"><el-tag>{{ dataTypeText(row.dataType) }}</el-tag></template></el-table-column>
          <el-table-column label="数值" min-width="160"><template #default="{ row }">{{ valueText(row) }}</template></el-table-column>
          <el-table-column prop="measureTime" label="测量时间" min-width="160" />
          <el-table-column prop="recorderName" label="录入人" min-width="100" show-overflow-tooltip />
        </el-table></el-card>
      <el-card shadow="never"><template #header><div class="card-header"><el-icon><Menu /></el-icon><span>快捷入口</span></div></template>
        <el-row :gutter="16"><el-col :xs="12" :sm="8" :md="6" v-for="entry in quickEntries" :key="entry.path"><div class="quick-entry" @click="router.push(entry.path)"><div class="quick-icon" :style="{ color: entry.color }"><el-icon :size="32"><component :is="entry.icon" /></el-icon></div><div class="quick-text">{{ entry.title }}</div></div></el-col></el-row></el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useUserStore } from "@/store/user"
import { useAlertStore } from "@/store/alert"
import ElderlySelector from "@/components/ElderlySelector.vue"
import { getList as getHealthDataList } from "@/api/healthData"
import { getMine as getMyAdvice } from "@/api/healthAdvice"
import { getMine as getMyIntervention } from "@/api/interventionPlan"

const router = useRouter()
const userStore = useUserStore()
const alertStore = useAlertStore()
const elderlyId = ref("")
const loaded = ref(false)
const hasElderly = ref(false)
const unreadCount = computed(() => alertStore.unreadCount)
const recentData = ref([])
const recentDataCount = ref(0)
const dataLoading = ref(false)
const adviceCount = ref(0)
const interventionCount = ref(0)

const avatarText = computed(() => {
  const name = userStore.userInfo.name
  return name ? name.charAt(0).toUpperCase() : "U"
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return "凌晨好"
  if (h < 9) return "早上好"
  if (h < 12) return "上午好"
  if (h < 14) return "中午好"
  if (h < 18) return "下午好"
  return "晚上好"
})

const dataTypeOptions = [
  { value: "HEART_RATE", label: "心率" },
  { value: "BLOOD_PRESSURE", label: "血压" },
  { value: "BLOOD_OXYGEN", label: "血氧" },
  { value: "BLOOD_SUGAR", label: "血糖" },
  { value: "TEMPERATURE", label: "体温" },
  { value: "STEPS", label: "步数" }
]

const dataTypeText = (val) => {
  const item = dataTypeOptions.find((d) => d.value === val)
  return item ? item.label : (val || "-")
}

const valueText = (row) => {
  if (row.dataType === "BLOOD_PRESSURE") {
    return `${row.valueHigh ?? "-"}/${row.valueLow ?? "-"} mmHg`
  }
  const unitMap = { HEART_RATE: "bpm", BLOOD_SUGAR: "mmol/L", BLOOD_OXYGEN: "%", TEMPERATURE: "℃", STEPS: "步" }
  const unit = unitMap[row.dataType] || ""
  return `${row.value ?? "-"}${unit ? " " + unit : ""}`
}

const quickEntries = [
  { path: "/family/data-view", title: "数据查看", icon: "DataLine", color: "#409EFF" },
  { path: "/family/data-input", title: "数据录入", icon: "EditPen", color: "#67C23A" },
  { path: "/family/alert", title: "预警通知", icon: "Bell", color: "#F56C6C" },
  { path: "/family/advice", title: "健康建议", icon: "ChatDotRound", color: "#E6A23C" },
  { path: "/family/profile", title: "健康档案", icon: "Files", color: "#9C27B0" },
  { path: "/family/relation", title: "关联管理", icon: "Connection", color: "#909399" }
]

const handleElderlyLoad = (list) => {
  loaded.value = true
  hasElderly.value = list && list.length > 0
}

const handleElderlyChange = (val) => {
  if (val) {
    hasElderly.value = true
    loadDashboardData(val)
  } else {
    hasElderly.value = false
    recentData.value = []
    recentDataCount.value = 0
    adviceCount.value = 0
    interventionCount.value = 0
  }
}

const loadDashboardData = async (id) => {
  dataLoading.value = true
  try {
    // 健康数据列表API要求dataType且返回List（非分页），查询多种类型合并取最近5条
    const types = ["BLOOD_PRESSURE", "HEART_RATE", "BLOOD_SUGAR", "BLOOD_OXYGEN"]
    const dataPromises = types.map((t) => getHealthDataList({ elderlyId: id, dataType: t }).catch(() => []))
    const [dataResults, adviceRes, interventionRes] = await Promise.allSettled([
      Promise.all(dataPromises),
      getMyAdvice({ elderlyId: id }),
      getMyIntervention({ elderlyId: id })
    ])
    if (dataResults.status === "fulfilled") {
      const merged = dataResults.value.flat()
      merged.sort((a, b) => new Date(b.measureTime) - new Date(a.measureTime))
      recentData.value = merged.slice(0, 5)
      recentDataCount.value = merged.length
    }
    if (adviceRes.status === "fulfilled") {
      const data = adviceRes.value
      adviceCount.value = Array.isArray(data) ? data.length : (data?.records?.length || 0)
    }
    if (interventionRes.status === "fulfilled") {
      const data = interventionRes.value
      interventionCount.value = Array.isArray(data) ? data.length : (data?.records?.length || 0)
    }
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    dataLoading.value = false
  }
}

onMounted(() => {
  alertStore.getUnreadCount()
})
</script>

<style scoped>
.family-dashboard { display: flex; flex-direction: column; gap: 16px; }
.welcome-card { border-radius: 8px; background: linear-gradient(135deg, #409EFF 0%, #6671f5 100%); color: #fff; border: none; }
.welcome-card :deep(.el-card__body) { padding: 24px; }
.welcome-inner { display: flex; align-items: center; gap: 20px; }
.welcome-text { flex: 1; }
.welcome-title { margin: 0 0 8px 0; font-size: 22px; color: #fff; }
.welcome-sub { margin: 0; font-size: 14px; color: rgba(255, 255, 255, 0.85); }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
.card-header .el-button { margin-left: auto; }
.stat-card-inner { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 52px; height: 52px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.quick-entry { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px 8px; border: 1px solid #ebeef5; border-radius: 8px; cursor: pointer; transition: all 0.2s; margin-bottom: 16px; background: #fff; }
.quick-entry:hover { border-color: #409EFF; background: #ecf5ff; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15); }
.quick-icon { margin-bottom: 8px; }
.quick-text { font-size: 14px; color: #606266; font-weight: 500; }
</style>
