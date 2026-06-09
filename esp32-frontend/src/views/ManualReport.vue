<template>
  <div class="manual-report-page">
    <el-card class="glass-card">
      <template #header>
        <div class="card-header">
          <span>✍️ 手动录入报告生成</span>
          <el-button type="primary" @click="generate" :loading="loading">生成报告</el-button>
        </div>
      </template>

      <el-form :inline="true">
        <el-form-item label="报告类型">
          <el-radio-group v-model="reportType">
            <el-radio label="DAILY_HEALTH">日报</el-radio>
            <el-radio label="WEEKLY_SUMMARY">周报</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>

      <div v-if="report" class="report-content">
        <h3>{{ report.title }}</h3>
        <pre>{{ report.content }}</pre>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="健康状态">
            <el-tag :type="healthStatusTag(report.healthStatus)">{{ report.healthStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="平均心率">{{ report.heartRate }} bpm</el-descriptions-item>
          <el-descriptions-item label="平均血压">{{ report.bloodPressureSys }}/{{ report.bloodPressureDia }} mmHg</el-descriptions-item>
          <el-descriptions-item label="平均体温">{{ report.bodyTemperature }} ℃</el-descriptions-item>
          <el-descriptions-item label="平均血氧">{{ report.bloodOxygen }}%</el-descriptions-item>
          <el-descriptions-item label="总步数">{{ report.stepCount }}</el-descriptions-item>
          <el-descriptions-item label="平均睡眠">{{ report.sleepDuration }}小时</el-descriptions-item>
          <el-descriptions-item label="按时服药">{{ report.medicationTaken }}次</el-descriptions-item>
          <el-descriptions-item label="漏服">{{ report.medicationMissed }}次</el-descriptions-item>
        </el-descriptions>

        <el-divider>智能建议</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="advice-card"><strong>健康建议</strong><br>{{ report.healthAdvice || '无' }}</div>
          </el-col>
          <el-col :span="8">
            <div class="advice-card"><strong>用药建议</strong><br>{{ report.medicationAdvice || '无' }}</div>
          </el-col>
          <el-col :span="8">
            <div class="advice-card"><strong>活动建议</strong><br>{{ report.activityAdvice || '无' }}</div>
          </el-col>
        </el-row>
      </div>

      <el-empty v-else description="请选择日期范围并点击「生成报告」" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { healthRecordApi } from '@/api/healthRecord'
import { useAuthStore } from '@/stores/auth'
import type { Report } from '@/api/types'

const authStore = useAuthStore()
const loading = ref(false)
const reportType = ref<'DAILY_HEALTH' | 'WEEKLY_SUMMARY'>('DAILY_HEALTH')
const dateRange = ref<[string, string] | null>(null)
const report = ref<Report | null>(null)

const healthStatusTag = (status: string) => {
  const map: Record<string, string> = {
    EXCELLENT: 'success',
    GOOD: 'primary',
    FAIR: 'warning',
    POOR: 'danger',
    CRITICAL: 'danger'
  }
  return map[status] || 'info'
}

const generate = async () => {
  if (!authStore.user?.id) {
    ElMessage.warning('请先登录')
    return
  }
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  const [startDate, endDate] = dateRange.value
  loading.value = true
  try {
    const res = await healthRecordApi.generateReport(authStore.user.id, startDate, endDate, reportType.value)
    report.value = res
    ElMessage.success('报告生成成功')
  } catch (err: any) {
    console.error('生成报告失败', err)
    ElMessage.error(err?.message || '生成失败，可能该时间段内无数据')
    report.value = null
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.manual-report-page {
  padding: 20px;
}
.glass-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
  padding: 20px;
}
.report-content {
  margin-top: 20px;
}
.advice-card {
  background: #f5f7fa;
  border-radius: 16px;
  padding: 16px;
}
</style>
