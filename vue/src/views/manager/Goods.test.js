import { flushPromises, mount } from '@vue/test-utils'
import { ElForm, ElFormItem, ElInput, ElInputNumber, ElOption, ElSelect } from 'element-plus'
import { describe, expect, it, vi } from 'vitest'
import Goods from './Goods.vue'
import goodsSource from './Goods.vue?raw'

const dialogStub = {
  template: '<section><slot /></section>',
}

describe('Goods view', () => {
  it('uses a single SEK price field without legacy unit text', () => {
    expect(goodsSource).not.toContain('prop="unit"')
    expect(goodsSource).not.toContain('label="Unit"')
    expect(goodsSource).not.toMatch(/(?:kr|SEK)\s*\/\s*p\b/i)
  })

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

  it('does not save a product until an image has been uploaded', async () => {
    localStorage.setItem('xm-user', JSON.stringify({ role: 'ADMIN' }))
    const request = vi.fn(() => Promise.resolve({ code: '200' }))
    const error = vi.fn()
    request.get = vi.fn(url => Promise.resolve({
      code: '200',
      data: url === '/type/selectAll' ? [] : { list: [], total: 0 },
    }))
    const wrapper = mount(Goods, {
      global: {
        components: { ElForm, ElFormItem, ElInput, ElInputNumber, ElOption, ElSelect },
        mocks: {
          $request: request,
          $message: { error, warning: vi.fn(), success: vi.fn() },
          $baseUrl: 'http://localhost:9090',
        },
        stubs: {
          ElTable: true,
          ElTableColumn: true,
          ElImage: true,
          ElDialog: dialogStub,
          ElPagination: true,
          ElButton: true,
          ElUpload: true,
        },
      },
    })
    await flushPromises()
    wrapper.vm.form = {
      name: 'Desk lamp',
      price: 499,
      count: 4,
      typeId: 2,
      description: 'A compact lamp.',
    }
    await wrapper.vm.$nextTick()

    wrapper.vm.save()
    await flushPromises()

    expect(request).not.toHaveBeenCalled()
    expect(error).toHaveBeenCalledWith('Upload a product image')
  })
})
