/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Decides which {@link ConversationBackend} answers a conversation.
 *
 * <p>
 * Configured in the {@link ConversationBackendService}. A resolver decides from what a conversation
 * is about - its subject - and not from the conversation's metadata, which the chat rewrites from
 * its own state. All conversations about subjects of the same type that a resolver answers offer
 * the same: a resolver names these {@link BackendFeatures} for the subject's type, so that the chat
 * knows them before the conversation, or even its subject, exists.
 * </p>
 */
public interface ConversationBackendResolver {

	/**
	 * The backend that answers a conversation about the given subject.
	 *
	 * <p>
	 * Called on the chat worker thread before every run of a conversation, and on a request thread
	 * whenever the chat asks what an existing conversation supports - loading it, sending into it,
	 * regenerating or editing a message. Always in the context of the account the conversation
	 * belongs to, and often: the answer is expected to be cheap and the same for the same subject.
	 * </p>
	 *
	 * @param subject
	 *        What the conversation is about; never <code>null</code>: a conversation about nothing is
	 *        answered by the framework's own agent without asking a resolver.
	 * @return The backend, or <code>null</code> when this resolver does not answer conversations
	 *         about this subject.
	 */
	ConversationBackend backendFor(TLObject subject);

	/**
	 * What a conversation about a subject of the given type offers.
	 *
	 * <p>
	 * Called on a request thread whenever a chat shows no conversation yet - an empty chat about a
	 * subject of this type - and again when its subject changes; often, so the answer is expected
	 * to be cheap. The answer must match the {@link ConversationBackend#features() features} of the
	 * backends {@link #backendFor(TLObject)} names for subjects of this type: the chat offers these
	 * controls before the first message and the backend's own features after it.
	 * </p>
	 *
	 * @param subjectType
	 *        The type of the subject a conversation is about; never <code>null</code>.
	 * @return The features, or <code>null</code> when this resolver does not answer conversations
	 *         about subjects of this type.
	 */
	BackendFeatures featuresFor(TLStructuredType subjectType);

}
