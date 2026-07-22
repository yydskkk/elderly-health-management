<template>
  <div class="log-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="操作用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作内容">
          <el-input
            v-model="queryParams.operation"
            placeholder="操作内容关键词"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Download" :loading="exporting" @click="handleExport">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>系统操作日志</span>
          <el-tag type="info" size="large" class="total-tag">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <div class="table-wrap">
        <el-table
          v-loading="loading"
          :data="tableData"
          border
          stripe
          height="100%"
          style="width: 100%"
        >
          <el-table-column prop="id" label="日志ID" width="90" align="center" />
          <el-table-column prop="username" label="用户名" width="130" show-overflow-tooltip />
          <el-table-column prop="operation" label="操作内容" min-width="180" show-overflow-tooltip />
          <el-table-column prop="method" label="请求方法" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="method-text">{{ row.method || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="ip" label="IP地址" width="140" align="center" />
          <el-table-column prop="location" label="操作地点" width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.location || '-' }}</template>
          </el-table-column>
          <el-table-column label="耗时" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="timeTagType(row.time)" size="small">{{ formatTime(row.time) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="操作时间" width="170" align="center" />
        </el-table>
      </div>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { getPage, exportLog } from '@/api/log'

const loading = ref(false)
const exporting = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  operation: ''
})

// 获取时间参数
const getTimeParams = () => {
  const params = {}
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  return params
}

// 加载日志列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      username: queryParams.username || undefined,
      operation: queryParams.operation || undefined,
      ...getTimeParams()
    }
    const data = await getPage(params)
    tableData.value = data?.records || []
    total.value = Number(data?.total) || 0
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  loadList()
}

// 重置
const handleReset = () => {
  queryParams.username = ''
  queryParams.operation = ''
  dateRange.value = []
  queryParams.pageNum = 1
  loadList()
}

// 导出
const handleExport = async () => {
  exporting.value = true
  try {
    const params = {
      username: queryParams.username || undefined,
      operation: queryParams.operation || undefined,
      ...getTimeParams()
    }
    const blob = await exportLog(params)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const now = new Date()
    const p = (n) => String(n).padStart(2, '0')
    link.download = `操作日志_${now.getFullYear()}${p(now.getMonth() + 1)}${p(now.getDate())}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    exporting.value = false
  }
}

// 耗时格式化
const formatTime = (ms) => {
  if (ms === null || ms === undefined) return '-'
  if (ms < 1000) return ms + 'ms'
  return (ms / 1000).toFixed(2) + 's'
}

// 耗时标签类型
const timeTagType = (ms) => {
  if (!ms) return 'info'
  if (ms < 500) return 'success'
  if (ms < 2000) return 'warning'
  return 'danger'
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 100px);
}

.search-card {
  border-radius: 8px;
  flex-shrink: 0;
}

.table-card {
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.total-tag {
  margin-left: auto;
}

.method-text {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #606266;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}
</style>
