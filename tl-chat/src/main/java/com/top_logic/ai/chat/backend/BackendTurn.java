/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import java.util.List;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * One user message a {@link ConversationBackend} answers.
 *
 * @param sessionId
 *        The id of the chat the message was sent in, as the chat's requests name it.
 * @param session
 *        The persistent conversation; never <code>null</code>.
 * @param owner
 *        The account the conversation belongs to and the run is done for.
 * @param text
 *        The message as the user wrote it, trimmed; never <code>null</code>, empty when the user
 *        sent attachments only.
 * @param attachments
 *        The files sent with the message; never <code>null</code>.
 */
public record BackendTurn(String sessionId, AgentSession session, Person owner, String text,
		List<AttachmentDto> attachments) {

	/**
	 * Canonicalizes absent values.
	 */
	public BackendTurn {
		text = text == null ? "" : text;
		attachments = attachments == null ? List.of() : List.copyOf(attachments);
	}
}
