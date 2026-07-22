<template>
  <div class="statistics-page">
    <!-- 总览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="item in overviewCards" :key="item.key">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-inner">
            <div class="overview-icon" :style="{ background: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
            </div>
            <div class="overview-content">
              <div class="overview-value">{{ overview[item.key] ?? 0 }}</div>
              <div class="overview-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
            @change="loadAll"
          />
        </el-form-item>
        <el-form-item label="统计维度">
          <el-radio-group v-model="statType" @change="loadAll">
            <el-radio-button value="day">按日</el-radio-button>
            <el-radio-button value="month">按月</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="快捷选择">
          <el-button-group>
            <el-button @click="setQuickRange(7)">近7天</el-button>
            <el-button @click="setQuickRange(30)">近30天</el-button>
            <el-button @click="setQuickRange(90)">近90天</el-button>
            <el-button @click="setQuickRange(365)">近1年</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><UserFilled /></el-icon>
              <span>用户注册趋势</span>
            </div>
          </template>
          <div ref="userChartRef" class="chart-box" v-loading="userLoading"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>健康数据录入趋势</span>
            </div>
          </template>
          <div ref="healthChartRef" class="chart-box" v-loading="healthLoading"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>预警发生趋势</span>
            </div>
          </template>
          <div ref="alertChartRef" class="chart-box" v-loading="alertLoading"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><PieChart /></el-icon>
              <span>健康数据类型分布</span>
            </div>
          </template>
          <div ref="distChartRef" class="chart-box" v-loading="distLoading"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getOverview,
  getUserRegister,
  getHealthData,
  getAlert,
  getDataTypeDistribution
} from '@/api/statistics'

// 总览数据
const overview = ref({})
const overviewCards = [
  { key: 'totalUsers', label: '总用户数', icon: 'UserFilled', color: '#409EFF' },
  { key: 'totalElderly', label: '老年用户', icon: 'Histogram', color: '#67C23A' },
  { key: 'totalFamily', label: '家属数', icon: 'User', color: '#E6A23C' },
  { key: 'totalDoctor', label: '医护数', icon: 'FirstAidKit', color: '#F56C6C' },
  { key: 'totalHealthData', label: '健康数据总量', icon: 'DataLine', color: '#909399' },
  { key: 'totalAlerts', label: '预警总数', icon: 'Bell', color: '#9C27B0' }
]

// 筛选条件
const dateRange = ref([])
const statType = ref('day')

// 图表引用
const userChartRef = ref(null)
const healthChartRef = ref(null)
const alertChartRef = ref(null)
const distChartRef = ref(null)

let userChart = null
let healthChart = null
let alertChart = null
let distChart = null

const userLoading = ref(false)
const healthLoading = ref(false)
const alertLoading = ref(false)
const distLoading = ref(false)

// 角色名称映射
const roleNameMap = {
  ELDERLY: '老年用户',
  FAMILY: '家属',
  DOCTOR: '医护人员',
  ADMIN: '管理员',
  UNKNOWN: '未知'
}

// 角色颜色映射
const roleColorMap = {
  ELDERLY: '#67C23A',
  FAMILY: '#E6A23C',
  DOCTOR: '#F56C6C',
  ADMIN: '#409EFF',
  UNKNOWN: '#909399'
}

// 设置快捷时间范围
const setQuickRange = (days) => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days + 1)
  const fmt = (d) => {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  dateRange.value = [fmt(start), fmt(end)]
  // 根据天数自动选择合适的统计维度：90天以内按日，超过90天按月
  statType.value = days <= 90 ? 'day' : 'month'
  loadAll()
}

// 获取日期参数
const getDateParams = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    return { startDate: dateRange.value[0], endDate: dateRange.value[1] }
  }
  // 默认近30天
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 29)
  const fmt = (d) => {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  return { startDate: fmt(start), endDate: fmt(end) }
}

