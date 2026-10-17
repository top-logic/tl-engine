/**
 * A new random id in the form of a version 4 UUID.
 *
 * `crypto.randomUUID` exists only in a secure context - HTTPS or `localhost`. An application served
 * over plain HTTP under another host name (an intranet installation, a test rig behind a proxy)
 * would otherwise fail at the first id and could not send a message at all.
 * `crypto.getRandomValues` exists in every context.
 */
export function randomId(): string {
  if (typeof crypto.randomUUID === 'function') return crypto.randomUUID()
  const bytes = crypto.getRandomValues(new Uint8Array(16))
  bytes[6] = (bytes[6] & 0x0f) | 0x40
  bytes[8] = (bytes[8] & 0x3f) | 0x80
  const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('')
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
}
