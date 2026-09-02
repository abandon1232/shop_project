<template>
  <div class="front-shell">
    <header class="front-header">
      <button class="front-header-left" type="button" @click="navTo('/front/home')">
        <span class="brand-mark" aria-hidden="true">N</span>
        <span class="title">NorrByte Market</span>
      </button>
      <div class="front-header-center">
        <el-input class="search-input" placeholder="Search products" v-model="name" @keyup.enter="search"></el-input>
        <el-button class="search-button" @click="search">Search</el-button>
      </div>
      <nav class="front-header-right" aria-label="Account navigation">
        <div v-if="!user.username" class="guest-actions">
          <el-button text @click="$router.push('/login')">Sign in</el-button>
          <el-button class="account-button" @click="$router.push('/register')">Create account</el-button>
        </div>
        <template v-else>
          <button v-if="user.role === 'USER'" class="cart-action" type="button" @click="navTo('/front/cart')">
            <span aria-hidden="true">Cart</span>
            <span class="cart-label">Cart</span>
            <span v-if="cartCount > 0" class="cart-count">{{ cartCount }}</span>
          </button>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img v-if="user.avatar" @click="navTo('/front/person')" :src="user.avatar" :alt="user.name || 'Account avatar'">
              <button v-else class="user-mark" type="button" @click="navTo('/front/person')">{{ userInitial }}</button>
              <div class="account-name">
                <span>{{ user.name }}</span><span style="margin-left: 5px">⌄</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="user.role === 'USER'" @click="navTo('/front/orders')">My orders</el-dropdown-item>
                <el-dropdown-item v-if="user.role === 'USER'" @click="navTo('/front/person')">Profile</el-dropdown-item>
                <el-dropdown-item @click="logout">Sign out</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </nav>
    </header>
    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" @cart-updated="loadCartCount" />
    </div>
  </div>

</template>

<script>

export default {
  name: "FrontLayout",

  data () {
    return {
      top: '',
      notice: [],
      user: JSON.parse(localStorage.getItem("xm-user") || '{}'),
      name: '',
      cartCount: 0,
    }
  },

  mounted() {
    this.loadNotice()
    if (this.user.role === 'USER') this.loadCartCount()
  },
  computed: {
    userInitial() {
      return (this.user.name || this.user.username || 'N').charAt(0).toUpperCase()
    },
  },
  methods: {
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notice = res.data
        let i = 0
        if (this.notice && this.notice.length) {
          this.top = this.notice[0].content
          setInterval(() => {
            this.top = this.notice[i].content
            i++
            if (i === this.notice.length) {
              i = 0
            }
          }, 2500)
        }
      })
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('xm-user') || '{}')   // Reload the latest cached account.
      if (this.user.role === 'USER') {
        this.loadCartCount()
      } else {
        this.cartCount = 0
      }
    },
    loadCartCount() {
      if (this.user.role !== 'USER') {
        this.cartCount = 0
        return Promise.resolve()
      }
      return this.$request.get('/cart/items').then(res => {
        if (res.code === '200') {
          this.cartCount = (res.data || []).reduce(
            (sum, item) => sum + Number(item.quantity || 0),
            0,
          )
        }
      })
    },
    navTo(url) {
      this.$router.push(url)
    },
    // Log out.
    logout() {
      localStorage.removeItem("xm-user");
      this.user = {}
      this.cartCount = 0
      this.$router.push("/login");
    },
    search() {
      this.$router.push({ path: '/front/search', query: { name: this.name || '' } })
    },
  }

}
</script>

<style scoped>
  @import "@/assets/css/front.css";
</style>
