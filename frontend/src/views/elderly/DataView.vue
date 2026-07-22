<template>
  <div class="data-view-page">
    <el-card shadow="never" class="view-card">
      <!-- 数据类型 Tab -->
      <el-tabs v-model="activeType" @tab-change="onTypeChange">
        <el-tab-pane v-for="t in typeList" :key="t.key" :label="t.label" :name="t.key" />
      </el-tabs>

      <!-- 时间范围选择 -->
      <div class="range-bar">
        <span class="range-label">时间范围：</span>
        <el-radio-group v-model="days" size="large" @change="onRangeChange">
          <el-radio-button :value="7">最近7天</el-radio-button>
          <el-radio-button :value="30">最近30天</el-radio-button>
          <el-radio-button :value="90">最近90天</el-radio-button>
        </el-radio-group>
        <span class="range-tip-text">{{ currentType.label }}正常范围：{{ currentType.range }}</span>
      </div>

      <!-- 趋势图 -->
      <div class="chart-wrap" v-loading="loading">
        <div ref="chartRef" class="chart-box"></div>
        <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      </div>

      <!-- 数据列表 -->
      <div class="table-wrap">
        <div class="table-title">
          <el-icon><List /></el-icon>
          <span>数据明细</span>
        </div>
        <el-table :data="pagedData" border stripe size="large" v-loading="loading" empty-text="暂无数据" max-height="calc(100vh - 280px)">
          <el-table-column label="测量时间" prop="time" min-width="170">
            <template #default="{ row }">{{ formatTime(row) }}</template>
          </el-table-column>
          <el-table-column label="数值" min-width="140">
            <template #default="{ row }">
              <span class="value-text" :class="{ abnormal: isAbnormal(row) }">{{ formatRowValue(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="isAbnormal(row)" type="danger" size="large">异常</el-tag>
              <el-tag v-else type="success" size="large">正常</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager" v-if="tableData.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20]"
            :total="tableData.length"
            layout="total, sizes, prev, pager, next, jumper"
            background
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getTrend, getList as getHealthList } from '@/api/healthData'

// 数据类型配置
const typeList = [
  { key: 'BLOOD_PRESSURE', label: '血压', unit: 'mmHg', range: '高压 90-140 / 低压 60-90', color: '#409EFF' },
  { key: 'BLOOD_SUGAR', label: '血糖', unit: 'mmol/L', range: '3.9-6.1（空腹）', color: '#67C23A' },
  { key: 'HEART_RATE', label: '心率', unit: '次/分', range: '60-100', color: '#F56C6C' },
  { key: 'BLOOD_OXYGEN', label: '血氧', unit: '%', range: '95-100', color: '#9C27B0' }
]

const activeType = ref('BLOOD_PRESSURE')
const days = ref(7)
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

const chartRef = ref(null)
let chartInstance = null

const currentType = computed(() => typeList.find((t) => t.key === activeType.value) || typeList[0])

// 客户端分页
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

// 数据取值辅助（字段对齐后端 HealthDataVO/HealthDataTrendVO：
// valueHigh/valueLow/value/isAbnormal/measureTime/date）
const timeOf = (r) => r.measureTime || r.date || r.createTime || ''
const isAbnormal = (r) => r.isAbnormal === 1 || r.isAbnormal === true
const num = (v) => (v == null || v === '' ? null : Number(v))

const formatTime = (r) => {
  const t = timeOf(r)
  if (!t) return '-'
  const d = new Date(t)
  if (isNaN(d.getTime())) return String(t)
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes())
}

const formatRowValue = (r) => {
  if (activeType.value === 'BLOOD_PRESSURE') {
    const high = r.valueHigh
    const low = r.valueLow
    if (high != null && low != null) return high + ' / ' + low
    return r.value != null ? String(r.value) : '-'
  }
  return r.value != null ? String(r.value) : '-'
}

const formatAxisTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return String(t)
  const p = (n) => String(n).padStart(2, '0')
  return p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes())
}

