<template>
  <div class="family-data-view">
    <el-card shadow="never" class="selector-card">
      <template #header><div class="card-header"><el-icon><Connection /></el-icon><span>选择关联老人</span></div></template>
      <ElderlySelector v-model="elderlyId" @change="handleElderlyChange" />
    </el-card>

    <template v-if="elderlyId">
      <el-card shadow="never">
        <el-tabs v-model="activeType" @tab-change="handleTypeChange">
          <el-tab-pane v-for="t in typeOptions" :key="t.value" :label="t.label" :name="t.value" />
        </el-tabs>
        <div class="filter-bar">
          <span class="filter-label">时间范围：</span>
          <el-radio-group v-model="dateRange" @change="loadData">
            <el-radio-button value="7">近7天</el-radio-button>
            <el-radio-button value="30">近30天</el-radio-button>
            <el-radio-button value="90">近90天</el-radio-button>
          </el-radio-group>
          <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header><div class="card-header"><el-icon><TrendCharts /></el-icon><span>趋势图</span></div></template>
        <div ref="chartRef" class="chart-container" v-loading="chartLoading"></div>
        <el-empty v-if="!chartLoading && trendData.length === 0" description="暂无趋势数据" />
      </el-card>

      <el-card shadow="never">
        <template #header><div class="card-header"><el-icon><DataLine /></el-icon><span>数据列表</span></div></template>
        <el-table v-loading="tableLoading" :data="tableData" border stripe max-height="calc(100vh - 280px)">
          <el-table-column label="数据类型" width="120" align="center">
            <template #default="{ row }"><el-tag>{{ dataTypeText(row.dataType) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="数值" min-width="160">
            <template #default="{ row }">{{ valueText(row) }}</template>
          </el-table-column>
          <el-table-column prop="measureTime" label="测量时间" min-width="160" />
          <el-table-column prop="recorderName" label="录入人" min-width="100" show-overflow-tooltip />
        </el-table>
        <div class="pagination-wrap">
          <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[5,10,20]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @size-change="handlePageChange" @current-change="handlePageChange" />
        </div>
      </el-card>
    </template>
    <el-card v-else shadow="never"><el-empty description="请先选择关联老人" /></el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from "vue"
import { Search } from "@element-plus/icons-vue"
import * as echarts from "echarts"
import ElderlySelector from "@/components/ElderlySelector.vue"
import { getList, getTrend } from "@/api/healthData"

const elderlyId = ref("")
const activeType = ref("BLOOD_PRESSURE")
const dateRange = ref("7")
const chartRef = ref(null)
const chartLoading = ref(false)
const tableLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const trendData = ref([])
let chartInstance = null

const queryParams = reactive({ page: 1, size: 10 })

const typeOptions = [
  { value: "BLOOD_PRESSURE", label: "血压" },
  { value: "BLOOD_SUGAR", label: "血糖" },
  { value: "HEART_RATE", label: "心率" },
  { value: "BLOOD_OXYGEN", label: "血氧" }
]

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

const handleElderlyChange = (val) => {
  if (val) {
    queryParams.page = 1
    loadData()
  } else {
    tableData.value = []
    trendData.value = []
    total.value = 0
    if (chartInstance) chartInstance.clear()
  }
}

const handleTypeChange = () => {
  queryParams.page = 1
  loadData()
}

const getTimeRange = () => {
  const days = parseInt(dateRange.value)
  const end = new Date()
  const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000)
  const pad = (n) => String(n).padStart(2, "0")
  const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  return { startTime: fmt(start) + " 00:00:00", endTime: fmt(end) + " 23:59:59" }
}

const loadData = () => {
  loadTrend()
  loadList()
}

const loadTrend = async () => {
  if (!elderlyId.value) return
  chartLoading.value = true
  try {
    // 趋势API接收days参数而非时间范围
    const params = { elderlyId: elderlyId.value, dataType: activeType.value, days: parseInt(dateRange.value) }
    const data = await getTrend(params)
    trendData.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
    renderChart()
  } catch (e) {
    trendData.value = []
  } finally {
    chartLoading.value = false
  }
}

// 所有列表数据（用于客户端分页）
const allListData = ref([])

const loadList = async () => {
  if (!elderlyId.value) return
  tableLoading.value = true
  try {
    // 列表API返回List（非分页），需客户端分页
    const params = { elderlyId: elderlyId.value, dataType: activeType.value, ...getTimeRange() }
    const data = await getList(params)
    allListData.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
    total.value = allListData.value.length
    queryParams.page = 1
    updatePagedData()
  } catch (e) {
    allListData.value = []
    tableData.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

// 客户端分页
const updatePagedData = () => {
  const start = (queryParams.page - 1) * queryParams.size
  const end = start + queryParams.size
  tableData.value = allListData.value.slice(start, end)
}

const handlePageChange = () => {
  updatePagedData()
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  // 趋势VO使用date字段
  const times = trendData.value.map((d) => d.date || d.measureTime || d.time || "")
  let series = []
  let yAxisName = ""
  if (activeType.value === "BLOOD_PRESSURE") {
    yAxisName = "mmHg"
    series = [
      { name: "高压", type: "line", data: trendData.value.map((d) => d.valueHigh), smooth: true, itemStyle: { color: "#F56C6C" } },
      { name: "低压", type: "line", data: trendData.value.map((d) => d.valueLow), smooth: true, itemStyle: { color: "#409EFF" } }
    ]
  } else {
    const unitMap = { HEART_RATE: "bpm", BLOOD_SUGAR: "mmol/L", BLOOD_OXYGEN: "%" }
    yAxisName = unitMap[activeType.value] || ""
    series = [{ name: dataTypeText(activeType.value), type: "line", data: trendData.value.map((d) => d.value), smooth: true, itemStyle: { color: "#67C23A" }, areaStyle: { opacity: 0.1 } }]
  }
  chartInstance.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: series.map((s) => s.name), bottom: 0 },
    grid: { left: "3%", right: "4%", bottom: "10%", top: "5%", containLabel: true },
    xAxis: { type: "category", data: times, boundaryGap: false, axisLabel: { rotate: 30 } },
    yAxis: { type: "value", name: yAxisName },
    series
  }, true)
}

const handleResize = () => {
  if (chartInstance) chartInstance.resize()
}

watch(elderlyId, (val) => {
  if (val) {
    nextTick(() => {
      if (!chartInstance && chartRef.value) {
        chartInstance = echarts.init(chartRef.value)
      }
    })
  }
})

onMounted(() => {
  window.addEventListener("resize", handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.family-data-view { display: flex; flex-direction: column; gap: 16px; }
.selector-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
.filter-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.filter-label { color: #606266; font-size: 14px; }
.chart-card { border-radius: 8px; }
.chart-container { width: 100%; height: 380px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
