import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue' // 确保登录组件存在
import Home from '../views/Home.vue'   // 确保主页组件存在

// 假设使用 localStorage 存储用户的登录状态
const isAuthenticated = () => {
  return !!localStorage.getItem('userToken'); // 假设通过 token 判断是否登录
};

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  // 添加其他路由...
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 不需要认证的页面直接放行
  if (!to.meta.requiresAuth) return next()
  
  // 需要认证且已登录
  if (isAuthenticated()) return next()
  
  // 需要认证但未登录，重定向到登录页
  next({
    path: '/login',
    query: { redirect: to.fullPath } // 携带跳转来源参数
  })
})

const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const host = window.location.host;
const ws = new WebSocket(`${protocol}//${host}/ws`);

export default router 