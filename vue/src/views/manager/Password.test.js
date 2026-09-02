import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Password from './Password.vue'

describe('Password settings', () => {
  beforeEach(() => {
    localStorage.clear()
    localStorage.setItem('xm-user', JSON.stringify({ id: 7, username: 'seller@example.test' }))
  })

  it('exposes responsive form hooks with a readable desktop label width', () => {
    const wrapper = mount(Password, {
      global: {
        mocks: {
          $request: { put: vi.fn() },
          $message: { error: vi.fn(), success: vi.fn() },
          $router: { push: vi.fn() },
        },
        stubs: {
          ElCard: { template: '<div v-bind="$attrs"><slot /></div>' },
          ElForm: { template: '<form v-bind="$attrs"><slot /></form>' },
          ElFormItem: { template: '<div v-bind="$attrs"><slot /></div>' },
          ElInput: { template: '<input>' },
          ElButton: { template: '<button><slot /></button>' },
        },
      },
    })

    const form = wrapper.get('.password-form')
    expect(form.attributes('label-width')).toBe('150px')
    expect(wrapper.findAll('.password-form-item')).toHaveLength(3)
  })
})
