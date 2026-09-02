export const ORDER_STATUS = Object.freeze({
  PLACED: 'PLACED',
  PROCESSING: 'PROCESSING',
  SHIPPED: 'SHIPPED',
  CANCELLED: 'CANCELLED',
})

const LABELS = Object.freeze({
  [ORDER_STATUS.PLACED]: 'Placed',
  [ORDER_STATUS.PROCESSING]: 'Processing',
  [ORDER_STATUS.SHIPPED]: 'Shipped',
  [ORDER_STATUS.CANCELLED]: 'Cancelled',
})

const TONES = Object.freeze({
  [ORDER_STATUS.PLACED]: 'warning',
  [ORDER_STATUS.PROCESSING]: 'primary',
  [ORDER_STATUS.SHIPPED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'danger',
})

const NEXT_STATUSES = Object.freeze({
  [ORDER_STATUS.PLACED]: [ORDER_STATUS.PROCESSING, ORDER_STATUS.CANCELLED],
  [ORDER_STATUS.PROCESSING]: [ORDER_STATUS.SHIPPED, ORDER_STATUS.CANCELLED],
  [ORDER_STATUS.SHIPPED]: [],
  [ORDER_STATUS.CANCELLED]: [],
})

export const orderStatusLabel = code => LABELS[code] || 'Unknown'

export const orderStatusTone = code => TONES[code] || 'info'

export const nextOrderStatuses = code => [...(NEXT_STATUSES[code] || [])]
