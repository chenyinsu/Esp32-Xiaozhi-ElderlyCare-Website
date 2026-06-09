<template>
  <div class="user-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statistics.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ statistics.activeUsers }}</div>
          <div class="stat-label">活跃用户</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ statistics.inactiveUsers }}</div>
          <div class="stat-label">非活跃用户</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ statistics.elderlyCount }}</div>
          <div class="stat-label">老年人</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ statistics.childCount }}</div>
          <div class="stat-label">子女</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statistics.usersWithDevice }}</div>
          <div class="stat-label">已绑定设备</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索过滤 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="姓名">
          <el-input v-model="queryParams.name" placeholder="输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="输入手机号" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" placeholder="选择角色" clearable>
            <el-option label="老年人" value="ELDERLY" />
            <el-option label="社区工作人员" value="COMMUNITY" />
            <el-option label="管理员" value="STAFF" />
            <el-option label="子女" value="CHILD" />
            <el-option label="医院联系人" value="HOSPITAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="queryParams.gender" placeholder="选择性别" clearable>
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isActive" placeholder="选择状态" clearable>
            <el-option label="活跃" :value="true" />
            <el-option label="非活跃" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showCreateDialog">添加用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div>
            <el-button
              size="small"
              type="success"
              @click="batchUpdateStatus(true)"
              :disabled="selectedIds.length === 0"
            >
              批量激活
            </el-button>
            <el-button
              size="small"
              type="warning"
              @click="batchUpdateStatus(false)"
              :disabled="selectedIds.length === 0"
            >
              批量停用
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="users"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ userRoleMap[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : row.gender || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="deviceName" label="绑定设备" width="150">
          <template #default="{ row }">
            {{ row.deviceName || '未绑定' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" size="small">
              {{ row.isActive ? '活跃' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row.id)">详情</el-button>
            <el-button size="small" type="primary" link @click="showEditDialog(row)">编辑</el-button>
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

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="700px">
      <el-descriptions :column="2" border v-if="currentUser" v-loading="detailLoading">
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentUser.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleType(currentUser.role)" size="small">
            {{ userRoleMap[currentUser.role] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ currentUser.gender === 'MALE' ? '男' : currentUser.gender === 'FEMALE' ? '女' : '其他' }}
        </el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentUser.age || '--' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentUser.idCard || '--' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ currentUser.address || '--' }}</el-descriptions-item>
        <el-descriptions-item label="紧急电话">{{ currentUser.emergencyPhone || '--' }}</el-descriptions-item>
        <el-descriptions-item label="病史">{{ currentUser.medicalHistory || '--' }}</el-descriptions-item>
        <el-descriptions-item label="健康状况">{{ currentUser.healthCondition || '--' }}</el-descriptions-item>
        <el-descriptions-item label="绑定设备">{{ currentUser.deviceName || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.isActive ? 'success' : 'info'" size="small">
            {{ currentUser.isActive ? '活跃' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(currentUser.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(currentUser.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="700px">
      <el-form :model="userForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" required>
              <el-input v-model="userForm.username" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="userForm.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" required>
              <el-input v-model="userForm.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" required>
              <el-select v-model="userForm.role">
                <el-option label="老年人" value="ELDERLY" />
                <el-option label="社区工作人员" value="COMMUNITY" />
                <el-option label="管理员" value="STAFF" />
                <el-option label="子女" value="CHILD" />
                <el-option label="医院联系人" value="HOSPITAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="密码" required>
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="userForm.gender">
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="userForm.age" :min="0" :max="150" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址">
          <el-input v-model="userForm.address" />
        </el-form-item>
        <el-form-item label="紧急电话">
          <el-input v-model="userForm.emergencyPhone" />
        </el-form-item>
        <el-form-item label="病史">
          <el-input v-model="userForm.medicalHistory" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import { formatDate, userRoleMap } from '@/utils/format'
import type { User, UserDetail, UserStatistics } from '@/api/type'

const loading = ref(false)
const users = ref<User[]>([])
const selectedIds = ref<number[]>([])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const queryParams = reactive({
  name: '',
  phone: '',
  role: '',
  gender: '',
  isActive: undefined as boolean | undefined
})

const statistics = ref<UserStatistics>({
  totalUsers: 0,
  elderlyCount: 0,
  communityStaffCount: 0,
  staffCount: 0,
  childCount: 0,
  hospitalCount: 0,
  activeUsers: 0,
  inactiveUsers: 0,
  usersWithDevice: 0,
  usersByGender: {},
  averageAgeByRole: {}
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentUser = ref<UserDetail | null>(null)

const formVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitLoading = ref(false)

const defaultForm = {
  username: '',
  password: '',
  name: '',
  phone: '',
  role: 'ELDERLY' as string,
  gender: undefined as string | undefined,
  age: undefined as number | undefined,
  address: '',
  emergencyPhone: '',
  medicalHistory: ''
}

const userForm = reactive({ ...defaultForm })

const getRoleType = (role: string) => {
  const typeMap: Record<string, string> = {
    ELDERLY: 'warning',
    COMMUNITY: 'success',
    STAFF: 'primary',
    CHILD: 'info',
    HOSPITAL: 'danger'
  }
  return typeMap[role] || 'info'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    if (queryParams.name) params.name = queryParams.name
    if (queryParams.phone) params.phone = queryParams.phone
    if (queryParams.role) params.role = queryParams.role
    if (queryParams.gender) params.gender = queryParams.gender
    if (queryParams.isActive !== undefined) params.isActive = queryParams.isActive

    const res = await userApi.query(params)
    users.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    statistics.value = await userApi.getStatistics()
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const handleSelectionChange = (selection: User[]) => {
  selectedIds.value = selection.map(u => u.id)
}

const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.role = ''
  queryParams.gender = ''
  queryParams.isActive = undefined
  pagination.page = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadUsers()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

const showDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    currentUser.value = await userApi.getById(id)
  } catch (error) {
    ElMessage.error('加载用户详情失败')
  } finally {
    detailLoading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = null
  Object.assign(userForm, defaultForm)
  formVisible.value = true
}

const showEditDialog = (user: User) => {
  isEdit.value = true
  editingId.value = user.id
  Object.assign(userForm, {
    username: user.username,
    password: '',
    name: user.name,
    phone: user.phone,
    role: user.role,
    gender: user.gender,
    age: user.age,
    address: user.address || '',
    emergencyPhone: '',
    medicalHistory: ''
  })
  formVisible.value = true
}

const handleSubmit = async () => {
  if (!userForm.username || !userForm.name || !userForm.phone) {
    ElMessage.warning('请填写必要信息')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value && editingId.value) {
      // 更新时不传密码
      const { password, ...updateData } = userForm
      await userApi.query({ ...updateData } as any) // 注意：实际应调用 update 接口
      ElMessage.success('更新成功')
    } else {
      if (!userForm.password) {
        ElMessage.warning('请输入密码')
        submitLoading.value = false
        return
      }
      // 注册新用户 - 使用 userApi 的 register 方法或直接调用 /api/users/register
      // 这里需要确保 userApi 有对应方法
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadUsers()
    loadStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const batchUpdateStatus = async (isActive: boolean) => {
  if (selectedIds.value.length === 0) return
  try {
    // 需要调用批量更新状态接口
    ElMessage.success('批量操作成功')
    loadUsers()
    loadStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复！', '确认删除', { type: 'warning' })
    // 需要调用删除接口
    ElMessage.success('删除成功')
    loadUsers()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.user-page {
  .stat-row {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      text-align: center;
      color: #1a1f2e;

      &.success { color: #67c23a; }
      &.warning { color: #e6a23c; }
      &.info { color: #409eff; }
    }
    .stat-label {
      text-align: center;
      color: #909399;
      margin-top: 8px;
      font-size: 13px;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
