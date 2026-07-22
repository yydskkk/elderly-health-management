<template>
  <div class="family-advice">
    <el-card shadow="never" class="selector-card">
      <template #header><div class="card-header"><el-icon><Connection /></el-icon><span>选择关联老人</span></div></template>
      <ElderlySelector v-model="elderlyId" @change="handleElderlyChange" />
    </el-card>
    <template v-if="elderlyId">
      <el-card shadow="never">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="健康建议" name="advice" />
          <el-tab-pane label="干预方案" name="intervention" />
        </el-tabs>
        <div v-if="activeTab === 'advice'" v-loading="adviceLoading">
          <el-empty v-if="!adviceLoading && adviceList.length === 0" description="暂无健康建议" />
          <div v-for="item in adviceList" :key="item.id" class="list-item">
            <div class="item-header">
              <el-tag v-if="item.adviceType" type="primary">{{ item.adviceType }}</el-tag>
              <span class="item-title">{{ item.title }}</span>
              <span class="item-time">{{ item.createTime }}</span>
            </div>
            <div class="item-content">{{ item.content }}</div>
            <div class="item-footer">
              <span v-if="item.doctorName" class="item-meta">发布医生：{{ item.doctorName }}</span>
              <span v-if="item.expireTime" class="item-meta">有效期：{{ item.expireTime }}</span>
            </div>
          </div>
        </div>
        <div v-if="activeTab === 'intervention'" v-loading="interventionLoading">
          <el-empty v-if="!interventionLoading && interventionList.length === 0" description="暂无干预方案" />
          <div v-for="item in interventionList" :key="item.id" class="list-item">
            <div class="item-header">
              <el-tag :type="statusTagType(item.status)">{{ statusText(item.status) }}</el-tag>
              <span class="item-title">{{ item.adviceTitle || "干预方案" }}</span>
              <el-button v-if="isPlanActive(item)" link type="primary" size="small" @click="openFeedback(item)">提交反馈</el-button>
            </div>
            <div class="item-content">{{ item.content || "-" }}</div>
            <div class="item-footer">
              <span v-if="item.cycle" class="item-meta">执行周期：{{ item.cycle }}</span>
              <span v-if="item.startDate" class="item-meta">开始：{{ item.startDate }}</span>
              <span v-if="item.endDate" class="item-meta">结束：{{ item.endDate }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </template>
    <el-card v-else shadow="never"><el-empty description="请先选择关联老人" /></el-card>
    <!-- 干预方案反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="提交干预方案反馈" width="520px">
      <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <el-form-item label="方案"><el-input :model-value="feedbackForm.planName" disabled /></el-form-item>
        <el-form-item label="是否已执行" prop="isExecuted">
          <el-radio-group v-model="feedbackForm.isExecuted">
            <el-radio-button :value="1">已执行</el-radio-button>
            <el-radio-button :value="0">未执行</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行感受" prop="executionFeeling">
          <el-input v-model="feedbackForm.executionFeeling" type="textarea" :rows="3" placeholder="请描述执行感受" />
        </el-form-item>
        <el-form-item label="异常情况" prop="abnormalSituation">
          <el-input v-model="feedbackForm.abnormalSituation" type="textarea" :rows="3" placeholder="如有异常情况请描述，无则留空" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="feedbackVisible = false">取消</el-button><el-button type="primary" :loading="feedbackLoading" @click="submitFeedback">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue"
import { ElMessage } from "element-plus"
import ElderlySelector from "@/components/ElderlySelector.vue"
import { getMine as getMyAdvice } from "@/api/healthAdvice"
import { getMine as getMyIntervention, addFeedback } from "@/api/interventionPlan"

const elderlyId = ref("")
const activeTab = ref("advice")
const adviceLoading = ref(false)
const interventionLoading = ref(false)
const adviceList = ref([])
const interventionList = ref([])

// InterventionPlanVO: status 0已终止1进行中2已完成
const statusText = (val) => {
  if (val === 0) return "已终止"
  if (val === 1) return "进行中"
  if (val === 2) return "已完成"
  return "-"
}
const statusTagType = (val) => {
  if (val === 0) return "info"
  if (val === 1) return "success"
  if (val === 2) return "primary"
  return "warning"
}
const isPlanActive = (item) => item.status === 1

const handleElderlyChange = (val) => {
  if (val) {
    loadAdvice()
    loadIntervention()
  } else {
    adviceList.value = []
    interventionList.value = []
  }
}
const handleTabChange = () => {
  if (activeTab.value === "advice" && adviceList.value.length === 0) loadAdvice()
  if (activeTab.value === "intervention" && interventionList.value.length === 0) loadIntervention()
}

const loadAdvice = async () => {
  if (!elderlyId.value) return
  adviceLoading.value = true
  try {
    // 健康建议API返回List（非分页），含interventionPlans子列表
    const data = await getMyAdvice({ elderlyId: elderlyId.value })
    adviceList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
  } catch (e) {
    adviceList.value = []
  } finally {
    adviceLoading.value = false
  }
}

const loadIntervention = async () => {
  if (!elderlyId.value) return
  interventionLoading.value = true
  try {
    // 干预方案mine API返回List（非分页），进行中的方案
    const data = await getMyIntervention({ elderlyId: elderlyId.value })
    interventionList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
  } catch (e) {
    interventionList.value = []
  } finally {
    interventionLoading.value = false
  }
}

// 干预方案反馈
const feedbackVisible = ref(false)
const feedbackLoading = ref(false)
const feedbackFormRef = ref(null)
// InterventionFeedbackAddDTO: planId/isExecuted/executionFeeling/abnormalSituation
const feedbackForm = reactive({ planId: undefined, planName: "", isExecuted: 1, executionFeeling: "", abnormalSituation: "" })
const feedbackRules = {
  isExecuted: [{ required: true, message: "请选择是否已执行", trigger: "change" }],
  executionFeeling: [{ required: true, message: "请描述执行感受", trigger: "blur" }]
}
const openFeedback = (item) => {
  feedbackForm.planId = item.id
  feedbackForm.planName = item.adviceTitle || "干预方案"
  feedbackForm.isExecuted = 1
  feedbackForm.executionFeeling = ""
  feedbackForm.abnormalSituation = ""
  feedbackVisible.value = true
}
const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  await feedbackFormRef.value.validate(async (valid) => {
    if (!valid) return
    feedbackLoading.value = true
    try {
      await addFeedback({
        planId: feedbackForm.planId,
        isExecuted: feedbackForm.isExecuted,
        executionFeeling: feedbackForm.executionFeeling,
        abnormalSituation: feedbackForm.abnormalSituation
      })
      ElMessage.success("反馈提交成功")
      feedbackVisible.value = false
      loadIntervention()
    } catch (e) {
      // 错误已由拦截器处理
    } finally {
      feedbackLoading.value = false
    }
  })
}
</script>

<style scoped>
.family-advice { display: flex; flex-direction: column; gap: 16px; }
.selector-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
.list-item { padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 12px; background: #fafafa; }
.item-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.item-title { font-size: 16px; font-weight: 600; color: #303133; flex: 1; }
.item-time { font-size: 13px; color: #909399; }
.item-content { font-size: 14px; color: #606266; line-height: 1.6; margin-bottom: 8px; white-space: pre-wrap; }
.item-footer { display: flex; gap: 16px; flex-wrap: wrap; }
.item-meta { font-size: 13px; color: #909399; }
.feedback-list { margin-top: 12px; padding-top: 12px; border-top: 1px dashed #dcdfe6; }
.feedback-item { display: flex; gap: 12px; padding: 6px 0; font-size: 13px; }
.feedback-time { color: #909399; flex-shrink: 0; }
.feedback-content { color: #606266; }
</style>
