import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Orders from './Orders.vue'

describe('Management orders', () => {
  beforeEach(() => {
    localStorage.clear()
    localStorage.setItem('xm-user', JSON.stringify({ id: 7, role: 'BUSINESS' }))
  })

  it('shows order context and advances an allowed status', async () => {
    const request = {
      get: vi.fn(() => Promise.resolve({
        code: '200',
        data: {
          list: [{
            id: 41,
            orderNumber: 'NB-20260902-ABCD1234',
            customerName: 'Maja Andersson',
            productName: 'QuietWave Wireless Headphones',
            quantity: 2,
            totalPrice: 3980,
            status: 'PLACED',
            createdAt: '2026-09-02T18:15:00',
          }],
          total: 1,
        },
      })),
      put: vi.fn(() => Promise.resolve({ code: '200' })),
    }
    const success = vi.fn()
    const wrapper = mount(Orders, {
      global: {
        mocks: { $request: request, $message: { success, error: vi.fn() } },
        stubs: { ElPagination: true },
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('NB-20260902-ABCD1234')
    expect(wrapper.text()).toContain('Maja Andersson')
    expect(wrapper.text()).toContain('QuietWave Wireless Headphones')
    expect(wrapper.text()).toContain('Start processing')

    const action = wrapper.findAll('button').find(button => button.text() === 'Start processing')
    await action.trigger('click')
    await flushPromises()

    expect(request.put).toHaveBeenCalledWith('/orders/status', { id: 41, status: 'PROCESSING' })
    expect(success).toHaveBeenCalledWith('Order status updated')
  })
})