// 渲染趋势图（异常数据用红色标注）
const renderChart = (records) => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const sorted = [...records].sort((a, b) => new Date(timeOf(a)) - new Date(timeOf(b)))
  const xData = sorted.map((r) => formatAxisTime(timeOf(r)))
  let series = []
  if (activeType.value === 'BLOOD_PRESSURE') {
    const mk = (getter, color) => sorted.map((r) => {
      const v = num(getter(r))
      const ab = isAbnormal(r)
      return { value: v, itemStyle: { color: ab ? '#f56c6c' : color }, symbolSize: ab ? 10 : 6 }
    })
    series = [
      { name: '高压', type: 'line', smooth: true, data: mk((r) => r.valueHigh, '#409EFF'), lineStyle: { width: 3 } },
      { name: '低压', type: 'line', smooth: true, data: mk((r) => r.valueLow, '#67C23A'), lineStyle: { width: 3 } }
    ]
  } else {
    const data = sorted.map((r) => {
      const v = num(r.value)
      const ab = isAbnormal(r)
      return { value: v, itemStyle: { color: ab ? '#f56c6c' : currentType.value.color }, symbolSize: ab ? 10 : 6 }
    })
    series = [{ name: currentType.value.label, type: 'line', smooth: true, data, lineStyle: { width: 3 } }]
  }
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: activeType.value === 'BLOOD_PRESSURE' ? ['高压', '低压'] : [currentType.value.label], top: 0 },
    grid: { left: 55, right: 24, top: 40, bottom: 50 },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 35, fontSize: 12 }, boundaryGap: false },
    yAxis: { type: 'value', name: currentType.value.unit, nameTextStyle: { fontSize: 13 } },
    series
  }, true)
}

// 加载数据
// 趋势接口 /health-data/trend 返回 List<HealthDataTrendVO>，用于绘图
// 列表接口 /health-data/list 返回 List<HealthDataVO>，用于明细展示
const loadData = async () => {
  loading.value = true
  try {
    const trendData = await getTrend({ dataType: activeType.value, days: days.value })
    const trendRecords = Array.isArray(trendData) ? trendData : []
    // 趋势数据用于绘图
    await nextTick()
    renderChart(trendRecords)
    // 列表数据：按时间范围查询
    let listRecords = trendRecords
    try {
      const endTime = new Date()
      const startTime = new Date()
      startTime.setDate(startTime.getDate() - days.value)
      const p = (n) => String(n).padStart(2, '0')
      const fmt = (d) => d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
      const listData = await getHealthList({
        dataType: activeType.value,
        startTime: fmt(startTime),
        endTime: fmt(endTime)
      })
      listRecords = Array.isArray(listData) ? listData : []
    } catch (e) {
      // 列表接口失败则沿用趋势数据
    }
    tableData.value = listRecords
    currentPage.value = 1
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const onTypeChange = () => {
  currentPage.value = 1
  loadData()
}

const onRangeChange = () => {
  currentPage.value = 1
  loadData()
}

// 窗口缩放重绘
const handleResize = () => {
  chartInstance && chartInstance.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 图表容器尺寸变化时重绘
watch(chartRef, async () => {
  await nextTick()
  if (chartRef.value && !chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
})
</script>
<style scoped>
.data-view-page {
  max-width: 1200px;
  margin: 0 auto;
}

.view-card {
  border-radius: 12px;
}

.range-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.range-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.range-tip-text {
  margin-left: auto;
  font-size: 14px;
  color: #e6a23c;
}

.chart-wrap {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 20px;
  min-height: 360px;
}

.chart-box {
  width: 100%;
  height: 340px;
}

.table-wrap {
  margin-top: 8px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.value-text {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.value-text.abnormal {
  color: #f56c6c;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* 适老化 */
.elderly-theme .range-label {
  font-size: 18px;
}

.elderly-theme .table-title {
  font-size: 20px;
}

.elderly-theme .range-tip-text {
  font-size: 16px;
}
</style>