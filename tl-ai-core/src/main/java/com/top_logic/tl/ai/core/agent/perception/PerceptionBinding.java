/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.perception;

import com.top_logic.tl.ai.core.binding.BindingSnapshot;
import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for the per-run {@link PerceptionContext}.
 *
 * <p>
 * Bound by {@link com.top_logic.tl.ai.core.agent.AgentLoop} for the duration of a run, so any tool
 * executing inside that run can queue content for the model to see. Registered with the binding
 * registry, so {@link BindingSnapshot} carries it onto parallel tool-execution worker threads.
 * </p>
 */
public class PerceptionBinding {

	private static final ThreadLocalBinding<PerceptionContext> CONTEXT = new ThreadLocalBinding<>();

	/**
	 * Binds the given context on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(PerceptionContext context) {
		return new Binding(CONTEXT.bind(context));
	}

	/**
	 * The bound context, or {@code null} when the caller does not run inside an agent loop.
	 */
	public static PerceptionContext getContext() {
		return CONTEXT.get();
	}

	/** Handle returned by {@link PerceptionBinding#bind(PerceptionContext)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<PerceptionContext>.Handle _handle;

		Binding(ThreadLocalBinding<PerceptionContext>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
