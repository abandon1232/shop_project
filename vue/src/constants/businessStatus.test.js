import { describe, expect, it } from 'vitest'
import { BUSINESS_STATUS, businessStatusLabel } from './businessStatus'

describe('business status presentation', () => {
  it('maps API status codes to English labels', () => {
    expect(businessStatusLabel(BUSINESS_STATUS.PENDING)).toBe('Pending review')
    expect(businessStatusLabel(BUSINESS_STATUS.APPROVED)).toBe('Approved')
    expect(businessStatusLabel(BUSINESS_STATUS.REJECTED)).toBe('Rejected')
    expect(businessStatusLabel('UNKNOWN')).toBe('Unknown')
  })
})
