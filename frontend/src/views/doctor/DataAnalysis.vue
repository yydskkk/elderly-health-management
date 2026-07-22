<template>
  <div class="data-analysis-page">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="辖区">
          <el-select v-model="regionId" placeholder="全部辖区" clearable style="--el-select-width: 180px; width: 180px" @change="onRegionChange">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="老人">
          <el-select v-model="elderlyId" placeholder="请选择老人" filterable style="--el-select-width: 200px; width: 200px" @change="loadAll">
            <el-option v-for="item in elderlyOptions" :key="item.userId" :label="item.elderlyName" :value="item.userId" />
          </el-select>
        </el-form-item>
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
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeType" @tab-change="loadAll">
        <el-tab-pane v-for="t in dataTypes" :key="t.value" :label="t.label" :name="t.value" />
      </el-tabs>

      <!-- 统计摘要 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-item"><div class="stat-num">{{ statText('avg') }}</div><div class="stat-name">平均值</div></div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item"><div class="stat-num">{{ statText('max') }}</div><div class="stat-name">最高值</div></div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item"><div class="stat-num">{{ statText('min') }}</div><div class="stat-name">最低值</div></div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item"><div class="stat-num">{{ statistics.abnormalCount ?? 0 }}</div><div class="stat-name">异常次数</div></div>
        </el-col>
      </el-row>

      <!-- 趋势图 -->
      <div ref="chartRef" class="trend-chart"></div>

      <!-- 数据列表 -->
      <div class="table-wrap">
      <el-table v-loading="loading" :data="pagedData" border stripe size="small" height="100%" style="margin-top: 16px">
        <el-table-column prop="measureTime" label="测量时间" min-width="160" />
        <el-table-column label="数值" width="160" align="center">
          <template #default="{ row }">
            <span v-if="activeType === 'BLOOD_PRESSURE'">{{ row.valueHigh ?? '-' }} / {{ row.valueLow ?? '-' }}</span>
            <span v-else>{{ row.value ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="是否异常" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isAbnormal === 1 ? 'danger' : 'success'" size="small">
              {{ row.isAbnormal === 1 ? '异常' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recorderName" label="录入人" width="120" align="center" />
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
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMyRegions } from '@/api/region'
import { getPage as getProfilePage } from '@/api/elderlyProfile'
import { getList, getTrend, getStatistics } from '@/api/healthData'

const regionId = ref('')
const elderlyId = ref('')
const elderlyOptions = ref([])
const regionOptions = ref([])
const dateRange = ref([])
const activeType = ref('BLOOD_PRESSURE')
const loading = ref(false)
const tableData = ref([])
const chartRef = ref(null)
let chartInstance = null

// 客户端分页
const currentPage = ref(1)
const pageSize = ref(10)
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

// 数据类型（与后端 HealthDataType code 对应）
const dataTypes = [
  { label: '血压', value: 'BLOOD_PRESSURE' },
  { label: '血糖', value: 'BLOOD_SUGAR' },
  { label: '心率', value: 'HEART_RATE' },
  { label: '血氧', value: 'BLOOD_OXYGEN' }
]

const typeText = (t) => {
  const item = dataTypes.find((x) => x.value === t)
  return item ? item.label : t
}

// 统计摘要（与后端 HealthDataStatisticsVO 字段对齐）
const statistics = reactive({
  avgValue: '',
  maxValue: '',
  minValue: '',
  avgValueHigh: '',
  maxValueHigh: '',
  minValueHigh: '',
  avgValueLow: '',
  maxValueLow: '',
  minValueLow: '',
  count: 0,
  abnormalCount: 0
})

// 统计卡片展示文本：血压展示高压/低压
const statText = (key) => {
  if (activeType.value === 'BLOOD_PRESSURE') {
    if (key === 'avg') return `${statistics.avgValueHigh ?? '-'}/${statistics.avgValueLow ?? '-'}`
    if (key === 'max') return `${statistics.maxValueHigh ?? '-'}/${statistics.maxValueLow ?? '-'}`
    if (key === 'min') return `${statistics.minValueHigh ?? '-'}/${statistics.minValueLow ?? '-'}`
  }
  if (key === 'avg') return statistics.avgValue ?? '-'
  if (key === 'max') return statistics.maxValue ?? '-'
  if (key === 'min') return statistics.minValue ?? '-'
  return '-'
}

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
    // 健康数据查询需使用 userId（老人用户ID），而非档案 id
    elderlyOptions.value = list.map((e) => ({ userId: e.userId, elderlyName: e.elderlyName }))
  } catch (e) {}
}

const getDateParams = () => {
  const params = {}
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0] + ' 00:00:00'
    params.endTime = dateRange.value[1] + ' 23:59:59'
  }
  return params
}

