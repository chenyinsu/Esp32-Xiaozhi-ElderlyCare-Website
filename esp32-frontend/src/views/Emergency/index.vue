<template>
  <div class="emergency-page">
    <!-- 统计卡片 + 趋势图标 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4">
        <el-card class="glass-card stat-card">
          <div class="stat-value">{{ statistics.totalEmergencies }}</div>
          <div class="stat-label">总事件数</div>
          <el-icon class="trend-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="glass-card stat-card warning">
          <div class="stat-value">{{ statistics.pendingCount }}</div>
          <div class="stat-label">待处理</div>
          <el-icon class="trend-icon up"><CaretTop /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="glass-card stat-card info">
          <div class="stat-value">{{ statistics.handlingCount }}</div>
          <div class="stat-label">处理中</div>
          <el-icon class="trend-icon"><Loading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="glass-card stat-card success">
          <div class="stat-value">{{ statistics.resolvedCount }}</div>
          <div class="stat-label">已解决</div>
          <el-icon class="trend-icon"><Select /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="glass-card">
          <div class="stat-value">{{ statistics.todayCount }}</div>
          <div class="stat-label">今日新增</div>
          <el-icon class="trend-icon"><Sunrise /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="glass-card">
          <div class="stat-value">{{ formatResponseTime(statistics.averageResponseTime) }}</div>
          <div class="stat-label">平均响应</div>
          <el-icon class="trend-icon"><Timer /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索过滤 -->
    <el-card class="glass-card filter-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="事件类型">
          <el-select v-model="queryParams.emergencyType" placeholder="选择类型" clearable>
            <el-option label="按钮按下" value="BUTTON_PRESS" />
            <el-option label="跌倒检测" value="FALL_DETECTION" />
            <el-option label="健康异常" value="HEALTH_ABNORMAL" />
            <el-option label="无活动" value="NO_MOVEMENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件等级">
          <el-select v-model="queryParams.emergencyLevel" placeholder="选择等级" clearable>
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="HANDLING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="误报" value="FALSE_ALARM" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="timeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 事件列表 + 快速处理按钮 + 悬浮详情 -->
    <el-card class="glass-card">
      <template #header>
        <div class="card-header">
          <span>紧急事件列表</span>
          <el-button type="primary" size="small" @click="refreshData" :icon="RefreshRight">刷新</el-button>
        </div>
      </template>
      <el-table :data="emergencies" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="emergencyType" label="类型" width="120">
          <template #default="{ row }">{{ getTypeText(row.emergencyType) }}</template>
        </el-table-column>
        <el-table-column prop="emergencyLevel" label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="emergencyLevelMap[row.emergencyLevel]?.type" size="small">{{ emergencyLevelMap[row.emergencyLevel]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="emergencyStatusMap[row.status]?.type" size="small">{{ emergencyStatusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="deviceName" label="设备" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="triggerTime" label="触发时间" width="180">
          <template #default="{ row }">{{ formatDate(row.triggerTime) }}</template>
        </el-table-column>
        <el-table-column prop="handledByName" label="处理人" width="100" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row.id)">详情</el-button>
            <el-tooltip :content="row.description || '无描述'" placement="top" :open-delay="300" v-if="row.status === 'PENDING'">
              <el-button size="small" type="warning" link @click="handleQuickProcess(row)">快速处理</el-button>
            </el-tooltip>
            <el-button
              size="small" type="success" link @click="handleResolve(row)"
              v-if="row.status === 'HANDLING'"
            >解决</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="事件详情" width="700px" class="rounded-dialog">
      <el-descriptions :column="2" border v-if="currentEmergency" v-loading="detailLoading">
        <el-descriptions-item label="ID">{{ currentEmergency.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeText(currentEmergency.emergencyType) }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="emergencyLevelMap[currentEmergency.emergencyLevel]?.type">{{ emergencyLevelMap[currentEmergency.emergencyLevel]?.text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="emergencyStatusMap[currentEmergency.status]?.type">{{ emergencyStatusMap[currentEmergency.status]?.text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentEmergency.userName }}</el-descriptions-item>
        <el-descriptions-item label="设备">{{ currentEmergency.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ formatDate(currentEmergency.triggerTime) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentEmergency.description || '--' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentEmergency.handledByName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentEmergency.resolutionNote || '--' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 解决事件弹窗 -->
    <el-dialog v-model="resolveVisible" title="解决事件" width="500px" class="rounded-dialog">
      <el-form :model="resolveForm">
        <el-form-item label="处理备注">
          <el-input v-model="resolveForm.resolutionNote" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="resolveForm.status">
            <el-radio value="RESOLVED">已解决</el-radio>
            <el-radio value="FALSE_ALARM">误报</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResolve" :loading="resolveLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { TrendCharts, CaretTop, Loading, Select, Sunrise, Timer, RefreshRight } from '@element-plus/icons-vue'
import { emergencyApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { formatDate, emergencyLevelMap, emergencyStatusMap } from '@/utils/format'
import type { Emergency, EmergencyStatistics, EmergencyStatus } from '@/api/type'

const authStore = useAuthStore()
const loading = ref(false)
const emergencies = ref<Emergency[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })
const timeRange = ref<[string, string] | null>(null)
const queryParams = reactive({ emergencyType: '', emergencyLevel: '', status: '', startTime: '', endTime: '' })
const statistics = ref<EmergencyStatistics>({
  totalEmergencies: 0, pendingCount: 0, handlingCount: 0, resolvedCount: 0,
  closedCount: 0, falseAlarmCount: 0, todayCount: 0, thisWeekCount: 0,
  averageResponseTime: 0, countByType: {}, countByLevel: {}
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentEmergency = ref<Emergency | null>(null)

const resolveVisible = ref(false)
const resolveLoading = ref(false)
const resolvingEmergencyId = ref<number | null>(null)
const resolveForm = reactive({ status: 'RESOLVED' as EmergencyStatus, resolutionNote: '' })

const typeTextMap: Record<string, string> = {
  BUTTON_PRESS: '按钮按下', FALL_DETECTION: '跌倒检测', HEALTH_ABNORMAL: '健康异常',
  NO_MOVEMENT: '无活动', OTHER: '其他'
}
const getTypeText = (type: string) => typeTextMap[type] || type

const formatResponseTime = (time: number) => {
  if (!time) return '--'
  if (time < 60) return `${time.toFixed(0)}秒`
  if (time < 3600) return `${(time / 60).toFixed(1)}分钟`
  return `${(time / 3600).toFixed(1)}小时`
}

const loadEmergencies = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.page, size: pagination.size }
    if (queryParams.emergencyType) params.emergencyType = queryParams.emergencyType
    if (queryParams.emergencyLevel) params.emergencyLevel = queryParams.emergencyLevel
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.startTime) params.startTime = queryParams.startTime
    if (queryParams.endTime) params.endTime = queryParams.endTime
    const res = await emergencyApi.getList(params)
    emergencies.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载事件列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    statistics.value = await emergencyApi.getStatistics()
  } catch (error) { console.error('加载统计失败') }
}

const refreshData = () => { loadEmergencies(); loadStatistics() }
const handleSearch = () => {
  pagination.page = 1
  if (timeRange.value) {
    queryParams.startTime = timeRange.value[0]
    queryParams.endTime = timeRange.value[1]
  }
  loadEmergencies()
}
const resetQuery = () => {
  Object.assign(queryParams, { emergencyType: '', emergencyLevel: '', status: '', startTime: '', endTime: '' })
  timeRange.value = null
  pagination.page = 1
  loadEmergencies()
}
const handlePageChange = (page: number) => { pagination.page = page; loadEmergencies() }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.page = 1; loadEmergencies() }

const showDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  try { currentEmergency.value = await emergencyApi.getById(id) }
  catch (error) { ElMessage.error('加载详情失败') }
  finally { detailLoading.value = false }
}

const handleQuickProcess = async (emergency: Emergency) => {
  try {
    await emergencyApi.updateStatus(emergency.id, 'HANDLING', authStore.user?.id || 1)
    ElMessage.success('已标记为处理中')
    refreshData()
  } catch (error) { ElMessage.error('操作失败') }
}

const handleResolve = (emergency: Emergency) => {
  resolvingEmergencyId.value = emergency.id
  resolveForm.status = 'RESOLVED'
  resolveForm.resolutionNote = ''
  resolveVisible.value = true
}

const confirmResolve = async () => {
  if (!resolvingEmergencyId.value) return
  resolveLoading.value = true
  try {
    await emergencyApi.updateStatus(resolvingEmergencyId.value, resolveForm.status, authStore.user?.id || 1, resolveForm.resolutionNote)
    ElMessage.success('操作成功')
    resolveVisible.value = false
    refreshData()
  } catch (error) { ElMessage.error('操作失败') }
  finally { resolveLoading.value = false }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该事件吗？', '确认删除', { type: 'warning' })
    await emergencyApi.delete(id)
    ElMessage.success('删除成功')
    refreshData()
  } catch (error) { if (error !== 'cancel') ElMessage.error('删除失败') }
}

onMounted(() => { loadEmergencies(); loadStatistics() })
</script>

<style scoped lang="scss">
.emergency-page {
  .stat-row { margin-bottom: 20px; }
  .stat-card { position: relative; .trend-icon { position: absolute; top: 12px; right: 12px; font-size: 18px; color: #aaa; &.up { color: #f56c6c; } } }
  .filter-card { margin-bottom: 20px; }
  .card-header { display: flex; justify-content: space-between; align-items: center; }
  .pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
  .rounded-dialog :deep(.el-dialog) { border-radius: 24px; }
}
</style>
