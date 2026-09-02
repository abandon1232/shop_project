export const BUSINESS_STATUS = Object.freeze({
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
})

const STATUS_LABELS = Object.freeze({
  [BUSINESS_STATUS.PENDING]: 'Pending review',
  [BUSINESS_STATUS.APPROVED]: 'Approved',
  [BUSINESS_STATUS.REJECTED]: 'Rejected',
})

export const businessStatusLabel = code => STATUS_LABELS[code] || 'Unknown'
