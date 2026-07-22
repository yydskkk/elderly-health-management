<template>
  <div class="doctor-dashboard">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <el-avatar :size="64" :src="userStore.userInfo.avatar">{{ avatarText }}</el-avatar>
        <div class="welcome-text">
          <h2 class="welcome-title">{{ greeting }}，{{ userStore.userInfo.name || '医生' }}</h2>
          <p class="welcome-sub">欢迎使用社区老年人健康管理服务平台，祝您工作顺利！</p>
        </div>
        <div class="welcome-time"><el-icon><Calendar /></el-icon><span>{{ currentDate }}</span></div>
      </div>
    </el-card>
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-elderly">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-info"><div class="stat-value">{{ stats.elderlyCount }}</div><div class="stat-label">辖区老人数</div></div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-alert">
          <div class="stat-icon"><el-icon><Bell /></el-icon></div>
          <div class="stat-info"><div class="stat-value">{{ stats.pendingAlertCount }}</div><div class="stat-label">待处理预警数</div></div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-data">
          <div class="stat-icon"><el-icon><DataLine /></el-icon></div>
          <div class="stat-info"><div class="stat-value">{{ stats.monthDataCount }}</div><div class="stat-label">本月录入数据数</div></div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-plan">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-info"><div class="stat-value">{{ stats.ongoingPlanCount }}</div><div class="stat-label">进行中干预方案数</div></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <span>待处理预警列表</span>
              <el-button link type="primary" @click="goPath('/doctor/alert-handle')">查看全部<el-icon><ArrowRight /></el-icon></el-button>
            </div>
          </template>
          <el-table v-loading="alertLoading" :data="alertList" border stripe size="small">
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
            <el-table-column label="操作" width="90" align="center">
              <template #default>
                <el-button link type="primary" size="small" @click="goPath('/doctor/alert-handle')">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!alertLoading && alertList.length === 0" description="暂无待处理预警" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="panel-card">
          <template #header><div class="card-header"><span>快捷入口</span></div></template>
          <div class="quick-entry">
            <div v-for="item in quickEntries" :key="item.path" class="entry-item" @click="goPath(item.path)">
              <el-icon class="entry-icon" :style="{ background: item.color }"><component :is="item.icon" /></el-icon>
              <span class="entry-text">{{ item.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getRecordPage } from '@/api/alert'
import { getPage as getProfilePage } from '@/api/elderlyProfile'
import { getList as getHealthDataList } from '@/api/healthData'
import { getPage as getInterventionPage } from '@/api/interventionPlan'

const router = useRouter()
const userStore = useUserStore()

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

const avatarText = computed(() => {
  const name = userStore.userInfo.name
  return name ? name.charAt(0).toUpperCase() : 'D'
})

const currentDate = computed(() => {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 星期${week[d.getDay()]}`
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const stats = reactive({
  elderlyCount: 0,
  pendingAlertCount: 0,
  monthDataCount: 0,
  ongoingPlanCount: 0
})

const alertLoading = ref(false)
const alertList = ref([])

const quickEntries = [
  { path: '/doctor/data-analysis', title: '数据分析', icon: 'TrendCharts', color: '#409EFF' },
  { path: '/doctor/alert-handle', title: '预警处理', icon: 'Bell', color: '#F56C6C' },
  { path: '/doctor/profile-manage', title: '健康档案', icon: 'Files', color: '#67C23A' },
  { path: '/doctor/advice-publish', title: '建议发布', icon: 'ChatDotRound', color: '#E6A23C' },
  { path: '/doctor/intervention-manage', title: '干预方案', icon: 'Document', color: '#909399' },
  { path: '/doctor/alert-rule-personal', title: '预警规则', icon: 'SetUp', color: '#9B59B6' }
]

const levelType = (level) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger', LOW: 'info', MIDDLE: 'warning', HIGH: 'danger' }
  return map[level] || 'info'
}

const levelText = (level) => {
  const map = { 1: '低', 2: '中', 3: '高', LOW: '低', MIDDLE: '中', HIGH: '高' }
  return map[level] || '低'
}

const goPath = (path) => {
  router.push(path)
}
const loadStats = async () => {
  // 辖区老人数
  try {
    const profileData = await getProfilePage({ pageNum: 1, pageSize: 1 })
    stats.elderlyCount = profileData?.total || 0
  } catch (e) {}

  // 待处理预警数 + 列表
  alertLoading.value = true
  try {
    const alertData = await getRecordPage({ status: 0, pageNum: 1, pageSize: 5 })
    stats.pendingAlertCount = alertData?.total || 0
    alertList.value = alertData?.records || []
  } catch (e) {} finally {
    alertLoading.value = false
  }

  // 本月录入数据数（按默认类型血压统计本月记录数）
  try {
    const now = new Date()
    const start = new Date(now.getFullYear(), now.getMonth(), 1)
    const startTime = `${start.getFullYear()}-${String(start.getMonth() + 1).padStart(2, '0')}-${String(start.getDate()).padStart(2, '0')} 00:00:00`
    const endTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} 23:59:59`
    const dataData = await getHealthDataList({ dataType: 'BLOOD_PRESSURE', startTime, endTime })
    stats.monthDataCount = Array.isArray(dataData) ? dataData.length : 0
  } catch (e) {}

  // 进行中干预方案数
  try {
    const planData = await getInterventionPage({ status: 1, pageNum: 1, pageSize: 1 })
    stats.ongoingPlanCount = planData?.total || 0
  } catch (e) {}
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.doctor-dashboard { display: flex; flex-direction: column; gap: 16px; }
.welcome-card { border-radius: 8px; background: linear-gradient(135deg, #409EFF 0%, #53a8ff 100%); color: #fff; border: none; }
.welcome-card :deep(.el-card__body) { padding: 20px 24px; }
.welcome-content { display: flex; align-items: center; gap: 20px; }
.welcome-text { flex: 1; }
.welcome-title { margin: 0 0 6px; font-size: 22px; color: #fff; }
.welcome-sub { margin: 0; font-size: 14px; color: rgba(255, 255, 255, 0.85); }
.welcome-time { display: flex; align-items: center; gap: 6px; font-size: 14px; color: rgba(255, 255, 255, 0.9); }
.stat-card { border-radius: 8px; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; width: 100%; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; color: #fff; }
.stat-elderly .stat-icon { background: linear-gradient(135deg, #409EFF, #53a8ff); }
.stat-alert .stat-icon { background: linear-gradient(135deg, #F56C6C, #f78989); }
.stat-data .stat-icon { background: linear-gradient(135deg, #67C23A, #85ce61); }
.stat-plan .stat-icon { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.panel-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; }
.quick-entry { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; padding: 8px 0; }
.entry-item { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 16px 8px; border-radius: 8px; cursor: pointer; transition: background 0.2s; }
.entry-item:hover { background: #f5f7fa; }
.entry-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; }
.entry-text { font-size: 14px; color: #606266; }
</style>