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
 * ({@link ReactWebAuthnControl#setOptions(String)}). The control has no error display: the listener
 * tells the user about a failure, e.g. through the messages of the window, and explains a button
 * that cannot be used in its {@link ReactWebAuthnControl#setTooltip(String) tooltip}. Before
 * offering a ceremony, it can check the {@link #onPageOrigin(ReactContext, String) origin of the
 * page} against the origins its relying party accepts.
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
	 * Called when the ceremony failed or was cancelled by the user.
	 *
	 * @param context
	 *        The context of the control that ran the ceremony.
	 * @param errorName
	 *        The name of the {@code DOMException} the browser reported, e.g.
	 *        {@code NotAllowedError} (cancelled, timed out, or no user activation),
	 *        {@code InvalidStateError} (the authenticator already holds an excluded credential),
	 *        {@code SecurityError} (the relying party ID does not match the page), or
	 *        {@code NotSupportedError} (the browser or the authenticator cannot handle the
	 *        options). A page that cannot run a ceremony at all reports
	 *        {@link ReactWebAuthnControl#INSECURE_CONTEXT_ERROR} or
	 *        {@link ReactWebAuthnControl#WEB_AUTHN_UNAVAILABLE_ERROR}; it is announced before by
	 *        {@link #onUnavailable(ReactContext, String, String)}.
	 * @param message
	 *        The technical message of the browser, not localized; may be empty.
	 */
	void onFailure(ReactContext context, String errorName, String message);

	/**
	 * Called once when the control is displayed on a page that cannot run a ceremony; the client
	 * shows the button disabled.
	 *
	 * <p>
	 * Does nothing by default. A listener explains the cause to the user, e.g. in the
	 * {@link ReactWebAuthnControl#setTooltip(String) tooltip} of the button.
	 * </p>
	 *
	 * @param context
	 *        The context of the control.
	 * @param errorName
	 *        {@link ReactWebAuthnControl#INSECURE_CONTEXT_ERROR} for a page that is no secure
	 *        context, {@link ReactWebAuthnControl#WEB_AUTHN_UNAVAILABLE_ERROR} for a browser without
	 *        Web Authentication.
	 * @param message
	 *        The technical message of the client, not localized; may be empty.
	 */
	default void onUnavailable(ReactContext context, String errorName, String message) {
		// The disabled button is all the user sees.
	}

	/**
	 * Called once when the control is displayed, with the origin of the page that displays it.
	 *
	 * <p>
	 * A ceremony on this page reports this origin to the relying party, which refuses a credential
	 * from an origin it does not accept - for a registration only after the authenticator has
	 * already stored the key. A listener therefore compares the origin with the origins its relying
	 * party accepts before offering a ceremony; for a page that does not match, it withholds the
	 * {@link ReactWebAuthnControl#setOptions(String) options} or
	 * {@link ReactWebAuthnControl#setDisabled(boolean) disables} the button and explains why in its
	 * {@link ReactWebAuthnControl#setTooltip(String) tooltip}. The origin stays available as
	 * {@link ReactWebAuthnControl#getPageOrigin()}, e.g. for options offered later.
	 * </p>
	 *
	 * <p>
	 * Reported also while the control is disabled or hidden, and before
	 * {@link #onUnavailable(ReactContext, String, String)} on a page that cannot run a ceremony. Does
	 * nothing by default.
	 * </p>
	 *
	 * @param context
	 *        The context of the control.
	 * @param origin
	 *        The origin of the page, e.g. {@code https://app.example.com}, as sent by the client.
	 */
	default void onPageOrigin(ReactContext context, String origin) {
		// A listener that accepts every origin has nothing to check.
	}

	/**
	 * Called shortly before the options of the control expire, while no ceremony runs.
	 *
	 * <p>
	 * The client asks for fresh options when the {@code publicKey.timeout} of the current ones has
	 * nearly passed. A listener whose challenges expire gives the control fresh options with
	 * {@link ReactWebAuthnControl#setOptions(String)}; it should not show an error, since the user
	 * did nothing. Does nothing by default.
	 * </p>
	 *
	 * @param context
	 *        The context of the control.
	 */
	default void onExpired(ReactContext context) {
		// Options without expiry need no refresh.
	}

}
