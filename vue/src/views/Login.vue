<template>
  <div class="container">
    <div style="width: 400px; padding: 30px; background-color: white; border-radius: 5px;">
      <div style="text-align: center; font-size: 20px; margin-bottom: 20px; color: #333">Sign in to NorrByte Market</div>
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
          <el-button style="width: 100%; background-color: #4169E1; border-color: #4169E1; color: white" @click="login">Sign in</el-button>
        </el-form-item>
        <div style="display: flex; align-items: center">
          <div style="flex: 1"></div>
          <div style="flex: 1; text-align: right">
            New here? <router-link to="/register">Create an account</router-link>
          </div>
        </div>
      </el-form>
    </div>
  </div>
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
                this.$router.push('/front/home')
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
.container {
  height: 100vh;
  overflow: hidden;
  background-image: url("@/assets/imgs/bg.jpg");
  background-size: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}
a {
  color: #2a60c9;
}
</style>
