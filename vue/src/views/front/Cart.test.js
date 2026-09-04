import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import Cart from './Cart.vue'

const items = [
  {
    id: 14,
    goodsId: 7,
    quantity: 2,
    productName: 'QuietWave Wireless Headphones',
    productImg: '/images/catalog/products/quietwave-headphones.webp',
    unitPrice: 1990,
    stock: 4,
    businessName: 'Nordic Sound AB',
    businessStatus: 'APPROVED',
  },
  {
    id: 15,
    goodsId: 8,
    quantity: 1,
    productName: 'NordKeys Mechanical Keyboard',
    productImg: '/images/catalog/products/nordkeys.webp',
    unitPrice: 990,
    stock: 8,
    businessName: 'Nordic Sound AB',
    businessStatus: 'APPROVED',
  },
]

function mountCart(lines = items) {
  const request = {
    get: vi.fn(() => Promise.resolve({ code: '200', data: lines })),
    put: vi.fn(() => Promise.resolve({ code: '200' })),
    delete: vi.fn(() => Promise.resolve({ code: '200' })),
    post: vi.fn(() => Promise.resolve({ code: '200', data: [{ id: 81 }, { id: 82 }] })),
  }
  const router = { push: vi.fn() }
  const message = { error: vi.fn(), success: vi.fn() }
  const wrapper = mount(Cart, {
    global: {
      mocks: { $request: request, $router: router, $message: message },
      stubs: {
        ElButton: { template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>', props: ['disabled'] },
        ElInputNumber: true,
      },
    },
  })
  return { message, request, router, wrapper }
}

describe('Customer cart', () => {
  it('offers a clear return to shopping when the cart is empty', async () => {
    const { router, wrapper } = mountCart([])
    await flushPromises()

    expect(wrapper.text()).toContain('Your cart is empty')
    const continueShopping = wrapper.get('.empty-cart button')
    await continueShopping.trigger('click')
    expect(router.push).toHaveBeenCalledWith('/front/home')
  })

  it('shows server cart lines and the presentation total', async () => {
    const { request, wrapper } = mountCart()
    await flushPromises()

    expect(request.get).toHaveBeenCalledWith('/cart/items')
    expect(wrapper.text()).toContain('Your cart')
    expect(wrapper.text()).toContain('Nordic Sound AB')
    expect(wrapper.text()).toMatch(/4[\s\u00a0]970,00\s*kr/)
  })

  it('labels a missing cart product image as unavailable', async () => {
    const { wrapper } = mountCart([{ ...items[0], productImg: '' }])
    await flushPromises()

    expect(wrapper.get('.cart-product-image-link img').attributes('alt')).toBe('Image unavailable')
  })

  it('opens product details from the cart image and product name', async () => {
    const { router, wrapper } = mountCart()
    await flushPromises()

    await wrapper.findAll('.cart-product-image-link')[0].trigger('click')
    await wrapper.findAll('.cart-product-name-link')[0].trigger('click')

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
    await wrapper.find('.cart-line-main > strong').trigger('click')
    await wrapper.find('.availability').trigger('click')
    await wrapper.find('.cart-line-subtotal').trigger('click')
    expect(router.push).not.toHaveBeenCalled()
  })

  it('updates a cart line quantity and reloads the current lines', async () => {
    const { request, wrapper } = mountCart()
    await flushPromises()

    await wrapper.vm.updateQuantity(items[0], 3)

    expect(request.put).toHaveBeenCalledWith('/cart/items/14', { quantity: 3 })
    expect(request.get).toHaveBeenCalledTimes(2)
    expect(wrapper.emitted('cart-updated')).toHaveLength(2)
  })

  it('removes a cart line and reloads the current lines', async () => {
    const { request, wrapper } = mountCart()
    await flushPromises()

    await wrapper.vm.removeItem(items[1])

    expect(request.delete).toHaveBeenCalledWith('/cart/items/15')
    expect(request.get).toHaveBeenCalledTimes(2)
    expect(wrapper.emitted('cart-updated')).toHaveLength(2)
  })

  it('disables checkout for unavailable lines', async () => {
    const unavailableStock = [{ ...items[0], quantity: 5, stock: 4 }]
    const unavailableSeller = [{ ...items[0], businessStatus: 'PENDING' }]
    const stockCart = mountCart(unavailableStock)
    const sellerCart = mountCart(unavailableSeller)
    await flushPromises()

    expect(stockCart.wrapper.vm.canCheckout).toBe(false)
    expect(sellerCart.wrapper.vm.canCheckout).toBe(false)
    expect(stockCart.wrapper.get('.checkout-button').attributes('disabled')).toBeDefined()
    expect(sellerCart.wrapper.get('.checkout-button').attributes('disabled')).toBeDefined()
  })

  it('checks out all cart lines then navigates to order history', async () => {
    const { message, request, router, wrapper } = mountCart()
    await flushPromises()

    await wrapper.vm.checkout()

    expect(request.post).toHaveBeenCalledWith('/cart/checkout')
    expect(message.success).toHaveBeenCalledWith('Checkout complete: 2 orders placed')
    expect(wrapper.emitted('cart-updated')).toHaveLength(2)
    expect(router.push).toHaveBeenCalledWith('/front/orders')
  })
})
