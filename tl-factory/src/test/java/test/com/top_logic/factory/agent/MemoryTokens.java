/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.factory.llm.LLMRoute;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.tl.ai.core.access.SecretTokens;

/**
 * {@link ProxyTokens} kept in memory, recording what was issued and revoked.
 */
public class MemoryTokens implements ProxyTokens {

	private final Clock _clock;

	private final Map<String, Instant> _valid = new ConcurrentHashMap<>();

	private final List<String> _issued = new CopyOnWriteArrayList<>();

	private final List<String> _revoked = new CopyOnWriteArrayList<>();

	/**
	 * Creates {@link MemoryTokens}.
	 */
	public MemoryTokens(Clock clock) {
		_clock = clock;
	}

	@Override
	public String issue(String workspaceId, Duration lifetime) {
		String token = SecretTokens.newToken(TokenGrants.TOKEN_PREFIX);
		_valid.put(token, _clock.instant().plus(lifetime));
		_issued.add(token);
		return token;
	}

	@Override
	public Instant expiresAt(String token) {
		return _valid.get(token);
	}

	@Override
	public Map<String, String> processEnv(String token) {
		return Map.of(LLMRoute.ENV_AUTH_TOKEN, token);
	}

	@Override
	public void revoke(String token) {
		if (_valid.remove(token) != null) {
			_revoked.add(token);
		}
	}

	/**
	 * Whether the given token is valid now.
	 */
	public boolean isValid(String token) {
		Instant expires = _valid.get(token);
		return expires != null && _clock.instant().isBefore(expires);
	}

	/**
	 * The tokens issued, in their order.
	 */
	public List<String> issued() {
		return _issued;
	}

	/**
	 * The tokens revoked, in their order.
	 */
	public List<String> revoked() {
		return _revoked;
	}

	/**
	 * The tokens issued and not revoked.
	 */
	public int active() {
		return _valid.size();
	}

}
