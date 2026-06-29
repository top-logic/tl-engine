/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

/**
 * Thread-local holder for the per-session {@link SubagentJobRegistry}, so worker threads
 * (which do not inherit thread-locals) can re-establish it. Mirrors
 * {@code com.top_logic.tl.ai.core.tools.task.TaskStoreBinding}.
 */
public class SubagentRegistryBinding {

	private static final ThreadLocal<SubagentJobRegistry> REGISTRY = new ThreadLocal<>();

	/**
	 * Binds the given registry on the current thread; the returned handle restores the
	 * previous value on {@link AutoCloseable#close()}.
	 */
	public static Binding bind(SubagentJobRegistry registry) {
		SubagentJobRegistry previous = REGISTRY.get();
		if (registry == null) {
			REGISTRY.remove();
		} else {
			REGISTRY.set(registry);
		}
		return new Binding(previous);
	}

	/** The bound registry, or {@code null}. */
	public static SubagentJobRegistry getRegistry() {
		return REGISTRY.get();
	}

	/** Handle returned by {@link SubagentRegistryBinding#bind(SubagentJobRegistry)}. */
	public static final class Binding implements AutoCloseable {

		private final SubagentJobRegistry _previous;

		private boolean _closed;

		Binding(SubagentJobRegistry previous) {
			_previous = previous;
		}

		@Override
		public void close() {
			if (_closed) {
				return;
			}
			_closed = true;
			if (_previous == null) {
				REGISTRY.remove();
			} else {
				REGISTRY.set(_previous);
			}
		}
	}
}
