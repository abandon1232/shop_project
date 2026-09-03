import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Login from './Login.vue'

describe('Login', () => {
  beforeEach(() => localStorage.clear())

  function mountLogin(redirect) {
    const router = { push: vi.fn() }
    const request = {
      post: vi.fn(() => Promise.resolve({
        code: '200',
        data: {
          id: 3,
          username: 'nordic-customer',
          role: 'USER',
          token: 'signed-token',
        },
      })),
    }
    const message = { error: vi.fn(), success: vi.fn() }
    const wrapper = mount(Login, {
      global: {
        mocks: {
          $route: { query: { redirect } },
          $router: router,
          $request: request,
          $message: message,
        },
        stubs: {
          ElForm: {
            template: '<form><slot /></form>',
            methods: { validate: callback => callback(true) },
          },
          ElFormItem: { template: '<div><slot /></div>' },
          ElInput: true,
          ElSelect: { template: '<select><slot /></select>' },
          ElOption: true,
          ElButton: { template: '<button><slot /></button>' },
          RouterLink: true,
        },
      },
    })
    return { router, wrapper }
  }

  it('returns a customer to a requested storefront page', async () => {
    const { router, wrapper } = mountLogin('/front/product/7')
    await wrapper.vm.login()
    await flushPromises()

    expect(router.push).toHaveBeenCalledWith('/front/product/7')
  })

  it('does not redirect a customer outside the storefront', async () => {
    const { router, wrapper } = mountLogin('https://example.com')
    await wrapper.vm.login()
    await flushPromises()

    expect(router.push).toHaveBeenCalledWith('/front/home')
  })
})
