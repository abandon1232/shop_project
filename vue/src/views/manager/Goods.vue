<template>
  <div>
    <div class="search">
      <el-input v-model="name" placeholder="请输入商品名称查询" style="width: 200px" />
      <el-button type="info" plain style="margin-left: 10px" @click="load(1)">查询</el-button>
      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
    </div>

    <div class="operation">
      <el-button type="primary" plain @click="handleAdd">发布商品</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="table">
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="序号" width="80" align="center" sortable />
        <el-table-column label="商品主图">
          <template #default="scope">
            <el-image
              v-if="scope.row.img"
              style="width: 40px; height: 40px"
              :src="scope.row.img"
              :preview-src-list="[scope.row.img]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
        <el-table-column prop="description" label="商品描述">
          <template #default="scope">
            <el-button type="primary" @click="viewEditor(scope.row.description)">点击查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="商品价格" show-overflow-tooltip />
        <el-table-column prop="unit" label="计件单位" show-overflow-tooltip />
        <el-table-column prop="typeName" label="商品分类" show-overflow-tooltip />
        <el-table-column prop="businessName" label="所属商家" show-overflow-tooltip />
        <el-table-column prop="count" label="库存数量" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button plain type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button plain type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
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

    <el-dialog v-model="formVisible" title="商品信息" width="40%" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="padding-right: 50px">
        <el-form-item label="商品主图">
          <el-upload
            class="avatar-uploader"
            :action="$baseUrl + '/files/upload'"
            :headers="{ token: user.token }"
            accept="image/jpeg,image/png,image/gif"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="name" label="商品名称">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="price" label="商品价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="typeId" label="商品分类">
          <el-select v-model="form.typeId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in typeData" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item prop="unit" label="计件单位">
          <el-input v-model="form.unit" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="count" label="库存数量">
          <el-input-number v-model="form.count" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item prop="description" label="商品介绍">
          <el-input v-model="form.description" type="textarea" :rows="6" maxlength="5000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="descriptionVisible" title="商品介绍" width="50%">
      <div class="description-preview">{{ viewData }}</div>
    </el-dialog>
  </div>
</template>

<script>
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
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        price: [{ required: true, message: '请输入商品价格', trigger: 'change' }],
        typeId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
        count: [{ required: true, message: '请输入库存数量', trigger: 'change' }],
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
      if (this.user.role === 'BUSINESS' && this.user.status !== '审核通过') {
        this.$message.warning('您的店铺信息还未审核通过，暂时不允许发布商品')
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
            this.$message.success('保存成功')
            this.load(1)
            this.formVisible = false
          } else {
            this.$message.error(res.msg)
          }
        })
      })
    },
    del(id) {
      this.$confirm('您确定删除吗？', '确认删除', { type: 'warning' })
        .then(() => this.$request.delete('/goods/delete/' + id))
        .then(res => {
          if (res.code === '200') {
            this.$message.success('操作成功')
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
        this.$message.warning('请选择数据')
        return
      }
      this.$confirm('您确定批量删除这些数据吗？', '确认删除', { type: 'warning' })
        .then(() => this.$request.delete('/goods/delete/batch', { data: this.ids }))
        .then(res => {
          if (res.code === '200') {
            this.$message.success('操作成功')
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
