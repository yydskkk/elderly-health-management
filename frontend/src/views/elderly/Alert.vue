<template>
  <div class="alert-page">
    <el-card shadow="never" class="alert-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><Bell /></el-icon>
          <span>预警通知</span>
          <el-button
            type="primary"
            size="large"
            class="read-all-btn"
            :disabled="!hasUnread"
            @click="handleReadAll"
          >
            <el-icon><Check /></el-icon>全部已读
          </el-button>
        </div>
      </template>

      <!-- 未读/全部 Tab -->
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane :label="'未读 (' + unreadCount + ')'" name="unread" />
        <el-tab-pane :label="'全部 (' + total + ')'" name="all" />
      </el-tabs>

      <div v-loading="loading">
        <div v-if="filteredList.length === 0 && !loading" class="empty-wrap">
          <el-empty description="暂无预警通知" />
        </div>

        <!-- 预警卡片列表 -->
        <div class="alert-list">
          <el-card
            v-for="item in filteredList"
            :key="item.id"
            shadow="hover"
            class="alert-item"
            :class="{ unread: !isRead(item) }"
          >
            <div class="alert-item-header">
              <div class="alert-indicator">
                <el-icon :size="20" :color="levelInfo(item).color"><Warning /></el-icon>
                <span class="indicator-name">{{ indicatorName(item) }}</span>
              </div>
              <div class="alert-tags">
                <el-tag :type="levelInfo(item).type" size="large" effect="dark">
                  {{ levelInfo(item).text }}预警
                </el-tag>
                <el-tag v-if="!isRead(item)" type="danger" size="large">未读</el-tag>
                <el-tag v-else type="info" size="large">已读</el-tag>
              </div>
            </div>
            <div class="alert-item-body">
              <div class="alert-value">
                <span class="value-label">异常数值：</span>
                <span class="value-num">{{ item.value || '-' }}</span>
              </div>
              <div class="alert-time">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
            <div class="alert-item-actions">
              <el-button size="large" @click="showDetail(item)">
                <el-icon><View /></el-icon>查看详情
              </el-button>
              <el-button
                v-if="!isRead(item)"
                type="success"
                size="large"
                :loading="item._reading"
                @click="handleRead(item)"
              >
                <el-icon><Select /></el-icon>标记已读
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > 0">
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
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="预警详情" width="520px">
      <el-descriptions :column="1" border size="large" v-if="detailItem">
        <el-descriptions-item label="异常指标">{{ indicatorName(detailItem) }}</el-descriptions-item>
        <el-descriptions-item label="异常数值">{{ detailItem.value || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预警等级">
          <el-tag :type="levelInfo(detailItem).type" effect="dark">{{ levelInfo(detailItem).text }}预警</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警时间">{{ formatTime(detailItem.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="isRead(detailItem)" type="info">已读</el-tag>
          <el-tag v-else type="danger">未读</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="detailItem.recordStatus === 1" type="success">已处理</el-tag>
          <el-tag v-else type="warning">待处理</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button v-if="detailItem && !isRead(detailItem)" type="success" size="large" @click="handleRead(detailItem)">标记已读</el-button>
        <el-button size="large" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAlertStore } from '@/store/alert'
import { getNotificationPage, readNotification, readAllNotifications } from '@/api/alert'

const alertStore = useAlertStore()

const loading = ref(false)
const activeTab = ref('unread')
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const detailVisible = ref(false)
const detailItem = ref(null)

// 字段对齐后端 AlertNotificationVO：
// id/alertRecordId/receiverId/isRead(0未读1已读)/readTime/createTime
// userId/userName/dataType/value/alertLevel(1低2中3高)/recordStatus(0待处理1已处理)
const isRead = (r) => r.isRead === 1 || r.isRead === true

// 未读数量：单独维护，切换Tab或标记已读时更新
const unreadCount = ref(0)
const hasUnread = computed(() => unreadCount.value > 0)

// 当前页展示的数据（服务端已分页，直接展示）
const filteredList = computed(() => list.value)

// 切换Tab时重置页码
const onTabChange = () => {
  queryParams.pageNum = 1
  loadList()
}

// 数据类型 code 转中文名
const dataTypeMap = {
  BLOOD_PRESSURE: '血压',
  BLOOD_SUGAR: '血糖',
  HEART_RATE: '心率',
  BLOOD_OXYGEN: '血氧'
}
const indicatorName = (r) => dataTypeMap[r.dataType] || r.dataType || '健康指标'

// 预警等级：1低2中3高
const levelInfo = (r) => {
  const lv = r.alertLevel
  if (lv === 3) return { text: '高', type: 'danger', color: '#f56c6c' }
  if (lv === 2) return { text: '中', type: 'warning', color: '#e6a23c' }
  return { text: '低', type: 'info', color: '#909399' }
}

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  if (isNaN(d.getTime())) return String(t)
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes())
}

// 加载预警通知列表
// 后端 /alert/notification/page 接口参数 pageNum/pageSize，返回 IPage<AlertNotificationVO>
// IPage 结构包含 records/total/size/current 等字段
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    // 未读Tab：传 isRead=0 过滤；全部Tab：不传
    if (activeTab.value === 'unread') {
      params.isRead = 0
    }
    const data = await getNotificationPage(params)
    list.value = (data && data.records) || (Array.isArray(data) ? data : []) || []
    total.value = (data && data.total) || list.value.length
    // 更新未读数量（仅在全部Tab时从服务端统计更准确）
    if (activeTab.value === 'all') {
      // 全部Tab下无法直接知道未读数，保留原值
    } else {
      // 未读Tab的total就是未读总数
      unreadCount.value = total.value
    }
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const showDetail = (item) => {
  detailItem.value = item
  detailVisible.value = true
}

const handleRead = async (item) => {
  item._reading = true
  try {
    await readNotification(item.id)
    alertStore.decrementUnread()
    ElMessage.success('已标记为已读')
    if (detailVisible.value) detailVisible.value = false
    // 重新加载当前页列表
    loadList()
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    item._reading = false
  }
}

const handleReadAll = async () => {
  try {
    await ElMessageBox.confirm('确定将所有预警通知标记为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await readAllNotifications()
    unreadCount.value = 0
    alertStore.resetUnread()
    ElMessage.success('已全部标记为已读')
    loadList()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  loadList()
})
</script>
<style scoped>
.alert-page {
  max-width: 1000px;
  margin: 0 auto;
  height: calc(100vh - 100px);
}

.alert-card {
  border-radius: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.alert-card :deep(.el-card__header) {
  flex-shrink: 0;
}

.alert-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.alert-card :deep(.el-tabs) {
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.read-all-btn {
  margin-left: auto;
}

.empty-wrap {
  padding: 40px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  flex-shrink: 0;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.alert-item {
  border-radius: 12px;
  border-left: 4px solid #dcdfe6;
  transition: all 0.2s;
}

.alert-item.unread {
  border-left-color: #f56c6c;
  background: #fffcfc;
}

.alert-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.alert-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.indicator-name {
  font-size: 19px;
  font-weight: 600;
  color: #303133;
}

.alert-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.alert-item-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.alert-value .value-label {
  font-size: 16px;
  color: #909399;
}

.alert-value .value-num {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
}

.alert-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: #909399;
}

.alert-item-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 适老化 */
.elderly-theme .card-header {
  font-size: 20px;
}

.elderly-theme .indicator-name {
  font-size: 21px;
}

.elderly-theme .alert-value .value-num {
  font-size: 24px;
}
</style>