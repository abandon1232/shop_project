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
        <div v-else>
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
                <el-dropdown-item @click="logout">Sign out</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </nav>
    </header>
    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" />
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
    }
  },

  mounted() {
    this.loadNotice()
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
    },
    navTo(url) {
      this.$router.push(url)
    },
    // Log out.
    logout() {
      localStorage.removeItem("xm-user");
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
