import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
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
        component: () => import('@/views/document/Document.vue')
      },
      {
        path: 'skill',
        name: 'Skill',
        component: () => import('@/views/skill/Skill.vue')
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/Config.vue')
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
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
