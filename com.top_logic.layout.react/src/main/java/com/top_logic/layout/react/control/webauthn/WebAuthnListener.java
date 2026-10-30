/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.webauthn;

import com.top_logic.layout.react.ReactContext;

/**
 * Receives the outcome of a ceremony run by a {@link ReactWebAuthnControl}.
 *
 * <p>
 * The listener is the owner of the ceremony: it supplied the options (and keeps the challenge they
 * carry), so it verifies the credential. To retry after a failure, it gives the control fresh options
 * ({@link ReactWebAuthnControl#setOptions(String)}) and, if the user is to be told, an
 * {@link ReactWebAuthnControl#setError(String) error message}.
 * </p>
 */
public interface WebAuthnListener {

	/**
	 * Called when the browser completed the ceremony.
	 *
	 * @param context
	 *        The context of the control that ran the ceremony.
	 * @param credentialJson
	 *        The credential as JSON in the shape of {@code PublicKeyCredential.toJSON()}: the
	 *        properties {@code id}, {@code rawId}, {@code type}, {@code response},
	 *        {@code clientExtensionResults} and {@code authenticatorAttachment}, all binary values
	 *        base64url-encoded. The response holds {@code clientDataJSON} and
	 *        {@code attestationObject} (and {@code transports}) for a
	 *        {@link WebAuthnCeremony#CREATE registration}, {@code clientDataJSON},
	 *        {@code authenticatorData}, {@code signature} and {@code userHandle} for an
	 *        {@link WebAuthnCeremony#GET authentication}. The value is sent by the client and must be
	 *        verified against the challenge of the options.
	 */
	void onCredential(ReactContext context, String credentialJson);

	/**
	 * Called when the ceremony failed, was cancelled by the user or is not supported by the browser.
	 *
	 * @param context
	 *        The context of the control that ran the ceremony.
	 * @param errorName
	 *        The name of the {@code DOMException} the browser reported, e.g.
	 *        {@code NotAllowedError} (cancelled, timed out, or no user activation),
	 *        {@code InvalidStateError} (the authenticator already holds an excluded credential),
	 *        {@code SecurityError} (the relying party ID does not match the page), or
	 *        {@code NotSupportedError} (the browser offers no Web Authentication).
	 * @param message
	 *        The technical message of the browser, not localized; may be empty.
	 */
	void onFailure(ReactContext context, String errorName, String message);

}
