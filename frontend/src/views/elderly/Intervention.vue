<template>
  <div class="intervention-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="22"><SetUp /></el-icon>
          <span>我的干预方案</span>
          <el-button class="refresh-btn" text :loading="loading" @click="loadPlans">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && planList.length === 0" description="暂无干预方案">
          <template #image>
            <el-icon :size="64" color="#C0C4CC"><DocumentRemove /></el-icon>
          </template>
        </el-empty>

        <div class="plan-list">
          <el-card
            v-for="plan in planList"
            :key="plan.id"
            shadow="hover"
            class="plan-item"
          >
            <!-- 方案头部 -->
            <div class="plan-header">
              <div class="plan-title">
                <el-icon :size="24" color="#67C23A"><SetUp /></el-icon>
                <span>{{ plan.adviceTitle || '干预方案' }}</span>
              </div>
              <el-tag :type="statusInfo(plan).type" size="large" effect="light">
                {{ statusInfo(plan).text }}
              </el-tag>
            </div>

            <!-- 方案内容 -->
            <div class="plan-content">{{ plan.content || '暂无内容' }}</div>

            <!-- 方案信息 -->
            <div class="plan-info">
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span class="info-label">执行周期：</span>
                <span class="info-value">{{ plan.cycle || '-' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span class="info-label">起止时间：</span>
                <span class="info-value">{{ formatDate(plan.startDate) }} 至 {{ formatDate(plan.endDate) }}</span>
              </div>
            </div>

            <!-- 反馈记录 -->
            <div class="feedback-section" v-if="plan.feedbacks && plan.feedbacks.length > 0">
              <div class="section-title">
                <el-icon><ChatLineSquare /></el-icon>
                <span>我的反馈记录（{{ plan.feedbacks.length }}）</span>
              </div>
              <el-timeline>
                <el-timeline-item
                  v-for="fb in plan.feedbacks"
                  :key="fb.id"
                  :timestamp="formatDateTime(fb.feedbackTime || fb.createTime)"
                  placement="top"
                  :type="fb.isExecuted === 1 ? 'success' : 'warning'"
                  size="large"
                >
                  <div class="feedback-card">
                    <div class="feedback-status">
                      <el-tag :type="fb.isExecuted === 1 ? 'success' : 'warning'" size="large">
                        {{ fb.isExecuted === 1 ? '已执行' : '未执行' }}
                      </el-tag>
                    </div>
                    <div class="feedback-row" v-if="fb.executionFeeling">
                      <span class="row-label">执行感受：</span>
                      <span class="row-text">{{ fb.executionFeeling }}</span>
                    </div>
                    <div class="feedback-row" v-if="fb.abnormalSituation">
                      <span class="row-label">异常情况：</span>
                      <span class="row-text">{{ fb.abnormalSituation }}</span>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>

            <!-- 操作按钮 -->
            <div class="plan-actions" v-if="isPlanActive(plan)">
              <el-button
                type="primary"
                size="large"
                round
                @click="openFeedback(plan)"
              >
                <el-icon><EditPen /></el-icon>提交执行反馈
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 反馈弹窗 -->
    <el-dialog
      v-model="feedbackVisible"
      title="提交执行反馈"
      width="560px"
      :close-on-click-modal="false"
    >
      <div class="dialog-plan-tip" v-if="currentPlan">
        <el-icon color="#409EFF"><InfoFilled /></el-icon>
        <span>{{ currentPlan.adviceTitle || '干预方案' }}</span>
      </div>
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="是否已执行" prop="isExecuted">
          <el-radio-group v-model="feedbackForm.isExecuted" size="large">
            <el-radio-button :value="1">已执行</el-radio-button>
            <el-radio-button :value="0">未执行</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行感受" prop="executionFeeling">
          <el-input
            v-model="feedbackForm.executionFeeling"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请描述您的执行感受，例如身体是否有不适、执行情况如何等"
          />
        </el-form-item>
        <el-form-item label="异常情况" prop="abnormalSituation">
          <el-input
            v-model="feedbackForm.abnormalSituation"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="如有异常情况请描述，无则留空"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="feedbackVisible = false">取消</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="feedbackSubmitting"
          @click="submitFeedback"
        >
          提交反馈
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMine, addFeedback } from '@/api/interventionPlan'

const loading = ref(false)
const planList = ref([])

// 状态信息：后端 InterventionPlanVO.status，0已终止1进行中2已完成
const statusInfo = (plan) => {
  const s = plan.status
  if (s === 0) return { text: '已终止', type: 'danger' }
  if (s === 2) return { text: '已完成', type: 'info' }
  return { text: '进行中', type: 'success' }
}

// 仅"进行中"状态可提交反馈
const isPlanActive = (plan) => plan.status === 1

// 日期格式化
const formatDate = (t) => {
  if (!t) return '-'
  return String(t).substring(0, 10)
}

const formatDateTime = (t) => {
  if (!t) return ''
  return String(t).substring(0, 16).replace('T', ' ')
}

// 加载干预方案（后端 /intervention-plan/mine 返回 List<InterventionPlanVO>）
const loadPlans = async () => {
  loading.value = true
  try {
    const data = await getMine()
    planList.value = Array.isArray(data) ? data : []
  } catch (e) {
    planList.value = []
  } finally {
    loading.value = false
  }
}

// 反馈弹窗
const feedbackVisible = ref(false)
const feedbackSubmitting = ref(false)
const feedbackFormRef = ref(null)
const currentPlan = ref(null)
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
  currentPlan.value = plan
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
      ElMessage.success('反馈提交成功，感谢您的配合！')
      feedbackVisible.value = false
      loadPlans()
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      feedbackSubmitting.value = false
    }
  })
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.intervention-page {
  max-width: 1000px;
  margin: 0 auto;
}

.main-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.refresh-btn {
  margin-left: auto;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-item {
  border-radius: 12px;
}

.plan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 19px;
  font-weight: 600;
  color: #303133;
}

.plan-content {
  font-size: 16px;
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.plan-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 15px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.feedback-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.feedback-card {
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
}

.feedback-status {
  margin-bottom: 8px;
}

.feedback-row {
  font-size: 15px;
  line-height: 1.6;
  margin-top: 6px;
}

.row-label {
  color: #909399;
  font-weight: 500;
}

.row-text {
  color: #606266;
}

.plan-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.dialog-plan-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #ecf5ff;
  padding: 10px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 15px;
  color: #409EFF;
  font-weight: 500;
}

/* 适老化样式 */
.elderly-theme .card-header {
  font-size: 20px;
}

.elderly-theme .plan-title {
  font-size: 21px;
}

.elderly-theme .plan-content {
  font-size: 18px;
}

.elderly-theme .plan-info {
  font-size: 17px;
}

.elderly-theme .section-title {
  font-size: 18px;
}

.elderly-theme .feedback-row {
  font-size: 17px;
}
</style>
