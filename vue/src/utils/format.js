const sekFormatter = new Intl.NumberFormat('en-SE', {
  style: 'currency',
  currency: 'SEK',
})

export const formatSek = value => sekFormatter.format(Number(value) || 0)
