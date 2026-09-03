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
            goodsId: 7,
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
          $router: { push: vi.fn() },
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

  it('opens product details from the order image and product name', async () => {
    const router = { push: vi.fn() }
    const request = {
      get: vi.fn(() => Promise.resolve({
        code: '200',
        data: {
          list: [{
            id: 41,
            goodsId: 7,
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
          $router: router,
        },
        stubs: { ElPagination: true },
      },
    })
    await flushPromises()

    await wrapper.get('.order-product-image-link').trigger('click')
    await wrapper.get('.order-product-name-link').trigger('click')

    expect(router.push).toHaveBeenCalledTimes(2)
    expect(router.push).toHaveBeenNthCalledWith(1, {
      name: 'ProductDetail',
      params: { id: 7 },
    })
    expect(router.push).toHaveBeenNthCalledWith(2, {
      name: 'ProductDetail',
      params: { id: 7 },
    })

    router.push.mockClear()
    await wrapper.get('.order-summary strong').trigger('click')
    await wrapper.get('.status-pill').trigger('click')
    expect(router.push).not.toHaveBeenCalled()
  })

  it('renders a deleted product snapshot without broken detail links', async () => {
    const router = { push: vi.fn() }
    const request = {
      get: vi.fn(() => Promise.resolve({
        code: '200',
        data: {
          list: [{
            id: 42,
            goodsId: null,
            orderNumber: 'NB-20260903-DELETED',
            productName: 'Discontinued speaker',
            productImg: '/images/catalog/products/discontinued-speaker.webp',
            quantity: 1,
            totalPrice: 799,
            status: 'PLACED',
            createdAt: '2026-09-03T10:00:00',
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
          $router: router,
        },
        stubs: { ElPagination: true },
      },
    })
    await flushPromises()

    expect(wrapper.find('.order-product-image-link').exists()).toBe(false)
    expect(wrapper.find('.order-product-name-link').exists()).toBe(false)
    expect(wrapper.text()).toContain('Discontinued speaker')
    expect(wrapper.text()).toContain('Product unavailable')
    expect(router.push).not.toHaveBeenCalled()
  })
})
