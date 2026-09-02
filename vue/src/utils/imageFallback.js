export const applyImageFallback = (event, fallbackUrl) => {
  const image = event.currentTarget

  if (!image || image.dataset.fallbackApplied === 'true') return

  image.dataset.fallbackApplied = 'true'
  image.src = fallbackUrl
}
