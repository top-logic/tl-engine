import type { AttachmentPolicy } from './features'
import type { bundle } from './i18n'

type Labels = ReturnType<typeof bundle>

function normalizeType(type: string): string {
  const bare = type.split(';')[0].trim().toLowerCase()
  return bare === 'image/jpg' ? 'image/jpeg' : bare
}

/** File pattern accepting every file. */
const ALL_FILES = '*.*'

/** MIME type accepting every file. */
const ALL_TYPES = '*/*'

/**
 * Whether the policy allows a file of this type, decided as the server's `AcceptedTypesChecker`
 * decides it: by the MIME type, matched exactly or by its main type (`text/*`), or by the name,
 * matched against a file pattern (`*.zip`) by its ending or against a plain name as a whole.
 */
export function typeAllowed(file: { name: string; type: string }, types: string[]): boolean {
  const accepted = types.map((type) => type.trim()).filter((type) => type.length > 0)
  if (accepted.length === 0 || accepted.includes(ALL_FILES) || accepted.includes(ALL_TYPES)) return true
  const mime = normalizeType(file.type)
  const main = `${mime.split('/')[0]}/*`
  const name = file.name.toLowerCase()
  return accepted.some((type) => {
    if (type.includes('/')) return mime !== '' && (type.toLowerCase() === mime || type.toLowerCase() === main)
    const wildcard = type.lastIndexOf('*')
    if (wildcard < 0) return type.toLowerCase() === name
    return name.endsWith(type.substring(wildcard + 1).toLowerCase())
  })
}

/** A size in bytes as the reader takes it in, e.g. `10 MB`. */
export function sizeLabel(bytes: number): string {
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let value = bytes
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit++
  }
  const shown = Number.isInteger(value) ? String(value) : value.toFixed(1)
  return `${shown} ${units[unit]}`
}

export interface AttachSelection {
  /** The files to upload. */
  usable: File[]
  /** Why the other files were left out, one sentence each. */
  problems: string[]
}

/** What a message carries already: the files attached and those on their way. */
export interface Attached {
  /** How many files. */
  count: number
  /** Their sizes together, in bytes. */
  size: number
}

/**
 * Picks out of dropped, pasted or picked files those the server would take, refusing the others.
 *
 * @param attached
 *        what the message carries already, so that the count and the size of the message cover both
 * @param policy
 *        which files a message of the conversation may carry; `null` while it is not known, and the
 *        server alone decides
 * @param labels
 *        the reader's language, for the refusal a rejected file gets
 */
export function selectAttachments(
  files: File[],
  attached: Attached,
  policy: AttachmentPolicy | null,
  labels: Labels,
): AttachSelection {
  if (policy === null) return { usable: [...files], problems: [] }
  const problems: string[] = []
  const usable: File[] = []
  let count = attached.count
  let size = attached.size

  for (const file of files) {
    if (policy.maxFiles <= 0) {
      problems.push(`${file.name}: ${labels.noAttachments}`)
      continue
    }
    if (!typeAllowed(file, policy.types)) {
      problems.push(`${file.name}: ${labels.typeNotAllowed(policy.types.join(', '))}`)
      continue
    }
    if (policy.maxFileSize > 0 && file.size > policy.maxFileSize) {
      problems.push(`${file.name}: ${labels.largerThan(sizeLabel(policy.maxFileSize))}`)
      continue
    }
    if (count >= policy.maxFiles) {
      problems.push(`${file.name}: ${labels.atMostFiles(policy.maxFiles)}`)
      continue
    }
    if (policy.maxMessageSize > 0 && size + file.size > policy.maxMessageSize) {
      problems.push(`${file.name}: ${labels.messageLargerThan(sizeLabel(policy.maxMessageSize))}`)
      continue
    }
    count++
    size += file.size
    usable.push(file)
  }
  return { usable, problems }
}

/** Whether a drag carries files, as opposed to text dragged inside the page. */
export function dragHasFiles(transfer: DataTransfer | null): boolean {
  if (!transfer) return false
  return Array.from(transfer.types ?? []).includes('Files')
}
