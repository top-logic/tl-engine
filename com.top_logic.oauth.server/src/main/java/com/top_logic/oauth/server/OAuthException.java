/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import com.nimbusds.oauth2.sdk.ErrorObject;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * A request the {@link OAuthAuthorizationServer} refuses, with the OAuth error to answer it with.
 *
 * <p>
 * The {@link #getErrorKey() message} names the reason; it is resolved in the log locale for the
 * <code>error_description</code> of the answer and for the log, see {@link #toErrorObject()}. It
 * never contains a presented secret.
 * </p>
 */
public class OAuthException extends I18NException {

	private final ErrorObject _error;

	private final boolean _redirectable;

	/**
	 * Creates a {@link OAuthException} that is answered directly, never by a redirect to the
	 * client.
	 *
	 * @param error
	 *        The OAuth error, e.g. {@link com.nimbusds.oauth2.sdk.OAuth2Error#INVALID_GRANT}.
	 * @param reason
	 *        What exactly is wrong.
	 */
	public OAuthException(ErrorObject error, ResKey reason) {
		this(error, reason, false);
	}

	/**
	 * Creates a {@link OAuthException}.
	 *
	 * @param error
	 *        The OAuth error, e.g. {@link com.nimbusds.oauth2.sdk.OAuth2Error#INVALID_GRANT}.
	 * @param reason
	 *        What exactly is wrong.
	 * @param redirectable
	 *        See {@link #isRedirectable()}.
	 */
	public OAuthException(ErrorObject error, ResKey reason, boolean redirectable) {
		super(reason);
		_error = error;
		_redirectable = redirectable;
	}

	/**
	 * Creates a {@link OAuthException} caused by another problem, answered directly.
	 *
	 * @param error
	 *        The OAuth error, e.g. {@link com.nimbusds.oauth2.sdk.OAuth2Error#INVALID_CLIENT}.
	 * @param reason
	 *        What exactly is wrong.
	 * @param cause
	 *        The problem that caused this one.
	 */
	public OAuthException(ErrorObject error, ResKey reason, Throwable cause) {
		super(reason, cause);
		_error = error;
		_redirectable = false;
	}

	/**
	 * The OAuth error, without a description.
	 */
	public ErrorObject getError() {
		return _error;
	}

	/**
	 * Whether an authorization request refused with this error is answered by a redirect to the
	 * client's redirect URI.
	 *
	 * <p>
	 * Only once client and redirect URI are known to be valid; before that, a redirect would send
	 * the person to a place nobody vouched for, so the error is shown instead (RFC 6749, section
	 * 4.1.2.1).
	 * </p>
	 */
	public boolean isRedirectable() {
		return _redirectable;
	}

	/**
	 * The {@link #getError() error} with the reason, in the log locale, as description.
	 *
	 * <p>
	 * A character that may not appear in an <code>error_description</code> (RFC 6749, section
	 * 5.2) - a double quote, a backslash, anything outside printable ASCII - is replaced.
	 * </p>
	 */
	public ErrorObject toErrorObject() {
		return _error.setDescription(wireDescription(description()));
	}

	/**
	 * The given text with every character that may not appear in an <code>error_description</code>
	 * replaced: a double quote by a single quote, everything else by a question mark.
	 */
	public static String wireDescription(String text) {
		StringBuilder result = new StringBuilder(text.length());
		for (int n = 0, cnt = text.length(); n < cnt; n++) {
			char c = text.charAt(n);
			if (c == '"') {
				result.append('\'');
			} else if (c < 0x20 || c > 0x7E || c == '\\') {
				result.append('?');
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * The reason in the log locale.
	 */
	public String description() {
		return Resources.getLogInstance().getString(getErrorKey());
	}

}
