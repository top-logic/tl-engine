/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.Iterator;
import java.util.LinkedHashMap;
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
 * {@link #MAX_IDLE} have accumulated, least recently asked for first.
 * </p>
 */
public final class SubagentRegistries {

	/** How many idle registries are kept before the oldest are dropped. */
	static final int MAX_IDLE = 1024;

	/** By key, least recently asked for first. */
	private static final Map<String, SubagentJobRegistry> REGISTRIES = new LinkedHashMap<>(16, 0.75f, true);

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
			SubagentJobRegistry registry = REGISTRIES.computeIfAbsent(key, k -> new SubagentJobRegistry());
			dropIdle(registry);
			return registry;
		}
	}

	/** The registry for the given key, or <code>null</code> if none was created. */
	public static SubagentJobRegistry find(String key) {
		synchronized (REGISTRIES) {
			return REGISTRIES.get(key);
		}
	}

	/**
	 * Stops and forgets the registry for the given key, because its conversation is gone.
	 *
	 * @return How many of its subagents were cancelled.
	 */
	public static int remove(String key) {
		SubagentJobRegistry registry;
		synchronized (REGISTRIES) {
			registry = REGISTRIES.remove(key);
		}
		if (registry == null) {
			return 0;
		}
		// Cancelled, not shut down: interrupting the pool would end a subagent as failed before its
		// cancellation is noticed, and the pool lets its threads go once they are idle.
		return registry.cancelAll();
	}

	/** The key of a conversation's registry, for an entry point that has no window of its own. */
	public static String conversationKey(String conversationId) {
		return "conversation:" + conversationId;
	}

	/** How many registries are kept. For tests. */
	static int size() {
		synchronized (REGISTRIES) {
			return REGISTRIES.size();
		}
	}

	/**
	 * Forgets idle registries beyond {@link #MAX_IDLE}, without shutting them down: one may still be
	 * bound to a run that has not spawned yet, and an idle registry holds no thread anyway - its pool
	 * lets idle threads go.
	 */
	private static void dropIdle(SubagentJobRegistry asked) {
		int excess = REGISTRIES.size() - MAX_IDLE;
		for (Iterator<SubagentJobRegistry> it = REGISTRIES.values().iterator(); excess > 0 && it.hasNext();) {
			SubagentJobRegistry registry = it.next();
			if (registry != asked && registry.isIdle()) {
				it.remove();
				excess--;
			}
		}
	}
}
