<template>
  <div class="dashboard-page">
    <!-- 欢迎卡片 -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-inner">
        <div class="welcome-avatar">
          <el-avatar :size="72" :src="userStore.userInfo.avatar">
            {{ avatarText }}
          </el-avatar>
        </div>
        <div class="welcome-text">
          <h2 class="welcome-title">{{ greeting }}，{{ userName }}</h2>
          <p class="welcome-tip">
            <el-icon><Sunny /></el-icon>
            <span>{{ todayTip }}</span>
          </p>
        </div>
        <div class="welcome-alert" v-if="alertStore.unreadCount > 0">
          <el-badge :value="alertStore.unreadCount" :max="99">
            <el-button type="danger" size="large" round @click="router.push('/elderly/alert')">
              <el-icon><Bell /></el-icon>
              您有 {{ alertStore.unreadCount }} 条未读预警
            </el-button>
          </el-badge>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="quick-card">
      <template #header>
        <div class="card-header">
          <el-icon><Menu /></el-icon>
          <span>快捷入口</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" v-for="entry in quickEntries" :key="entry.path">
          <div class="quick-entry" @click="router.push(entry.path)">
            <div class="quick-icon" :style="{ background: entry.color }">
              <el-icon :size="34"><component :is="entry.icon" /></el-icon>
            </div>
            <div class="quick-text">{{ entry.title }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 最近健康数据摘要 -->
    <el-card shadow="never" class="summary-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>最近健康数据</span>
          <el-button link type="primary" class="header-more" @click="router.push('/elderly/data-view')">
            查看趋势<el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="6" v-for="item in summaryList" :key="item.key">
          <div class="summary-item" :class="{ 'is-abnormal': item.abnormal }">
            <div class="summary-icon" :style="{ color: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-label">{{ item.label }}</div>
              <div class="summary-value">
                <template v-if="item.loading">-</template>
                <template v-else-if="item.value !== null && item.value !== undefined && item.value !== ''">
                  {{ item.value }}
                  <span class="summary-unit">{{ item.unit }}</span>
                </template>
                <template v-else>暂无数据</template>
              </div>
              <div class="summary-time" v-if="item.time">{{ item.time }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAlertStore } from '@/store/alert'
import { getList as getHealthList } from '@/api/healthData'

const router = useRouter()
const userStore = useUserStore()
const alertStore = useAlertStore()

const loading = ref(false)

const userName = computed(() => userStore.userInfo.name || '老人家')

const avatarText = computed(() => {
  const name = userStore.userInfo.name
  return name ? name.charAt(0).toUpperCase() : 'U'
})

// 问候语
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 今日健康提示
const todayTip = computed(() => {
  const tips = [
    '请记得按时测量并记录今日健康数据',
    '保持规律作息，适量运动，注意饮食清淡',
    '今日天气适宜，可适当户外活动',
    '按时服药，定期监测血压血糖'
  ]
  const abnormal = summaryList.value.find((i) => i.abnormal)
  if (abnormal) {
    return '您最近的' + abnormal.label + '数据异常，请关注并咨询医生'
  }
  const idx = new Date().getDate() % tips.length
  return tips[idx]
})

// 快捷入口
const quickEntries = [
  { path: '/elderly/data-input', title: '录入健康数据', icon: 'EditPen', color: '#409EFF' },
  { path: '/elderly/data-view', title: '查看健康趋势', icon: 'DataLine', color: '#67C23A' },
  { path: '/elderly/health-profile', title: '健康档案', icon: 'Files', color: '#E6A23C' },
  { path: '/elderly/alert', title: '预警通知', icon: 'Bell', color: '#F56C6C' }
]

// 最近数据摘要
const summaryList = ref([
  { key: 'BLOOD_PRESSURE', label: '血压', unit: 'mmHg', icon: 'BloodPressureIcon', color: '#409EFF', value: '', time: '', abnormal: false, loading: true },
  { key: 'BLOOD_SUGAR', label: '血糖', unit: 'mmol/L', icon: 'BloodSugarIcon', color: '#67C23A', value: '', time: '', abnormal: false, loading: true },
  { key: 'HEART_RATE', label: '心率', unit: '次/分', icon: 'HeartRateIcon', color: '#F56C6C', value: '', time: '', abnormal: false, loading: true },
  { key: 'BLOOD_OXYGEN', label: '血氧', unit: '%', icon: 'BloodOxygenIcon', color: '#9C27B0', value: '', time: '', abnormal: false, loading: true }
])

// 格式化数值显示（字段对齐后端 HealthDataVO：valueHigh/valueLow/value/isAbnormal/measureTime）
const formatValue = (item) => {
  const rec = item.record
  if (!rec) return { value: '', time: '', abnormal: false }
  const time = rec.measureTime || ''
  const abnormal = rec.isAbnormal === 1 || rec.isAbnormal === true
  if (item.key === 'BLOOD_PRESSURE') {
    const high = rec.valueHigh
    const low = rec.valueLow
    if (high != null && low != null) {
      return { value: high + '/' + low, time, abnormal }
    }
    if (rec.value != null && rec.value !== '') {
      return { value: String(rec.value), time, abnormal }
    }
    return { value: '', time, abnormal }
  }
  const val = rec.value
  return { value: val != null && val !== '' ? String(val) : '', time, abnormal }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) {
    const s = String(t)
    return s.length > 16 ? s.slice(5, 16) : s
  }
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return mm + '-' + dd + ' ' + hh + ':' + mi
}

