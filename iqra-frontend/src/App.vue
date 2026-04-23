<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './store/user'

const userStore = useUserStore()

onMounted(() => {
  // 应用启动时恢复用户状态
  userStore.initUser()
})
</script>

<style>
</style>