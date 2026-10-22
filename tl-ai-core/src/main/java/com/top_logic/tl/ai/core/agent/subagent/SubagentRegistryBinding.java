/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import com.top_logic.tl.ai.core.binding.BindingSnapshot;
import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding for the per-session {@link SubagentJobRegistry}.
 *
 * <p>Prefer {@link #bind(SubagentJobRegistry)} with try-with-resources. The previous value is
 * captured at bind time and restored on close, making the binding safe across cached thread
 * pools. Registered with the binding registry, so {@link BindingSnapshot} carries it onto
 * parallel tool-execution worker threads.</p>
 */
public class SubagentRegistryBinding {

	private static final ThreadLocalBinding<SubagentJobRegistry> REGISTRY = new ThreadLocalBinding<>();

	/**
	 * Binds the given registry on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(SubagentJobRegistry registry) {
		return new Binding(REGISTRY.bind(registry));
	}

	/** The bound registry, or {@code null}. */
	public static SubagentJobRegistry getRegistry() {
		return REGISTRY.get();
	}

	/** Handle returned by {@link SubagentRegistryBinding#bind(SubagentJobRegistry)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<SubagentJobRegistry>.Handle _handle;

		Binding(ThreadLocalBinding<SubagentJobRegistry>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
