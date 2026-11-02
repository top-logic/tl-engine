/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.util.function.UnaryOperator;

/**
 * Cache of the current {@link IssuedToken} of a single client.
 *
 * <p>
 * The token is renewed before it expires. The renewal time is the end of the lifetime minus a
 * margin, which is the configured renewal margin, but at most half of the token's lifetime: a token
 * that lives 1 hour is renewed 30 s before it expires (with the default margin of 30 s), a token
 * that lives 20 s is renewed after 10 s. This keeps a token from expiring while a request carrying
 * it is underway, also with clocks of client and server slightly apart, and still uses short-lived
 * tokens for a while. A token whose lifetime the authorization server did not specify is treated as
 * living for the configured default lifetime.
 * </p>
 *
 * <p>
 * When several threads need a token at the same time and the cached one must be renewed, only one
 * of them fetches a new token, the others wait for it and use it. While no renewal is due, getting
 * the token takes no lock.
 * </p>
 */
public class TokenCache {

	private final long _renewalMargin;

	private final long _defaultLifetime;

	private final Object _lock = new Object();

	/**
	 * The cached token, <code>null</code> before the first fetch and after {@link #invalidate()}.
	 */
	private volatile Entry _current;

	/**
	 * The token fetched last, also after it has been invalidated, e.g. for renewing it with its
	 * refresh token.
	 */
	private IssuedToken _last;

	/**
	 * Creates a {@link TokenCache}.
	 *
	 * @param renewalMargin
	 *        Time in milliseconds before the end of a token's lifetime when it is renewed. At most
	 *        half of the lifetime is used.
	 * @param defaultLifetime
	 *        Lifetime in milliseconds of a token without specified lifetime.
	 */
	public TokenCache(long renewalMargin, long defaultLifetime) {
		_renewalMargin = renewalMargin;
		_defaultLifetime = defaultLifetime;
	}

	/**
	 * The current token, fetched with the given function if there is none or it is due for
	 * renewal.
	 *
	 * @param fetch
	 *        Function fetching a new token. Its argument is the token fetched last (also when it has
	 *        been invalidated), or <code>null</code> on the first fetch. The function is never
	 *        called concurrently.
	 * @return The current token.
	 */
	public IssuedToken getToken(UnaryOperator<IssuedToken> fetch) {
		Entry entry = _current;
		if (entry != null && !entry.isDue()) {
			return entry.getToken();
		}
		synchronized (_lock) {
			entry = _current;
			if (entry != null && !entry.isDue()) {
				// Fetched by another thread in the meantime.
				return entry.getToken();
			}
			IssuedToken token = fetch.apply(_last);
			_last = token;
			_current = new Entry(token, renewalTime(token));
			return token;
		}
	}

	/**
	 * Drops the cached token, so that the next call to {@link #getToken(UnaryOperator)} fetches a
	 * new one.
	 */
	public void invalidate() {
		_current = null;
	}

	private long renewalTime(IssuedToken token) {
		long expiresIn = token.getLifetime();
		long lifetime = expiresIn > 0 ? expiresIn * 1000 : _defaultLifetime;
		long margin = Math.min(_renewalMargin, lifetime / 2);
		return token.getIssuedAt() + lifetime - margin;
	}

	private static final class Entry {

		private final IssuedToken _token;

		private final long _renewalTime;

		Entry(IssuedToken token, long renewalTime) {
			_token = token;
			_renewalTime = renewalTime;
		}

		IssuedToken getToken() {
			return _token;
		}

		boolean isDue() {
			return System.currentTimeMillis() >= _renewalTime;
		}

	}

}
