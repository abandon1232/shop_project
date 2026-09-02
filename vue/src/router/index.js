import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    children: [
      { path: '403', name: 'NoAuth', meta: { name: '无权限' }, component: () => import('../views/manager/403.vue') },
      { path: 'home', name: 'ManagerHome', meta: { name: '系统首页' }, component: () => import('../views/manager/Home.vue') },
      { path: 'admin', name: 'Admin', meta: { name: '管理员信息' }, component: () => import('../views/manager/Admin.vue') },
      { path: 'business', name: 'Business', meta: { name: '商家信息' }, component: () => import('../views/manager/Business.vue') },
      { path: 'user', name: 'User', meta: { name: '用户信息' }, component: () => import('../views/manager/User.vue') },
      { path: 'adminPerson', name: 'AdminPerson', meta: { name: '个人信息' }, component: () => import('../views/manager/AdminPerson.vue') },
      { path: 'businessPerson', name: 'BusinessPerson', meta: { name: '个人信息' }, component: () => import('../views/manager/BusinessPerson.vue') },
      { path: 'password', name: 'Password', meta: { name: '修改密码' }, component: () => import('../views/manager/Password.vue') },
      { path: 'notice', name: 'Notice', meta: { name: '公告信息' }, component: () => import('../views/manager/Notice.vue') },
      { path: 'type', name: 'ManagerType', meta: { name: '分类信息' }, component: () => import('../views/manager/Type.vue') },
      { path: 'goods', name: 'Goods', meta: { name: '商品信息' }, component: () => import('../views/manager/Goods.vue') },
    ],
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    children: [
      { path: 'home', name: 'StoreHome', meta: { name: '商城首页' }, component: () => import('../views/front/Home.vue') },
      { path: 'person', name: 'Person', meta: { name: '个人信息' }, component: () => import('../views/front/Person.vue') },
      { path: 'type', name: 'StoreType', meta: { name: '分类商品' }, component: () => import('../views/front/Type.vue') },
      { path: 'search', name: 'Search', meta: { name: '搜索页面' }, component: () => import('../views/front/Search.vue') },
    ],
  },
  { path: '/login', name: 'Login', meta: { name: '登录', public: true }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册', public: true }, component: () => import('../views/Register.vue') },
  { path: '/:pathMatch(.*)*', name: 'NotFound', meta: { name: '无法访问' }, component: () => import('../views/404.vue') },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(to => {
  if (to.meta.public) {
    return true
  }

  let user = {}
  try {
    user = JSON.parse(localStorage.getItem('xm-user') || '{}')
  } catch {
    localStorage.removeItem('xm-user')
  }

  if (!user.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/') {
    return user.role === 'USER' ? '/front/home' : '/home'
  }
  return true
})

export { routes }
export default router
