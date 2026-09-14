import type { ComposerChip } from './extensions'

/**
 * Builds the text the server stores for a send: optional context, then host chips as quotes,
 * then the user's typed words.
 *
 * There is no extra send field; {@code ChatSendRequest.text} already becomes a {@code TextPart}.
 */
export function composeOutboundText(
  typed: string,
  contextTag: string | undefined,
  chips: ComposerChip[],
): string {
  const parts: string[] = []
  const context = contextTag?.trim()
  if (context) parts.push(`<context>${context}</context>`)
  for (const chip of chips) {
    const payload = (chip.payload ?? chip.label).trim()
    if (!payload) continue
    parts.push(`<quote label="${escapeAttr(chip.label)}">${payload}</quote>`)
  }
  const body = typed.trim()
  if (body) parts.push(body)
  return parts.join('\n')
}

function escapeAttr(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;')
}
