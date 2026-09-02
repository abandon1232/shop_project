import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Home from './Home.vue'

describe('store home', () => {
  beforeEach(() => {
    localStorage.setItem('xm-user', JSON.stringify({ name: 'Alice' }))
  })

  it('loads and labels the simple featured-products feed', async () => {
    const requestedUrls = []
    const request = {
      get: vi.fn(url => {
        requestedUrls.push(url)
        if (url === '/goods/featured') {
          return Promise.resolve({
            code: '200',
            data: [{ id: 7, name: 'Nordic desk lamp', price: 1299, unit: 'each' }],
          })
        }
        if (url === '/type/selectAll') {
          return Promise.resolve({ code: '200', data: [] })
        }
        return Promise.resolve({ code: '400', msg: 'unexpected endpoint', data: [] })
      }),
    }

    const wrapper = mount(Home, {
      global: {
        mocks: {
          $request: request,
          $message: { error() {} },
        },
        stubs: {
          ElCarousel: { template: '<div><slot /></div>' },
          ElCarouselItem: { template: '<div><slot /></div>' },
        },
      },
    })
    await flushPromises()

    expect(requestedUrls).toContain('/goods/featured')
    expect(requestedUrls).not.toContain('/goods/recommend')
    expect(requestedUrls).not.toContain('/goods/selectTop15')
    expect(wrapper.text()).toContain('Featured products')
    expect(wrapper.text()).toContain('Shop by category')
    expect(wrapper.text()).toContain('Nordic desk lamp')
    expect(wrapper.text()).toMatch(/1[\s\u00a0]299,00\s*kr/)
  })
})
