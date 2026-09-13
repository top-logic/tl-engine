import type { ModelOption } from './types'

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
  const parts = id.split('-')
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

/** The effort levels the given model offers, empty when it offers none. */
export function effortLevelsOf(models: ModelOption[], modelId: string): string[] {
  return models.find((item) => item.id === modelId)?.effortLevels ?? []
}

/** An effort level written for a reader: "high" becomes "High". */
export function effortLabel(level: string): string {
  return level ? level.charAt(0).toUpperCase() + level.slice(1) : ''
}
