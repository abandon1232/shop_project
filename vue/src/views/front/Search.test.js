import { flushPromises, shallowMount } from '@vue/test-utils'
import { reactive } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import Search from './Search.vue'

describe('Store search', () => {
  it('reloads results when the route search term changes', async () => {
    const route = reactive({ query: { name: 'headphones' } })
    const request = {
      get: vi.fn(() => Promise.resolve({ code: '200', data: [] })),
    }
    const wrapper = shallowMount(Search, {
      global: {
        mocks: {
          $route: route,
          $request: request,
          $message: { error: vi.fn() },
          $router: { push: vi.fn() },
        },
        stubs: {
          ElCol: { template: '<div><slot /></div>' },
          ElRow: { template: '<div><slot /></div>' },
          ProductCard: { template: '<article />' },
        },
      },
    })
    await flushPromises()

    route.query.name = 'keyboard'
    await flushPromises()

    const searchCalls = request.get.mock.calls.filter(([url]) => url === '/goods/selectByName')
    expect(searchCalls).toEqual([
      ['/goods/selectByName', { params: { name: 'headphones' } }],
      ['/goods/selectByName', { params: { name: 'keyboard' } }],
    ])
    expect(wrapper.text()).toContain('Search results for “keyboard”')
  })
})