// 加载总览
const loadOverview = async () => {
  try {
    const data = await getOverview()
    overview.value = data || {}
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// 加载用户注册统计
const loadUserRegister = async () => {
  userLoading.value = true
  try {
    const data = await getUserRegister({ ...getDateParams(), type: statType.value })
    renderUserChart(data || [])
  } catch (e) {
    renderUserChart([])
  } finally {
    userLoading.value = false
  }
}

// 加载健康数据统计
const loadHealthData = async () => {
  healthLoading.value = true
  try {
    const data = await getHealthData({ ...getDateParams(), type: statType.value })
    renderHealthChart(data || [])
  } catch (e) {
    renderHealthChart([])
  } finally {
    healthLoading.value = false
  }
}

// 加载预警统计
const loadAlert = async () => {
  alertLoading.value = true
  try {
    const data = await getAlert({ ...getDateParams(), type: statType.value })
    renderAlertChart(data || [])
  } catch (e) {
    renderAlertChart([])
  } finally {
    alertLoading.value = false
  }
}

// 加载数据类型分布
const loadDistribution = async () => {
  distLoading.value = true
  try {
    const data = await getDataTypeDistribution(getDateParams())
    renderDistChart(data || [])
  } catch (e) {
    renderDistChart([])
  } finally {
    distLoading.value = false
  }
}

// 渲染用户注册图表
const renderUserChart = (data) => {
  if (!userChartRef.value) return
  if (!userChart) userChart = echarts.init(userChartRef.value)

  // 按日期和角色分组
  const dateSet = new Set()
  const categoryMap = new Map()
  data.forEach((item) => {
    dateSet.add(item.date)
    if (!categoryMap.has(item.category)) {
      categoryMap.set(item.category, new Map())
    }
    categoryMap.get(item.category).set(item.date, item.value)
  })
  const dates = Array.from(dateSet).sort()
  const categories = Array.from(categoryMap.keys())

  const series = categories.map((cat) => ({
    name: roleNameMap[cat] || cat,
    type: 'bar',
    stack: 'total',
    emphasis: { focus: 'series' },
    itemStyle: { color: roleColorMap[cat] || '#409EFF' },
    data: dates.map((d) => categoryMap.get(cat).get(d) || 0)
  }))

  userChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 'bottom' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: dates.length > 10 ? 45 : 0 } },
    yAxis: { type: 'value', minInterval: 1 },
    series
  }, true)
}

// 渲染健康数据图表
const renderHealthChart = (data) => {
  if (!healthChartRef.value) return
  if (!healthChart) healthChart = echarts.init(healthChartRef.value)

  const dateSet = new Set()
  const categoryMap = new Map()
  data.forEach((item) => {
    dateSet.add(item.date)
    if (!categoryMap.has(item.category)) {
      categoryMap.set(item.category, new Map())
    }
    categoryMap.get(item.category).set(item.date, item.value)
  })
  const dates = Array.from(dateSet).sort()
  const categories = Array.from(categoryMap.keys())

  const series = categories.map((cat) => ({
    name: cat,
    type: 'line',
    smooth: true,
    data: dates.map((d) => categoryMap.get(cat).get(d) || 0)
  }))

  healthChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 'bottom' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false, axisLabel: { rotate: dates.length > 10 ? 45 : 0 } },
    yAxis: { type: 'value', minInterval: 1 },
    series
  }, true)
}

// 渲染预警图表
const renderAlertChart = (data) => {
  if (!alertChartRef.value) return
  if (!alertChart) alertChart = echarts.init(alertChartRef.value)

  const dateSet = new Set()
  const categoryMap = new Map()
  data.forEach((item) => {
    dateSet.add(item.date)
    if (!categoryMap.has(item.category)) {
      categoryMap.set(item.category, new Map())
    }
    categoryMap.get(item.category).set(item.date, item.value)
  })
  const dates = Array.from(dateSet).sort()
  const categories = Array.from(categoryMap.keys())

  const levelColorMap = { '高': '#F56C6C', '中': '#E6A23C', '低': '#67C23A', '未知': '#909399' }

  const series = categories.map((cat) => ({
    name: cat,
    type: 'bar',
    stack: 'total',
    emphasis: { focus: 'series' },
    itemStyle: { color: levelColorMap[cat] || '#409EFF' },
    data: dates.map((d) => categoryMap.get(cat).get(d) || 0)
  }))

  alertChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 'bottom' },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: dates.length > 10 ? 45 : 0 } },
    yAxis: { type: 'value', minInterval: 1 },
    series
  }, true)
}

// 渲染分布饼图
const renderDistChart = (data) => {
  if (!distChartRef.value) return
  if (!distChart) distChart = echarts.init(distChartRef.value)

  const pieData = data.map((item) => ({
    name: item.name || '未知',
    value: item.value || 0
  }))

  distChart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { type: 'scroll', orient: 'vertical', right: '5%', top: 'middle' },
    series: [{
      name: '数据类型',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 18, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: pieData
    }]
  }, true)
}

// 加载所有数据
const loadAll = async () => {
  await Promise.all([loadOverview(), loadUserRegister(), loadHealthData(), loadAlert(), loadDistribution()])
}

// 窗口大小变化处理
const handleResize = () => {
  userChart && userChart.resize()
  healthChart && healthChart.resize()
  alertChart && alertChart.resize()
  distChart && distChart.resize()
}

onMounted(async () => {
  // 默认近30天
  setQuickRange(30)
  await nextTick()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userChart && userChart.dispose()
  healthChart && healthChart.dispose()
  alertChart && alertChart.dispose()
  distChart && distChart.dispose()
})
</script>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-row {
  margin-bottom: 0;
}

.overview-card {
  margin-bottom: 16px;
  border-radius: 8px;
  transition: transform 0.2s;
}

.overview-card:hover {
  transform: translateY(-2px);
}

.overview-inner {
  display: flex;
  align-items: center;
  gap: 12px;
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.overview-content {
  flex: 1;
  min-width: 0;
}

.overview-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.overview-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.filter-card {
  border-radius: 8px;
}

.chart-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.chart-box {
  width: 100%;
  height: 340px;
}
</style>
