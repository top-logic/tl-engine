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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.Logger;

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
 * A registry nobody has borrowed for {@link #ABANDONED_AFTER_MILLIS} - the window that showed it was
 * closed, the run that used it ended long ago - is cancelled and forgotten even when it still has
 * work: no lead reads what its subagents report, and its jobs, their logs and the listeners of a
 * window that is gone would otherwise stay in memory for the life of the process. A chat window
 * {@link #attach(String) attaches} to its registry for as long as it listens to it.
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

	/**
	 * How long a registry nobody borrows is kept before it is cancelled and forgotten: a day, so that
	 * a window that was only reloaded or a chat the user comes back to the same day never meets it.
	 */
	static final long ABANDONED_AFTER_MILLIS = TimeUnit.HOURS.toMillis(24);

	/** How often, at most, {@link #expireAbandoned(long)} looks at all registries. */
	private static final long SWEEP_MILLIS = TimeUnit.MINUTES.toMillis(1);

	/** When {@link #expireAbandoned(long)} last ran. */
	private static long _lastSweep;

	/** Between the conversation's key and the window in a {@link #windowKey(String, String)}. */
	private static final char WINDOW_SEPARATOR = '@';

	/** By key, least recently asked for first. */
	private static final Map<String, Entry> REGISTRIES = new LinkedHashMap<>(16, 0.75f, true);

	/**
	 * The subagent id counter of each conversation, by its {@link #conversationKey(String) key}, least
	 * recently asked for first; guarded by {@link #REGISTRIES}. See {@link #idsOf(String)}.
	 */
	private static final Map<String, AtomicInteger> IDS = new LinkedHashMap<>(16, 0.75f, true);

	/** A registry, how many runs and windows borrow it now, and since when nobody does. */
	private static final class Entry {

		final SubagentJobRegistry _registry;

		int _borrowers;

		long _unborrowedSince = System.currentTimeMillis();

		Entry(SubagentJobRegistry registry) {
			_registry = registry;
		}

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
		sweepIfDue();
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
		return lease(key, true);
	}

	/**
	 * Borrows the registry for the given key, created on first use, for a window that listens to it.
	 *
	 * <p>
	 * Unlike {@link #borrow(String)}, closing the last lease keeps an idle registry: the window may
	 * show the chat again, and its subagents' ids go on from where they were. A registry nobody
	 * borrows is forgotten after {@link #ABANDONED_AFTER_MILLIS}.
	 * </p>
	 */
	public static Lease attach(String key) {
		return lease(key, false);
	}

	private static Lease lease(String key, boolean forgetIfIdle) {
		sweepIfDue();
		synchronized (REGISTRIES) {
			Entry entry = entry(key);
			entry._borrowers++;
			return new Lease(key, entry, forgetIfIdle);
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
			// The conversation is gone, and with it every id it handed out.
			IDS.remove(conversationKey(conversationId));
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

	/**
	 * The entry for the given key, created on first use. Must be called holding the lock.
	 *
	 * <p>
	 * A new registry numbers its subagents with the counter of the other registries of its
	 * conversation, if one is kept, so that the windows of a conversation never hand out the same id.
	 * </p>
	 */
	private static Entry entry(String key) {
		Entry entry = REGISTRIES.get(key);
		if (entry == null) {
			entry = new Entry(new SubagentJobRegistry(SubagentJobRegistry.DEFAULT_MAX_CONCURRENT, idsOf(key)));
			REGISTRIES.put(key, entry);
		}
		dropIdle(entry);
		return entry;
	}

	/**
	 * The subagent id counter of the conversation of the given key, shared by all its registries. Must
	 * be called holding the lock.
	 *
	 * <p>
	 * Kept apart from the registries, so that it outlives one that expired or was forgotten while idle:
	 * a registry made for the conversation later counts on. Counters of conversations that have no
	 * registry any more are dropped beyond {@link #MAX_IDLE}, oldest first; a spawn then still counts on
	 * from the lead's log - see {@link SubagentJobRegistry#countOnFrom(int)}.
	 * </p>
	 */
	private static AtomicInteger idsOf(String key) {
		AtomicInteger ids = IDS.computeIfAbsent(conversationPart(key), k -> new AtomicInteger());
		int excess = IDS.size() - MAX_IDLE;
		for (Iterator<String> it = IDS.keySet().iterator(); excess > 0 && it.hasNext();) {
			String conversation = it.next();
			if (!hasRegistry(conversation)) {
				it.remove();
				excess--;
			}
		}
		return ids;
	}

	private static boolean hasRegistry(String conversation) {
		for (String key : REGISTRIES.keySet()) {
			if (conversationPart(key).equals(conversation)) {
				return true;
			}
		}
		return false;
	}

	/** The key without its window: the {@link #conversationKey(String) conversation's key}. */
	private static String conversationPart(String key) {
		int window = key.indexOf(WINDOW_SEPARATOR);
		return window < 0 ? key : key.substring(0, window);
	}

	/**
	 * Runs {@link #expireAbandoned(long)} when it has not run for {@link #SWEEP_MILLIS}. Called before
	 * the lock is taken, because cancelling subagents is not something to do while holding it.
	 */
	private static void sweepIfDue() {
		long now = System.currentTimeMillis();
		synchronized (REGISTRIES) {
			if (now - _lastSweep < SWEEP_MILLIS) {
				return;
			}
			_lastSweep = now;
		}
		expireAbandoned(now);
	}

	/**
	 * Cancels and forgets every registry nobody has borrowed for {@link #ABANDONED_AFTER_MILLIS}.
	 *
	 * @param now
	 *        The time to measure against.
	 * @return How many registries were forgotten.
	 */
	static int expireAbandoned(long now) {
		List<Entry> expired = new ArrayList<>();
		synchronized (REGISTRIES) {
			for (Iterator<Entry> it = REGISTRIES.values().iterator(); it.hasNext();) {
				Entry entry = it.next();
				if (entry._borrowers == 0 && now - entry._unborrowedSince >= ABANDONED_AFTER_MILLIS) {
					expired.add(entry);
					it.remove();
				}
			}
		}
		int cancelled = 0;
		for (Entry entry : expired) {
			cancelled += cancel(entry);
		}
		if (!expired.isEmpty()) {
			Logger.info("Forgot " + expired.size() + " subagent registr" + (expired.size() == 1 ? "y" : "ies")
				+ " nobody had used for " + TimeUnit.MILLISECONDS.toHours(ABANDONED_AFTER_MILLIS)
				+ " hours; cancelled " + cancelled + " subagent(s) still at work in them.", SubagentRegistries.class);
		}
		return expired.size();
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

		private final boolean _forgetIfIdle;

		private boolean _closed;

		Lease(String key, Entry entry, boolean forgetIfIdle) {
			_key = key;
			_entry = entry;
			_forgetIfIdle = forgetIfIdle;
		}

		/** The borrowed registry. */
		public SubagentJobRegistry registry() {
			return _entry._registry;
		}

		/**
		 * Lets go of the registry. A run's lease forgets it when nobody else borrows it and it has no
		 * work left; see {@link SubagentRegistries#borrow(String)}.
		 */
		@Override
		public void close() {
			synchronized (REGISTRIES) {
				if (_closed) {
					return;
				}
				_closed = true;
				_entry._borrowers--;
				if (_entry._borrowers > 0) {
					return;
				}
				_entry._unborrowedSince = System.currentTimeMillis();
				if (_forgetIfIdle && _entry._registry.isIdle() && REGISTRIES.get(_key) == _entry) {
					REGISTRIES.remove(_key);
				}
			}
		}
	}
}
