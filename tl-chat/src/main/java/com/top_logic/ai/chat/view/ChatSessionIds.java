/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Collection;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;

/**
 * Maps a chat session between the {@link TLObject} a view channel holds and the id string the
 * browser and {@code /chat/*} already use.
 */
public final class ChatSessionIds {

	private ChatSessionIds() {
		// Static utility.
	}

	/**
	 * The id {@link com.top_logic.ai.chat.api.ChatServlet} writes for {@code session}, or
	 * {@code ""} when there is no session.
	 */
	public static String idOf(TLObject session) {
		if (session == null || session.tHandle() == null) {
			return "";
		}
		return session.tHandle().getObjectName().toExternalForm();
	}

	/**
	 * The {@link TLObject} on a selection channel: a session, or the only element of a one-item
	 * collection, otherwise {@code null}.
	 */
	public static TLObject sessionOf(Object channelValue) {
		if (channelValue instanceof TLObject session) {
			return session;
		}
		if (channelValue instanceof Collection<?> many && many.size() == 1) {
			Object only = many.iterator().next();
			return only instanceof TLObject session ? session : null;
		}
		return null;
	}

	/**
	 * The id of {@code channelValue} when it names a session the {@code owner} owns, otherwise
	 * {@code ""}.
	 */
	public static String selectedId(Object channelValue, Person owner) {
		TLObject session = sessionOf(channelValue);
		if (session == null) {
			return "";
		}
		String id = idOf(session);
		return ChatHistoryService.findOwnedSessionById(id, owner) == null ? "" : id;
	}

	/**
	 * The owned session for {@code id}, or {@code null} when it is missing or not this person's.
	 */
	public static TLObject ownedSession(String id, Person owner) {
		if (id == null || id.isBlank()) {
			return null;
		}
		return ChatHistoryService.findOwnedSessionById(id, owner);
	}
}
