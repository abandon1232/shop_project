import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    children: [
      { path: '403', name: 'NoAuth', meta: { name: 'Access denied' }, component: () => import('../views/manager/403.vue') },
      { path: 'home', name: 'ManagerHome', meta: { name: 'Dashboard' }, component: () => import('../views/manager/Home.vue') },
      { path: 'admin', name: 'Admin', meta: { name: 'Administrators' }, component: () => import('../views/manager/Admin.vue') },
      { path: 'business', name: 'Business', meta: { name: 'Sellers' }, component: () => import('../views/manager/Business.vue') },
      { path: 'user', name: 'User', meta: { name: 'Customers' }, component: () => import('../views/manager/User.vue') },
      { path: 'adminPerson', name: 'AdminPerson', meta: { name: 'Profile' }, component: () => import('../views/manager/AdminPerson.vue') },
      { path: 'businessPerson', name: 'BusinessPerson', meta: { name: 'Profile' }, component: () => import('../views/manager/BusinessPerson.vue') },
      { path: 'password', name: 'Password', meta: { name: 'Change password' }, component: () => import('../views/manager/Password.vue') },
      { path: 'notice', name: 'Notice', meta: { name: 'Notices' }, component: () => import('../views/manager/Notice.vue') },
      { path: 'type', name: 'ManagerType', meta: { name: 'Categories' }, component: () => import('../views/manager/Type.vue') },
      { path: 'goods', name: 'Goods', meta: { name: 'Products' }, component: () => import('../views/manager/Goods.vue') },
    ],
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    children: [
      { path: 'home', name: 'StoreHome', meta: { name: 'Store', public: true }, component: () => import('../views/front/Home.vue') },
      { path: 'person', name: 'Person', meta: { name: 'Profile' }, component: () => import('../views/front/Person.vue') },
      { path: 'type', name: 'StoreType', meta: { name: 'Category', public: true }, component: () => import('../views/front/Type.vue') },
      { path: 'search', name: 'Search', meta: { name: 'Search', public: true }, component: () => import('../views/front/Search.vue') },
      { path: 'product/:id', name: 'ProductDetail', meta: { name: 'Product details', public: true }, component: () => import('../views/front/ProductDetail.vue') },
    ],
  },
  { path: '/login', name: 'Login', meta: { name: 'Sign in', public: true }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: 'Create account', public: true }, component: () => import('../views/Register.vue') },
  { path: '/:pathMatch(.*)*', name: 'NotFound', meta: { name: 'Page not found' }, component: () => import('../views/404.vue') },
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
