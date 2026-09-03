import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import Home from './Home.vue'

describe('Management dashboard', () => {
  beforeEach(() => localStorage.clear())
  afterEach(() => vi.useRealTimers())

  it('shows the global business summary to administrators', async () => {
    vi.useFakeTimers({ toFake: ['Date'] })
    vi.setSystemTime(new Date('2024-01-01T19:00:00'))
    localStorage.setItem('xm-user', JSON.stringify({ id: 1, role: 'ADMIN', name: 'Alicia' }))
    const request = {
      get: vi.fn(path => Promise.resolve(path === '/dashboard/summary'
        ? {
            code: '200',
            data: {
              products: 18,
              categories: 6,
              orders: 12,
              customers: 8,
              sellers: 2,
              lowStockProducts: 3,
              revenue: 12590,
            },
          }
        : { code: '200', data: [] })),
    }

    const wrapper = mount(Home, {
      global: { mocks: { $request: request, $message: { error: vi.fn() } } },
    })
    await flushPromises()

    expect(request.get).toHaveBeenCalledWith('/dashboard/summary')
    expect(wrapper.text()).toContain('Good evening, Alicia')
    expect(wrapper.text()).toContain('18')
    expect(wrapper.text()).toContain('Products')
    expect(wrapper.text()).toContain('Categories')
    expect(wrapper.text()).toContain('Customers')
    expect(wrapper.text()).toContain('Sellers')
    expect(wrapper.text()).toMatch(/12[\s\u00a0]590,00\s*kr/)
  })

  it('keeps global account totals out of the seller dashboard', async () => {
    localStorage.setItem('xm-user', JSON.stringify({ id: 7, role: 'BUSINESS', name: 'Nordic Store' }))
    const request = {
      get: vi.fn(() => Promise.resolve({
        code: '200',
        data: { products: 5, orders: 4, lowStockProducts: 1, revenue: 2498 },
      })),
    }

    const wrapper = mount(Home, {
      global: { mocks: { $request: request, $message: { error: vi.fn() } } },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Store overview')
    expect(wrapper.text()).not.toContain('Customers')
    expect(wrapper.text()).not.toContain('Sellers')
    expect(wrapper.text()).not.toContain('Categories')
  })
})
