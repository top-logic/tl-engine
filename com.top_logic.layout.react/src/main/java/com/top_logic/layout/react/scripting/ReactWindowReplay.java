/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.util.List;
import java.util.Map;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * Runs a headless {@link ScriptingSession#act} against a window other than the one driving the current
 * request — the basis for replaying a recorded step on the recorded (opener) window from a recorder
 * side-window, or for any server-side command into a sibling React window of the same session.
 *
 * <p>
 * The target window has its own subsession and {@link SSEUpdateQueue}, so {@link #inWindow} installs
 * the target's subsession, runs the action under the session-wide
 * {@link ReactWindowRegistry#beginInteraction() interaction} and settles derived state, then restores
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
	 * Runs the given action against the control tree of the given window: in that window's
	 * subsession, as one {@link Interaction}, with the derived state settled afterwards.
	 *
	 * <p>
	 * The way into a window from a thread that is serving another one - a command replayed from a
	 * side-window, a request returning from an external site, an event of the session - so that
	 * what the action changes reaches that window's browser page. Everything the action touches is
	 * seen in the target window's subsession, its locale included; the caller's subsession is
	 * restored afterwards.
	 * </p>
	 *
	 * @param registry
	 *        The session's window registry.
	 * @param windowName
	 *        The target window.
	 * @param action
	 *        What to do in that window. Run on the calling thread.
	 * @return Whether the action ran. A window whose page is gone - no queue is held for it any
	 *         longer - is not acted on.
	 */
	public static boolean inWindow(ReactWindowRegistry registry, String windowName, Runnable action) {
		if (registry == null || registry.getQueue(windowName) == null) {
			return false;
		}
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		TLSubSessionContext callerSubSession = displayContext.getSubSessionContext();
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);
		try (Interaction interaction = registry.beginInteraction()) {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				action.run();
				registry.synthesizeModelEvents(windowName);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
		} finally {
			if (callerSubSession != null) {
				displayContext.installSubSessionContext(callerSubSession);
			}
		}
		return true;
	}

	/**
	 * Replays the given recorded step in the given window, in that window's subsession and as one
	 * {@link Interaction}, settling derived state afterwards. An {@link AssertCommand assertion}
	 * step is verified against the window's current state; any other step is dispatched to the
	 * control at the step's address.
	 *
	 * @param registry
	 *        The session's window registry.
	 * @param windowName
	 *        The target window (e.g. the opener window of a recorder side-window).
	 * @param step
	 *        The recorded step to replay.
	 * @return The {@link HandlerResult} of the command or assertion.
	 * @throws IllegalArgumentException
	 *         If the window has no rendered tree or the step's address does not resolve.
	 */
	public static HandlerResult act(ReactWindowRegistry registry, String windowName, ReactCommand step) {
		SSEUpdateQueue queue = registry == null ? null : registry.getQueue(windowName);
		ReactControl root = queue == null ? null : queue.getRootControl();
		if (root == null) {
			throw new IllegalArgumentException("Window '" + windowName + "' has no rendered control tree.");
		}

		HandlerResult[] result = new HandlerResult[1];
		inWindow(registry, windowName, () -> {
			ScriptingSession session = ScriptingSession.forRoot(root);
			if (step instanceof AssertCommand assertion) {
				result[0] = verify(session, assertion);
			} else {
				result[0] = session.act(step.getAddress(), step.getName(), ReactCommands.arguments(step));
			}
		});
		return result[0];
	}

	/**
	 * Verifies an {@link AssertCommand} against the target window's current state: the node at the
	 * step's address must have, for each expected entry, a state value equal to the recorded one
	 * (subset match).
	 */
	private static HandlerResult verify(ScriptingSession session, AssertCommand assertion) {
		Map<String, Object> expected = assertion.stateEntries();
		Map<String, Object> actual = ScriptingTreeProjector.nodeState(session.resolve(assertion.getAddress()));
		List<String> mismatches = AssertCommand.mismatchingKeys(expected, actual);
		if (mismatches.isEmpty()) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return HandlerResult.error(
			I18NConstants.ERROR_ASSERTION_FAILED__ADDRESS_KEYS.fill(assertion.getAddress(), mismatches));
	}
}
