/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * Decides which {@link ConversationBackend} answers a conversation.
 *
 * <p>
 * Configured in the {@link ConversationBackendService}. A resolver decides from what the
 * conversation is - typically its {@link AgentSession#getSubject() subject} - and not from the
 * session's metadata, which the chat rewrites from its own state.
 * </p>
 */
public interface ConversationBackendResolver {

	/**
	 * The backend that answers the given conversation.
	 *
	 * <p>
	 * Called on the chat worker thread before every run of the conversation, and on a request thread
	 * whenever the chat asks what the conversation supports - loading it, sending into it,
	 * regenerating or editing a message. Always in the context of the account the conversation
	 * belongs to, and often: the answer is expected to be cheap and the same for the same
	 * conversation.
	 * </p>
	 *
	 * @param session
	 *        The conversation; never <code>null</code>.
	 * @return The backend, or <code>null</code> when this resolver does not answer the
	 *         conversation.
	 */
	ConversationBackend backendFor(AgentSession session);

}
