<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-button type="primary" size="small">添加设备</el-button>
        </div>
      </template>
      <el-empty description="设备管理功能开发中" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>