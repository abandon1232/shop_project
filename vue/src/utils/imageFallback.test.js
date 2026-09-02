import { describe, expect, it } from 'vitest'

import { applyImageFallback } from './imageFallback'

describe('applyImageFallback', () => {
  it('replaces a broken image only once', () => {
    const image = { src: '/broken.jpg', dataset: {} }

    applyImageFallback({ currentTarget: image }, '/product-placeholder.webp')

    expect(image.src).toBe('/product-placeholder.webp')
    expect(image.dataset.fallbackApplied).toBe('true')

    image.src = '/another-broken.jpg'
    applyImageFallback({ currentTarget: image }, '/product-placeholder.webp')

    expect(image.src).toBe('/another-broken.jpg')
  })
})
