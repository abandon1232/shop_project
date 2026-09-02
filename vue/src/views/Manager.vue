<template>
  <div class="manager-shell">
    <aside class="manager-sidebar">
      <button class="manager-brand" type="button" @click="$router.push('/home')">
        <span class="manager-brand-mark" aria-hidden="true">N</span>
        <span><strong>NorrByte</strong><small>Management</small></span>
      </button>

      <nav class="manager-navigation" aria-label="Management navigation">
        <div v-for="section in navigation" :key="section.label" class="manager-nav-section">
          <p>{{ section.label }}</p>
          <router-link v-for="item in section.items" :key="item.path" :to="item.path">
            <span class="nav-symbol" aria-hidden="true">{{ item.symbol }}</span>
            {{ item.label }}
          </router-link>
        </div>
      </nav>

      <button class="storefront-link" type="button" @click="$router.push('/front/home')">
        View storefront <span aria-hidden="true">↗</span>
      </button>
    </aside>

    <section class="manager-workspace">
      <header class="manager-topbar">
        <div><span class="topbar-eyebrow">{{ roleLabel }}</span><strong>{{ $route.meta.name || 'Management' }}</strong></div>
        <el-dropdown placement="bottom-end">
          <button class="manager-account" type="button">
            <img v-if="user.avatar" :src="user.avatar" :alt="user.name || 'Account avatar'">
            <span v-else class="manager-user-mark">{{ userInitial }}</span>
            <span class="account-copy"><strong>{{ user.name || user.username || 'Account' }}</strong><small>{{ roleLabel }}</small></span>
            <span aria-hidden="true">⌄</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToPerson">Profile</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/password')">Change password</el-dropdown-item>
              <el-dropdown-item @click="logout">Sign out</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="manager-content"><router-view @update:user="updateUser" /></main>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Manager',
  data() {
    return { user: JSON.parse(localStorage.getItem('xm-user') || '{}') }
  },
  computed: {
    userInitial() {
      return (this.user.name || this.user.username || 'N').charAt(0).toUpperCase()
    },
    roleLabel() {
      return this.user.role === 'BUSINESS' ? 'Seller workspace' : 'Administrator workspace'
    },
    navigation() {
      const commerce = this.user.role === 'ADMIN'
        ? [
            { path: '/goods', label: 'Products', symbol: 'P' },
            { path: '/type', label: 'Categories', symbol: 'C' },
            { path: '/orders', label: 'Orders', symbol: 'O' },
          ]
        : [
            { path: '/goods', label: 'My products', symbol: 'P' },
            { path: '/orders', label: 'My orders', symbol: 'O' },
          ]
      const sections = [
        { label: 'Overview', items: [{ path: '/home', label: 'Dashboard', symbol: 'D' }] },
        { label: 'Commerce', items: commerce },
      ]
      if (this.user.role === 'ADMIN') {
        sections.push({
          label: 'People & content',
          items: [
            { path: '/business', label: 'Sellers', symbol: 'S' },
            { path: '/user', label: 'Customers', symbol: 'U' },
            { path: '/admin', label: 'Administrators', symbol: 'A' },
            { path: '/notice', label: 'Notices', symbol: 'N' },
          ],
        })
      }
      return sections
    },
  },
  created() {
    if (!this.user.id) this.$router.push('/login')
  },
  methods: {
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')
    },
    goToPerson() {
      this.$router.push(this.user.role === 'BUSINESS' ? '/businessPerson' : '/adminPerson')
    },
    logout() {
      localStorage.removeItem('xm-user')
      this.$router.push('/login')
    },
  },
}
</script>
