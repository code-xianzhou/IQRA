import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/login/ChangePassword.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/chat',
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/Chat.vue')
      },
      {
        path: 'document',
        name: 'Document',
        component: () => import('@/views/document/Document.vue'),
        meta: { requiresAdmin: false }
      },
      {
        path: 'skill',
        name: 'Skill',
        component: () => import('@/views/skill/Skill.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/Config.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/UserManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'department',
        name: 'DepartmentManagement',
        component: () => import('@/views/department/DepartmentManagement.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/change-password' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router