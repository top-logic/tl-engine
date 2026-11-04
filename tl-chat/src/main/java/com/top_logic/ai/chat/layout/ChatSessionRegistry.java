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

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * The {@link TaskProgressTracker}s of the conversations of an application window.
 *
 * <p>
 * A single {@code HttpSession} (one login) can host several TopLogic sub-sessions - one per browser
 * window (cloned tabs, popped-out views). The trackers hang on the window's sub-session, so every
 * window has chats of its own; the chat controls of a window find them here
 * ({@code ChatViewBinding}), and so does a chat started on the server for the window
 * ({@link ChatStart}).
 * </p>
 *
 * <p>
 * <b>A window can show more than one conversation</b> - a page's chat beside a
 * {@code <chat-dock>}'s, a chat in a dialog over the one on the page. Each of them is a
 * conversation of its own: its own active session, its own run, its own approvals. So the tracker
 * is keyed by {@code (sub-session, conversation)}, the conversation being the {@code chat-id} of
 * the chat elements. A caller that names none addresses the window's unnamed conversation (Gitea
 * #502). One tracker per window would let two conversations in one window take each other's
 * turns: the second one to send got the first one's stream, and the first one waited for an
 * answer that never arrived.
 * </p>
 *
 * <p>
 * <b>A tracker serves one account.</b> A login in a window keeps the window's sub-session and
 * changes its person, so {@link #tracker(TLSubSessionContext, String, Person)} retires the
 * trackers that serve another account - their runs are stopped - and hands out a new one: the
 * chats, runs and approvals one person had in the window are never the next one's.
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
	 *        The key an element configured or a caller names; may be <code>null</code>.
	 * @return The key to store and look up under; never <code>null</code>.
	 */
	public static String key(String conversation) {
		return conversation == null ? "" : conversation.trim();
	}

	/**
	 * Stores the given tracker as the one of {@code conversation} in the sub-session, so that
	 * {@link #tracker(TLSubSessionContext, String)} finds it.
	 *
	 * <p>
	 * Called while rendering a chat element, where the application's sub-session is bound to the
	 * current thread.
	 * </p>
	 *
	 * @param conversation
	 *        The conversation key, the {@code chat-id} of the chat elements; <code>null</code> is
	 *        the window's unnamed conversation.
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
			close(tracker);
		}
	}

	private static void close(TaskProgressTracker tracker) {
		try {
			tracker.close();
		} catch (RuntimeException ex) {
			Logger.warn("Could not let go of a window's chat: " + ex.getMessage(), ex, ChatSessionRegistry.class);
		}
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
	 * The {@link TaskProgressTracker} of one conversation of the given sub-session for the given
	 * account, registered when the window has none for it.
	 *
	 * <p>
	 * The trackers of the window that serve another account - the one signed in to the window
	 * before a login in it - are retired, in every conversation of the window and not only in the
	 * one asked for: what that account still runs there is stopped as the stop button stops it
	 * ({@link TaskProgressTracker#cancelRuns()}), which ends its waits for an approval or an answer,
	 * and the tracker is closed and taken out of the window. The conversation asked for gets a new
	 * tracker in its place. A tracker that serves nobody yet is claimed for the account (see
	 * {@link TaskProgressTracker#claim(Person)}).
	 * </p>
	 *
	 * @param subSession
	 *        The window; <code>null</code> gives a tracker that is registered nowhere.
	 * @param conversation
	 *        The conversation key; <code>null</code> is the window's unnamed conversation.
	 * @param account
	 *        The person acting in the window; <code>null</code> for a window nobody is signed in to.
	 * @return The tracker; never <code>null</code>.
	 */
	public static TaskProgressTracker tracker(TLSubSessionContext subSession, String conversation, Person account) {
		if (subSession == null) {
			TaskProgressTracker detached = new TaskProgressTracker();
			detached.claim(account);
			return detached;
		}
		List<TaskProgressTracker> retired;
		TaskProgressTracker result;
		synchronized (subSession) {
			retired = retireOthers(subSession, account);
			TaskProgressTracker existing = tracker(subSession, conversation);
			if (existing != null) {
				// Serves the account or nobody: a tracker of another one was retired above.
				existing.claim(account);
				result = existing;
			} else {
				result = new TaskProgressTracker();
				result.claim(account);
				register(subSession, conversation, result);
			}
		}
		for (TaskProgressTracker tracker : retired) {
			retire(tracker);
		}
		return result;
	}

	/**
	 * Takes the trackers of the window that serve another account than the given one out of it.
	 *
	 * @return The trackers taken out, to be retired outside the window's lock.
	 */
	private static List<TaskProgressTracker> retireOthers(TLSubSessionContext subSession, Person account) {
		if (!subSession.isSet(CHAT_TRACKERS)) {
			return List.of();
		}
		Map<String, TaskProgressTracker> trackers = subSession.get(CHAT_TRACKERS);
		List<TaskProgressTracker> retired = new ArrayList<>();
		synchronized (trackers) {
			trackers.values().removeIf(tracker -> {
				Person served = tracker.getAccount();
				if (served == null || served.equals(account)) {
					return false;
				}
				retired.add(tracker);
				return true;
			});
		}
		return retired;
	}

	/** Stops what a tracker of a former account of its window runs, and lets go of it. */
	private static void retire(TaskProgressTracker tracker) {
		try {
			tracker.cancelRuns();
		} catch (RuntimeException ex) {
			Logger.warn("Could not stop the runs of a window's former account: " + ex.getMessage(), ex,
				ChatSessionRegistry.class);
		}
		close(tracker);
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

}
