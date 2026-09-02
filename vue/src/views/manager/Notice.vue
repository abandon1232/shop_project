<template>
  <div>
    <div class="search">
      <el-input placeholder="Search by title" style="width: 200px" v-model="title"></el-input>
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">Add notice</el-button>
      <el-button type="danger" plain @click="delBatch">Delete selected</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" stripe  @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="ID" width="80" align="center" sortable></el-table-column>
        <el-table-column prop="title" label="Title" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="Content" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="Created at"></el-table-column>
        <el-table-column prop="user" label="Created by"></el-table-column>

        <el-table-column label="Actions" width="180" align="center">
          <template #default="scope">
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="small">Edit</el-button>
            <el-button plain type="danger" size="small" @click="del(scope.row.id)">Delete</el-button>
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


    <el-dialog v-model="fromVisible" title="Notice details" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="100px" style="padding-right: 50px" :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="title" label="Title">
          <el-input v-model="form.title" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="content" label="Content">
          <el-input type="textarea" :rows="5" v-model="form.content" autocomplete="off"></el-input>
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
  name: "Notice",
  data() {
    return {
      tableData: [],  // All records.
      pageNum: 1,   // Current page number.
      pageSize: 10,  // Records per page.
      total: 0,
      title: null,
      fromVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        title: [
          {required: true, message: 'Enter a title', trigger: 'blur'},
        ],
        content: [
          {required: true, message: 'Enter the notice content', trigger: 'blur'},
        ]
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
            url: this.form.id ? '/notice/update' : '/notice/add',
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
      this.$confirm('Delete this notice?', 'Confirm deletion', {type: "warning"}).then(response => {
        this.$request.delete('/notice/delete/' + id).then(res => {
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
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // Delete multiple records.
      if (!this.ids.length) {
        this.$message.warning('Select at least one notice')
        return
      }
      this.$confirm('Delete the selected notices?', 'Confirm deletion', {type: "warning"}).then(response => {
        this.$request.delete('/notice/delete/batch', {data: this.ids}).then(res => {
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
      this.$request.get('/notice/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          title: this.title,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    reset() {
      this.title = null
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style scoped>

</style>
