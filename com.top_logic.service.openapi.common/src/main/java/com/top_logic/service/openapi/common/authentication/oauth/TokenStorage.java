/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.sched.SchedulerService;

/**
 * Cache for tokens.
 * 
 * <p>
 * This is a storage for tokens until a given expiration date is reached. When a token is stored
 * ({@link #storeToken(Object, Object, Date)}), it can be accessed via
 * {@link #getValidToken(Object)} until the expiry date is reached.
 * </p>
 * 
 * <p>
 * This storage contains a clean-up mechanism to to automatically remove tokens that have expired.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TokenStorage<K, V> extends ConcurrentHashMap<K, TokenStorage.Value<V>> {

	static final class Value<V> {

		/** Actually cached value. */
		final V _value;

		/** Expiry date in milliseconds since 01.01.1970. */
		final long _expiration;

		public Value(V value, long expiration) {
			_value = value;
			_expiration = expiration;
		}

	}

	private volatile long _maxTokenCachingTime;

	/**
	 * Creates a {@link TokenStorage}.
	 */
	public TokenStorage() {
		setMaxCachingTime(1, TimeUnit.HOURS);
	}

	/**
	 * Determines how long the tokens are cached maximal.
	 * 
	 * <p>
	 * Tokens that are contained in the cache longer than the given time are removed at next
	 * cleanup.
	 * </p>
	 */
	public void setMaxCachingTime(long duration, TimeUnit unit) {
		_maxTokenCachingTime = unit.toMillis(duration);
	}

	/**
	 * Determines the cached token for the given key, if it is not expired.
	 * 
	 * <p>
	 * When no token was added for the given key before, then <code>null</code> is returned.
	 * Otherwise it is checked whether the expiry date is not yet reached. In this case the token is
	 * returned, otherwise <code>null</code>.
	 * </p>
	 * 
	 * @see #storeToken(Object, Object, Date)
	 * 
	 * @param key
	 *        Key under which the token was stored before.
	 * @return Either the formerly still valid token or <code>null</code>, if there is none.
	 */
	public V getValidToken(K key) {
		Value<V> token = get(key);
		if (token == null) {
			// Unknown token.
			return null;
		}
		if (Long.compare(token._expiration, System.currentTimeMillis()) >= 0) {
			return token._value;
		}
		return null;
	}

	/**
	 * Stores the given token under the given key until the given expiry date is reached.
	 * 
	 * @param key
	 *        The key under which the token should be stored.
	 * @param token
	 *        The Token to store.
	 * @param expiryDate
	 *        Expiry date of the token.
	 */
	public final void storeToken(K key, V token, Date expiryDate) {
		long nowMillis = System.currentTimeMillis();
		long expiration = Math.min(expiryDate.getTime(), nowMillis + _maxTokenCachingTime);
		boolean registerCleanUp = isEmpty();
		put(key, new Value<>(token, expiration));
		if (registerCleanUp) {
			registerCleanup(expiration, nowMillis);
		}
	}

	private ScheduledFuture<?> registerCleanup(long maxExpiration, long now) {
		long nextCall = (maxExpiration - now) + 1000; // add some buffer.
		nextCall = Math.max(nextCall, 1000); // at least 1s.
		return SchedulerService.getInstance().schedule(this::cleanupTokens, nextCall, TimeUnit.MILLISECONDS);
	}

	private void cleanupTokens() {
		long now = System.currentTimeMillis();
		long maxExpirationDate = -1;
		for (Iterator<Value<V>> expirationDates = values().iterator(); expirationDates.hasNext();) {
			long nextExpiration = expirationDates.next()._expiration;
			if (Long.compare(nextExpiration, now) < 0) {
				// expired
				expirationDates.remove();
				continue;
			}
			if (Long.compare(maxExpirationDate, nextExpiration) < 0) {
				maxExpirationDate = nextExpiration;
			}
		}
		if (!isEmpty()) {
			if (maxExpirationDate == -1) {
				/* may happen in rare concurrent modification cases: After removing all entries and
				 * before checking for emptiness, a new token was added. */
				maxExpirationDate = now + TimeUnit.MINUTES.toMillis(1);
			}
			registerCleanup(maxExpirationDate, now);
		}
	}

}

