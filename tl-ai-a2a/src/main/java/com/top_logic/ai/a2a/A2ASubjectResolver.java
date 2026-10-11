/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import java.util.Map;

import com.top_logic.model.TLObject;

/**
 * Application hook: the subject of an incoming A2A conversation, set on the {@code AgentSession} the
 * conversation is recorded in and named to the agent in each message.
 *
 * <p>
 * Asked once per context, before its first run, in the {@code TLContext} of the account the run acts
 * as - so it may read and create objects (a creation commits its own transaction). Resolvers are
 * asked in order ({@code <subject-resolvers>} on {@link A2AService}); the first non-<code>null</code>
 * answer wins. When none answers, the conversation gets the
 * {@link Request#rememberedSubject() remembered subject}, or none.
 * </p>
 */
public interface A2ASubjectResolver {

	/**
	 * The subject of the conversation, or <code>null</code> to leave it to the next resolver.
	 */
	TLObject resolveSubject(Request request);

	/**
	 * What is known about an incoming conversation.
	 *
	 * @param agent
	 *        The exposed agent's path.
	 * @param agentName
	 *        The tl-ai agent that serves it.
	 * @param contextId
	 *        The A2A context id.
	 * @param caller
	 *        The account the caller authenticated as.
	 * @param peer
	 *        The calling application as it named itself, else the account.
	 * @param metadata
	 *        The request and message metadata, merged; a tl-ai caller sends
	 *        <code>{"tlAi": {"app", "agent", "conversationId", "subject"}}</code>.
	 * @param firstMessage
	 *        The text of the first message of the conversation.
	 * @param rememberedSubject
	 *        The subject of this application's own conversation that sent this context id to the peer
	 *        - set when a peer calls back within a context this application started - or
	 *        <code>null</code>.
	 */
	record Request(String agent, String agentName, String contextId, String caller, String peer,
			Map<String, Object> metadata, String firstMessage, TLObject rememberedSubject) {
		// Pure value.
	}
}
