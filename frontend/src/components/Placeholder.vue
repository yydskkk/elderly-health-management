<template>
  <div class="page-placeholder">
    <el-result icon="info" :title="title" sub-title="功能开发中，敬请期待" />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '页面'
  }
})
</script>

<style scoped>
.page-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
</style>
