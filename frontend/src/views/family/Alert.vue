<template>
  <div class="family-alert">
    <el-card shadow="never" class="selector-card">
      <template #header><div class="card-header"><el-icon><Connection /></el-icon><span>选择关联老人</span></div></template>
      <ElderlySelector v-model="elderlyId" @change="handleElderlyChange" />
    </el-card>

    <template v-if="elderlyId">
      <el-card shadow="never" class="table-card">
        <template #header>
          <div class="card-header">
            <el-icon><Bell /></el-icon><span>预警通知</span>
            <el-button link type="primary" @click="handleReadAll" v-if="tableData.length > 0">全部已读</el-button>
          </div>
        </template>
        <div class="table-wrap">
        <el-table v-loading="loading" :data="tableData" border stripe height="100%">
          <el-table-column label="老人姓名" width="110" align="center">
            <template #default="{ row }">{{ row.userName || "-" }}</template>
          </el-table-column>
          <el-table-column label="等级" width="90" align="center">
            <template #default="{ row }"><el-tag :type="levelTagType(row.alertLevel)">{{ levelText(row.alertLevel) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="类型" width="120" align="center">
            <template #default="{ row }"><el-tag>{{ dataTypeText(row.dataType) }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="value" label="异常值" min-width="140" show-overflow-tooltip />
          <el-table-column prop="createTime" label="预警时间" min-width="160" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isRead === 1 ? 'info' : 'danger'">{{ row.isRead === 1 ? "已读" : "未读" }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="row.isRead !== 1" link type="primary" size="small" @click="handleRead(row)">标记已读</el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>
        <div class="pagination-wrap">
          <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :page-sizes="[5,10,20]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadList" @current-change="loadList" />
        </div>
      </el-card>
    </template>
    <el-card v-else shadow="never"><el-empty description="请先选择关联老人" /></el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue"
import { ElMessage } from "element-plus"
import ElderlySelector from "@/components/ElderlySelector.vue"
import { getNotificationPage, readNotification, readAllNotifications } from "@/api/alert"
import { useAlertStore } from "@/store/alert"

const elderlyId = ref("")
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const alertStore = useAlertStore()
// 预警通知API接收pageNum/pageSize/isRead，不支持elderlyId参数
const queryParams = reactive({ pageNum: 1, pageSize: 10 })

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

// AlertNotificationVO: alertLevel 1低2中3高
const levelText = (val) => {
  if (val === 1) return "低"
  if (val === 2) return "中"
  if (val === 3) return "高"
  return "-"
}

const levelTagType = (val) => {
  if (val === 3) return "danger"
  if (val === 2) return "warning"
  return "info"
}

const handleElderlyChange = (val) => {
  if (val) {
    queryParams.pageNum = 1
    loadList()
  } else {
    tableData.value = []
    total.value = 0
  }
}

const loadList = async () => {
  if (!elderlyId.value) return
  loading.value = true
  try {
    // 预警通知API按当前用户(receiverId)过滤，不支持elderlyId参数
    // 返回的通知VO中包含userId(老人ID)和userName(老人姓名)，前端按选中的elderlyId客户端过滤
    const data = await getNotificationPage({ ...queryParams })
    const allRecords = data?.records || data?.list || (Array.isArray(data) ? data : [])
    // 客户端按选中的老人ID过滤
    const filtered = allRecords.filter((r) => r.userId === elderlyId.value)
    tableData.value = filtered
    total.value = data?.total || filtered.length
  } catch (e) {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleRead = async (row) => {
  try {
    await readNotification(row.id)
    ElMessage.success("已标记为已读")
    row.isRead = 1
    alertStore.decrementUnread()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const handleReadAll = async () => {
  try {
    await readAllNotifications()
    ElMessage.success("已全部标记为已读")
    tableData.value.forEach((item) => { item.isRead = 1 })
    alertStore.resetUnread()
  } catch (e) {
    // 错误已由拦截器处理
  }
}
</script>

<style scoped>
.family-alert { display: flex; flex-direction: column; gap: 16px; height: calc(100vh - 100px); }
.selector-card { border-radius: 8px; flex-shrink: 0; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
.card-header .el-button { margin-left: auto; }
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
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; flex-shrink: 0; }
</style>
