<template>
  <div class="manager-container">
    <!--  Header.  -->
    <div class="manager-header">
      <div class="manager-header-left">
        <span class="manager-brand-mark" aria-hidden="true">N</span>
        <div class="title">NorrByte Admin</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <el-dropdown placement="bottom">
          <div class="avatar">
            <img v-if="user.avatar" :src="user.avatar" :alt="user.name || 'Account avatar'" />
            <span v-else class="manager-user-mark">{{ userInitial }}</span>
            <div>{{ user.name || 'Administrator' }}</div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToPerson">Profile</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/password')">Change password</el-dropdown-item>
              <el-dropdown-item @click="logout">Sign out</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!--  Main content.  -->
    <div class="manager-main">
      <!--  Sidebar.  -->
      <div class="manager-main-left">
        <el-menu :default-openeds="['info', 'user']" router style="border: none" :default-active="$route.path">
          <el-menu-item index="/home">
            <span>Dashboard</span>
          </el-menu-item>
          <el-sub-menu index="info">
            <template #title>
              <span>Catalogue</span>
            </template>
            <el-menu-item v-if="user.role === 'ADMIN'" index="/notice">Notices</el-menu-item>
            <el-menu-item v-if="user.role === 'ADMIN'" index="/type">Categories</el-menu-item>
            <el-menu-item index="/goods">Products</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="user" v-if="user.role === 'ADMIN'">
            <template #title>
              <span>Accounts</span>
            </template>
            <el-menu-item index="/admin">Administrators</el-menu-item>
            <el-menu-item index="/business">Sellers</el-menu-item>
            <el-menu-item index="/user">Customers</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!--  Data table.  -->
      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "Manager",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    }
  },
  created() {
    if (!this.user.id) {
      this.$router.push('/login')
    }
  },
  computed: {
    userInitial() {
      return (this.user.name || this.user.username || 'N').charAt(0).toUpperCase()
    },
  },
  methods: {
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // Reload the latest cached account.
    },
    goToPerson() {
      if (this.user.role === 'ADMIN') {
        this.$router.push('/adminPerson')
      }
      if (this.user.role === 'BUSINESS') {
        this.$router.push('/businessPerson')
      }
    },
    logout() {
      localStorage.removeItem('xm-user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/manager.css";
</style>
