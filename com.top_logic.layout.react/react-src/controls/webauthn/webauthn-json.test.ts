import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  base64urlToBuffer, bufferToBase64url, credentialToJSON, failureOf, startCeremony, toCreationOptions,
  toCredentialOptions, toRequestOptions, KIND_CREATE, KIND_GET, NOT_SUPPORTED_ERROR,
} from './webauthn-json';
import type { PublicKeyCredentialLike } from './webauthn-json';

/** The bytes of the given values. */
function bytes(...values: number[]): ArrayBuffer {
  return new Uint8Array(values).buffer;
}

/** The values of the given bytes. */
function values(buffer: ArrayBuffer | BufferSource | undefined): number[] {
  return Array.from(new Uint8Array(buffer as ArrayBuffer));
}

/** Lets the window offer `PublicKeyCredential` (with the given statics) and `navigator.credentials`. */
function offerWebAuthn(statics: Record<string, unknown>, credentials: Partial<CredentialsContainer>): void {
  vi.stubGlobal('PublicKeyCredential', statics);
  Object.defineProperty(navigator, 'credentials', { configurable: true, value: credentials });
}

afterEach(() => {
  vi.unstubAllGlobals();
  delete (navigator as unknown as Record<string, unknown>).credentials;
});

describe('base64url', () => {
  it('encodes without padding and with the URL alphabet', () => {
    // 0xfb 0xff encodes to "+/8=" in plain base64.
    expect(bufferToBase64url(bytes(0xfb, 0xff))).toBe('-_8');
  });

  it('decodes what it encodes, with or without padding', () => {
    const data = bytes(0, 1, 2, 250, 251, 252, 253, 254, 255);
    expect(values(base64urlToBuffer(bufferToBase64url(data)))).toEqual(values(data));
    expect(values(base64urlToBuffer('-_8='))).toEqual([0xfb, 0xff]);
  });

  it('encodes a view of a larger buffer by the bytes it covers', () => {
    const view = new Uint8Array([9, 1, 2, 9]).subarray(1, 3);
    expect(values(base64urlToBuffer(bufferToBase64url(view)))).toEqual([1, 2]);
  });
});

describe('options without the conversion of the browser', () => {
  it('decodes the binary values of registration options', () => {
    const options = toCreationOptions({
      challenge: bufferToBase64url(bytes(1, 2, 3)),
      rp: { id: 'example.com', name: 'Example' },
      user: { id: bufferToBase64url(bytes(7)), name: 'jdoe', displayName: 'J. Doe' },
      pubKeyCredParams: [{ type: 'public-key', alg: -7 }],
      excludeCredentials: [{ type: 'public-key', id: bufferToBase64url(bytes(4, 5)), transports: ['usb'] }],
      timeout: 60000,
    });
    expect(values(options.challenge)).toEqual([1, 2, 3]);
    expect(values(options.user.id)).toEqual([7]);
    expect(options.user.name).toBe('jdoe');
    expect(options.rp.id).toBe('example.com');
    expect(values(options.excludeCredentials![0].id)).toEqual([4, 5]);
    expect(options.excludeCredentials![0].transports).toEqual(['usb']);
    expect(options.timeout).toBe(60000);
  });

  it('decodes the binary values of authentication options', () => {
    const options = toRequestOptions({
      challenge: bufferToBase64url(bytes(9)),
      rpId: 'example.com',
      allowCredentials: [{ type: 'public-key', id: bufferToBase64url(bytes(8)) }],
      userVerification: 'required',
    });
    expect(values(options.challenge)).toEqual([9]);
    expect(values(options.allowCredentials![0].id)).toEqual([8]);
    expect(options.userVerification).toBe('required');
  });

  it('keeps the properties beside publicKey and requires publicKey', () => {
    const json = JSON.stringify({ publicKey: { challenge: bufferToBase64url(bytes(1)) }, mediation: 'conditional' });
    const options = toCredentialOptions(KIND_GET, json) as CredentialRequestOptions;
    expect(options.mediation).toBe('conditional');
    expect(values(options.publicKey!.challenge)).toEqual([1]);
    expect(() => toCredentialOptions(KIND_GET, '{}')).toThrow(TypeError);
  });
});

describe('options with the conversion of the browser', () => {
  it('prefers the parse functions of PublicKeyCredential', () => {
    const parsed = { challenge: bytes(42) };
    const parseRequestOptionsFromJSON = vi.fn(() => parsed);
    offerWebAuthn({ parseRequestOptionsFromJSON }, {});
    const json = { challenge: 'Kg' };
    expect(toRequestOptions(json)).toBe(parsed);
    expect(parseRequestOptionsFromJSON).toHaveBeenCalledWith(json);
  });
});

