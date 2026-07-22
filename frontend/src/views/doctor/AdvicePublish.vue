<template>
  <div class="advice-publish-page">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header"><span>发布健康建议</span></div>
      </template>
      <el-form ref="adviceFormRef" :model="adviceForm" :rules="adviceRules" label-width="100px">
        <el-form-item label="目标老人" prop="userId">
          <el-select v-model="adviceForm.userId" placeholder="请选择老人" filterable style="--el-select-width: 280px; width: 280px">
            <el-option v-for="item in elderlyOptions" :key="item.userId" :label="item.elderlyName" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="adviceForm.title" placeholder="请输入建议标题" style="width: 480px" />
        </el-form-item>
        <el-form-item label="建议类型" prop="adviceType">
          <el-select v-model="adviceForm.adviceType" placeholder="请选择建议类型" style="--el-select-width: 200px; width: 200px">
            <el-option label="饮食" value="饮食" />
            <el-option label="运动" value="运动" />
            <el-option label="用药" value="用药" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="adviceForm.content" type="textarea" :rows="4" placeholder="请输入建议内容" style="width: 600px" />
        </el-form-item>
        <el-form-item label="有效期" prop="expireTime">
          <el-date-picker v-model="adviceForm.expireTime" type="date" placeholder="请选择有效期" value-format="YYYY-MM-DD" style="width: 200px" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span>干预方案（可添加多个）</span>
          <el-button type="primary" :icon="Plus" size="small" @click="addPlan">添加方案</el-button>
        </div>
      </template>
      <div v-for="(plan, index) in adviceForm.interventionPlans" :key="index" class="plan-item">
        <el-row :gutter="12">
          <el-col :span="20">
            <el-form label-width="90px">
              <el-form-item label="方案内容">
                <el-input v-model="plan.content" type="textarea" :rows="2" placeholder="请输入方案内容" />
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="8">
                  <el-form-item label="开始日期">
                    <el-date-picker v-model="plan.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="结束日期">
                    <el-date-picker v-model="plan.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="执行周期">
                    <el-input v-model="plan.cycle" placeholder="如：每日/每周" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-col>
          <el-col :span="4" class="plan-action">
            <el-button type="danger" :icon="Delete" circle @click="removePlan(index)" />
          </el-col>
        </el-row>
      </div>
      <el-empty v-if="adviceForm.interventionPlans.length === 0" description="暂无干预方案，点击右上角添加" />
    </el-card>

    <div class="submit-bar">
      <el-button type="primary" :loading="submitLoading" @click="submitPublish">发布建议</el-button>
      <el-button @click="resetForm">重置</el-button>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { add as addAdvice } from '@/api/healthAdvice'
import { getPage as getProfilePage } from '@/api/elderlyProfile'

const router = useRouter()
const elderlyOptions = ref([])
const submitLoading = ref(false)
const adviceFormRef = ref(null)

const adviceForm = reactive({
  userId: '',
  title: '',
  adviceType: '',
  content: '',
  expireTime: '',
  interventionPlans: []
})

const adviceRules = {
  userId: [{ required: true, message: '请选择目标老人', trigger: 'change' }],
  title: [{ required: true, message: '请输入建议标题', trigger: 'blur' }],
  adviceType: [{ required: true, message: '请选择建议类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入建议内容', trigger: 'blur' }]
}

const loadElderly = async () => {
  try {
    const data = await getProfilePage({ pageNum: 1, pageSize: 200 })
    const list = data?.records || []
    // 健康建议的 userId 为老人用户ID
    elderlyOptions.value = list.map((e) => ({ userId: e.userId, elderlyName: e.elderlyName }))
  } catch (e) {}
}

const addPlan = () => {
  adviceForm.interventionPlans.push({
    content: '',
    startDate: '',
    endDate: '',
    cycle: ''
  })
}

const removePlan = (index) => {
  adviceForm.interventionPlans.splice(index, 1)
}

const submitPublish = async () => {
  if (!adviceFormRef.value) return
  await adviceFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      // 构造与 HealthAdviceAddDTO 一致的载荷
      const payload = {
        userId: adviceForm.userId,
        title: adviceForm.title,
        adviceType: adviceForm.adviceType,
        content: adviceForm.content,
        // expireTime 为 LocalDateTime（yyyy-MM-dd HH:mm:ss）
        expireTime: adviceForm.expireTime ? adviceForm.expireTime + ' 23:59:59' : undefined,
        interventionPlans: adviceForm.interventionPlans.filter((p) => p.content)
      }
      await addAdvice(payload)
      ElMessage.success('发布成功')
      resetForm()
      router.push('/doctor/intervention-manage')
    } catch (e) {} finally {
      submitLoading.value = false
    }
  })
}

const resetForm = () => {
  adviceForm.userId = ''
  adviceForm.title = ''
  adviceForm.adviceType = ''
  adviceForm.content = ''
  adviceForm.expireTime = ''
  adviceForm.interventionPlans = []
  adviceFormRef.value?.clearValidate()
}

onMounted(() => {
  loadElderly()
})
</script>

<style scoped>
.advice-publish-page { display: flex; flex-direction: column; gap: 16px; }
.form-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; }
.plan-item { padding: 16px; background: #f5f7fa; border-radius: 8px; margin-bottom: 12px; }
.plan-action { display: flex; align-items: center; justify-content: center; }
.submit-bar { display: flex; justify-content: center; gap: 12px; padding: 12px 0; }
</style>