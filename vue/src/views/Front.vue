<template>
  <div>
    <!--Header.-->
    <div class="front-header">
      <div class="front-header-left" @click="navTo('/front/home')">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title">NorrByte Market</div>
      </div>
      <div class="front-header-center" style="text-align: right">
        <el-input style="width: 200px" placeholder="Search products" v-model="name"></el-input>
        <el-button type="primary" style="margin-left: 5px" @click="search">Search</el-button>
      </div>
      <div class="front-header-right">
        <div v-if="!user.username">
          <el-button @click="$router.push('/login')">Sign in</el-button>
          <el-button @click="$router.push('/register')">Create account</el-button>
        </div>
        <div v-else>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img @click="navTo('/front/person')" :src="user.avatar" alt="">
              <div style="margin-left: 10px">
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
      </div>
    </div>
    <!--Main content.-->
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
