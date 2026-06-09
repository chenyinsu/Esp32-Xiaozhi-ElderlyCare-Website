<template>
  <div class="device-page">
    <!-- 统计卡片 + 刷新按钮 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="glass-card stat-card" shadow="hover">
          <div class="stat-value">{{ statistics.totalDevices }}</div>
          <div class="stat-label">总设备数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="glass-card stat-card success">
          <div class="stat-value">{{ statistics.onlineDevices }}</div>
          <div class="stat-label">在线设备</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="glass-card stat-card warning">
          <div class="stat-value">{{ statistics.lowBatteryDevices }}</div>
          <div class="stat-label">低电量设备</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="glass-card stat-card danger">
          <div class="stat-value">{{ statistics.offlineDevices + statistics.errorDevices }}</div>
          <div class="stat-label">离线/故障</div>
        </el-card>
      </el-col>
      <el-col :span="24" class="refresh-btn-col">
        <el-button type="primary" @click="refreshData" :icon="RefreshRight">刷新</el-button>
      </el-col>
    </el-row>

    <!-- 搜索过滤 -->
    <el-card class="glass-card filter-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="设备ID">
          <el-input v-model="queryParams.deviceId" placeholder="输入设备ID" clearable />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="queryParams.deviceName" placeholder="输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="错误" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定状态">
          <el-select v-model="queryParams.boundToUser" placeholder="选择状态" clearable>
            <el-option label="已绑定" :value="true" />
            <el-option label="未绑定" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showCreateDialog">添加设备</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设备列表 -->
    <el-card class="glass-card">
      <template #header><span>设备列表</span></template>
      <el-table :data="devices" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceId" label="设备标识" width="150" show-overflow-tooltip />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="deviceStatusMap[row.status]?.type">{{ deviceStatusMap[row.status]?.text || row.status }}</el-tag>
          </template>
        </el-table-column>
        <!-- 环形电量 -->
        <el-table-column prop="batteryLevel" label="电量" width="130" align="center">
          <template #default="{ row }">
            <el-progress
              type="circle"
              :percentage="row.batteryLevel || 0"
              :width="40"
              :stroke-width="6"
              :color="getBatteryColor(row.batteryLevel)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="绑定用户" width="120" />
        <el-table-column prop="lastOnlineTime" label="最后在线" width="180">
          <template #default="{ row }">{{ row.lastOnlineTime ? formatDate(row.lastOnlineTime) : '--' }}</template>
        </el-table-column>
        <el-table-column prop="firmwareVersion" label="固件版本" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row.id)">详情</el-button>
            <el-button size="small" type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="handleBindUser(row)" v-if="!row.userId">绑定用户</el-button>
            <el-button size="small" type="warning" link @click="handleUnbind(row)" v-if="row.userId">解绑</el-button>
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

    <!-- 设备详情弹窗 -->
    <el-dialog v-model="detailVisible" title="设备详情" width="600px" class="rounded-dialog">
      <el-descriptions :column="2" border v-if="currentDevice" v-loading="detailLoading">
        <el-descriptions-item label="ID">{{ currentDevice.id }}</el-descriptions-item>
        <el-descriptions-item label="设备标识">{{ currentDevice.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="deviceStatusMap[currentDevice.status]?.type">{{ deviceStatusMap[currentDevice.status]?.text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="电量">{{ currentDevice.batteryLevel || '--' }}%</el-descriptions-item>
        <el-descriptions-item label="音量">{{ currentDevice.volumeLevel }}</el-descriptions-item>
        <el-descriptions-item label="绑定用户">{{ currentDevice.userName || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="固件版本">{{ currentDevice.firmwareVersion || '--' }}</el-descriptions-item>
        <el-descriptions-item label="摄像头">{{ currentDevice.hasCamera ? '有' : '无' }}</el-descriptions-item>
        <el-descriptions-item label="最后在线">{{ currentDevice.lastOnlineTime ? formatDate(currentDevice.lastOnlineTime) : '--' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentDevice.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(currentDevice.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 绑定用户弹窗（美化） -->
    <el-dialog v-model="bindVisible" title="绑定用户" width="500px" class="rounded-dialog">
      <el-form>
        <el-form-item label="选择用户">
          <el-select v-model="selectedUserId" placeholder="请选择老年人用户" filterable style="width:100%">
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="`${user.name} (${user.phone})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBind">确认绑定</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑设备弹窗（完整版） -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑设备' : '添加设备'" width="600px" class="rounded-dialog">
      <el-form :model="deviceForm" label-width="120px" ref="deviceFormRef">
        <el-form-item label="设备标识" required prop="deviceId">
          <el-input v-model="deviceForm.deviceId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="设备名称" required prop="deviceName">
          <el-input v-model="deviceForm.deviceName" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="deviceForm.status">
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="错误" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="电量" prop="batteryLevel">
          <el-input-number v-model="deviceForm.batteryLevel" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="音量" prop="volumeLevel">
          <el-input-number v-model="deviceForm.volumeLevel" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="固件版本" prop="firmwareVersion">
          <el-input v-model="deviceForm.firmwareVersion" />
        </el-form-item>
        <el-form-item label="摄像头" prop="hasCamera">
          <el-switch v-model="deviceForm.hasCamera" />
        </el-form-item>
        <el-form-item label="摄像头地址" prop="cameraUrl" v-if="deviceForm.hasCamera">
          <el-input v-model="deviceForm.cameraUrl" />
        </el-form-item>
        <el-form-item label="按钮1模式" prop="button1Mode">
          <el-select v-model="deviceForm.button1Mode">
            <el-option label="单击" value="SINGLE_CLICK" />
            <el-option label="双击" value="DOUBLE_CLICK" />
            <el-option label="长按" value="LONG_PRESS" />
            <el-option label="紧急" value="EMERGENCY" />
          </el-select>
        </el-form-item>
        <el-form-item label="按钮1功能" prop="button1Function">
          <el-input v-model="deviceForm.button1Function" />
        </el-form-item>
        <el-form-item label="按钮2模式" prop="button2Mode">
          <el-select v-model="deviceForm.button2Mode">
            <el-option label="单击" value="SINGLE_CLICK" />
            <el-option label="双击" value="DOUBLE_CLICK" />
            <el-option label="长按" value="LONG_PRESS" />
            <el-option label="紧急" value="EMERGENCY" />
          </el-select>
        </el-form-item>
        <el-form-item label="按钮2功能" prop="button2Function">
          <el-input v-model="deviceForm.button2Function" />
        </el-form-item>
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
import { RefreshRight } from '@element-plus/icons-vue'
import { deviceApi, userApi } from '@/api'
import { formatDate, deviceStatusMap } from '@/utils/format'
import type { Device, DeviceStatistics } from '@/api/type'

const loading = ref(false)
const devices = ref<Device[]>([])
const pagination = reactive({ page: 1, size: 20, total: 0 })
const queryParams = reactive({ deviceId: '', deviceName: '', status: '', boundToUser: undefined as boolean | undefined })
const statistics = ref<DeviceStatistics>({
  totalDevices: 0, onlineDevices: 0, offlineDevices: 0, maintenanceDevices: 0,
  errorDevices: 0, lowBatteryDevices: 0, unboundDevices: 0, cameraDevices: 0
})

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentDevice = ref<Device | null>(null)

// 表单
const formVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitLoading = ref(false)
const deviceForm = reactive({
  deviceId: '', deviceName: '', status: 'OFFLINE', batteryLevel: 100,
  volumeLevel: 80, firmwareVersion: '', hasCamera: false, cameraUrl: '',
  button1Mode: 'SINGLE_CLICK', button2Mode: 'SINGLE_CLICK',
  button1Function: '', button2Function: ''
})

// 绑定用户
const bindVisible = ref(false)
const selectedUserId = ref<number | null>(null)
const selectedDeviceId = ref<number | null>(null)
const availableUsers = ref<any[]>([])

const getBatteryColor = (level: number | null | undefined) => {
  if (level == null) return '#909399'
  if (level <= 20) return '#f56c6c'
  if (level <= 50) return '#e6a23c'
  return '#67c23a'
}

const loadDevices = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.page, size: pagination.size, ...queryParams }
    Object.keys(params).forEach(k => (params[k] === '' || params[k] === undefined) && delete params[k])
    const res = await deviceApi.getList(params)
    devices.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    statistics.value = await deviceApi.getStatistics()
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const refreshData = () => {
  loadDevices()
  loadStatistics()
}

const handleSearch = () => {
  pagination.page = 1
  loadDevices()
}

const resetQuery = () => {
  queryParams.deviceId = ''
  queryParams.deviceName = ''
  queryParams.status = ''
  queryParams.boundToUser = undefined
  pagination.page = 1
  loadDevices()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadDevices()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadDevices()
}

const showDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    currentDevice.value = await deviceApi.getById(id)
  } catch (error) {
    ElMessage.error('加载设备详情失败')
  } finally {
    detailLoading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  Object.assign(deviceForm, {
    deviceId: '', deviceName: '', status: 'OFFLINE', batteryLevel: 100,
    volumeLevel: 80, firmwareVersion: '', hasCamera: false, cameraUrl: '',
    button1Mode: 'SINGLE_CLICK', button2Mode: 'SINGLE_CLICK',
    button1Function: '', button2Function: ''
  })
  formVisible.value = true
}

const showEditDialog = (device: Device) => {
  isEdit.value = true
  editingId.value = device.id
  Object.assign(deviceForm, {
    deviceId: device.deviceId, deviceName: device.deviceName, status: device.status,
    batteryLevel: device.batteryLevel ?? 100, volumeLevel: device.volumeLevel,
    firmwareVersion: device.firmwareVersion || '', hasCamera: device.hasCamera,
    cameraUrl: device.cameraUrl || '', button1Mode: device.button1Mode,
    button2Mode: device.button2Mode, button1Function: device.button1Function || '',
    button2Function: device.button2Function || ''
  })
  formVisible.value = true
}

const handleSubmit = async () => {
  if (!deviceForm.deviceId || !deviceForm.deviceName) {
    ElMessage.warning('请填写设备标识和名称')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && editingId.value) {
      await deviceApi.update(editingId.value, deviceForm)
      ElMessage.success('更新成功')
    } else {
      await deviceApi.create(deviceForm)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    refreshData()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleBindUser = async (device: Device) => {
  selectedDeviceId.value = device.id
  try {
    const users = await userApi.query({ hasDevice: false, role: 'ELDERLY', size: 100 })
    availableUsers.value = users.content
    bindVisible.value = true
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const confirmBind = async () => {
  if (!selectedUserId.value || !selectedDeviceId.value) {
    ElMessage.warning('请选择用户')
    return
  }
  try {
    await deviceApi.bindToUser(selectedDeviceId.value, selectedUserId.value)
    ElMessage.success('绑定成功')
    bindVisible.value = false
    refreshData()
  } catch (error) {
    ElMessage.error('绑定失败')
  }
}

const handleUnbind = async (device: Device) => {
  try {
    await ElMessageBox.confirm(`确定解除设备 "${device.deviceName}" 的绑定吗？`, '确认解绑', { type: 'warning' })
    await deviceApi.unbind(device.id)
    ElMessage.success('解绑成功')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('解绑失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该设备吗？', '确认删除', { type: 'warning' })
    await deviceApi.delete(id)
    ElMessage.success('删除成功')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadDevices()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.device-page {
  .stat-row {
    margin-bottom: 20px;
    position: relative;
    .refresh-btn-col {
      text-align: right;
      margin-top: -10px;
      margin-bottom: 10px;
    }
  }
  .stat-card {
    .stat-value { font-size: 28px; font-weight: bold; text-align: center; }
    .stat-label { text-align: center; color: #909399; margin-top: 8px; }
    &.success .stat-value { color: #67c23a; }
    &.warning .stat-value { color: #e6a23c; }
    &.danger .stat-value { color: #f56c6c; }
  }
  .filter-card { margin-bottom: 20px; }
  .pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
  .rounded-dialog :deep(.el-dialog) { border-radius: 24px; }
}
</style>
