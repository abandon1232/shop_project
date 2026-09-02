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
      'ManagerHome', 'ManagerType', 'StoreHome', 'StoreType',
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
})
