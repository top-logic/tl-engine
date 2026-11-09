/**
 * Checks of addresses the page opens or links to: a value of the application's data becomes a link
 * or a window only where it names a document, never where it runs script in the page's origin.
 */

/**
 * Schemes whose address runs script instead of naming a document. Opened or followed, such an
 * address runs in the origin of the application, so it is neither opened nor made a link, wherever
 * it came from.
 */
const SCRIPT_SCHEMES = new Set(['javascript:', 'vbscript:', 'data:']);

/**
 * The absolute form of the given address, resolved against the address of the page, or `null`
 * where there is nothing to open: an empty address, one that is not an address, or one running
 * script.
 */
export function resolveAddress(address: string): string | null {
  if (address === '') return null;
  let resolved: URL;
  try {
    resolved = new URL(address, document.baseURI);
  } catch {
    return null;
  }
  return SCRIPT_SCHEMES.has(resolved.protocol) ? null : resolved.href;
}

/** Whether the given address runs script when opened, see {@link resolveAddress}. */
export function isScriptAddress(address: string): boolean {
  try {
    return SCRIPT_SCHEMES.has(new URL(address, document.baseURI).protocol);
  } catch {
    return false;
  }
}
