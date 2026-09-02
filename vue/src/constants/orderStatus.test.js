import { describe, expect, it } from 'vitest'
import { ORDER_STATUS, nextOrderStatuses, orderStatusLabel, orderStatusTone } from './orderStatus'

describe('order status presentation', () => {
  it('provides clear English labels and visual tones', () => {
    expect(orderStatusLabel(ORDER_STATUS.PLACED)).toBe('Placed')
    expect(orderStatusLabel(ORDER_STATUS.PROCESSING)).toBe('Processing')
    expect(orderStatusLabel(ORDER_STATUS.SHIPPED)).toBe('Shipped')
    expect(orderStatusLabel(ORDER_STATUS.CANCELLED)).toBe('Cancelled')
    expect(orderStatusLabel('UNKNOWN')).toBe('Unknown')
    expect(orderStatusTone(ORDER_STATUS.SHIPPED)).toBe('success')
    expect(orderStatusTone(ORDER_STATUS.CANCELLED)).toBe('danger')
  })

  it('exposes only valid next fulfilment states', () => {
    expect(nextOrderStatuses(ORDER_STATUS.PLACED)).toEqual([
      ORDER_STATUS.PROCESSING,
      ORDER_STATUS.CANCELLED,
    ])
    expect(nextOrderStatuses(ORDER_STATUS.PROCESSING)).toEqual([
      ORDER_STATUS.SHIPPED,
      ORDER_STATUS.CANCELLED,
    ])
    expect(nextOrderStatuses(ORDER_STATUS.SHIPPED)).toEqual([])
  })
})
