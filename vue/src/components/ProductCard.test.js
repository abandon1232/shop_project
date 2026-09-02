import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ProductCard from './ProductCard.vue'

describe('ProductCard', () => {
  const product = {
    id: 7,
    name: 'Nordic desk lamp',
    typeName: 'Smart Home',
    img: '/images/catalog/products/climate-sensor.webp',
    price: 1299,
    unit: 'p',
    count: 4,
  }

  it('shows a complete product summary without a unit suffix', () => {
    const wrapper = mount(ProductCard, { props: { product } })

    expect(wrapper.get('img').attributes('src')).toBe(product.img)
    expect(wrapper.text()).toContain('Nordic desk lamp')
    expect(wrapper.text()).toContain('Smart Home')
    expect(wrapper.text()).toContain('In stock')
    expect(wrapper.text()).toMatch(/1[\s\u00a0]299,00\s*kr/)
    expect(wrapper.text()).not.toContain('/ p')
  })

  it('emits the product when the accessible card button is selected', async () => {
    const wrapper = mount(ProductCard, { props: { product } })

    await wrapper.get('button').trigger('click')

    expect(wrapper.emitted('select')).toEqual([[product]])
  })
})
