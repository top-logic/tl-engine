/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * Runs a headless {@link AgentSession#act} against a window other than the one driving the current
 * request — the basis for replaying a recorded step on the recorded (opener) window from a recorder
 * side-window, or for any server-side command into a sibling React window of the same session.
 *
 * <p>
 * The target window has its own subsession and {@link SSEUpdateQueue}, so {@link #act} installs the
 * target's subsession, runs the command under the session-wide
 * {@link ReactWindowRegistry#getRequestLock() request lock} and settles derived state, then restores
 * the caller's subsession. The command's control updates enqueue to the target window's queue, which
 * flushes them to that window's SSE connection — the effect appears in the target browser window.
 * </p>
 */
public final class ReactWindowReplay {

	private ReactWindowReplay() {
		// Static utility.
	}

	/**
	 * Installs the subsession registered for the given window (by {@code ViewServlet} at page load) on
	 * the current thread's {@link DisplayContext}, so command execution and label resolution run in
	 * that window's context.
	 *
	 * @param displayContext
	 *        The current display context.
	 * @param windowName
	 *        The target window.
	 * @return The window's {@link SubsessionHandler} (only present for traditional-layout windows;
	 *         {@code null} for a React view window), for legacy update gating.
	 */
	public static SubsessionHandler installSubSession(DisplayContext displayContext, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			return null;
		}
		TLSessionContext sessionContext = TLContextManager.getSession();
		if (sessionContext == null) {
			return null;
		}
		TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
		if (subSession != null) {
			displayContext.installSubSessionContext(subSession);
		} else {
			Logger.warn("No SubSession found for window '" + windowName
				+ "'. The view page may not have been loaded yet.", ReactWindowReplay.class);
		}
		ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
		return handlersRegistry.getContentHandler(windowName);
	}

	/**
	 * Dispatches {@code command} (with {@code arguments}) to the control at {@code address} in the given
	 * window, in that window's subsession and under the session request lock, settling derived state
	 * afterwards.
	 *
	 * @param registry
	 *        The session's window registry.
	 * @param windowName
	 *        The target window (e.g. the opener window of a recorder side-window).
	 * @param address
	 *        The semantic address of the target control (as {@link AgentSession#resolve(String)}
	 *        accepts).
	 * @param command
	 *        The command id to dispatch.
	 * @param arguments
	 *        The command arguments, or an empty map.
	 * @return The {@link HandlerResult} of the command.
	 * @throws IllegalArgumentException
	 *         If the window has no rendered tree or the address does not resolve.
	 */
	public static HandlerResult act(ReactWindowRegistry registry, String windowName, String address,
			String command, Map<String, Object> arguments) {
		SSEUpdateQueue queue = registry.getQueue(windowName);
		ReactControl root = queue == null ? null : queue.getRootControl();

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		TLSubSessionContext callerSubSession = displayContext.getSubSessionContext();
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);
		ReentrantLock requestLock = registry.getRequestLock();
		requestLock.lock();
		try {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				HandlerResult result = AgentSession.forRoot(root).act(address, command, arguments);
				registry.synthesizeModelEvents(windowName);
				return result;
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
		} finally {
			requestLock.unlock();
			if (callerSubSession != null) {
				displayContext.installSubSessionContext(callerSubSession);
			}
		}
	}
}