const loadAll = async () => {
  if (!elderlyId.value) return
  await Promise.all([loadTrend(), loadStatistics(), loadList()])
}

const loadTrend = async () => {
  try {
    const params = { elderlyId: elderlyId.value, dataType: activeType.value, ...getDateParams() }
    const data = await getTrend(params)
    renderChart(Array.isArray(data) ? data : [])
  } catch (e) {
    renderChart([])
  }
}

const loadStatistics = async () => {
  try {
    const params = { elderlyId: elderlyId.value, dataType: activeType.value, ...getDateParams() }
    const data = await getStatistics(params) || {}
    statistics.avgValue = data.avgValue ?? ''
    statistics.maxValue = data.maxValue ?? ''
    statistics.minValue = data.minValue ?? ''
    statistics.avgValueHigh = data.avgValueHigh ?? ''
    statistics.maxValueHigh = data.maxValueHigh ?? ''
    statistics.minValueHigh = data.minValueHigh ?? ''
    statistics.avgValueLow = data.avgValueLow ?? ''
    statistics.maxValueLow = data.maxValueLow ?? ''
    statistics.minValueLow = data.minValueLow ?? ''
    statistics.count = data.count ?? 0
    statistics.abnormalCount = data.abnormalCount ?? 0
  } catch (e) {
    Object.keys(statistics).forEach((k) => { statistics[k] = (k === 'count' || k === 'abnormalCount') ? 0 : '' })
  }
}

const loadList = async () => {
  if (!elderlyId.value) return
  loading.value = true
  try {
    // 健康数据列表接口返回数组（非分页）
    const params = { elderlyId: elderlyId.value, dataType: activeType.value, ...getDateParams() }
    const data = await getList(params)
    tableData.value = Array.isArray(data) ? data : []
    currentPage.value = 1
  } catch (e) {} finally {
    loading.value = false
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const xData = data.map((item) => item.date || '')
  let series
  if (activeType.value === 'BLOOD_PRESSURE') {
    // 血压展示高压、低压双系列
    series = [
      {
        name: '高压',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#F56C6C' },
        data: data.map((item) => item.valueHigh)
      },
      {
        name: '低压',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#409EFF' },
        data: data.map((item) => item.valueLow)
      }
    ]
  } else {
    series = [{
      name: typeText(activeType.value),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.2 },
      itemStyle: { color: '#409EFF' },
      data: data.map((item) => item.value)
    }]
  }
  const option = {
    tooltip: { trigger: 'axis' },
    legend: activeType.value === 'BLOOD_PRESSURE' ? { data: ['高压', '低压'] } : undefined,
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: xData, boundaryGap: false },
    yAxis: { type: 'value' },
    series
  }
  chartInstance.setOption(option, true)
}

const handleResize = () => {
  chartInstance && chartInstance.resize()
}

onMounted(async () => {
  await loadRegions()
  await loadElderly()
  await nextTick()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.data-analysis-page { display: flex; flex-direction: column; gap: 16px; height: calc(100vh - 100px); }
.filter-card, .table-card { border-radius: 8px; }
.filter-card { flex-shrink: 0; }
.stat-row { margin-bottom: 16px; flex-shrink: 0; }
.stat-item { background: #f5f7fa; border-radius: 8px; padding: 16px; text-align: center; }
.stat-num { font-size: 22px; font-weight: 700; color: #303133; }
.stat-name { font-size: 13px; color: #909399; margin-top: 4px; }
.trend-chart { width: 100%; height: 360px; flex-shrink: 0; }
.table-card :deep(.el-tabs) { flex-shrink: 0; }
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
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; flex-shrink: 0; }
</style>
