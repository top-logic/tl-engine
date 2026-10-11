/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link SubagentJobRegistry registries} of this process, one per conversation.
 *
 * <p>
 * A background subagent outlives the turn that started it, and it must outlive whatever showed that
 * turn too: a chat window that drops a conversation from its cache, or reloads it because another
 * window wrote to it, must not take the conversation's running subagents down with it. So the
 * registries are kept here, by a key the caller chooses - the conversation, and in the chat also the
 * window - and the chat only borrows them. Every entry point that records its run under a
 * conversation gets one, because {@code AgentRunner} binds it when nothing else is bound.
 * </p>
 *
 * <p>
 * Kept for the process, not for a service instance: an administrator saving the agent runtime's
 * configuration restarts that service, and that is no reason to stop work in progress. A registry
 * nobody needs any more - no job queued or running, nothing undelivered - is forgotten once more than
 * {@link #MAX_IDLE} have accumulated, least recently asked for first. A registry that a run only
 * {@link #borrow(String) borrows} is forgotten as soon as the last borrower lets go and it is idle.
 * </p>
 *
 * <p>
 * Every registry of one conversation has a key that starts with that conversation's
 * {@link #conversationKey(String) key}, so deleting the conversation finds and stops all of them -
 * those of every window and the one of a run without a window - with
 * {@link #removeConversation(String)}.
 * </p>
 */
public final class SubagentRegistries {

	/** How many idle registries are kept before the oldest are dropped. */
	static final int MAX_IDLE = 1024;

	/** Between the conversation's key and the window in a {@link #windowKey(String, String)}. */
	private static final char WINDOW_SEPARATOR = '@';

	/** By key, least recently asked for first. */
	private static final Map<String, Entry> REGISTRIES = new LinkedHashMap<>(16, 0.75f, true);

	/** A registry and how many runs borrow it now. */
	private static final class Entry {

		final SubagentJobRegistry _registry = new SubagentJobRegistry();

		int _borrowers;

	}

	private SubagentRegistries() {
		// Static.
	}

	/**
	 * The registry for the given key, created on first use.
	 *
	 * @param key
	 *        Which registry; see {@link #conversationKey(String)}.
	 */
	public static SubagentJobRegistry forKey(String key) {
		synchronized (REGISTRIES) {
			return entry(key)._registry;
		}
	}

	/**
	 * Borrows the registry for the given key, created on first use, until the returned lease is
	 * closed.
	 *
	 * <p>
	 * For a run that needs the registry only while it works, and maybe its subagents after it: when
	 * the last lease is closed and the registry is idle, it is forgotten at once. A registry that still
	 * has work stays, as one asked for with {@link #forKey(String)} does.
	 * </p>
	 */
	public static Lease borrow(String key) {
		synchronized (REGISTRIES) {
			Entry entry = entry(key);
			entry._borrowers++;
			return new Lease(key, entry);
		}
	}

	/** The registry for the given key, or <code>null</code> if none was created. */
	public static SubagentJobRegistry find(String key) {
		synchronized (REGISTRIES) {
			Entry entry = REGISTRIES.get(key);
			return entry == null ? null : entry._registry;
		}
	}

	/**
	 * The registries of the given conversation: the one of a run without a window, and those of every
	 * window that shows it.
	 */
	public static List<SubagentJobRegistry> ofConversation(String conversationId) {
		List<SubagentJobRegistry> result = new ArrayList<>();
		synchronized (REGISTRIES) {
			for (Map.Entry<String, Entry> entry : REGISTRIES.entrySet()) {
				if (belongsTo(entry.getKey(), conversationId)) {
					result.add(entry.getValue()._registry);
				}
			}
		}
		return result;
	}

	/**
	 * Stops and forgets the registry for the given key, because its conversation is gone.
	 *
	 * @return How many of its subagents were cancelled.
	 */
	public static int remove(String key) {
		Entry entry;
		synchronized (REGISTRIES) {
			entry = REGISTRIES.remove(key);
		}
		if (entry == null) {
			return 0;
		}
		return cancel(entry);
	}

	/**
	 * Stops and forgets every registry of the given conversation - see
	 * {@link #ofConversation(String)} - because the conversation was deleted.
	 *
	 * @return How many subagents were cancelled.
	 */
	public static int removeConversation(String conversationId) {
		List<Entry> removed = new ArrayList<>();
		synchronized (REGISTRIES) {
			for (Iterator<Map.Entry<String, Entry>> it = REGISTRIES.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Entry> entry = it.next();
				if (belongsTo(entry.getKey(), conversationId)) {
					removed.add(entry.getValue());
					it.remove();
				}
			}
		}
		int cancelled = 0;
		for (Entry entry : removed) {
			cancelled += cancel(entry);
		}
		return cancelled;
	}

	/** The key of a conversation's registry, for an entry point that has no window of its own. */
	public static String conversationKey(String conversationId) {
		return "conversation:" + conversationId;
	}

	/**
	 * The key of a conversation's registry in one window: each window keeps its own conversation, and
	 * a lead turn reads the results of its own subagents.
	 */
	public static String windowKey(String conversationId, String window) {
		return conversationKey(conversationId) + WINDOW_SEPARATOR + window;
	}

	private static boolean belongsTo(String key, String conversationId) {
		String conversationKey = conversationKey(conversationId);
		return key.equals(conversationKey)
			|| (key.startsWith(conversationKey) && key.charAt(conversationKey.length()) == WINDOW_SEPARATOR);
	}

	/** How many registries are kept. For tests. */
	static int size() {
		synchronized (REGISTRIES) {
			return REGISTRIES.size();
		}
	}

	/** The entry for the given key, created on first use. Must be called holding the lock. */
	private static Entry entry(String key) {
		Entry entry = REGISTRIES.computeIfAbsent(key, k -> new Entry());
		dropIdle(entry);
		return entry;
	}

	/**
	 * Cancelled, not shut down: interrupting the pool would end a subagent as failed before its
	 * cancellation is noticed, and the pool lets its threads go once they are idle.
	 */
	private static int cancel(Entry entry) {
		return entry._registry.cancelAll();
	}

	/**
	 * Forgets idle registries beyond {@link #MAX_IDLE}, without shutting them down: one may still be
	 * bound to a run that has not spawned yet, and an idle registry holds no thread anyway - its pool
	 * lets idle threads go. A borrowed registry is in use and is kept.
	 */
	private static void dropIdle(Entry asked) {
		int excess = REGISTRIES.size() - MAX_IDLE;
		for (Iterator<Entry> it = REGISTRIES.values().iterator(); excess > 0 && it.hasNext();) {
			Entry entry = it.next();
			if (entry != asked && entry._borrowers == 0 && entry._registry.isIdle()) {
				it.remove();
				excess--;
			}
		}
	}

	/**
	 * A run's use of a registry, from {@link SubagentRegistries#borrow(String)} until it is closed.
	 */
	public static final class Lease implements AutoCloseable {

		private final String _key;

		private final Entry _entry;

		private boolean _closed;

		Lease(String key, Entry entry) {
			_key = key;
			_entry = entry;
		}

		/** The borrowed registry. */
		public SubagentJobRegistry registry() {
			return _entry._registry;
		}

		/**
		 * Lets go of the registry, and forgets it when nobody else borrows it and it has no work left.
		 */
		@Override
		public void close() {
			synchronized (REGISTRIES) {
				if (_closed) {
					return;
				}
				_closed = true;
				_entry._borrowers--;
				if (_entry._borrowers == 0 && _entry._registry.isIdle() && REGISTRIES.get(_key) == _entry) {
					REGISTRIES.remove(_key);
				}
			}
		}
	}
}
