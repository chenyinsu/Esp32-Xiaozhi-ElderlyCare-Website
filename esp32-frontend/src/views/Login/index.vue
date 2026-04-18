<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>暖心伴</h1>
        <p>智能养老 · 安全守护</p>
      </div>
      
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tip">
        <span>测试账号：admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    // ✅ 使用 setTimeout 确保状态更新后再跳转
    setTimeout(() => {
      router.push('/dashboard')
    }, 100)
  } catch (error: any) {
    console.error('登录失败:', error)
    ElMessage.error(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1f2e 0%, #2d3a5e 100%);
  
  .login-card {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
    
    .login-header {
      text-align: center;
      margin-bottom: 32px;
      
      h1 {
        font-size: 28px;
        color: #1a1f2e;
        margin-bottom: 8px;
      }
      
      p {
        color: #909399;
        font-size: 14px;
      }
    }
    
    .login-btn {
      width: 100%;
    }
    
    .login-tip {
      margin-top: 20px;
      text-align: center;
      color: #909399;
      font-size: 13px;
    }
  }
}
</style>