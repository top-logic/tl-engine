/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.knowledge.wrap.person.Person;

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

	/**
	 * The active chat UIs of this account that are showing the given conversation.
	 *
	 * <p>
	 * How a run reaches the reader rather than the window it was started in. A run belongs to a
	 * conversation, not to a tab: the tab that sent the message may be closed long before the answer
	 * is ready, while another window of the same account has that very conversation open and is
	 * still showing it as running (issue #512). What the run has to say when it ends is addressed to
	 * whoever is looking at the conversation, and that is what this answers.
	 * </p>
	 *
	 * <p>
	 * Which conversation a window is showing is its tracker's active session, so a window that has
	 * moved on to another chat is not in the list - the answer of a chat it is no longer looking at
	 * is not its business, and it will read the recorded turn when it comes back.
	 * </p>
	 *
	 * @param owner
	 *        The account the run belongs to; <code>null</code> matches nothing.
	 * @param sessionId
	 *        The id of the chat the run belongs to; <code>null</code> matches nothing.
	 * @return The matching handlers, in no particular order; never <code>null</code>.
	 */
	public static List<ChatEventHandler> findForSession(Person owner, String sessionId) {
		List<ChatEventHandler> result = new ArrayList<>();
		if (owner == null || sessionId == null) {
			return result;
		}
		for (ChatEventHandler handler : HANDLERS) {
			TaskProgressTracker tracker = handler.getTracker();
			if (tracker == null || !handler.isActive()) {
				continue;
			}
			if (owner.equals(handler.getAccount()) && sessionId.equals(tracker.getActiveSessionId())) {
				result.add(handler);
			}
		}
		return result;
	}

	/**
	 * The chat UIs showing that conversation, or the given one when none is.
	 *
	 * <p>
	 * What a run says when it ends - the rendered answer, or the error that replaces it - is
	 * addressed with this. The fallback is the window the run was started in, which is what every
	 * run addressed before {@link #findForSession(Person, String)} existed, and it is used only when
	 * nobody has the conversation open: the turn is recorded before the run reports it, so the next
	 * window to open the chat reads it from the transcript.
	 * </p>
	 *
	 * @param owner
	 *        The account the run belongs to.
	 * @param sessionId
	 *        The id of the chat the run belongs to; <code>null</code> for a run that has none.
	 * @param startedIn
	 *        The handler of the window the run was started in, or <code>null</code> when it had
	 *        none.
	 * @return Never <code>null</code>; empty when nothing can be told.
	 */
	public static List<ChatEventHandler> findForSession(Person owner, String sessionId,
			ChatEventHandler startedIn) {
		List<ChatEventHandler> attached = findForSession(owner, sessionId);
		if (!attached.isEmpty()) {
			return attached;
		}
		return startedIn == null ? List.of() : List.of(startedIn);
	}

	/**
	 * The active handlers of every window this account has a chat open in, except the given one.
	 *
	 * <p>
	 * What a run asks its user belongs to the account, so several windows can be showing the same
	 * pending approval. When one of them answers, the others have to be told; they have no run of
	 * their own to hear it from. The answering window is excluded because it already knows.
	 * </p>
	 *
	 * <p>
	 * Which account a chat UI belongs to is the UI's own answer ({@link ChatEventHandler#getAccount()});
	 * a UI that does not say is left out.
	 * </p>
	 *
	 * @param owner
	 *        The account; <code>null</code> matches nothing.
	 * @param except
	 *        The tracker to leave out, or <code>null</code> to leave out none.
	 * @return The matching handlers, in no particular order; never <code>null</code>.
	 */
	public static List<ChatEventHandler> findOthers(Person owner, TaskProgressTracker except) {
		List<ChatEventHandler> result = new ArrayList<>();
		if (owner == null) {
			return result;
		}
		for (ChatEventHandler handler : HANDLERS) {
			TaskProgressTracker tracker = handler.getTracker();
			if (tracker == null || tracker == except || !handler.isActive()) {
				continue;
			}
			if (owner.equals(handler.getAccount())) {
				result.add(handler);
			}
		}
		return result;
	}

}
