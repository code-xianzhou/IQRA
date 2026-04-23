<template>
  <el-container class="main-layout">
    <!-- Header -->
    <el-header class="main-header">
      <div class="header-left">
        <el-icon :size="24" class="logo-icon"><ChatDotSquare /></el-icon>
        <span class="system-name">企业内部知识库智能问答系统 (IRQA)</span>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ userStore.username || '用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- Sidebar -->
      <el-aside width="200px" class="main-aside">
        <el-menu
          :default-active="$route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/chat">
            <el-icon><ChatLineSquare /></el-icon>
            <span>智能问答</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/document">
            <el-icon><Document /></el-icon>
            <span>文档管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/skill">
            <el-icon><Tools /></el-icon>
            <span>技能管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/user">
            <el-icon><User /></el-icon>
            <span>账户管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/department">
            <el-icon><OfficeBuilding /></el-icon>
            <span>部门管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- Main Content -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import { logout } from '@/api/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const isAdmin = computed(() => userStore.role === 'ADMIN')

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await logout()
    } catch (e) {}
    localStorage.removeItem('token')
    userStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.main-header {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: white;
}

.system-name {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: white;
}

.main-aside {
  background-color: #304156;
  overflow-y: auto;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>