/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
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
 * The target window has its own subsession and {@link SSEUpdateQueue}, so {@link #inWindow(ReactWindowRegistry, String, Runnable)} installs
 * the target's subsession, runs the action under the session-wide
 * {@link ReactWindowRegistry#beginInteraction() interaction} and settles derived state, then restores
 * the caller's subsession. The command's control updates enqueue to the target window's queue, which
 * flushes them to that window's SSE connection — the effect appears in the target browser window.
 * </p>
 *
 * <p>
 * A thread that knows no window name but a sub-session - a background thread such as an agent run -
 * enters the window displaying that sub-session through
 * {@link #inWindow(TLSubSessionContext, long, Consumer)}.
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
	 * Timeout for {@link #inWindow(TLSubSessionContext, long, Consumer)} to wait for another
	 * thread's interaction as long as it lasts.
	 */
	public static final long WAIT = -1;

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
		if (registry == null) {
			return false;
		}
		return enter(registry, windowName, WAIT, action) == WindowAccess.DONE;
	}

	/**
	 * Runs the given action in the browser window that displays the given sub-session, waiting for
	 * another thread's interaction with the window's session as long as it lasts.
	 *
	 * @see #inWindow(TLSubSessionContext, long, Consumer)
	 */
	public static WindowAccess inWindow(TLSubSessionContext subSession, Consumer<? super ReactControl> action) {
		return inWindow(subSession, WAIT, action);
	}

	/**
	 * Runs the given action in the browser window that displays the given sub-session, from any
	 * thread - a background thread such as an agent run included.
	 *
	 * <p>
	 * The window is the live browser window whose session context holds the given sub-session
	 * under the window's name, see {@link ReactWindowRegistry#registryDisplaying(TLSubSessionContext)}.
	 * The action runs on the calling thread with the sub-session installed, within the
	 * {@link Interaction} of the window's session, and receives the window's root control. The
	 * window's pending model events are synthesized before the action, so that it sees the state of
	 * the model the calling thread sees, and after it, so that what it changes reaches the browser
	 * page. The caller's context is restored afterwards.
	 * </p>
	 *
	 * <p>
	 * A thread that already holds the interaction of the window's session, such as a request of
	 * that session, acts within it at once.
	 * </p>
	 *
	 * @param subSession
	 *        The sub-session whose window to act in. May be {@code null}, which is
	 *        {@link WindowAccess#NO_WINDOW}.
	 * @param timeoutMillis
	 *        The time in milliseconds to wait at most for another thread's interaction with the
	 *        window's session to end, {@code 0} not to wait, {@link #WAIT} to wait as long as it
	 *        lasts.
	 * @param action
	 *        What to do in the window. Receives the window's root control, {@code null} while the
	 *        window has no rendered tree. An exception it throws is thrown here.
	 * @return What became of the action; never {@code null}.
	 */
	public static WindowAccess inWindow(TLSubSessionContext subSession, long timeoutMillis,
			Consumer<? super ReactControl> action) {
		ReactWindowRegistry registry = ReactWindowRegistry.registryDisplaying(subSession);
		if (registry == null) {
			return WindowAccess.NO_WINDOW;
		}
		String windowName = registry.windowDisplaying(subSession);
		if (windowName == null) {
			return WindowAccess.NO_WINDOW;
		}
		return ThreadContextManager.inContext(subSession,
			(ComputationEx2<WindowAccess, RuntimeException, RuntimeException>) () -> enter(registry, windowName,
				timeoutMillis, () -> {
					registry.synthesizeModelEvents(windowName);
					SSEUpdateQueue queue = registry.getQueue(windowName);
					action.accept(queue == null ? null : queue.getRootControl());
				}));
	}

	/**
	 * Runs the given action in the given window: in the window's subsession, within the
	 * {@link Interaction} of its session, and synthesizes the window's model events afterwards.
	 *
	 * @param timeoutMillis
	 *        See {@link #inWindow(TLSubSessionContext, long, Consumer)}.
	 * @return {@link WindowAccess#NO_WINDOW} if the registry holds no queue for the window, before
	 *         or after the interaction was entered.
	 */
	private static WindowAccess enter(ReactWindowRegistry registry, String windowName, long timeoutMillis,
			Runnable action) {
		if (registry.getQueue(windowName) == null) {
			return WindowAccess.NO_WINDOW;
		}
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		TLSubSessionContext callerSubSession = displayContext.getSubSessionContext();
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);
		try (Interaction interaction = timeoutMillis < 0 ? registry.beginInteraction()
			: registry.tryBeginInteraction(timeoutMillis)) {
			if (interaction == null) {
				return WindowAccess.BUSY;
			}
			if (registry.getQueue(windowName) == null) {
				// Closed while waiting for the interaction.
				return WindowAccess.NO_WINDOW;
			}
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
		return WindowAccess.DONE;
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
