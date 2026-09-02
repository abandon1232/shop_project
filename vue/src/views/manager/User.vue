<template>
  <div class="management-page">
    <header class="page-heading">
      <div><span class="page-eyebrow">Accounts</span><h1>Customers</h1><p>Review and maintain registered customer accounts.</p></div>
    </header>
    <div class="search">
      <el-input placeholder="Search by username" style="width: 200px" v-model="username"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Delete selected</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="username" label="Username"></el-table-column>
        <el-table-column prop="name" label="Name"></el-table-column>
        <el-table-column prop="phone" label="Phone"></el-table-column>
        <el-table-column prop="email" label="Email"></el-table-column>
        <el-table-column label="Avatar">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-image style="width: 40px; height: 40px; border-radius: 50%" v-if="scope.row.avatar"
                        :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]"></el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="Role"></el-table-column>

        <el-table-column label="Actions" align="center" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">Edit</el-button>
            <el-button size="small" type="danger" plain @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
            background
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </div>


    <el-dialog v-model="fromVisible" title="Customer account" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="form" label-width="100px" style="padding-right: 50px" :rules="rules" ref="formRef">
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="Username"></el-input>
        </el-form-item>
        <el-form-item v-if="!form.id" label="Initial password" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="Set an initial password" />
        </el-form-item>
        <el-form-item label="Display name" prop="name">
          <el-input v-model="form.name" placeholder="Display name"></el-input>
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="form.phone" placeholder="Phone"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="Email"></el-input>
        </el-form-item>
        <el-form-item label="Avatar">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/files/upload'"
              :headers="{ token: user.token }"
              accept="image/jpeg,image/png,image/gif"
              list-type="picture"
              :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">Upload avatar</el-button>
          </el-upload>
        </el-form-item>

    
      </el-form>

      <template #footer>
        <el-button @click="fromVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Save</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script>
export default {
  name: "User",
  data() {
    return {
      tableData: [],  // All records.
      pageNum: 1,   // Current page number.
      pageSize: 10,  // Records per page.
      total: 0,
      username: null,
      fromVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        username: [
          {required: true, message: 'Enter a username', trigger: 'blur'},
        ],
        password: [
          {required: true, message: 'Set an initial password', trigger: 'blur'},
        ],
      },
      ids: []
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    handleAdd() {   // Add a record.
      this.form = {}  // Reset the form before adding a record.
      this.fromVisible = true   // Open the dialog.
    },
    handleEdit(row) {   // Edit a record.
      this.form = JSON.parse(JSON.stringify(row))  // Copy the row into the form using a deep clone.
      this.fromVisible = true   // Open the dialog.
    },
    save() {   // Save the form by creating or updating a record.
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id ? '/user/update' : '/user/add',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {  // Save succeeded.
              this.$message.success('Saved successfully')
              this.load(1)
              this.fromVisible = false
            } else {
              this.$message.error(res.msg)  // Display the error message.
            }
          })
        }
      })
    },
    del(id) {   // Delete one record.
      this.$confirm('Delete this customer?', 'Confirm deletion', {type: "warning"}).then(response => {
        this.$request.delete('/user/delete/' + id).then(res => {
          if (res.code === '200') {   // Operation succeeded.
            this.$message.success('Deleted successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // Display the error message.
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // Currently selected rows.
      this.ids = rows.map(v => v.id)
    },
    delBatch() {   // Delete multiple records.
      if (!this.ids.length) {
        this.$message.warning('Select at least one record')
        return
      }
      this.$confirm('Delete the selected customers?', 'Confirm deletion', {type: "warning"}).then(response => {
        this.$request.delete('/user/delete/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // Operation succeeded.
            this.$message.success('Deleted successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)  // Display the error message.
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // Find records with pagination.
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/user/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.username = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handleAvatarSuccess(response, file, fileList) {
      if (response.code === '200') {
        this.form.avatar = response.data
      } else {
        this.$message.error(response.msg)
      }
    },
  }
}
</script>

<style scoped>

</style>
