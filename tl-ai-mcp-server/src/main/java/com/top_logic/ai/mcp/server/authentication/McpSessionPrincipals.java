/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Which account opened which MCP session.
 *
 * <p>
 * The streamable HTTP transport creates a session at {@code initialize} and keys every later request
 * by the {@code mcp-session-id} header. <b>The transport's session map has no owner</b>: it resolves
 * the session and serves the request, whoever sent it. Authenticating each request - which the MCP
 * specification requires anyway - is therefore not enough on its own: two accounts that both hold a
 * valid credential could take turns on one session, and the second would inherit the first one's
 * conversation.
 * </p>
 *
 * <p>
 * So the session is bound to the account that opened it, and a request whose authenticated account
 * differs is refused. The binding is by <b>account name</b>, not by {@link Person} wrapper: a session
 * outlives a single request, and a name compares correctly across revisions.
 * </p>
 *
 * <h2>Lifetime</h2>
 *
 * <p>
 * An entry is removed when the client deletes its session (HTTP {@code DELETE} on {@code /mcp/*}),
 * which is the same event that removes the transport's own entry. Nothing evicts an entry on a size
 * bound, deliberately: evicting a binding would let the next caller claim a session that is still
 * live in the transport, which is exactly what this class exists to prevent. The map therefore grows
 * with the number of sessions the transport itself is holding, which is where the real bound is.
 * </p>
 *
 * @see McpAuthenticationFilter
 */
public final class McpSessionPrincipals {

	private final Map<String, String> _ownerBySession = new ConcurrentHashMap<>();

	/**
	 * Creates an empty {@link McpSessionPrincipals}.
	 */
	public McpSessionPrincipals() {
		super();
	}

	/**
	 * Records that the account {@code owner} opened the session {@code sessionId}.
	 *
	 * <p>
	 * Called after an {@code initialize} request succeeded, with the session id the transport put
	 * into the response.
	 * </p>
	 *
	 * @param owner
	 *        The login name of the account, from {@link Person#getName()}.
	 */
	public void bind(String sessionId, String owner) {
		_ownerBySession.put(sessionId, owner);
	}

	/**
	 * Whether {@code caller} may use the session {@code sessionId}.
	 *
	 * <p>
	 * An unknown session id is accepted: either the client is opening its session in this very
	 * request, or the transport does not know the id either and answers {@code 404} itself. Deciding
	 * that here would duplicate the transport's session lookup and could disagree with it.
	 * </p>
	 *
	 * @param caller
	 *        The login name of the requesting account, from {@link Person#getName()}.
	 * @return {@code true} if the session is unbound or was opened by {@code caller}.
	 */
	public boolean mayUse(String sessionId, String caller) {
		String owner = _ownerBySession.get(sessionId);
		return owner == null || owner.equals(caller);
	}

	/**
	 * Forgets the session {@code sessionId}, after the client deleted it.
	 */
	public void release(String sessionId) {
		_ownerBySession.remove(sessionId);
	}

	/**
	 * Forgets every session, after the transport that held them was destroyed.
	 */
	public void clear() {
		_ownerBySession.clear();
	}

	/**
	 * The number of sessions currently bound to an account.
	 *
	 * <p>
	 * For tests and for the boot log; nothing decides anything on it.
	 * </p>
	 */
	public int size() {
		return _ownerBySession.size();
	}
}
