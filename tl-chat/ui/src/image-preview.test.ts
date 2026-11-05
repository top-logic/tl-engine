// @vitest-environment jsdom
import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { eventually } from '../test/eventually'
import { bundle } from './i18n'
import { IMAGE_PREVIEW_ATTR, THUMBNAIL_CLASS } from './image-preview'
import { Markdown, renderMarkdown } from './markdown'

const labels = bundle('en')

const SCREENSHOT = '![page.png](/screenshot/a7f3b2)'

/** A picture of an answer is a thumbnail, whatever wrote it - markdown or HTML. */
describe('a picture in an answer', () => {
  const parse = (html: string) => {
    const box = document.createElement('div')
    box.innerHTML = html
    return box
  }

  it('is a thumbnail that opens in full size', () => {
    const img = parse(renderMarkdown(SCREENSHOT, labels)).querySelector('img')!
    expect(img.classList.contains(THUMBNAIL_CLASS)).toBe(true)
    expect(img.hasAttribute(IMAGE_PREVIEW_ATTR)).toBe(true)
    expect(img.getAttribute('role')).toBe('button')
    expect(img.getAttribute('tabindex')).toBe('0')
    expect(img.getAttribute('title')).toBe(labels.imageEnlarge)
    expect(img.getAttribute('alt')).toBe('page.png')
    expect(img.getAttribute('src')).toBe('/screenshot/a7f3b2')
  })

  it('is a thumbnail also when it is written as HTML', () => {
    const img = parse(renderMarkdown('<img src="https://example.com/chart.png" alt="chart">', labels)).querySelector('img')!
    expect(img.classList.contains(THUMBNAIL_CLASS)).toBe(true)
    expect(img.getAttribute('alt')).toBe('chart')
  })

  it('inside a link is left to the link', () => {
    const img = parse(renderMarkdown('[![logo](/logo.png)](https://example.com)', labels)).querySelector('img')!
    expect(img.hasAttribute(IMAGE_PREVIEW_ATTR)).toBe(false)
    expect(img.hasAttribute('role')).toBe(false)
  })

  it('cannot be made one by the text itself', () => {
    // The marks are added after the filter; the text cannot write a handler next to them.
    const html = renderMarkdown('<img src="/x.png" alt="x" data-image-preview onclick="alert(1)">', labels)
    expect(html).not.toContain('onclick')
  })
})

describe('the full-size picture', () => {
  let mounted: Root | null = null

  afterEach(() => {
    mounted?.unmount()
    mounted = null
    document.body.replaceChildren()
  })

  const mount = async (text: string) => {
    const container = document.createElement('div')
    document.body.append(container)
    const root = createRoot(container)
    mounted = root
    root.render(React.createElement(Markdown, { text, labels }))
    await eventually(() => expect(thumbnail(container)).not.toBeNull())
    return container
  }

  const thumbnail = (container: Element) => container.querySelector<HTMLImageElement>('img.' + THUMBNAIL_CLASS)

  const opened = async (container: Element) => {
    await eventually(() => expect(container.querySelector('dialog')).not.toBeNull())
    return container.querySelector('dialog')!
  }

  const closed = (container: Element) =>
    eventually(() => expect(container.querySelector('dialog')).toBeNull())

  const key = (target: Element, name: string) =>
    target.dispatchEvent(new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true }))

  it('opens on a click on the thumbnail', async () => {
    const container = await mount(SCREENSHOT)
    expect(container.querySelector('dialog')).toBeNull()

    thumbnail(container)!.click()

    const dialog = await opened(container)
    expect(dialog.hasAttribute('open')).toBe(true)
    expect(dialog.getAttribute('aria-label')).toBe('page.png')
    const full = dialog.querySelector('img')!
    expect(full.getAttribute('src')).toBe('/screenshot/a7f3b2')
    expect(full.getAttribute('alt')).toBe('page.png')
    expect(full.classList.contains(THUMBNAIL_CLASS)).toBe(false)
  })

  it('opens on Enter on the thumbnail, and Escape closes it and gives the focus back', async () => {
    const container = await mount(SCREENSHOT)
    const thumb = thumbnail(container)!
    thumb.focus()

    key(thumb, 'Enter')
    const dialog = await opened(container)

    key(dialog, 'Escape')
    await closed(container)
    await eventually(() => expect(document.activeElement).toBe(thumb))
  })

  it('closes on its close button and on a click beside the picture', async () => {
    const container = await mount(SCREENSHOT)
    const thumb = thumbnail(container)!

    thumb.click()
    ;(await opened(container)).querySelector<HTMLButtonElement>('button')!.click()
    await closed(container)

    thumb.click()
    ;(await opened(container)).dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await closed(container)
  })

  it('names its close button', async () => {
    const container = await mount(SCREENSHOT)
    thumbnail(container)!.click()
    const dialog = await opened(container)
    expect(dialog.querySelector('button')!.getAttribute('aria-label')).toBe(labels.imageClose)
  })
})
