<template>
  <div class="family-profile">
    <el-card shadow="never" class="selector-card">
      <template #header><div class="card-header"><el-icon><Connection /></el-icon><span>选择关联老人</span></div></template>
      <ElderlySelector v-model="elderlyId" @change="handleElderlyChange" />
    </el-card>

    <template v-if="elderlyId">
      <el-card shadow="never" v-loading="loading">
        <template #header><div class="card-header"><el-icon><Files /></el-icon><span>健康档案</span></div></template>
        <el-descriptions v-if="profile" :column="2" border>
          <el-descriptions-item label="姓名">{{ profile.name || "-" }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderText(profile.gender) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ profile.age != null ? profile.age + " 岁" : "-" }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile.phone || "-" }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ profile.email || "-" }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ profile.idCard || "-" }}</el-descriptions-item>
          <el-descriptions-item label="身高">{{ profile.height ? profile.height + " cm" : "-" }}</el-descriptions-item>
          <el-descriptions-item label="体重">{{ profile.weight ? profile.weight + " kg" : "-" }}</el-descriptions-item>
          <el-descriptions-item label="血型">{{ bloodTypeText(profile.bloodType) }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ profile.address || "-" }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ profile.emergencyContact || "-" }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系电话">{{ profile.emergencyPhone || "-" }}</el-descriptions-item>
          <el-descriptions-item label="既往病史" :span="2">{{ profile.medicalHistory || "-" }}</el-descriptions-item>
          <el-descriptions-item label="过敏史" :span="2">{{ profile.allergyHistory || "-" }}</el-descriptions-item>
          <el-descriptions-item label="家族病史" :span="2">{{ profile.familyHistory || "-" }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else-if="!loading" description="暂无健康档案信息" />
      </el-card>
    </template>
    <el-card v-else shadow="never"><el-empty description="请先选择关联老人" /></el-card>
  </div>
</template>

<script setup>
import { ref } from "vue"
import ElderlySelector from "@/components/ElderlySelector.vue"
import { getElderlyProfile } from "@/api/elderlyProfile"

const elderlyId = ref("")
const profile = ref(null)
const loading = ref(false)

const genderText = (g) => {
  // ElderlyProfileVO: gender 0女1男
  if (g === 1) return "男"
  if (g === 0) return "女"
  return "-"
}

const bloodTypeText = (val) => {
  const map = { A: "A型", B: "B型", AB: "AB型", O: "O型" }
  return map[val] || val || "-"
}

const handleElderlyChange = async (val) => {
  if (val) {
    await loadProfile(val)
  } else {
    profile.value = null
  }
}

const loadProfile = async (id) => {
  loading.value = true
  try {
    const data = await getElderlyProfile(id)
    profile.value = data || null
  } catch (e) {
    profile.value = null
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.family-profile { display: flex; flex-direction: column; gap: 16px; }
.selector-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
</style>
