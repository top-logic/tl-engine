import { React } from 'tl-react-bridge'
import type { bundle } from './i18n'

const { useEffect, useRef } = React

type Labels = ReturnType<typeof bundle>

/** Marks a picture of an answer that opens in full size; the click handler looks for it. */
export const IMAGE_PREVIEW_ATTR = 'data-image-preview'

/** The class of a picture of an answer drawn as a thumbnail. */
export const THUMBNAIL_CLASS = 'md-thumb'

/** The picture shown in full size, and the thumbnail it was opened from. */
export interface PreviewImage {
  src: string
  alt: string
  /** Where the focus goes back to when the preview closes. */
  trigger: HTMLElement | null
}

/**
 * Turns a sanitized `img` into a thumbnail that opens in full size: a `DOMPurify`
 * `afterSanitizeAttributes` hook, so it applies to every picture of an answer - a markdown image as
 * well as one written as HTML - after the filter decided what stays.
 *
 * A picture inside a link is left alone: the link decides what a click on it does.
 */
export function previewHook(labels: Pick<Labels, 'imageEnlarge'>): (node: Element) => void {
  return (node: Element) => {
    if (node.nodeName !== 'IMG' || node.closest('a')) return
    node.classList.add(THUMBNAIL_CLASS)
    node.setAttribute(IMAGE_PREVIEW_ATTR, '')
    node.setAttribute('role', 'button')
    node.setAttribute('tabindex', '0')
    node.setAttribute('title', labels.imageEnlarge)
  }
}

/** The thumbnail an event happened on, or `null`. */
export function previewTarget(event: Event): HTMLImageElement | null {
  return (event.target as HTMLElement | null)?.closest<HTMLImageElement>(`img[${IMAGE_PREVIEW_ATTR}]`) ?? null
}

/** What a thumbnail opens: its source and its alternative text. */
export function previewOf(image: HTMLImageElement): PreviewImage {
  return { src: image.getAttribute('src') ?? '', alt: image.getAttribute('alt') ?? '', trigger: image }
}

/**
 * A picture of an answer in full size, in a modal dialog over the page.
 *
 * The browser's own `<dialog>`: it keeps the focus inside while it is open and closes on Escape.
 * A click beside the picture closes it as well, and the focus goes back to the thumbnail.
 */
export function ImagePreview({
  image,
  labels,
  onClose,
}: {
  image: PreviewImage
  labels: Pick<Labels, 'imageClose' | 'imageEnlarge'>
  onClose: () => void
}) {
  const dialog = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const el = dialog.current
    if (!el) return undefined
    if (typeof el.showModal === 'function') {
      if (!el.open) el.showModal()
    } else {
      el.setAttribute('open', '')
    }
    return () => {
      if (typeof el.close === 'function' && el.open) el.close()
    }
  }, [])

  return (
    <dialog
      ref={dialog}
      className="md-image-preview"
      aria-label={image.alt || labels.imageEnlarge}
      onCancel={(event) => {
        // Escape: closed by the component, not by the browser, so the state follows.
        event.preventDefault()
        onClose()
      }}
      onKeyDown={(event) => {
        if (event.key === 'Escape') {
          event.preventDefault()
          event.stopPropagation()
          onClose()
        }
      }}
      onClick={(event) => {
        // The dialog itself is the backdrop around the picture.
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <button type="button" className="md-image-close" aria-label={labels.imageClose} autoFocus onClick={onClose}>
        ×
      </button>
      <img className="md-image-full" src={image.src} alt={image.alt} />
    </dialog>
  )
}
