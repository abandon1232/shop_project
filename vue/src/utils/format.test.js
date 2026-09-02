import { describe, expect, it } from 'vitest'
import { formatSek } from './format'

describe('formatSek', () => {
  it('formats a number for an English Swedish storefront', () => {
    expect(formatSek(1299)).toMatch(/1[\s\u00a0]299,00\s*kr/)
  })

  it('uses zero for an invalid value', () => {
    expect(formatSek(undefined)).toMatch(/0,00\s*kr/)
  })
})
