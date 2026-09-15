import { fileToAttachment } from './api'
import type { bundle } from './i18n'
import type { Attachment } from './types'

type Labels = ReturnType<typeof bundle>

/**
 * What the server accepts, restated here so a file is refused before it is read and uploaded.
 *
 * The authority is `ChatServlet.ALLOWED_MIME_TYPES` (built from `ImageContent.ACCEPTED_MEDIA_TYPES`
 * plus PDF); this list only spares the user a round trip and a 400. A type that gets through here
 * and is refused there still fails safely.
 */
export const ACCEPTED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp', 'application/pdf']

/** Matches `ChatServlet.MAX_ATTACHMENTS`. */
export const MAX_FILES = 5

/** Matches `ChatServlet.MAX_ATTACHMENT_SIZE`. */
export const MAX_FILE_BYTES = 10 * 1024 * 1024

/** The `accept` attribute of the file input, kept in step with {@link ACCEPTED_TYPES}. */
export const ACCEPT_ATTRIBUTE = ACCEPTED_TYPES.join(',')

function normalizeType(type: string): string {
  const bare = type.split(';')[0].trim().toLowerCase()
  return bare === 'image/jpg' ? 'image/jpeg' : bare
}

export interface AttachResult {
  attachments: Attachment[]
  /** Why some files were left out, ready to show; empty when everything was taken. */
  rejected: string
}

/**
 * Turns dropped, pasted or picked files into attachments, refusing what the server would refuse.
 *
 * @param current
 *        attachments already on the composer, so the count limit covers both
 * @param labels
 *        the reader's language, for the refusal a rejected file gets
 */
export async function collectAttachments(
  files: File[],
  current: Attachment[],
  labels: Labels,
): Promise<AttachResult> {
  const problems: string[] = []
  const usable: File[] = []
  let free = MAX_FILES - current.length

  for (const file of files) {
    if (!ACCEPTED_TYPES.includes(normalizeType(file.type))) {
      problems.push(`${file.name}: ${labels.onlyImagesAndPdf}`)
      continue
    }
    if (file.size > MAX_FILE_BYTES) {
      problems.push(`${file.name}: ${labels.largerThan10Mb}`)
      continue
    }
    if (free <= 0) {
      problems.push(`${file.name}: ${labels.atMostFiles(MAX_FILES)}`)
      continue
    }
    free--
    usable.push(file)
  }

  const attachments = await Promise.all(usable.map(fileToAttachment))
  return { attachments, rejected: problems.join(' · ') }
}

/** Whether a drag carries files, as opposed to text dragged inside the page. */
export function dragHasFiles(transfer: DataTransfer | null): boolean {
  if (!transfer) return false
  return Array.from(transfer.types ?? []).includes('Files')
}