// 加载最近一次各类型健康数据
// 后端 /health-data/list 接口返回 List<HealthDataVO>（按时间倒序），取第一条作为最近数据
const loadSummary = async () => {
  loading.value = true
  const tasks = summaryList.value.map(async (item) => {
    try {
      const data = await getHealthList({ dataType: item.key })
      const records = Array.isArray(data) ? data : []
      const rec = records.length > 0 ? records[0] : null
      item.record = rec
      const f = formatValue(item)
      item.value = f.value
      item.time = formatTime(f.time)
      item.abnormal = f.abnormal
    } catch (e) {
      item.value = ''
      item.time = ''
      item.abnormal = false
    } finally {
      item.loading = false
    }
  })
  await Promise.all(tasks)
  loading.value = false
}

onMounted(async () => {
  alertStore.getUnreadCount()
  await loadSummary()
})
</script>
<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #1a73e8 100%);
  border: none;
}

.welcome-card :deep(.el-card__body) {
  padding: 24px;
}

.welcome-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.welcome-avatar {
  flex-shrink: 0;
}

.welcome-text {
  flex: 1;
  min-width: 220px;
}

.welcome-title {
  margin: 0 0 8px 0;
  font-size: 26px;
  color: #fff;
  font-weight: 700;
}

.welcome-tip {
  margin: 0;
  color: rgba(255, 255, 255, 0.92);
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.welcome-alert {
  flex-shrink: 0;
}

.quick-card,
.summary-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.header-more {
  margin-left: auto;
  font-size: 15px;
}

.quick-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 8px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 16px;
  background: #fff;
}

.quick-entry:hover {
  border-color: #409EFF;
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.quick-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 10px;
}

.quick-text {
  font-size: 17px;
  color: #303133;
  font-weight: 600;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  margin-bottom: 16px;
  background: #fff;
  transition: all 0.2s;
}

.summary-item.is-abnormal {
  border-color: #f56c6c;
  background: #fef0f0;
}

.summary-icon {
  flex-shrink: 0;
}

.summary-info {
  flex: 1;
  min-width: 0;
}

.summary-label {
  font-size: 15px;
  color: #909399;
}

.summary-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
  margin-top: 2px;
}

.summary-item.is-abnormal .summary-value {
  color: #f56c6c;
}

.summary-unit {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}

.summary-time {
  font-size: 13px;
  color: #c0c4cc;
  margin-top: 2px;
}

.elderly-theme .welcome-title {
  font-size: 28px;
}

.elderly-theme .summary-value {
  font-size: 28px;
}

.elderly-theme .quick-text {
  font-size: 19px;
}
</style>