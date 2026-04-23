<template>
  <div class="user-management-page">
    <!-- Toolbar -->
    <div class="toolbar">
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- User Table -->
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="username" label="用户名" width="120" />

      <el-table-column prop="department" label="部门" width="150" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="添加时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            size="small" 
            @click="handleResetPassword(row)"
            :disabled="row.username === 'admin'"
            style="margin-right: 8px"
          >
            重置密码
          </el-button>
          <el-popconfirm
            title="确认删除该用户? 删除后不可恢复"
            @confirm="handleDelete(row.id)"
            :disabled="row.username === 'admin'"
          >
            <template #reference>
              <el-button 
                type="danger" 
                size="small" 
                :disabled="row.username === 'admin'"
              >
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create User Dialog -->
    <el-dialog v-model="showCreateDialog" title="新增用户" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请设置初始密码（至少6位）" show-password />
        </el-form-item>

        <el-form-item label="部门">
          <el-input v-model="createForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, createUser, deleteUser, resetPassword } from '@/api/user'

const loading = ref(false)
const users = ref([])
const showCreateDialog = ref(false)
const creating = ref(false)
const createFormRef = ref(null)

const createForm = reactive({
  username: '',
  password: '',

  department: '',
  email: '',
  phone: '',
  role: 'USER'
})

const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],

  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const loadUsers = async () => {
  loading.value = true
  try {
    users.value = await getUsers()
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return

    creating.value = true
    try {
      await createUser(createForm)
      ElMessage.success('创建成功')
      showCreateDialog.value = false
      // 重置表单
      Object.assign(createForm, {
        username: '',
        password: '',

        department: '',
        email: '',
        phone: '',
        role: 'USER'
      })
      loadUsers()
    } catch (e) {
      ElMessage.error('创建失败: ' + (e.message || '未知错误'))
    } finally {
      creating.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (e) {
    ElMessage.error('删除失败: ' + (e.message || '未知错误'))
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 ${row.username} 的密码重置为与用户名一致吗？`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await resetPassword(row.id)
    ElMessage.success('密码重置成功，新密码与用户名一致')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('重置密码失败: ' + (e.message || '未知错误'))
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management-page {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>