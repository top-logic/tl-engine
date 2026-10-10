/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.tl.ai.core.limits.RunLimitSettings;

/**
 * The installation's background subagents, counted over all chats, against
 * {@link RunLimitSettings#subagentWorkers()}.
 *
 * <p>
 * Each {@link SubagentJobRegistry} bounds its own chat; this bounds them all together. A registry that
 * finds no slot here keeps its job queued and is told when one frees up, so that it can start the job
 * then - nothing waits on a thread for a slot.
 * </p>
 */
final class SubagentSlots {

	private static int _running;

	/** Registries that found no slot and want to hear when one frees up, in the order they asked. */
	private static final Set<SubagentJobRegistry> WAITING = new LinkedHashSet<>();

	private SubagentSlots() {
		// Static.
	}

	/**
	 * Takes a slot for one subagent of the given registry.
	 *
	 * @return Whether there was one; when not, the registry is told once a slot frees up.
	 */
	static synchronized boolean tryAcquire(SubagentJobRegistry registry) {
		int limit = RunLimitSettings.subagentWorkers();
		if (limit > 0 && _running >= limit) {
			WAITING.add(registry);
			return false;
		}
		_running++;
		return true;
	}

	/** Gives back the slot of a subagent that has ended, and lets the waiting registries try again. */
	static void release() {
		List<SubagentJobRegistry> waiting;
		synchronized (SubagentSlots.class) {
			_running--;
			waiting = new ArrayList<>(WAITING);
			WAITING.clear();
		}
		// Outside the lock: a registry takes its own lock to start a job, then asks here again.
		for (SubagentJobRegistry registry : waiting) {
			registry.dispatch();
		}
	}

	/** Forgets a registry that is shut down. */
	static synchronized void forget(SubagentJobRegistry registry) {
		WAITING.remove(registry);
	}

	/** Whether the installation's ceiling is reached. */
	static synchronized boolean isFull() {
		int limit = RunLimitSettings.subagentWorkers();
		return limit > 0 && _running >= limit;
	}

	/** How many slots are taken. For tests. */
	static synchronized int running() {
		return _running;
	}
}
