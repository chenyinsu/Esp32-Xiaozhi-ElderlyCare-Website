<template>
  <div class="reminder-page">
    <!-- 今日提醒 - 时间轴样式 + 动画 -->
    <el-card class="glass-card today-card" v-if="todayReminders.length > 0">
      <template #header>
        <span>📅 今日提醒</span>
        <el-badge :value="todayReminders.filter(r => !r.isTaken).length" type="warning" class="badge" />
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="reminder in todayReminders"
          :key="reminder.id"
          :timestamp="reminder.remindTime"
          placement="top"
          :type="reminder.isTaken ? 'success' : 'warning'"
          :icon="reminder.isTaken ? 'CircleCheck' : 'Clock'"
        >
          <div class="timeline-content">
            <strong>{{ reminder.title }}</strong>
            <p>{{ reminder.content || '无内容' }}</p>
            <div class="meta">
              <span>👤 {{ reminder.userName }}</span>
              <span>📱 {{ reminder.deviceName }}</span>
            </div>
            <el-button v-if="!reminder.isTaken" size="small" type="primary" @click="handleMarkAsTaken(reminder.id)">标记完成</el-button>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 搜索过滤 -->
    <el-card class="glass-card filter-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="提醒类型">
          <el-select v-model="queryParams.reminderType" placeholder="选择类型" clearable>
            <el-option label="用药" value="MEDICATION" />
            <el-option label="闹钟" value="ALARM" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="预约" value="APPOINTMENT" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="重复类型">
          <el-select v-model="queryParams.repeatType" placeholder="选择类型" clearable>
            <el-option label="不重复" value="NONE" />
            <el-option label="每天" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="工作日" value="WORKDAYS" />
            <el-option label="周末" value="WEEKENDS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isActive" placeholder="选择状态" clearable>
            <el-option label="激活" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="服用状态">
          <el-select v-model="queryParams.isTaken" placeholder="服用状态" clearable>
            <el-option label="已服用" :value="true" />
            <el-option label="未服用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showCreateDialog">添加提醒</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提醒列表表格 -->
    <el-card class="glass-card">
      <template #header><span>提醒列表</span></template>
      <el-table :data="reminders" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reminderType" label="类型" width="100">
          <template #default="{ row }">{{ reminderTypeMap[row.reminderType] || row.reminderType }}</template>
        </el-table-column>
        <el-table-column prop="repeatType" label="重复" width="100">
          <template #default="{ row }">{{ repeatTypeMap[row.repeatType] || row.repeatType }}</template>
        </el-table-column>
        <el-table-column prop="remindTime" label="提醒时间" width="100">
          <template #default="{ row }">{{ formatTime(row.remindTime) }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="deviceName" label="设备" width="120" />
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }"><el-switch :model-value="row.isActive" @change="toggleActive(row)" size="small" /></template>
        </el-table-column>
        <el-table-column prop="isTaken" label="已服用" width="80">
          <template #default="{ row }"><el-tag :type="row.isTaken ? 'success' : 'info'" size="small">{{ row.isTaken ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="success" link @click="handleMarkAsTaken(row.id)" v-if="!row.isTaken">标记已服用</el-button>
            <el-button size="small" type="warning" link @click="handleTrigger(row.id)">触发提醒</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10,20,50,100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框（完整，略简化但保留所有字段） -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑提醒' : '添加提醒'" width="600px" class="rounded-dialog">
      <el-form :model="reminderForm" label-width="100px">
        <el-form-item label="标题" required><el-input v-model="reminderForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="reminderForm.content" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="reminderForm.reminderType">
            <el-option label="用药提醒" value="MEDICATION" />
            <el-option label="闹钟" value="ALARM" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="预约" value="APPOINTMENT" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="重复">
          <el-select v-model="reminderForm.repeatType">
            <el-option label="不重复" value="NONE" />
            <el-option label="每天" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="工作日" value="WORKDAYS" />
            <el-option label="周末" value="WEEKENDS" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间" required>
          <el-time-picker v-model="reminderForm.remindTime" format="HH:mm:ss" value-format="HH:mm:ss" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="用药名称" v-if="reminderForm.reminderType === 'MEDICATION'"><el-input v-model="reminderForm.medicationName" /></el-form-item>
        <el-form-item label="剂量" v-if="reminderForm.reminderType === 'MEDICATION'"><el-input v-model="reminderForm.doseAmount" placeholder="如：1片" /></el-form-item>
        <el-form-item label="用户" required>
          <el-select v-model="reminderForm.userId" placeholder="选择用户"><el-option v-for="u in users" :key="u.id" :label="`${u.name} (${u.phone})`" :value="u.id" /></el-select>
        </el-form-item>
        <el-form-item label="设备" required>
          <el-select v-model="reminderForm.deviceId" placeholder="选择设备"><el-option v-for="d in devices" :key="d.id" :label="`${d.deviceName} (${d.deviceId})`" :value="d.id" /></el-select>
        </el-form-item>
        <el-form-item label="激活"><el-switch v-model="reminderForm.isActive" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">{{ isEdit ? '更新' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reminderApi, userApi, deviceApi } from '@/api'
import type { Reminder } from '@/api/type'

const loading = ref(false)
const reminders = ref<Reminder[]>([])
const todayReminders = ref<Reminder[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })
const queryParams = reactive({ reminderType: '', repeatType: '', isActive: undefined as boolean | undefined, isTaken: undefined as boolean | undefined })

