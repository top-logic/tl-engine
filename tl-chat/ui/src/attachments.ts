import type { bundle } from './i18n'

type Labels = ReturnType<typeof bundle>

/**
 * What the server accepts, restated here so a file is refused before it is uploaded.
 *
 * The authority is `ChatAttachments.ALLOWED_MIME_TYPES` (built from `ImageContent.ACCEPTED_MEDIA_TYPES`
 * plus PDF); this list only spares the user a round trip. A type that gets through here and is
 * refused there is answered with the server's sentence.
 */
export const ACCEPTED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp', 'application/pdf']

/** Matches `ChatAttachments.MAX_ATTACHMENTS`. */
export const MAX_FILES = 5

/** Matches `ChatAttachments.MAX_ATTACHMENT_SIZE`. */
export const MAX_FILE_BYTES = 10 * 1024 * 1024

/** The `accept` attribute of the file input, kept in step with {@link ACCEPTED_TYPES}. */
export const ACCEPT_ATTRIBUTE = ACCEPTED_TYPES.join(',')

function normalizeType(type: string): string {
  const bare = type.split(';')[0].trim().toLowerCase()
  return bare === 'image/jpg' ? 'image/jpeg' : bare
}

export interface AttachSelection {
  /** The files to upload. */
  usable: File[]
  /** Why the other files were left out, one sentence each. */
  problems: string[]
}

/**
 * Picks out of dropped, pasted or picked files those the server would take, refusing the others.
 *
 * @param attached
 *        how many files are attached to the message already, so the count limit covers both
 * @param labels
 *        the reader's language, for the refusal a rejected file gets
 */
export function selectAttachments(files: File[], attached: number, labels: Labels): AttachSelection {
  const problems: string[] = []
  const usable: File[] = []
  let free = MAX_FILES - attached

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
  return { usable, problems }
}

/** Whether a drag carries files, as opposed to text dragged inside the page. */
export function dragHasFiles(transfer: DataTransfer | null): boolean {
  if (!transfer) return false
  return Array.from(transfer.types ?? []).includes('Files')
}
