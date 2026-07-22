<template>
  <el-select
    v-model="selectedId"
    :placeholder="placeholder"
    :loading="loading"
    clearable
    :style="{ '--el-select-width': width, width: width }"
    @change="handleChange"
  >
    <el-option
      v-for="item in elderlyList"
      :key="item.elderlyId || item.id"
      :label="getLabel(item)"
      :value="item.elderlyId || item.id"
    />
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyElderly } from '@/api/relation'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择关联老人'
  },
  width: {
    type: String,
    default: '260px'
  },
  autoLoad: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'load'])

const selectedId = ref(props.modelValue)
const elderlyList = ref([])
const loading = ref(false)

const getLabel = (item) => {
  const name = item.elderlyName || item.name || item.elderlyEmail || item.email || '未知'
  const relation = item.relationType || item.relation || ''
  return relation ? `${name}（${relation}）` : name
}

const loadList = async () => {
  loading.value = true
  try {
    const data = await getMyElderly()
    const list = Array.isArray(data) ? data : (data?.records || data?.list || [])
    elderlyList.value = list
    emit('load', list)

    if (list.length > 0 && !selectedId.value) {
      const first = list[0]
      const firstId = first.elderlyId || first.id
      selectedId.value = firstId
      emit('update:modelValue', firstId)
      emit('change', firstId, first)
    }
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

const handleChange = (val) => {
  emit('update:modelValue', val)
  const selected = elderlyList.value.find((item) => (item.elderlyId || item.id) === val)
  emit('change', val, selected)
}

watch(
  () => props.modelValue,
  (val) => {
    selectedId.value = val
  }
)

defineExpose({
  loadList,
  getList: () => elderlyList.value
})

onMounted(() => {
  if (props.autoLoad) {
    loadList()
  }
})
</script>
