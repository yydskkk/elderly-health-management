<template>
  <div class="advice-page">
    <el-card shadow="never" class="advice-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><ChatDotRound /></el-icon>
          <span>健康建议与干预方案</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 健康建议 -->
        <el-tab-pane label="健康建议" name="advice">
          <div v-loading="adviceLoading">
            <el-empty v-if="!adviceLoading && adviceList.length === 0" description="暂无健康建议" />
            <div class="list-wrap">
              <el-card v-for="item in adviceList" :key="item.id" shadow="hover" class="list-item">
                <div class="item-header">
                  <div class="item-title">
                    <el-icon color="#409EFF"><Document /></el-icon>
                    <span>{{ item.title || '健康建议' }}</span>
                  </div>
                  <el-tag v-if="item.adviceType" type="primary" size="large">
                    {{ item.adviceType }}
                  </el-tag>
                </div>
                <div class="item-content">{{ item.content || '暂无内容' }}</div>
                <div class="item-footer">
                  <span class="footer-item">
                    <el-icon><User /></el-icon>
                    发布医生：{{ item.doctorName || '医生' }}
                  </span>
                  <span class="footer-item">
                    <el-icon><Clock /></el-icon>
                    有效期：{{ formatTime(item.expireTime) }}
                  </span>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 干预方案 -->
        <el-tab-pane label="干预方案" name="intervention">
          <div v-loading="planLoading">
            <el-empty v-if="!planLoading && planList.length === 0" description="暂无干预方案" />
            <div class="list-wrap">
              <el-card v-for="item in planList" :key="item.id" shadow="hover" class="list-item">
                <div class="item-header">
                  <div class="item-title">
                    <el-icon color="#67C23A"><SetUp /></el-icon>
                    <span>{{ item.adviceTitle || '干预方案' }}</span>
                  </div>
                  <el-tag :type="planStatusInfo(item).type" size="large">{{ planStatusInfo(item).text }}</el-tag>
                </div>
                <div class="item-content">{{ item.content || '暂无内容' }}</div>
                <div class="item-footer">
                  <span class="footer-item">
                    <el-icon><Calendar /></el-icon>
                    执行周期：{{ item.cycle || '-' }}
                  </span>
                  <span class="footer-item" v-if="item.startDate || item.endDate">
                    <el-icon><Clock /></el-icon>
                    {{ formatTime(item.startDate) }} 至 {{ formatTime(item.endDate) }}
                  </span>
                </div>
                <div class="item-actions" v-if="isPlanActive(item)">
                  <el-button type="primary" size="large" @click="openFeedback(item)">
                    <el-icon><EditPen /></el-icon>提交反馈
                  </el-button>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="干预方案反馈" width="520px">
      <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules" label-width="120px">
        <el-form-item label="是否已执行" prop="isExecuted">
          <el-radio-group v-model="feedbackForm.isExecuted" size="large">
            <el-radio-button :value="1">已执行</el-radio-button>
            <el-radio-button :value="0">未执行</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行感受" prop="executionFeeling">
          <el-input v-model="feedbackForm.executionFeeling" type="textarea" :rows="3" placeholder="请描述您的执行感受" />
        </el-form-item>
        <el-form-item label="异常情况" prop="abnormalSituation">
          <el-input v-model="feedbackForm.abnormalSituation" type="textarea" :rows="3" placeholder="如有异常情况请描述，无则留空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" size="large" :loading="feedbackSubmitting" @click="submitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMine as getMyAdvice } from '@/api/healthAdvice'
import { getMine as getMyPlans, addFeedback } from '@/api/interventionPlan'

const activeTab = ref('advice')

const adviceList = ref([])
const adviceLoading = ref(false)
const planList = ref([])
const planLoading = ref(false)

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  if (isNaN(d.getTime())) return String(t)
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate())
}

// 状态信息：后端 InterventionPlanVO.status，0已终止1进行中2已完成
const planStatusInfo = (r) => {
  const s = r.status
  if (s === 0) return { text: '已终止', type: 'danger' }
  if (s === 2) return { text: '已完成', type: 'info' }
  return { text: '进行中', type: 'success' }
}

const isPlanActive = (r) => r.status === 1

// 加载健康建议（后端 /health-advice/mine 返回 List<HealthAdviceVO>）
const loadAdvice = async () => {
  adviceLoading.value = true
  try {
    const data = await getMyAdvice()
    adviceList.value = Array.isArray(data) ? data : []
  } catch (e) {
    adviceList.value = []
  } finally {
    adviceLoading.value = false
  }
}

// 加载干预方案（后端 /intervention-plan/mine 返回 List<InterventionPlanVO>）
const loadPlans = async () => {
  planLoading.value = true
  try {
    const data = await getMyPlans()
    planList.value = Array.isArray(data) ? data : []
  } catch (e) {
    planList.value = []
  } finally {
    planLoading.value = false
  }
}

const onTabChange = (name) => {
  if (name === 'intervention' && planList.value.length === 0) {
    loadPlans()
  }
}

// 反馈表单（字段对齐后端 InterventionFeedbackAddDTO：
// planId/isExecuted(0未执行1已执行)/executionFeeling/abnormalSituation）
const feedbackVisible = ref(false)
const feedbackSubmitting = ref(false)
const feedbackFormRef = ref(null)
const feedbackForm = reactive({
  planId: null,
  isExecuted: 1,
  executionFeeling: '',
  abnormalSituation: ''
})

const feedbackRules = {
  isExecuted: [{ required: true, message: '请选择是否已执行', trigger: 'change' }],
  executionFeeling: [{ required: true, message: '请描述执行感受', trigger: 'blur' }]
}

const openFeedback = (plan) => {
  feedbackForm.planId = plan.id
  feedbackForm.isExecuted = 1
  feedbackForm.executionFeeling = ''
  feedbackForm.abnormalSituation = ''
  feedbackVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  await feedbackFormRef.value.validate(async (valid) => {
    if (!valid) return
    feedbackSubmitting.value = true
    try {
      await addFeedback({
        planId: feedbackForm.planId,
        isExecuted: feedbackForm.isExecuted,
        executionFeeling: feedbackForm.executionFeeling,
        abnormalSituation: feedbackForm.abnormalSituation
      })
      ElMessage.success('反馈提交成功，感谢您的反馈！')
      feedbackVisible.value = false
      // 刷新干预方案列表，以展示最新反馈
      loadPlans()
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      feedbackSubmitting.value = false
    }
  })
}

onMounted(() => {
  loadAdvice()
  loadPlans()
})
</script>
<style scoped>
.advice-page {
  max-width: 1000px;
  margin: 0 auto;
}

.advice-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.list-item {
  border-radius: 12px;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 19px;
  font-weight: 600;
  color: #303133;
}

.item-content {
  font-size: 16px;
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
  margin-bottom: 12px;
}

.item-footer {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 15px;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

/* 适老化 */
.elderly-theme .card-header {
  font-size: 20px;
}

.elderly-theme .item-title {
  font-size: 21px;
}

.elderly-theme .item-content {
  font-size: 18px;
}

.elderly-theme .item-footer {
  font-size: 17px;
}
</style>