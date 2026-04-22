<template>
  <div class="change-password-container">
    <div class="password-card">
      <div class="password-header">
        <el-icon :size="48" color="#e6a23c"><Lock /></el-icon>
        <h1 class="password-title">首次登录，请修改密码</h1>
        <p class="password-subtitle">修改密码后请重新登录</p>
      </div>

      <el-form
        ref="formRef"
        :model="passwordForm"
        :rules="passwordRules"
        class="password-form"
        label-position="right"
        label-width="80px"
        @keyup.enter="handleChangePassword"
      >
        <el-form-item prop="oldPassword" label="原密码">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="newPassword" label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="warning"
            size="large"
            class="password-button"
            :loading="loading"
            :disabled="!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword"
            @click="handleChangePassword"
          >
            修改密码并重新登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { changePassword } from '@/api/user'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleChangePassword = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      // 清除登录状态
      userStore.clearUser()
      localStorage.removeItem('token')
      router.push('/login')
    } catch (e) {
      // Error already handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.change-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
}

.password-card {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.password-header {
  text-align: center;
  margin-bottom: 30px;
}

.password-title {
  font-size: 22px;
  color: #303133;
  margin: 16px 0 8px;
}

.password-subtitle {
  font-size: 14px;
  color: #909399;
}

.password-form {
  margin-top: 20px;
}

.password-button {
  width: 100%;
}
</style>