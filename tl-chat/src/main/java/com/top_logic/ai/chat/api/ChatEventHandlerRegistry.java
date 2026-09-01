/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.concurrent.CopyOnWriteArraySet;

import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;

/**
 * The chat UIs currently observing agent runs, keyed by {@link TaskProgressTracker}.
 *
 * <p>
 * A {@link ChatEventHandler} registers itself when its connection opens and unregisters when it
 * closes; {@link ChatProcessor} looks up the handler of the tracker it is running for. Because the
 * registry is typed by the interface, a chat UI other than {@link ChatSSEHandler} can register and
 * receives the same events at the same points.
 * </p>
 *
 * <p>
 * At most one handler per tracker is addressed at a time: {@link #find(TaskProgressTracker)}
 * returns the most recently registered active one. The browser opens a fresh SSE stream when the
 * user switches sessions, so a run must always resolve its handler per event rather than capture
 * one at run start.
 * </p>
 */
public final class ChatEventHandlerRegistry {

	private static final CopyOnWriteArraySet<ChatEventHandler> HANDLERS = new CopyOnWriteArraySet<>();

	/** Not instantiable. */
	private ChatEventHandlerRegistry() {
		// Static registry.
	}

	/**
	 * Registers a handler as the observer of its {@link ChatEventHandler#getTracker() tracker}.
	 *
	 * <p>
	 * Handlers of the same tracker that have gone inactive are dropped, so a reconnecting UI does
	 * not accumulate dead entries.
	 * </p>
	 */
	public static void register(ChatEventHandler handler) {
		TaskProgressTracker tracker = handler.getTracker();
		HANDLERS.removeIf(existing -> existing.getTracker() == tracker && !existing.isActive());
		HANDLERS.add(handler);
	}

	/**
	 * Removes a handler; a no-op if it is not registered.
	 */
	public static void unregister(ChatEventHandler handler) {
		HANDLERS.remove(handler);
	}

	/**
	 * The active handler observing the given tracker.
	 *
	 * @return The most recently registered active handler, or {@code null} if the tracker is
	 *         currently unobserved.
	 */
	public static ChatEventHandler find(TaskProgressTracker tracker) {
		ChatEventHandler candidate = null;
		for (ChatEventHandler handler : HANDLERS) {
			if (handler.getTracker() == tracker && handler.isActive()) {
				candidate = handler;
			}
		}
		return candidate;
	}

}
