import { beforeEach, describe, expect, it } from 'vitest'
import router from './index'

describe('router', () => {
  beforeEach(async () => {
    localStorage.clear()
    await router.replace('/login')
  })

  it('has unique route names', () => {
    const names = router.getRoutes().map(route => route.name).filter(Boolean)

    expect(new Set(names).size).toBe(names.length)
    expect(names).toEqual(expect.arrayContaining([
      'ManagerHome', 'ManagerType', 'StoreHome', 'StoreType', 'ProductDetail',
      'ManagerOrders',
    ]))
  })

  it('redirects a signed-out user away from protected routes', async () => {
    await router.push('/home')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/home')
  })

  it('allows public login and registration routes', () => {
    const publicPaths = router.getRoutes()
      .filter(route => route.meta.public)
      .map(route => route.path)

    expect(publicPaths).toEqual(expect.arrayContaining(['/login', '/register']))
  })

  it('allows signed-out visitors to browse the storefront', async () => {
    await router.push('/front/home')

    expect(router.currentRoute.value.path).toBe('/front/home')
  })

  it('allows signed-out visitors to open a product detail route', async () => {
    const detailRoute = router.getRoutes().find(route => route.name === 'ProductDetail')

    expect(detailRoute.path).toBe('/front/product/:id')
    expect(detailRoute.meta.public).toBe(true)

    await router.push('/front/product/7')
    expect(router.currentRoute.value.path).toBe('/front/product/7')
  })

  it('protects customer order history', async () => {
    const orderRoute = router.getRoutes().find(route => route.name === 'CustomerOrders')

    expect(orderRoute.path).toBe('/front/orders')
    expect(orderRoute.meta.public).not.toBe(true)

    await router.push('/front/orders')
    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/front/orders')
  })

  it('protects the customer cart from guests and non-customer accounts', async () => {
    const cartRoute = router.getRoutes().find(route => route.name === 'CustomerCart')

    expect(cartRoute.path).toBe('/front/cart')
    expect(cartRoute.meta.roles).toEqual(['USER'])

    await router.push('/front/cart')
    expect(router.currentRoute.value.path).toBe('/login')

    localStorage.setItem('xm-user', JSON.stringify({ id: 3, role: 'USER', token: 'token' }))
    await router.push('/front/cart')
    expect(router.currentRoute.value.path).toBe('/front/cart')

    localStorage.setItem('xm-user', JSON.stringify({ id: 7, role: 'BUSINESS', token: 'token' }))
    await router.push('/front/home')
    await router.push('/front/cart')
    expect(router.currentRoute.value.path).toBe('/403')

    localStorage.setItem('xm-user', JSON.stringify({ id: 1, role: 'ADMIN', token: 'token' }))
    await router.push('/front/home')
    await router.push('/front/cart')
    expect(router.currentRoute.value.path).toBe('/403')
  })

  it('stops a seller from opening administrator-only pages', async () => {
    localStorage.setItem('xm-user', JSON.stringify({ id: 7, role: 'BUSINESS', token: 'token' }))

    await router.push('/business')

    expect(router.currentRoute.value.path).toBe('/403')
  })
})
