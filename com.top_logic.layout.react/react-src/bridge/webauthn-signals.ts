// Client side of the WebAuthn signals of ReactWebAuthnControl: the server sends a FunctionCall to
// window.TLReact.webAuthnSignal (ReactWebAuthnControl.CLIENT_NAMESPACE / CLIENT_FUNCTION).

/**
 * The signal that one credential is unknown to the relying party
 * (`ReactWebAuthnControl.SIGNAL_UNKNOWN_CREDENTIAL`), options `rpId` and `credentialId`.
 */
export const SIGNAL_UNKNOWN_CREDENTIAL = 'signalUnknownCredential';

/**
 * The signal listing all credentials the relying party accepts for a user
 * (`ReactWebAuthnControl.SIGNAL_ALL_ACCEPTED_CREDENTIALS`), options `rpId`, `userId` and
 * `allAcceptedCredentialIds`.
 */
export const SIGNAL_ALL_ACCEPTED_CREDENTIALS = 'signalAllAcceptedCredentials';

/** The signals the server may send; each is the name of a static function of `PublicKeyCredential`. */
const SIGNALS: ReadonlySet<string> = new Set([SIGNAL_UNKNOWN_CREDENTIAL, SIGNAL_ALL_ACCEPTED_CREDENTIALS]);

/**
 * Passes a signal of the server to the WebAuthn Signal API of the browser: calls the static function
 * of `PublicKeyCredential` the signal names with the given options (binary values base64url-encoded,
 * as the Signal API expects them).
 *
 * <p>A signal is a hint to the authenticator or password manager, nothing the page waits for: a
 * browser without the Signal API (or without this function of it) ignores the signal, a refused call
 * (e.g. an `rpId` that does not match the page) is logged at debug level only. A name that is no
 * known signal is ignored as well, so the server cannot call other functions through it.</p>
 *
 * @param signal The name of the signal, e.g. {@link SIGNAL_UNKNOWN_CREDENTIAL}.
 * @param options The options of the signal.
 * @returns Whether the browser accepted the signal.
 */
export async function webAuthnSignal(signal: string, options: Record<string, unknown>): Promise<boolean> {
  if (!SIGNALS.has(signal)) {
    console.warn('[TLReact] Unknown WebAuthn signal:', signal);
    return false;
  }
  const api = (globalThis as Record<string, unknown>).PublicKeyCredential as Record<string, unknown> | undefined;
  const fn = api?.[signal];
  if (typeof fn !== 'function') {
    return false;
  }
  try {
    await (fn as (options: Record<string, unknown>) => Promise<void>).call(api, options);
    return true;
  } catch (error) {
    console.debug('[TLReact] WebAuthn signal refused:', signal, error);
    return false;
  }
}
