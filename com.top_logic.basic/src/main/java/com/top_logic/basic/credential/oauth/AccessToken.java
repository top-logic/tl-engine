/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import com.top_logic.basic.credential.CredentialProvider;

/**
 * Access token issued by an OAuth 2.0 authorization server (RFC 6749 §5.1).
 */
public final class AccessToken {

	private final String _value;

	private final String _type;

	private final Long _expiresIn;

	private final String _refreshToken;

	private final long _issuedAt;

	/**
	 * Creates a {@link AccessToken}.
	 *
	 * @param value
	 *        See {@link #getValue()}.
	 * @param type
	 *        See {@link #getType()}.
	 * @param expiresIn
	 *        See {@link #getExpiresIn()}.
	 * @param refreshToken
	 *        See {@link #getRefreshToken()}.
	 * @param issuedAt
	 *        See {@link #getIssuedAt()}.
	 */
	public AccessToken(String value, String type, Long expiresIn, String refreshToken, long issuedAt) {
		_value = value;
		_type = type;
		_expiresIn = expiresIn;
		_refreshToken = refreshToken;
		_issuedAt = issuedAt;
	}

	/**
	 * The token itself.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * The token type, e.g. {@link CredentialProvider#BEARER}.
	 */
	public String getType() {
		return _type;
	}

	/**
	 * Lifetime of the token in seconds, counted from {@link #getIssuedAt()}, or <code>null</code>
	 * if the authorization server did not specify it.
	 */
	public Long getExpiresIn() {
		return _expiresIn;
	}

	/**
	 * Token for obtaining a new access token with the refresh token grant, or <code>null</code>.
	 */
	public String getRefreshToken() {
		return _refreshToken;
	}

	/**
	 * Time in milliseconds since the epoch when the token request was sent.
	 *
	 * <p>
	 * The lifetime is counted from the time the request was sent, so that the time the request
	 * takes shortens the computed lifetime and does not extend it.
	 * </p>
	 */
	public long getIssuedAt() {
		return _issuedAt;
	}

	/**
	 * Whether the lifetime of this token has ended at the given time.
	 *
	 * <p>
	 * A token without specified lifetime never counts as expired.
	 * </p>
	 *
	 * @param now
	 *        The time in milliseconds since the epoch.
	 */
	public boolean isExpired(long now) {
		return _expiresIn != null && now >= _issuedAt + _expiresIn.longValue() * 1000;
	}

	/**
	 * The value of the {@link CredentialProvider#AUTHORIZATION} header presenting this token.
	 *
	 * <p>
	 * A bearer token is presented with the scheme {@link CredentialProvider#BEARER}, regardless of
	 * the capitalization of the token type in the token response.
	 * </p>
	 */
	public String getAuthorization() {
		String scheme = CredentialProvider.BEARER.equalsIgnoreCase(_type) ? CredentialProvider.BEARER : _type;
		return scheme + " " + _value;
	}

	@Override
	public String toString() {
		// Never reveal the token.
		return "AccessToken(type=" + _type + ", expiresIn=" + _expiresIn + ")";
	}

}
