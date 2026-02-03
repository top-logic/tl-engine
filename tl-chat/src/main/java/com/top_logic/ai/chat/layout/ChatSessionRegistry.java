/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import jakarta.servlet.http.HttpSession;

/**
 * Session-scoped registry for {@link TaskProgressTracker} so the JSON chat API (servlet) can
 * resolve the same tracker used by the TL chat UI when both run in the same session.
 * <p>
 * The servlet has only {@link HttpSession}; the layout uses {@link com.top_logic.layout.WindowScope}.
 * This registry stores the tracker in a session attribute so the API and the UI can share one
 * tracker per session when Option A is used: call {@link #registerSession(HttpSession, TaskProgressTracker)}
 * when building the chat component (e.g. when request/session is available in the layout context).
 * </p>
 */
public final class ChatSessionRegistry {

	private static final String SESSION_ATTR_TRACKER = "tl.chat.tracker";

	private ChatSessionRegistry() {
	}

	/**
	 * Returns the {@link TaskProgressTracker} for the given session, creating one if none exists.
	 */
	public static TaskProgressTracker getOrCreate(HttpSession session) {
		if (session == null) {
			return new TaskProgressTracker();
		}
		synchronized (session) {
			Object attr = session.getAttribute(SESSION_ATTR_TRACKER);
			if (attr instanceof TaskProgressTracker tracker) {
				return tracker;
			}
			TaskProgressTracker tracker = new TaskProgressTracker();
			session.setAttribute(SESSION_ATTR_TRACKER, tracker);
			return tracker;
		}
	}

	/**
	 * Associates the given tracker with the session so that {@link #getOrCreate(HttpSession)}
	 * returns this tracker for this session (Option A: share state between TL UI and JSON API).
	 * Call this when building the chat component if the current request/session is available.
	 */
	public static void registerSession(HttpSession session, TaskProgressTracker tracker) {
		if (session != null && tracker != null) {
			synchronized (session) {
				session.setAttribute(SESSION_ATTR_TRACKER, tracker);
			}
		}
	}
}
