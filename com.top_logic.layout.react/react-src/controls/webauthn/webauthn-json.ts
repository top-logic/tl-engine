/**
 * Conversion between the JSON form of WebAuthn options and credentials (binary values as base64url
 * text, as a WebAuthn server library reads and writes them) and the browser's binary form
 * (`ArrayBuffer`s, as `navigator.credentials` takes and returns them).
 *
 * <p>Where the browser offers the conversion itself (`PublicKeyCredential.parseCreationOptionsFromJSON`,
 * `parseRequestOptionsFromJSON`, `PublicKeyCredential.prototype.toJSON`), it is used; otherwise the
 * binary values defined by the WebAuthn specification are converted here.</p>
 */

/** The ceremony kinds, as the server names them. */
export type WebAuthnKind = 'create' | 'get';

/** The kind of a registration ceremony (`navigator.credentials.create`). */
export const KIND_CREATE: WebAuthnKind = 'create';

/** The kind of an authentication ceremony (`navigator.credentials.get`). */
export const KIND_GET: WebAuthnKind = 'get';

/** The property of the options JSON holding the public key options. */
export const PUBLIC_KEY = 'publicKey';

/** Encodes bytes as base64url text without padding. */
export function bufferToBase64url(buffer: ArrayBuffer | ArrayBufferView): string {
  const bytes = buffer instanceof ArrayBuffer
    ? new Uint8Array(buffer)
    : new Uint8Array(buffer.buffer, buffer.byteOffset, buffer.byteLength);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** Decodes base64url text (with or without padding) into bytes. */
export function base64urlToBuffer(text: string): ArrayBuffer {
  const base64 = text.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
  const binary = atob(padded);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

/** The static conversion methods of `PublicKeyCredential`, where the browser has them. */
interface PublicKeyCredentialStatics {
  parseCreationOptionsFromJSON?: (options: PublicKeyCredentialCreationOptionsJSON) => PublicKeyCredentialCreationOptions;
  parseRequestOptionsFromJSON?: (options: PublicKeyCredentialRequestOptionsJSON) => PublicKeyCredentialRequestOptions;
}

/** `PublicKeyCredential` of the window, `undefined` in a browser without WebAuthn. */
function publicKeyCredentialClass(): PublicKeyCredentialStatics | undefined {
  return (globalThis as { PublicKeyCredential?: PublicKeyCredentialStatics }).PublicKeyCredential;
}

/** Whether the browser offers WebAuthn at all. */
export function isWebAuthnSupported(): boolean {
  return publicKeyCredentialClass() !== undefined
    && typeof navigator !== 'undefined'
    && navigator.credentials !== undefined;
}

function toDescriptors(list: PublicKeyCredentialDescriptorJSON[] | undefined): PublicKeyCredentialDescriptor[] | undefined {
  return list?.map(descriptor => ({
    ...descriptor,
    type: descriptor.type as PublicKeyCredentialType,
    id: base64urlToBuffer(descriptor.id),
    transports: descriptor.transports as AuthenticatorTransport[] | undefined,
  }));
}

/** Converts the JSON form of registration options into the form `navigator.credentials.create` takes. */
export function toCreationOptions(json: PublicKeyCredentialCreationOptionsJSON): PublicKeyCredentialCreationOptions {
  const parse = publicKeyCredentialClass()?.parseCreationOptionsFromJSON;
  if (parse) {
    return parse(json);
  }
  const { challenge, user, excludeCredentials, ...rest } = json;
  return {
    ...rest,
    challenge: base64urlToBuffer(challenge),
    user: { ...user, id: base64urlToBuffer(user.id) },
    excludeCredentials: toDescriptors(excludeCredentials),
  } as PublicKeyCredentialCreationOptions;
}

/** Converts the JSON form of authentication options into the form `navigator.credentials.get` takes. */
export function toRequestOptions(json: PublicKeyCredentialRequestOptionsJSON): PublicKeyCredentialRequestOptions {
  const parse = publicKeyCredentialClass()?.parseRequestOptionsFromJSON;
  if (parse) {
    return parse(json);
  }
  const { challenge, allowCredentials, ...rest } = json;
  return {
    ...rest,
    challenge: base64urlToBuffer(challenge),
    allowCredentials: toDescriptors(allowCredentials),
  } as PublicKeyCredentialRequestOptions;
}

/**
 * The options of `navigator.credentials.create` (kind `create`) or `navigator.credentials.get`
 * (kind `get`) from the options JSON of the server: an object holding the JSON form of the public
 * key options in {@link PUBLIC_KEY}; further properties (e.g. `mediation`) are taken as they are.
 */
export function toCredentialOptions(kind: WebAuthnKind, optionsJson: string): CredentialCreationOptions | CredentialRequestOptions {
  const parsed = JSON.parse(optionsJson) as Record<string, unknown>;
  const { [PUBLIC_KEY]: publicKey, ...rest } = parsed;
  if (publicKey == null || typeof publicKey !== 'object') {
    throw new TypeError('WebAuthn options lack the property "' + PUBLIC_KEY + '".');
  }
  if (kind === KIND_CREATE) {
    return { ...rest, publicKey: toCreationOptions(publicKey as PublicKeyCredentialCreationOptionsJSON) };
  }
  return { ...rest, publicKey: toRequestOptions(publicKey as PublicKeyCredentialRequestOptionsJSON) };
}

/** Encodes every binary value in a structure of extension results. */
function encodeBinaries(value: unknown): unknown {
  if (value instanceof ArrayBuffer || ArrayBuffer.isView(value)) {
    return bufferToBase64url(value as ArrayBuffer | ArrayBufferView);
  }
  if (Array.isArray(value)) {
    return value.map(encodeBinaries);
  }
  if (value !== null && typeof value === 'object') {
    const result: Record<string, unknown> = {};
    for (const [key, entry] of Object.entries(value)) {
      result[key] = encodeBinaries(entry);
    }
    return result;
  }
  return value;
}

/** The part of `AuthenticatorAttestationResponse` read here. */
interface AttestationResponseLike {
  clientDataJSON: ArrayBuffer;
  attestationObject: ArrayBuffer;
  getTransports?: () => string[];
}

/** The part of `AuthenticatorAssertionResponse` read here. */
interface AssertionResponseLike {
  clientDataJSON: ArrayBuffer;
  authenticatorData: ArrayBuffer;
  signature: ArrayBuffer;
  userHandle: ArrayBuffer | null;
}

/** The part of `PublicKeyCredential` read here. */
export interface PublicKeyCredentialLike {
  id: string;
  rawId: ArrayBuffer;
  type: string;
  authenticatorAttachment?: string | null;
  response: AttestationResponseLike | AssertionResponseLike;
  getClientExtensionResults(): unknown;
  toJSON?: () => unknown;
}

/**
 * The JSON form of a credential in the shape of `PublicKeyCredential.toJSON()`, all binary values
 * base64url-encoded.
 */
export function credentialToJSON(credential: PublicKeyCredentialLike): Record<string, unknown> {
  if (typeof credential.toJSON === 'function') {
    return credential.toJSON() as Record<string, unknown>;
  }
  const source = credential.response;
  let response: Record<string, unknown>;
  if ('attestationObject' in source) {
    response = {
      clientDataJSON: bufferToBase64url(source.clientDataJSON),
      attestationObject: bufferToBase64url(source.attestationObject),
      transports: typeof source.getTransports === 'function' ? source.getTransports() : [],
    };
  } else {
    response = {
      clientDataJSON: bufferToBase64url(source.clientDataJSON),
      authenticatorData: bufferToBase64url(source.authenticatorData),
      signature: bufferToBase64url(source.signature),
    };
    if (source.userHandle) {
      response.userHandle = bufferToBase64url(source.userHandle);
    }
  }
  const result: Record<string, unknown> = {
    id: credential.id,
    rawId: bufferToBase64url(credential.rawId),
    type: credential.type,
    response,
    clientExtensionResults: encodeBinaries(credential.getClientExtensionResults() ?? {}),
  };
  if (credential.authenticatorAttachment) {
    result.authenticatorAttachment = credential.authenticatorAttachment;
  }
  return result;
}

/** The error name reported for a browser without WebAuthn. */
export const NOT_SUPPORTED_ERROR = 'NotSupportedError';

/** The error name reported for an error that carries no name. */
export const UNKNOWN_ERROR = 'UnknownError';

/** The name and the technical message of a failed ceremony. */
export interface CeremonyFailure {
  name: string;
  message: string;
}

/** The name and message of an error thrown by a ceremony, a `DOMException` in the regular case. */
export function failureOf(error: unknown): CeremonyFailure {
  if (error !== null && typeof error === 'object') {
    const { name, message } = error as { name?: unknown; message?: unknown };
    return {
      name: typeof name === 'string' && name !== '' ? name : UNKNOWN_ERROR,
      message: typeof message === 'string' ? message : '',
    };
  }
  return { name: UNKNOWN_ERROR, message: error == null ? '' : String(error) };
}

/**
 * Starts a ceremony and resolves with the JSON form of the resulting credential.
 *
 * <p>`navigator.credentials` is called before this function returns, not after an await: a browser
 * requiring a user gesture (Safari) accepts the ceremony only while the gesture is still in
 * progress, so this function must be called directly in the event handler of the gesture.</p>
 *
 * <p>The promise rejects with the error of the browser, with a {@link NOT_SUPPORTED_ERROR} in a
 * browser without WebAuthn, and with a `TypeError` for options that cannot be read.</p>
 *
 * @param kind The ceremony to run.
 * @param optionsJson The options JSON of the server, see {@link toCredentialOptions}.
 * @param signal Aborts the ceremony.
 */
export function startCeremony(kind: WebAuthnKind, optionsJson: string, signal?: AbortSignal): Promise<Record<string, unknown>> {
  if (!isWebAuthnSupported()) {
    return Promise.reject(new DOMException('WebAuthn is not supported by this browser.', NOT_SUPPORTED_ERROR));
  }
  let pending: Promise<Credential | null>;
  try {
    const options = { ...toCredentialOptions(kind, optionsJson), signal };
    pending = kind === KIND_CREATE
      ? navigator.credentials.create(options as CredentialCreationOptions)
      : navigator.credentials.get(options as CredentialRequestOptions);
  } catch (error) {
    return Promise.reject(error);
  }
  return pending.then(credential => {
    if (credential === null) {
      throw new DOMException('The browser returned no credential.', 'NotAllowedError');
    }
    return credentialToJSON(credential as unknown as PublicKeyCredentialLike);
  });
}
