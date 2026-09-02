import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
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
})
