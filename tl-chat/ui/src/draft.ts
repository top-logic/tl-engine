/**
 * When the composer tells its host what stands unsent in it.
 *
 * A draft reaches the host over a channel, and a channel write is a round trip to the server. A
 * keystroke is not worth one, so an ordinary report waits for the typing to stop. The two moments
 * that must not wait are the ones where the host is about to act on what it reads: the field losing
 * the focus, and the send itself - before it, and once it has emptied the field.
 */
export interface DraftReporter {
  /** Reports the draft after the pause. A later report before the pause is over replaces it. */
  report: (text: string) => void
  /** Reports the draft now, cancelling a pending report - on blur, and before a send. */
  flush: (text: string) => void
  /** Drops a pending report without sending it, e.g. when the composer goes away. */
  cancel: () => void
}

/** How long the typing has to stop before the draft is reported, in milliseconds. */
export const DRAFT_PAUSE = 300

/**
 * A {@link DraftReporter} that publishes through `publish`.
 *
 * The same text is never published twice in a row: the composer's own state changes for reasons
 * that leave the text alone, and a host that sees a write it already has learns nothing.
 */
export function createDraftReporter(
  publish: (text: string) => void,
  delay: number = DRAFT_PAUSE,
): DraftReporter {
  let timer: ReturnType<typeof setTimeout> | null = null
  let pending: string | null = null
  let published: string | null = null

  const send = (text: string) => {
    if (published === text) return
    published = text
    publish(text)
  }

  const cancel = () => {
    if (timer != null) clearTimeout(timer)
    timer = null
    pending = null
  }

  return {
    report: (text: string) => {
      pending = text
      if (timer != null) clearTimeout(timer)
      timer = setTimeout(() => {
        timer = null
        const due = pending
        pending = null
        if (due != null) send(due)
      }, delay)
    },
    flush: (text: string) => {
      cancel()
      send(text)
    },
    cancel,
  }
}