describe('credentials', () => {
  it('prefers toJSON of the credential', () => {
    const json = { id: 'abc' };
    const credential = { toJSON: () => json } as unknown as PublicKeyCredentialLike;
    expect(credentialToJSON(credential)).toBe(json);
  });

  it('encodes a registration response', () => {
    const credential: PublicKeyCredentialLike = {
      id: 'AQI',
      rawId: bytes(1, 2),
      type: 'public-key',
      authenticatorAttachment: 'platform',
      response: { clientDataJSON: bytes(3), attestationObject: bytes(4), getTransports: () => ['internal'] },
      getClientExtensionResults: () => ({ credProps: { rk: true } }),
    };
    expect(credentialToJSON(credential)).toEqual({
      id: 'AQI',
      rawId: 'AQI',
      type: 'public-key',
      authenticatorAttachment: 'platform',
      response: { clientDataJSON: 'Aw', attestationObject: 'BA', transports: ['internal'] },
      clientExtensionResults: { credProps: { rk: true } },
    });
  });

  it('encodes an authentication response, binary extension results included', () => {
    const credential: PublicKeyCredentialLike = {
      id: 'AQ',
      rawId: bytes(1),
      type: 'public-key',
      authenticatorAttachment: null,
      response: { clientDataJSON: bytes(3), authenticatorData: bytes(4), signature: bytes(5), userHandle: bytes(6) },
      getClientExtensionResults: () => ({ prf: { results: { first: bytes(7) } } }),
    };
    expect(credentialToJSON(credential)).toEqual({
      id: 'AQ',
      rawId: 'AQ',
      type: 'public-key',
      response: { clientDataJSON: 'Aw', authenticatorData: 'BA', signature: 'BQ', userHandle: 'Bg' },
      clientExtensionResults: { prf: { results: { first: 'Bw' } } },
    });
  });

  it('leaves out an absent user handle', () => {
    const credential: PublicKeyCredentialLike = {
      id: 'AQ',
      rawId: bytes(1),
      type: 'public-key',
      response: { clientDataJSON: bytes(3), authenticatorData: bytes(4), signature: bytes(5), userHandle: null },
      getClientExtensionResults: () => ({}),
    };
    expect((credentialToJSON(credential).response as Record<string, unknown>).userHandle).toBeUndefined();
  });
});

describe('startCeremony', () => {
  const OPTIONS = JSON.stringify({ publicKey: { challenge: 'AQ', rp: { name: 'x' }, user: { id: 'Ag', name: 'u', displayName: 'U' }, pubKeyCredParams: [] } });

  it('reports a browser without WebAuthn', async () => {
    await expect(startCeremony(KIND_CREATE, OPTIONS)).rejects.toMatchObject({ name: NOT_SUPPORTED_ERROR });
  });

  it('calls the browser before it returns, with the converted options and the signal', async () => {
    const create = vi.fn(() => Promise.resolve({ toJSON: () => ({ id: 'new' }) } as unknown as Credential));
    offerWebAuthn({}, { create });
    const signal = new AbortController().signal;

    const pending = startCeremony(KIND_CREATE, OPTIONS, signal);

    expect(create).toHaveBeenCalledTimes(1);
    const options = (create.mock.calls[0] as unknown as [CredentialCreationOptions])[0];
    expect(options.signal).toBe(signal);
    expect(values(options.publicKey!.challenge)).toEqual([1]);
    expect(values(options.publicKey!.user.id)).toEqual([2]);
    await expect(pending).resolves.toEqual({ id: 'new' });
  });

  it('runs an authentication with get', async () => {
    const get = vi.fn(() => Promise.reject(new DOMException('cancelled', 'NotAllowedError')));
    offerWebAuthn({}, { get });

    await expect(startCeremony(KIND_GET, JSON.stringify({ publicKey: { challenge: 'AQ' } })))
      .rejects.toMatchObject({ name: 'NotAllowedError' });
    expect(get).toHaveBeenCalledTimes(1);
  });

  it('rejects options it cannot read without calling the browser', async () => {
    const get = vi.fn();
    offerWebAuthn({}, { get });

    await expect(startCeremony(KIND_GET, 'not json')).rejects.toBeInstanceOf(SyntaxError);
    expect(get).not.toHaveBeenCalled();
  });
});

describe('failureOf', () => {
  it('reads name and message of a DOMException', () => {
    expect(failureOf(new DOMException('timed out', 'NotAllowedError'))).toEqual({ name: 'NotAllowedError', message: 'timed out' });
  });

  it('names an error without a name', () => {
    expect(failureOf('boom')).toEqual({ name: 'UnknownError', message: 'boom' });
    expect(failureOf({})).toEqual({ name: 'UnknownError', message: '' });
  });
});
