<template>
  <main class="auth-page">
    <section class="auth-panel">
      <button class="auth-brand" type="button" @click="$router.push('/front/home')">
        <span class="brand-mark">N</span>
        <span>NorrByte Market</span>
      </button>
      <div class="auth-card">
        <span class="eyebrow">Welcome back</span>
        <h1>Sign in</h1>
        <p class="auth-intro">Manage your account or continue browsing the marketplace.</p>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="Username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="Password" show-password />
        </el-form-item>
        <el-form-item>
          <el-select v-model="form.role" placeholder="Choose account type" style="width: 100%">
            <el-option label="Administrator" value="ADMIN"></el-option>
            <el-option label="Seller" value="BUSINESS"></el-option>
            <el-option label="Customer" value="USER"></el-option>
          </el-select>
        </el-form-item>


        <el-form-item>
          <el-button class="submit-button" @click="login">Sign in</el-button>
        </el-form-item>
        <div class="auth-switch">
          <div>
            New here? <router-link to="/register">Create an account</router-link>
          </div>
        </div>
      </el-form>
      </div>
    </section>
  </main>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      form: {  },
      rules: {
        username: [{ required: true, message: 'Enter your username', trigger: 'blur' },],
        password: [{ required: true, message: 'Enter your password', trigger: 'blur' },],
        role: [{ required: true, message: 'Choose an account type', trigger: 'blur' },],
      }
    }
  },
  created() {

  },
  methods: {
    login() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // Validation succeeded.
          this.$request.post('/login', this.form).then(res => {
            if (res.code === '200') {
              let user = res.data
              localStorage.setItem("xm-user", JSON.stringify(res.data))  // Cache the authenticated account.
              if (user.role === 'USER') {
                const requested = this.$route.query.redirect
                const destination = typeof requested === 'string' && requested.startsWith('/front/')
                  ? requested
                  : '/front/home'
                this.$router.push(destination)
              } else {
                this.$router.push('/home')
              }

              this.$message.success('Signed in successfully')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 36px max(28px, calc((100vw - 1400px) / 2));
  display: flex;
  align-items: center;
  background: linear-gradient(90deg, rgba(9, 27, 48, 0.3), rgba(9, 27, 48, 0.02)), url("@/assets/imgs/auth-electronics.webp") center / cover no-repeat;
  color: #142033;
}
.auth-panel {
  width: min(430px, 100%);
}
.auth-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  cursor: pointer;
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #e76f2e;
}
.auth-card {
  padding: 38px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 60px rgba(4, 18, 35, 0.28);
}
.eyebrow {
  color: #c94f13;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}
h1 {
  margin: 8px 0 8px;
  font-size: 34px;
  letter-spacing: -0.04em;
}
.auth-intro {
  margin-bottom: 24px;
  color: #687586;
  line-height: 1.5;
}
.submit-button {
  width: 100%;
  min-height: 42px;
  border-color: #e76f2e;
  background: #e76f2e;
  color: #fff;
  font-weight: 700;
}
.auth-switch {
  display: flex;
  justify-content: flex-end;
  color: #687586;
}
a {
  color: #1d527e;
  font-weight: 700;
}
@media (max-width: 620px) {
  .auth-page {
    padding: 22px;
    background-position: 62% center;
  }
  .auth-card {
    padding: 28px 24px;
  }
}
</style>
