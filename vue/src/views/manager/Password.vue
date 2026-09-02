<template>
  <div>
    <el-card style="width: 50%">
      <el-form ref="formRef" :model="user" :rules="rules" label-width="100px" style="padding-right: 50px">
        <el-form-item label="Current password" prop="password">
          <el-input v-model="user.password" type="password" show-password placeholder="Current password" />
        </el-form-item>
        <el-form-item label="New password" prop="newPassword">
          <el-input v-model="user.newPassword" type="password" show-password placeholder="New password" />
        </el-form-item>
        <el-form-item label="Confirm password" prop="confirmPassword">
          <el-input v-model="user.confirmPassword" type="password" show-password placeholder="Confirm password" />
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">Change password</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Password",
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Confirm your new password'))
      } else if (value !== this.user.newPassword) {
        callback(new Error('The passwords do not match'))
      } else {
        callback()
      }
    }

    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        password: [
          { required: true, message: 'Enter your current password', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: 'Enter a new password', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      }
    }
  },
  created() {

  },
  methods: {
    update() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.put('/updatePassword', this.user).then(res => {
            if (res.code === '200') {
              // Update succeeded.
              localStorage.removeItem('xm-user')   // Clear the cached account.
              this.$message.success('Password changed successfully')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>
:deep(.el-form-item__label) {
  font-weight: bold;
}
</style>
