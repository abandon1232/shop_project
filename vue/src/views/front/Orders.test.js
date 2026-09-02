import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import Orders from './Orders.vue'

describe('Customer orders', () => {
  it('renders persisted order snapshots with price and status', async () => {
    const request = {
      get: vi.fn(() => Promise.resolve({
        code: '200',
        data: {
          list: [{
            id: 41,
            orderNumber: 'NB-20260902-ABCD1234',
            productName: 'QuietWave Wireless Headphones',
            productImg: '/images/catalog/products/quietwave-headphones.webp',
            quantity: 2,
            totalPrice: 3980,
            status: 'PLACED',
            createdAt: '2026-09-02T18:15:00',
          }],
          total: 1,
        },
      })),
    }
    const wrapper = mount(Orders, {
      global: {
        mocks: {
          $request: request,
          $message: { error: vi.fn() },
        },
        stubs: { ElPagination: true },
      },
    })
    await flushPromises()

    expect(request.get).toHaveBeenCalledWith('/orders/selectPage', {
      params: { pageNum: 1, pageSize: 10 },
    })
    expect(wrapper.text()).toContain('NB-20260902-ABCD1234')
    expect(wrapper.text()).toContain('QuietWave Wireless Headphones')
    expect(wrapper.text()).toContain('Quantity 2')
    expect(wrapper.text()).toContain('Placed')
    expect(wrapper.text()).toMatch(/2 Sept? 2026/)
    expect(wrapper.text()).toMatch(/3[\s\u00a0]980,00\s*kr/)
  })
})
