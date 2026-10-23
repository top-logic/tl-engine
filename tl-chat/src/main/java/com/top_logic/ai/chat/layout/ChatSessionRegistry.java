/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * Bridges the conversations of an application window to the {@link TaskProgressTracker}s used by
 * the JSON chat API (servlet).
 *
 * <p>
 * A single {@code HttpSession} (one login) can host several TopLogic sub-sessions - one per browser
 * window (cloned tabs, popped-out views). Each sub-session has its own
 * {@link com.top_logic.layout.WindowScope}. The plain HTTP request of the {@code ChatServlet}
 * carries only the session cookie, so the servlet cannot tell the sub-sessions apart on its own.
 * </p>
 *
 * <p>
 * To make the right sub-session resolvable, the chat UI is rendered with a {@code data-window-id}
 * attribute carrying the sub-session registry key (the {@code windowName}, see
 * {@link #windowId(TLSessionContext, TLSubSessionContext)}). The client echoes this id with every
 * chat request, and the servlet resolves it back to the sub-session via
 * {@link TLSessionContext#getSubSession(String)}.
 * </p>
 *
 * <p>
 * <b>A window can show more than one conversation</b> - a page's chat beside a
 * {@code <chat-dock>}'s, two {@code <agent-chat>} elements, a chat in a dialog over the one on the
 * page. Each of them is a conversation of its own: its own active session, its own run, its own
 * approvals. So the tracker is keyed by {@code (sub-session, conversation)}, and the client echoes
 * the conversation key - the browser's own {@code chat-id} - as {@code chatId} beside the window
 * id. A request that names none addresses the window's unnamed conversation, which is what the
 * classic {@code chat-client.js} used until #618 (Gitea #502). Before that, one tracker per window meant that
 * two conversations in one window took each other's turns: the second one to send got the first
 * one's stream and the first one waited for an answer that never arrived.
 * </p>
 */
public final class ChatSessionRegistry {

	/**
	 * The {@link TaskProgressTracker}s of the conversations rendered in a sub-session, by
	 * conversation key.
	 */
	@SuppressWarnings("unchecked")
	private static final Property<Map<String, TaskProgressTracker>> CHAT_TRACKERS =
		TypedAnnotatable.propertyMap("tl.chat.trackers");

	private ChatSessionRegistry() {
		// Static utility.
	}

	/**
	 * Normalizes a conversation key, so that an absent, blank and empty one are the same
	 * conversation.
	 *
	 * @param conversation
	 *        The key the client sent or an element configured; may be <code>null</code>.
	 * @return The key to store and look up under; never <code>null</code>.
	 */
	public static String key(String conversation) {
		return conversation == null ? "" : conversation.trim();
	}

	/**
	 * Stores the given tracker as the one of {@code conversation} in the sub-session, so that
	 * {@link #resolveTracker} can find it.
	 *
	 * <p>
	 * Called while rendering a chat element, where the application's sub-session is bound to the
	 * current thread.
	 * </p>
	 *
	 * @param conversation
	 *        The conversation key the client echoes as {@code chatId}; <code>null</code> is the
	 *        window's unnamed conversation.
	 */
	public static void register(TLSubSessionContext subSession, String conversation, TaskProgressTracker tracker) {
		if (subSession == null || tracker == null) {
			return;
		}
		boolean first;
		Map<String, TaskProgressTracker> trackers;
		synchronized (subSession) {
			first = !subSession.isSet(CHAT_TRACKERS);
			trackers = subSession.mkMap(CHAT_TRACKERS);
		}
		synchronized (trackers) {
			trackers.put(key(conversation), tracker);
		}
		if (first) {
			subSession.addUnboundListener(ChatSessionRegistry::windowEnded);
		}
	}

	/**
	 * The window - the sub-session - has ended: its conversations stop listening to their subagents,
	 * which are kept by the process and would otherwise keep the window's state in memory.
	 */
	private static void windowEnded(TLSubSessionContext subSession) {
		for (TaskProgressTracker tracker : trackers(subSession)) {
			try {
				tracker.close();
			} catch (RuntimeException ex) {
				Logger.warn("Could not let go of a closed window's chat: " + ex.getMessage(), ex,
					ChatSessionRegistry.class);
			}
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
	 * Resolves the {@link TaskProgressTracker} of one conversation of the given client-provided
	 * window id, or {@code null} if the sub-session is unknown or expired or shows no such
	 * conversation.
	 */
	public static TaskProgressTracker resolveTracker(TLSessionContext session, String windowId, String conversation) {
		return tracker(resolveSubSession(session, windowId), conversation);
	}

	/**
	 * The {@link TaskProgressTracker} of one conversation of the given sub-session, or {@code null}
	 * if it shows no such conversation.
	 */
	public static TaskProgressTracker tracker(TLSubSessionContext subSession, String conversation) {
		if (subSession == null) {
			return null;
		}
		Map<String, TaskProgressTracker> trackers = subSession.get(CHAT_TRACKERS);
		synchronized (trackers) {
			return trackers.get(key(conversation));
		}
	}

	/**
	 * The trackers of every conversation rendered in the given sub-session.
	 *
	 * <p>
	 * What a change that is about the window rather than about one conversation has to reach - a
	 * chat this account deleted in another window, say.
	 * </p>
	 *
	 * @return The trackers, in no particular order; never <code>null</code>.
	 */
	public static Collection<TaskProgressTracker> trackers(TLSubSessionContext subSession) {
		if (subSession == null) {
			return List.of();
		}
		Map<String, TaskProgressTracker> trackers = subSession.get(CHAT_TRACKERS);
		synchronized (trackers) {
			return new ArrayList<>(trackers.values());
		}
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
