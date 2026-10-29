/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.access.SecretTokens;

/**
 * The valid tokens of the {@link LLMProxyService}, kept in memory by their hashes.
 *
 * <p>
 * A token is {@value LLMProxyService#TOKEN_PREFIX} followed by a random secret (see
 * {@link SecretTokens}), issued for a workspace with a lifetime. Besides its validity, the
 * {@link #admit(String, String) admission} of a request checks:
 * </p>
 *
 * <ul>
 * <li><b>The source.</b> A token is bound to the address its first admitted request came from
 * (trust on first use). A request from another address is refused and logged as a warning; the token
 * stays valid for its own address.</li>
 * <li><b>The rate.</b> A token may make at most {@link #getRateLimit()} requests per minute, with
 * bursts up to that number; a request beyond is refused with the time after which the next one is
 * admitted.</li>
 * </ul>
 */
public class TokenGrants {

	/**
	 * What a request costs from the allowance of a token, a minute in nanoseconds: the allowance
	 * grows by the rate limit per nanosecond, so that a token gains as many requests per minute as
	 * its limit.
	 */
	private static final long REQUEST_COST = Duration.ofMinutes(1).toNanos();

	/**
	 * A valid token.
	 */
	private static final class Grant {

		private final String _workspaceId;

		private final Instant _expires;

		/**
		 * The address of the first admitted request, {@code null} before.
		 */
		private String _source;

		/**
		 * What the token may spend on requests right now, in units of which a request costs
		 * {@link TokenGrants#REQUEST_COST}, at most the rate limit times that cost.
		 */
		private long _allowance;

		/**
		 * When {@link #_allowance} was last refilled.
		 */
		private Instant _refilled;

		Grant(String workspaceId, Instant expires, int rateLimit, Instant now) {
			_workspaceId = workspaceId;
			_expires = expires;
			_allowance = rateLimit * REQUEST_COST;
			_refilled = now;
		}

		String workspaceId() {
			return _workspaceId;
		}

		Instant expires() {
			return _expires;
		}

		/**
		 * Binds the grant to the given address if it has none yet.
		 *
		 * @return The address the grant is bound to.
		 */
		synchronized String bind(String source) {
			if (_source == null) {
				_source = source;
			}
			return _source;
		}

		/**
		 * Takes one request from the allowance.
		 *
		 * @return {@link Duration#ZERO} if the request is admitted, otherwise the time until the
		 *         next one is.
		 */
		synchronized Duration take(int rateLimit, Instant now) {
			if (rateLimit <= 0) {
				return Duration.ZERO;
			}
			long max = rateLimit * REQUEST_COST;
			// After a minute the allowance is full anyway; the cut keeps the product in range.
			long elapsed = Math.min(Math.max(0, Duration.between(_refilled, now).toNanos()), REQUEST_COST);
			_allowance = Math.min(max, _allowance + elapsed * rateLimit);
			_refilled = now;
			if (_allowance >= REQUEST_COST) {
				_allowance -= REQUEST_COST;
				return Duration.ZERO;
			}
			long missing = REQUEST_COST - _allowance;
			return Duration.ofNanos((missing + rateLimit - 1) / rateLimit);
		}
	}

	private final Map<String, Grant> _grants = new ConcurrentHashMap<>();

	private final Clock _clock;

	private final Predicate<String> _workspaceExists;

	private final int _rateLimit;

	/**
	 * Creates a {@link TokenGrants}.
	 *
	 * @param clock
	 *        The clock deciding about lifetimes and rates.
	 * @param workspaceExists
	 *        Whether the workspace of the given ID still exists; a token of a workspace that does
	 *        not is refused and dropped.
	 * @param rateLimit
	 *        The requests per minute a token may make, {@code 0} for no limit.
	 */
	public TokenGrants(Clock clock, Predicate<String> workspaceExists, int rateLimit) {
		_clock = clock;
		_workspaceExists = workspaceExists;
		_rateLimit = rateLimit;
	}

	/**
	 * The requests per minute a token may make, {@code 0} for no limit.
	 */
	public int getRateLimit() {
		return _rateLimit;
	}

	/**
	 * Issues a token.
	 *
	 * @param workspaceId
	 *        The workspace the token is issued for.
	 * @param lifetime
	 *        How long the token is valid.
	 * @return The token.
	 */
	public String issue(String workspaceId, Duration lifetime) {
		Instant now = _clock.instant();
		dropExpired(now);
		String plain = SecretTokens.newToken(LLMProxyService.TOKEN_PREFIX);
		_grants.put(SecretTokens.hash(plain), new Grant(workspaceId, now.plus(lifetime), _rateLimit, now));
		return plain;
	}

	/**
	 * When the given token stops being valid.
	 *
	 * @return The end of its lifetime, {@code null} if it is not valid.
	 */
	public Instant expiresAt(String token) {
		Grant grant = lookup(token);
		return grant == null ? null : grant.expires();
	}

	/**
	 * Revokes a token: the next request presenting it is refused.
	 */
	public void revoke(String token) {
		if (SecretTokens.hasFormat(LLMProxyService.TOKEN_PREFIX, token)) {
			_grants.remove(SecretTokens.hash(token));
		}
	}

	/**
	 * Revokes all tokens issued for the given workspace.
	 */
	public void revokeAll(String workspaceId) {
		_grants.values().removeIf(grant -> grant.workspaceId().equals(workspaceId));
	}

	/**
	 * Decides about a request presenting the given token from the given address.
	 *
	 * @see LLMProxy#admit(String, String)
	 */
	public LLMAdmission admit(String token, String source) {
		Grant grant = lookup(token);
		if (grant == null) {
			return LLMAdmission.INVALID_TOKEN;
		}
		if (source == null) {
			return LLMAdmission.FOREIGN_SOURCE;
		}
		String hash = SecretTokens.hash(token);
		Instant now = _clock.instant();
		if (!now.isBefore(grant.expires())) {
			_grants.remove(hash, grant);
			return LLMAdmission.INVALID_TOKEN;
		}
		if (!_workspaceExists.test(grant.workspaceId())) {
			_grants.remove(hash, grant);
			return LLMAdmission.INVALID_TOKEN;
		}
		String bound = grant.bind(source);
		if (!bound.equals(source)) {
			Logger.warn("LLM proxy: refused a request from " + source + " with a token of workspace "
				+ grant.workspaceId() + " that is bound to " + bound + ".", TokenGrants.class);
			return LLMAdmission.FOREIGN_SOURCE;
		}
		Duration wait = grant.take(_rateLimit, now);
		if (!wait.isZero()) {
			return LLMAdmission.rateLimited(wait);
		}
		return LLMAdmission.ADMITTED;
	}

	private Grant lookup(String token) {
		if (!SecretTokens.hasFormat(LLMProxyService.TOKEN_PREFIX, token)) {
			return null;
		}
		return _grants.get(SecretTokens.hash(token));
	}

	private void dropExpired(Instant now) {
		_grants.values().removeIf(grant -> !now.isBefore(grant.expires()));
	}

}
