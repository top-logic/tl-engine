/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;

import com.top_logic.basic.credential.CredentialProvider;

/**
 * Access token issued by an OAuth 2.0 authorization server, together with the time it was
 * requested.
 *
 * <p>
 * The lifetime of an {@link AccessToken} counts from the moment it was issued, which the token
 * itself does not know. The {@link TokenCache} computes the renewal time from
 * {@link #getIssuedAt()} and {@link #getLifetime()}.
 * </p>
 */
public final class IssuedToken {

	private final AccessToken _accessToken;

	private final RefreshToken _refreshToken;

	private final long _issuedAt;

	/**
	 * Creates a {@link IssuedToken}.
	 *
	 * @param accessToken
	 *        See {@link #getAccessToken()}.
	 * @param refreshToken
	 *        See {@link #getRefreshToken()}.
	 * @param issuedAt
	 *        See {@link #getIssuedAt()}.
	 */
	public IssuedToken(AccessToken accessToken, RefreshToken refreshToken, long issuedAt) {
		_accessToken = accessToken;
		_refreshToken = refreshToken;
		_issuedAt = issuedAt;
	}

	/**
	 * Creates a {@link IssuedToken} from the tokens of a token response.
	 *
	 * @param tokens
	 *        The tokens of the response.
	 * @param issuedAt
	 *        See {@link #getIssuedAt()}.
	 */
	public static IssuedToken fromTokens(Tokens tokens, long issuedAt) {
		return new IssuedToken(tokens.getAccessToken(), tokens.getRefreshToken(), issuedAt);
	}

	/**
	 * The access token.
	 */
	public AccessToken getAccessToken() {
		return _accessToken;
	}

	/**
	 * Token for obtaining a new access token with the refresh token grant, or <code>null</code>.
	 */
	public RefreshToken getRefreshToken() {
		return _refreshToken;
	}

	/**
	 * Lifetime of the access token in seconds, counted from {@link #getIssuedAt()}, or
	 * <code>0</code> if the authorization server did not specify it.
	 */
	public long getLifetime() {
		return _accessToken.getLifetime();
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
	 * Whether the lifetime of the access token has ended at the given time.
	 *
	 * <p>
	 * A token without specified lifetime never counts as expired.
	 * </p>
	 *
	 * @param now
	 *        The time in milliseconds since the epoch.
	 */
	public boolean isExpired(long now) {
		long lifetime = getLifetime();
		return lifetime > 0 && now >= _issuedAt + lifetime * 1000;
	}

	/**
	 * The value of the {@link CredentialProvider#AUTHORIZATION} header presenting the access
	 * token, e.g. <code>Bearer &lt;token&gt;</code>.
	 */
	public String getAuthorization() {
		return _accessToken.toAuthorizationHeader();
	}

	@Override
	public String toString() {
		// Never reveal the token.
		return "IssuedToken(type=" + _accessToken.getType() + ", lifetime=" + getLifetime() + ")";
	}

}
