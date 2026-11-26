/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Collection;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Maps a chat session between the {@link TLObject} a view channel holds and the id string the
 * browser uses.
 */
public final class ChatSessionIds {

	private ChatSessionIds() {
		// Static utility.
	}

	/**
	 * The id the browser names {@code session} by, or
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
	 * The id of {@code channelValue} when it names a session the {@code person} may read, otherwise
	 * {@code ""}.
	 */
	public static String selectedId(Object channelValue, Person person) {
		TLObject session = sessionOf(channelValue);
		if (session == null) {
			return "";
		}
		String id = idOf(session);
		return accessibleSession(id, person, SimpleBoundCommandGroup.READ) == null ? "" : id;
	}

	/**
	 * The session for {@code id}, or {@code null} when it is missing or the person may not perform
	 * the given operation on it.
	 *
	 * @see ChatHistoryService#findAccessibleSession(String, Person, BoundCommandGroup)
	 */
	public static TLObject accessibleSession(String id, Person person, BoundCommandGroup operation) {
		if (id == null || id.isBlank()) {
			return null;
		}
		return ChatHistoryService.findAccessibleSession(id, person, operation);
	}
}
