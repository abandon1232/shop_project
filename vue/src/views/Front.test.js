import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Front from './Front.vue'

describe('Front layout', () => {
  beforeEach(() => localStorage.clear())

  it('shows the total item quantity in a customer cart link', async () => {
    localStorage.setItem('xm-user', JSON.stringify({
      id: 3,
      username: 'customer@example.test',
      name: 'Customer',
      role: 'USER',
      token: 'token',
    }))
    const request = {
      get: vi.fn(url => Promise.resolve({
        code: '200',
        data: url === '/cart/items'
          ? [{ id: 14, quantity: 1 }, { id: 15, quantity: 2 }]
          : [],
      })),
    }
    const router = { push: vi.fn() }
    const wrapper = mount(Front, {
      global: {
        mocks: { $request: request, $router: router },
        stubs: {
          ElInput: true,
          ElButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          ElDropdown: { template: '<div><slot /><slot name="dropdown" /></div>' },
          ElDropdownMenu: true,
          ElDropdownItem: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          RouterView: true,
        },
      },
    })
    await flushPromises()

    expect(request.get).toHaveBeenCalledWith('/cart/items')
    expect(wrapper.get('.cart-action').text()).toContain('Cart')
    expect(wrapper.get('.cart-count').text()).toBe('3')

    await wrapper.get('.cart-action').trigger('click')
    expect(router.push).toHaveBeenCalledWith('/front/cart')
  })
})
