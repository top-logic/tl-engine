import { React } from 'tl-react-bridge'
import type { bundle } from './i18n'
import type { NoticeTone } from './types'

type Labels = ReturnType<typeof bundle>

/**
 * One line above the composer: a failure, or merely what just happened.
 *
 * Extracted so a host composition can reuse it without mounting the whole composer.
 */
export function ComposerNotice({
  text,
  detail,
  tone,
  labels,
  onDismiss,
}: {
  text: string
  detail?: string
  tone: NoticeTone
  labels: Labels
  onDismiss: () => void
}) {
  if (!text) return null
  return (
    <div className={`composer-notice${tone === 'info' ? ' is-neutral' : ''}`} role="status">
      <span title={detail ? `${labels.errorDetails}: ${detail}` : undefined}>{text}</span>
      <button type="button" className="icon-button tiny" aria-label={labels.dismiss} onClick={onDismiss}>
        <svg viewBox="0 0 16 16" aria-hidden="true">
          <path d="m4 4 8 8M12 4l-8 8" />
        </svg>
      </button>
    </div>
  )
}