const reminderTypeMap: Record<string,string> = { MEDICATION:'用药', ALARM:'闹钟', ACTIVITY:'活动', APPOINTMENT:'预约', CUSTOM:'自定义' }
const repeatTypeMap: Record<string,string> = { NONE:'不重复', DAILY:'每天', WEEKLY:'每周', MONTHLY:'每月', WORKDAYS:'工作日', WEEKENDS:'周末' }

const formVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitLoading = ref(false)
const users = ref<any[]>([])
const devices = ref<any[]>([])
const reminderForm = reactive({
  title: '', content: '', reminderType: 'MEDICATION', repeatType: 'DAILY',
  remindTime: '08:00:00', medicationName: '', doseAmount: '',
  userId: null as number | null, deviceId: null as number | null, isActive: true
})

const formatTime = (time: any) => {
  if (!time) return '--'
  if (typeof time === 'string') return time.substring(0,5)
  if (time.hour !== undefined) return `${String(time.hour).padStart(2,'0')}:${String(time.minute).padStart(2,'0')}`
  return time
}

const loadReminders = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.page, size: pagination.size }
    if (queryParams.reminderType) params.reminderType = queryParams.reminderType
    if (queryParams.repeatType) params.repeatType = queryParams.repeatType
    if (queryParams.isActive !== undefined) params.isActive = queryParams.isActive
    if (queryParams.isTaken !== undefined) params.isTaken = queryParams.isTaken
    const res = await reminderApi.getList(params)
    reminders.value = res.content
    pagination.total = res.totalElements
  } catch (error) { ElMessage.error('加载提醒失败') } finally { loading.value = false }
}
const loadTodayReminders = async () => { try { todayReminders.value = await reminderApi.getToday() } catch(e){} }
const loadUsersAndDevices = async () => {
  try {
    const [userRes, deviceRes] = await Promise.all([userApi.query({ size:100 }), deviceApi.getList({ size:100 })])
    users.value = userRes.content
    devices.value = deviceRes.content
  } catch(e){}
}
const handleSearch = () => { pagination.page = 1; loadReminders() }
const resetQuery = () => { Object.assign(queryParams, { reminderType:'', repeatType:'', isActive:undefined, isTaken:undefined }); pagination.page=1; loadReminders() }
const handlePageChange = (page: number) => { pagination.page = page; loadReminders() }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.page = 1; loadReminders() }
const showCreateDialog = () => {
  isEdit.value = false; editingId.value = null
  Object.assign(reminderForm, { title:'', content:'', reminderType:'MEDICATION', repeatType:'DAILY', remindTime:'08:00:00', medicationName:'', doseAmount:'', userId:null, deviceId:null, isActive:true })
  formVisible.value = true; loadUsersAndDevices()
}
const showEditDialog = (reminder: Reminder) => {
  isEdit.value = true; editingId.value = reminder.id
  Object.assign(reminderForm, {
    title: reminder.title, content: reminder.content || '', reminderType: reminder.reminderType,
    repeatType: reminder.repeatType, remindTime: typeof reminder.remindTime === 'string' ? reminder.remindTime : `${String(reminder.remindTime?.hour).padStart(2,'0')}:${String(reminder.remindTime?.minute).padStart(2,'0')}:00`,
    medicationName: reminder.medicationName || '', doseAmount: reminder.doseAmount || '',
    userId: reminder.userId, deviceId: reminder.deviceId, isActive: reminder.isActive
  })
  formVisible.value = true; loadUsersAndDevices()
}
const handleSubmit = async () => {
  if (!reminderForm.title || !reminderForm.userId || !reminderForm.deviceId) { ElMessage.warning('请填写必要信息'); return }
  submitLoading.value = true
  try {
    if (isEdit.value && editingId.value) await reminderApi.update(editingId.value, reminderForm as any)
    else await reminderApi.create(reminderForm as any)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    formVisible.value = false
    loadReminders(); loadTodayReminders()
  } catch (error) { ElMessage.error('操作失败') } finally { submitLoading.value = false }
}
const toggleActive = async (reminder: Reminder) => {
  try { await reminderApi.update(reminder.id, { isActive: !reminder.isActive } as any); ElMessage.success('状态更新'); loadReminders() }
  catch (error) { ElMessage.error('更新失败') }
}
const handleMarkAsTaken = async (id: number) => {
  try { await reminderApi.markAsTaken(id); ElMessage.success('已标记'); loadReminders(); loadTodayReminders() }
  catch (error) { ElMessage.error('操作失败') }
}
const handleTrigger = async (id: number) => {
  try { await reminderApi.trigger(id); ElMessage.success('已触发提醒') }
  catch (error) { ElMessage.error('触发失败') }
}
const handleDelete = async (id: number) => {
  try { await ElMessageBox.confirm('确定删除？','确认',{type:'warning'}); await reminderApi.delete(id); ElMessage.success('删除成功'); loadReminders(); loadTodayReminders() }
  catch (error) { if (error !== 'cancel') ElMessage.error('删除失败') }
}
onMounted(() => { loadReminders(); loadTodayReminders() })
</script>

<style scoped lang="scss">
.reminder-page {
  .today-card {
    margin-bottom: 20px;
    animation: fadeInUp 0.5s ease;
    .badge { margin-left: 10px; }
  }
  @keyframes fadeInUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
  }
  .timeline-content {
    background: #f9fafc;
    padding: 12px 16px;
    border-radius: 16px;
    p { margin: 8px 0; color: #606266; }
    .meta { display: flex; gap: 16px; font-size: 12px; color: #909399; margin-bottom: 8px; }
  }
  .filter-card { margin-bottom: 20px; }
  .pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
  .rounded-dialog :deep(.el-dialog) { border-radius: 24px; }
}
</style>
