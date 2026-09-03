import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Person from './Person.vue'

describe('Customer profile', () => {
  beforeEach(() => {
    localStorage.clear()
    localStorage.setItem('xm-user', JSON.stringify({
      id: 3,
      username: 'customer@example.test',
      role: 'USER',
    }))
  })

  it('keeps password labels on one line with responsive form hooks', () => {
    const wrapper = mount(Person, {
      global: {
        mocks: {
          $baseUrl: 'http://localhost:9090',
          $request: { put: vi.fn() },
          $message: { error: vi.fn(), success: vi.fn() },
          $router: { push: vi.fn() },
        },
        stubs: {
          ElButton: { template: '<button><slot /></button>' },
          ElCard: { template: '<section><slot /></section>' },
          ElDialog: { template: '<section><slot /><slot name="footer" /></section>' },
          ElForm: { template: '<form v-bind="$attrs"><slot /></form>' },
          ElFormItem: { template: '<div v-bind="$attrs"><slot /></div>' },
          ElInput: { template: '<input>' },
          ElUpload: { template: '<div><slot /></div>' },
        },
      },
    })

    const passwordForm = wrapper.get('.profile-password-form')
    expect(passwordForm.attributes('label-width')).toBe('150px')
    expect(wrapper.findAll('.profile-password-form-item')).toHaveLength(3)
  })
})
