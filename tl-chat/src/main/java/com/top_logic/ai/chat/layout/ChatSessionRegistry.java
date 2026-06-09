/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.Map;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * Bridges the TopLogic sub-session of an application window to the {@link TaskProgressTracker}
 * used by the JSON chat API (servlet).
 *
 * <p>
 * A single {@code HttpSession} (one login) can host several TopLogic sub-sessions - one per
 * browser window (cloned tabs, popped-out views). Each sub-session has its own
 * {@link com.top_logic.layout.WindowScope} and therefore its own {@link TaskProgressTracker}. The
 * plain HTTP request of the {@code ChatServlet} carries only the session cookie, so the servlet
 * cannot tell the sub-sessions apart on its own.
 * </p>
 *
 * <p>
 * To make the right sub-session resolvable, the chat UI is rendered with a {@code data-window-id}
 * attribute carrying the sub-session registry key (the {@code windowName}, see
 * {@link #windowId(TLSessionContext, TLSubSessionContext)}). The client echoes this id with every
 * chat request, and the servlet resolves it back to the sub-session via
 * {@link TLSessionContext#getSubSession(String)} and to the tracker stored on it under
 * {@link #CHAT_TRACKER}.
 * </p>
 */
public final class ChatSessionRegistry {

	/** The {@link TaskProgressTracker} of the chat UI rendered in a sub-session. */
	public static final Property<TaskProgressTracker> CHAT_TRACKER =
		TypedAnnotatable.property(TaskProgressTracker.class, "tl.chat.tracker");

	private ChatSessionRegistry() {
		// Static utility.
	}

	/**
	 * Stores the given tracker on the sub-session so {@link #resolveTracker} can find it.
	 *
	 * <p>
	 * Called while rendering the chat component, where the application's sub-session is bound to the
	 * current thread.
	 * </p>
	 */
	public static void register(TLSubSessionContext subSession, TaskProgressTracker tracker) {
		if (subSession != null && tracker != null) {
			subSession.set(CHAT_TRACKER, tracker);
		}
	}

	/**
	 * Resolves the sub-session addressed by the given client-provided window id, or {@code null} if
	 * none matches (unknown or expired window).
	 */
	public static TLSubSessionContext resolveSubSession(TLSessionContext session, String windowId) {
		if (session == null || windowId == null || windowId.isEmpty()) {
			return null;
		}
		return session.getSubSession(windowId);
	}

	/**
	 * Resolves the {@link TaskProgressTracker} for the given client-provided window id, or
	 * {@code null} if the sub-session is unknown/expired or has no chat tracker.
	 */
	public static TaskProgressTracker resolveTracker(TLSessionContext session, String windowId) {
		TLSubSessionContext subSession = resolveSubSession(session, windowId);
		return subSession == null ? null : subSession.get(CHAT_TRACKER);
	}

	/**
	 * The registry key ({@code windowName}) under which the given sub-session is registered in its
	 * session, or {@code null} if it is not (yet) registered.
	 *
	 * <p>
	 * This is the value the client must send back as the window id. It matches the key expected by
	 * {@link TLSessionContext#getSubSession(String)}; layout sub-sessions are registered under their
	 * {@code windowName} only (see
	 * {@code ContentHandlersRegistry#startLogin}), so the reverse lookup is unambiguous.
	 * </p>
	 */
	public static String windowId(TLSessionContext session, TLSubSessionContext subSession) {
		if (session == null || subSession == null) {
			return null;
		}
		for (Map.Entry<String, TLSubSessionContext> entry : session.getSubSessions().entrySet()) {
			if (entry.getValue() == subSession) {
				return entry.getKey();
			}
		}
		return null;
	}
}
