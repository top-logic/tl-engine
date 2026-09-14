import type { bundle } from './i18n'
import type { ModelOption } from './types'

type Labels = ReturnType<typeof bundle>

/**
 * A model id written the way a person would say it.
 *
 * The capabilities endpoint has no display name - the model catalogue records ids and limits, not
 * labels - so the id is the only source, and it is well formed enough to read: segments separated
 * by dashes, the trailing ones a version. "claude-sonnet-4-6" becomes "Claude Sonnet 4.6". An id
 * that does not fit the shape is shown as it is rather than mangled.
 */
export function modelLabel(id: string): string {
  if (!id) return ''
  // A trailing eight-digit segment is the catalogue's release date, not part of the name:
  // "claude-haiku-4-5-20251001" is Haiku 4.5, not Haiku 4.5.20251001.
  const parts = id.replace(/-\d{8}$/, '').split('-')
  const words: string[] = []
  const version: string[] = []
  for (const part of parts) {
    if (/^\d+$/.test(part) && words.length > 0) {
      version.push(part)
      continue
    }
    if (version.length > 0) {
      // A word after the version number is part of the name again, e.g. "gpt-4-turbo".
      words.push(...version.splice(0), capitalize(part))
      continue
    }
    words.push(capitalize(part))
  }
  const name = words.join(' ')
  return version.length ? `${name} ${version.join('.')}` : name
}

function capitalize(word: string): string {
  if (!word) return word
  // Known acronyms stay upper case; everything else gets one capital.
  if (/^(gpt|xai|ai|llm|o\d+)$/i.test(word)) return word.toUpperCase()
  return word.charAt(0).toUpperCase() + word.slice(1)
}

/**
 * A tool id written the way a person would say it.
 *
 * Tools have no display name of their own - the registry records an id and an English description
 * written for the model - so the id is read out instead: the factory prefix is dropped and the
 * camel case is broken up, turning `model-modification.createModule` into "Create module". The id
 * stays available as a tooltip, the same way a model id does.
 *
 * A run of capitals survives: it is a name, not a sentence. Folding the whole id to lower case read
 * `executeTLScript` out as "Execute tlscript".
 */
export function toolLabel(id: string): string {
  if (!id) return ''
  const bare = id.includes('.') ? id.slice(id.lastIndexOf('.') + 1) : id
  const words = bare
    .replace(/[_-]+/g, ' ')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
  if (!words.length) return id
  return words
    .map((word, index) => {
      if (/[A-Z]{2}/.test(word)) return word
      const lower = word.toLowerCase()
      return index === 0 ? lower.charAt(0).toUpperCase() + lower.slice(1) : lower
    })
    .join(' ')
}

/** The effort levels the given model offers, empty when it offers none. */
export function effortLevelsOf(models: ModelOption[], modelId: string): string[] {
  return models.find((item) => item.id === modelId)?.effortLevels ?? []
}

/**
 * An effort level in the reader's language.
 *
 * The levels themselves are the providers' own words and never reach the user: they are keys into
 * the bundle, so "high" reads as "Lang" in a German UI rather than as "High".
 */
export function effortLabel(level: string, labels: Labels): string {
  switch (level) {
    case 'low':
      return labels.effortLow
    case 'medium':
      return labels.effortMedium
    case 'high':
      return labels.effortHigh
    case 'max':
      return labels.effortMax
    default:
      return level
  }
}
