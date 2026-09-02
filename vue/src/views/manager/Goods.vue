<template>
  <div class="management-page">
    <header class="page-heading">
      <div><span class="page-eyebrow">Catalogue</span><h1>{{ user.role === 'BUSINESS' ? 'My products' : 'Products' }}</h1><p>Keep product details, pricing and availability accurate.</p></div>
    </header>
    <div class="search">
      <el-input v-model="name" placeholder="Search by product name" style="width: 200px" />
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">Search</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">Reset</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">Add product</el-button>
      <el-button type="danger" plain @click="delBatch">Delete selected</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" sortable />
        <el-table-column label="Product image">
          <template #default="scope">
            <el-image
              v-if="scope.row.img"
              style="width: 40px; height: 40px"
              :src="scope.row.img"
              :preview-src-list="[scope.row.img]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Product name" show-overflow-tooltip />
        <el-table-column prop="description" label="Description">
          <template #default="scope">
            <el-button type="primary" @click="viewEditor(scope.row.description)">View</el-button>
          </template>
        </el-table-column>
        <el-table-column label="Price" show-overflow-tooltip>
          <template #default="scope">{{ formatSek(scope.row.price) }}</template>
        </el-table-column>
        <el-table-column prop="typeName" label="Category" show-overflow-tooltip />
        <el-table-column prop="businessName" label="Seller" show-overflow-tooltip />
        <el-table-column prop="count" label="Stock" show-overflow-tooltip />
        <el-table-column label="Actions" width="180" align="center">
          <template #default="scope">
            <el-button plain type="primary" size="small" @click="handleEdit(scope.row)">Edit</el-button>
            <el-button plain type="danger" size="small" @click="del(scope.row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          :current-page="pageNum"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog v-model="formVisible" title="Product details" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="padding-right: 50px">
        <el-form-item label="Product image">
          <el-upload
            class="avatar-uploader"
            :action="$baseUrl + '/files/upload'"
            :headers="{ token: user.token }"
            accept="image/jpeg,image/png,image/webp"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">Upload image</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="name" label="Product name">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="price" label="Price (SEK)">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="typeId" label="Category">
          <el-select v-model="form.typeId" placeholder="Select a category" style="width: 100%">
            <el-option v-for="item in typeData" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="count" label="Stock">
          <el-input-number v-model="form.count" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="description" label="Description">
          <el-input v-model="form.description" type="textarea" :rows="6" maxlength="5000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Save</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="descriptionVisible" title="Product description" width="50%">
      <div class="description-preview">{{ viewData }}</div>
    </el-dialog>
  </div>
</template>

<script>
import { BUSINESS_STATUS } from '@/constants/businessStatus'
import { formatSek } from '@/utils/format'

export default {
  name: 'Goods',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      name: '',
      formVisible: false,
      descriptionVisible: false,
      form: { price: 0, count: 0 },
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      rules: {
        name: [{ required: true, message: 'Enter a product name', trigger: 'blur' }],
        price: [{ required: true, message: 'Enter a product price', trigger: 'change' }],
        typeId: [{ required: true, message: 'Select a product category', trigger: 'change' }],
        count: [{ required: true, message: 'Enter the available stock', trigger: 'change' }],
      },
      ids: [],
      typeData: [],
      viewData: '',
    }
  },
  created() {
    this.load(1)
    this.loadType()
  },
  methods: {
    formatSek,
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        if (res.code === '200') {
          this.typeData = res.data || []
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleAdd() {
      if (this.user.role === 'BUSINESS' && this.user.status !== BUSINESS_STATUS.APPROVED) {
        this.$message.warning('Your seller account must be approved before you can publish products')
        return
      }
      this.form = { price: 0, count: 0 }
      this.formVisible = true
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.formVisible = true
    },
    viewEditor(content) {
      this.viewData = content || ''
      this.descriptionVisible = true
    },
    save() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        this.$request({
          url: this.form.id ? '/goods/update' : '/goods/add',
          method: this.form.id ? 'PUT' : 'POST',
          data: this.form,
        }).then(res => {
          if (res.code === '200') {
            this.$message.success('Saved successfully')
            this.load(1)
            this.formVisible = false
          } else {
            this.$message.error(res.msg)
          }
        })
      })
    },
    del(id) {
      this.$confirm('Delete this product?', 'Confirm deletion', { type: 'warning' })
        .then(() => this.$request.delete('/goods/delete/' + id))
        .then(res => {
          if (res.code === '200') {
            this.$message.success('Deleted successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)
          }
        })
        .catch(() => {})
    },
    handleSelectionChange(rows) {
      this.ids = rows.map(row => row.id)
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning('Select at least one product')
        return
      }
      this.$confirm('Delete the selected products?', 'Confirm deletion', { type: 'warning' })
        .then(() => this.$request.delete('/goods/delete/batch', { data: this.ids }))
        .then(res => {
          if (res.code === '200') {
            this.$message.success('Deleted successfully')
            this.load(1)
          } else {
            this.$message.error(res.msg)
          }
        })
        .catch(() => {})
    },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/goods/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name || undefined,
        },
      }).then(res => {
        this.tableData = res.data?.list || []
        this.total = res.data?.total || 0
      })
    },
    reset() {
      this.name = ''
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
    handleAvatarSuccess(response) {
      if (response.code === '200') {
        this.form.img = response.data
      } else {
        this.$message.error(response.msg)
      }
    },
  },
}
</script>

<style scoped>
.description-preview {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
</style>
