import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import Goods from './Goods.vue'

const dialogStub = {
  template: '<section><slot /></section>',
}

describe('Goods view', () => {
  it('renders a product description as text', async () => {
    const request = {
      get: () => Promise.resolve({ code: '200', data: { list: [], total: 0 } }),
    }
    const wrapper = mount(Goods, {
      global: {
        mocks: {
          $request: request,
          $message: { error() {}, warning() {}, success() {} },
          $baseUrl: 'http://localhost:9090',
        },
        stubs: {
          ElTable: true,
          ElDialog: dialogStub,
          ElForm: true,
          ElPagination: true,
          ElInput: true,
          ElButton: true,
          ElUpload: true,
        },
      },
    })
    await flushPromises()

    await wrapper.vm.viewEditor('<img src=x onerror=alert(1)>')

    expect(wrapper.text()).toContain('<img src=x onerror=alert(1)>')
    expect(wrapper.find('img[src="x"]').exists()).toBe(false)
  })

  it('blocks product publishing for a pending business account', async () => {
    localStorage.setItem('xm-user', JSON.stringify({ role: 'BUSINESS', status: 'PENDING' }))
    const warning = vi.fn()
    const wrapper = mount(Goods, {
      global: {
        mocks: {
          $request: { get: () => Promise.resolve({ code: '200', data: [] }) },
          $message: { error() {}, warning, success() {} },
          $baseUrl: 'http://localhost:9090',
        },
        stubs: {
          ElTable: true,
          ElDialog: dialogStub,
          ElForm: true,
          ElPagination: true,
          ElInput: true,
          ElButton: true,
          ElUpload: true,
        },
      },
    })
    await flushPromises()

    wrapper.vm.handleAdd()

    expect(warning).toHaveBeenCalledWith(
      'Your seller account must be approved before you can publish products',
    )
    expect(wrapper.vm.formVisible).toBe(false)
  })
})
