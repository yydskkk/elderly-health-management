<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-col" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-card-inner">
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="26"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="quick-card">
      <template #header>
        <div class="card-header">
          <el-icon><Menu /></el-icon>
          <span>快捷入口</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="entry in quickEntries" :key="entry.path">
          <div class="quick-entry" @click="router.push(entry.path)">
            <div class="quick-icon" :style="{ color: entry.color }">
              <el-icon :size="32"><component :is="entry.icon" /></el-icon>
            </div>
            <div class="quick-text">{{ entry.title }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOverview } from '@/api/statistics'

const router = useRouter()

const overview = ref({})

const statCards = ref([
  { key: 'totalUsers', label: '总用户数', value: 0, icon: 'UserFilled', color: '#409EFF' },
  { key: 'totalElderly', label: '老年用户', value: 0, icon: 'Histogram', color: '#67C23A' },
  { key: 'totalFamily', label: '家属数', value: 0, icon: 'User', color: '#E6A23C' },
  { key: 'totalDoctor', label: '医护数', value: 0, icon: 'FirstAidKit', color: '#F56C6C' },
  { key: 'totalHealthData', label: '健康数据总量', value: 0, icon: 'DataLine', color: '#909399' },
  { key: 'totalAlerts', label: '预警总数', value: 0, icon: 'Bell', color: '#9C27B0' },
  { key: 'pendingAlerts', label: '待处理预警', value: 0, icon: 'Warning', color: '#FF9800' }
])

const quickEntries = [
  { path: '/admin/user', title: '用户管理', icon: 'UserFilled', color: '#409EFF' },
  { path: '/admin/role', title: '角色管理', icon: 'User', color: '#67C23A' },
  { path: '/admin/region', title: '辖区管理', icon: 'Location', color: '#E6A23C' },
  { path: '/admin/alert-rule', title: '预警规则', icon: 'Bell', color: '#F56C6C' },
  { path: '/admin/statistics', title: '统计分析', icon: 'TrendCharts', color: '#9C27B0' },
  { path: '/admin/log', title: '系统日志', icon: 'Document', color: '#909399' }
]

const loadOverview = async () => {
  try {
    const data = await getOverview()
    overview.value = data || {}
    statCards.value.forEach((card) => {
      const val = overview.value[card.key]
      card.value = val === undefined || val === null ? 0 : val
    })
  } catch (e) {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.stat-col {
  flex: 1 1 0;
  min-width: 120px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.quick-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.quick-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 8px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
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
  margin-bottom: 8px;
}

.quick-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}
</style>
