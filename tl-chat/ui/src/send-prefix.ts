/**
 * Builds the text the server stores for a send: the optional context prefix, then the draft.
 *
 * The draft already carries its references inline - `<quote label="…" ref="…">text</quote>` where
 * the user put them - so nothing is prepended on their behalf any more. There is no extra send
 * field; {@code ChatSendRequest.text} already becomes a {@code TextPart}.
 */
export function composeOutboundText(draft: string, contextTag: string | undefined): string {
  const context = contextTag?.trim()
  const body = draft.trim()
  if (!context) return body
  return body ? `<context>${context}</context>\n${body}` : `<context>${context}</context>`
}
