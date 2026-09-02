<template>
  <div class="management-page">
    <header class="page-heading">
      <div>
        <span class="page-eyebrow">Fulfilment</span>
        <h1>{{ user.role === 'BUSINESS' ? 'My orders' : 'All orders' }}</h1>
        <p>Review purchases and move each order through a clear delivery workflow.</p>
      </div>
      <button type="button" class="secondary-action" @click="load(1)">Refresh</button>
    </header>

    <section class="data-panel order-management-panel">
      <div v-if="loading" class="dashboard-loading">Loading orders…</div>
      <div v-else-if="orders.length" class="management-order-list">
        <article v-for="order in orders" :key="order.id" class="management-order-card">
          <div class="order-identity"><span>{{ order.orderNumber }}</span><strong>{{ order.productName }}</strong><small>{{ formatDate(order.createdAt) }}</small></div>
          <div class="order-customer"><span>Customer</span><strong>{{ order.customerName || 'Customer account' }}</strong><small v-if="user.role === 'ADMIN'">{{ order.businessName || 'NorrByte Market' }}</small></div>
          <div class="order-value"><span>{{ order.quantity }} item{{ order.quantity === 1 ? '' : 's' }}</span><strong>{{ formatSek(order.totalPrice) }}</strong></div>
          <div class="order-state"><span class="status-pill" :class="`status-${orderStatusTone(order.status)}`">{{ orderStatusLabel(order.status) }}</span></div>
          <div class="order-actions">
            <button v-for="status in nextOrderStatuses(order.status)" :key="status" type="button" :class="{ danger: status === ORDER_STATUS.CANCELLED }" @click="updateStatus(order.id, status)">{{ actionLabel(status) }}</button>
            <span v-if="!nextOrderStatuses(order.status).length">No further actions</span>
          </div>
        </article>
      </div>
      <div v-else class="management-empty-state"><strong>No orders to manage</strong><p>New customer purchases will appear here.</p></div>

      <el-pagination v-if="total > pageSize" background :current-page="pageNum" :page-size="pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" />
    </section>
  </div>
</template>

<script>
import { ORDER_STATUS, nextOrderStatuses, orderStatusLabel, orderStatusTone } from '@/constants/orderStatus'
import { formatSek } from '@/utils/format'

const dateFormatter = new Intl.DateTimeFormat('en-SE', { dateStyle: 'medium', timeStyle: 'short' })

export default {
  name: 'ManagerOrders',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      orders: [], pageNum: 1, pageSize: 10, total: 0, loading: true, ORDER_STATUS,
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    formatSek, orderStatusLabel, orderStatusTone, nextOrderStatuses,
    actionLabel(status) {
      return {
        [ORDER_STATUS.PROCESSING]: 'Start processing',
        [ORDER_STATUS.SHIPPED]: 'Mark shipped',
        [ORDER_STATUS.CANCELLED]: 'Cancel order',
      }[status] || status
    },
    formatDate(value) {
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? 'Date unavailable' : dateFormatter.format(date)
    },
    load(pageNum) {
      this.pageNum = pageNum || 1
      this.loading = true
      return this.$request.get('/orders/selectPage', { params: { pageNum: this.pageNum, pageSize: this.pageSize } }).then(res => {
        if (res.code === '200') {
          this.orders = res.data?.list || []
          this.total = res.data?.total || 0
        } else this.$message.error(res.msg)
      }).finally(() => { this.loading = false })
    },
    updateStatus(id, status) {
      return this.$request.put('/orders/status', { id, status }).then(res => {
        if (res.code === '200') {
          this.$message.success('Order status updated')
          return this.load(this.pageNum)
        }
        this.$message.error(res.msg)
      })
    },
  },
}
</script>
