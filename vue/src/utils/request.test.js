import { describe, expect, it } from 'vitest'
import { applyAuthHeader } from './request'

describe('applyAuthHeader', () => {
  it('adds only the token header from stored account data', () => {
    const storage = { getItem: () => JSON.stringify({ token: 'abc' }) }

    const config = applyAuthHeader({ headers: {} }, storage)

    expect(config.headers).toMatchObject({ token: 'abc' })
    expect(config.params).toBeUndefined()
    expect(config.headers['Content-Type']).toBeUndefined()
  })

  it('leaves the request anonymous when storage is malformed', () => {
    const storage = { getItem: () => '{bad json' }

    const config = applyAuthHeader({ headers: {} }, storage)

    expect(config.headers.token).toBeUndefined()
  })
})
