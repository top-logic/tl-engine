import { afterEach, describe, expect, it, vi } from 'vitest';
import { webAuthnSignal, SIGNAL_ALL_ACCEPTED_CREDENTIALS, SIGNAL_UNKNOWN_CREDENTIAL } from './webauthn-signals';

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('webAuthnSignal', () => {
  it('calls signalUnknownCredential with the options of the server', async () => {
    const signalUnknownCredential = vi.fn().mockResolvedValue(undefined);
    vi.stubGlobal('PublicKeyCredential', { signalUnknownCredential });

    const options = { rpId: 'app.example.com', credentialId: 'AQID' };
    await expect(webAuthnSignal(SIGNAL_UNKNOWN_CREDENTIAL, options)).resolves.toBe(true);

    expect(signalUnknownCredential).toHaveBeenCalledWith(options);
  });

  it('calls signalAllAcceptedCredentials with the options of the server', async () => {
    const signalAllAcceptedCredentials = vi.fn().mockResolvedValue(undefined);
    vi.stubGlobal('PublicKeyCredential', { signalAllAcceptedCredentials });

    const options = { rpId: 'app.example.com', userId: 'dXNlcg', allAcceptedCredentialIds: ['AQ', 'Ag'] };
    await expect(webAuthnSignal(SIGNAL_ALL_ACCEPTED_CREDENTIALS, options)).resolves.toBe(true);

    expect(signalAllAcceptedCredentials).toHaveBeenCalledWith(options);
  });

  it('ignores a browser without the Signal API', async () => {
    vi.stubGlobal('PublicKeyCredential', {});
    await expect(webAuthnSignal(SIGNAL_UNKNOWN_CREDENTIAL, { rpId: 'x', credentialId: 'AQ' })).resolves.toBe(false);
  });

  it('ignores a browser without WebAuthn', async () => {
    vi.stubGlobal('PublicKeyCredential', undefined);
    await expect(webAuthnSignal(SIGNAL_UNKNOWN_CREDENTIAL, { rpId: 'x', credentialId: 'AQ' })).resolves.toBe(false);
  });

  it('swallows a refused signal', async () => {
    const debug = vi.spyOn(console, 'debug').mockImplementation(() => undefined);
    const signalUnknownCredential = vi.fn().mockRejectedValue(new DOMException('No match', 'SecurityError'));
    vi.stubGlobal('PublicKeyCredential', { signalUnknownCredential });

    await expect(webAuthnSignal(SIGNAL_UNKNOWN_CREDENTIAL, { rpId: 'x', credentialId: 'AQ' })).resolves.toBe(false);
    expect(debug).toHaveBeenCalled();
  });

  it('calls no other function of PublicKeyCredential', async () => {
    vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    const isConditionalMediationAvailable = vi.fn();
    vi.stubGlobal('PublicKeyCredential', { isConditionalMediationAvailable });

    await expect(webAuthnSignal('isConditionalMediationAvailable', {})).resolves.toBe(false);
    expect(isConditionalMediationAvailable).not.toHaveBeenCalled();
  });
});
