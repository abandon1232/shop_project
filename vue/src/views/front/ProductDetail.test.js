import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ProductDetail from './ProductDetail.vue'

describe('ProductDetail', () => {
  const product = {
    id: 7,
    name: 'NordBook Air 14 Laptop',
    description: 'A lightweight laptop for study and work.',
    img: '/images/catalog/products/nordbook-air-14.webp',
    price: 10990,
    unit: 'p',
    count: 14,
    typeName: 'Computers & Tablets',
    businessName: null,
  }

  beforeEach(() => localStorage.clear())

  function mountDetail() {
    const request = {
      get: vi.fn(() => Promise.resolve({ code: '200', data: product })),
      post: vi.fn(() => Promise.resolve({ code: '200' })),
    }
    const router = { push: vi.fn() }
    const message = { error: vi.fn(), success: vi.fn() }
    const wrapper = mount(ProductDetail, {
      global: {
        mocks: {
          $route: { params: { id: '7' }, fullPath: '/front/product/7' },
          $router: router,
          $request: request,
          $message: message,
        },
        stubs: {
          ElButton: { template: '<button><slot /></button>' },
          ElInputNumber: { template: '<input>' },
          ElSkeleton: true,
        },
      },
    })
    return { request, router, message, wrapper }
  }

  it('loads a product with an add-to-cart action without a unit suffix', async () => {
    const { request, wrapper } = mountDetail()
    await flushPromises()

    expect(request.get).toHaveBeenCalledWith('/goods/selectById', { params: { id: '7' } })
    expect(wrapper.text()).toContain(product.name)
    expect(wrapper.text()).toContain(product.description)
    expect(wrapper.text()).toContain(product.typeName)
    expect(wrapper.text()).toContain('NorrByte Market')
    expect(wrapper.text()).toContain('14 in stock')
    expect(wrapper.text()).toContain('Add to cart')
    expect(wrapper.text()).not.toContain('Buy now')
    expect(wrapper.text()).not.toContain('Demo checkout')
    expect(wrapper.text()).toMatch(/10[\s\u00a0]990,00\s*kr/)
    expect(wrapper.text()).not.toContain('/ p')
  })

  it('shows the seller business name from the product', async () => {
    const originalBusinessName = product.businessName
    product.businessName = 'Nordic Sound AB'
    const { wrapper } = mountDetail()
    await flushPromises()

    expect(wrapper.text()).toContain('Nordic Sound AB')
    expect(wrapper.text()).not.toContain('Approved marketplace seller')
    product.businessName = originalBusinessName
  })

  it('sends a guest to sign in with a return address', async () => {
    const { router, wrapper } = mountDetail()
    await flushPromises()

    await wrapper.vm.addToCart()

    expect(router.push).toHaveBeenCalledWith({
      path: '/login',
      query: { redirect: '/front/product/7' },
    })
  })

  it('adds a product to the cart using only product id and quantity', async () => {
    localStorage.setItem('xm-user', JSON.stringify({ id: 3, role: 'USER', token: 'signed-token' }))
    const { message, request, wrapper } = mountDetail()
    await flushPromises()
    wrapper.vm.quantity = 2

    await wrapper.vm.addToCart()
    await flushPromises()

    expect(request.post).toHaveBeenCalledWith('/cart/items', { goodsId: 7, quantity: 2 })
    expect(message.success).toHaveBeenCalledWith('Added to cart')
    expect(wrapper.emitted('cart-updated')).toBeTruthy()
  })
})
