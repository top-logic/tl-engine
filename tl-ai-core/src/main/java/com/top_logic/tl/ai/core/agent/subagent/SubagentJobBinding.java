/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding of the {@link SubagentJob} the run on this thread is - bound by the spawner on
 * a background subagent's thread, so that a tool of the subagent, {@code ask_lead}, can reach its own
 * job; unbound everywhere else.
 */
public final class SubagentJobBinding {

	private static final ThreadLocalBinding<SubagentJob> JOB = new ThreadLocalBinding<>();

	private SubagentJobBinding() {
		// Static binding.
	}

	/**
	 * Binds the given job on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(SubagentJob job) {
		return new Binding(JOB.bind(job));
	}

	/** The job of the subagent running on this thread, or <code>null</code>. */
	public static SubagentJob getJob() {
		return JOB.get();
	}

	/** Handle returned by {@link SubagentJobBinding#bind(SubagentJob)}. */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<SubagentJob>.Handle _handle;

		Binding(ThreadLocalBinding<SubagentJob>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
