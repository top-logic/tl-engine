/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;

/**
 * Thread-local binding of the run a tool call belongs to.
 *
 * <p>
 * A tool itself does not need this: it is handed the {@link ToolExecutionContext}, and
 * {@link ToolExecutionContext#runInfo()} answers the same question directly. This exists for what a
 * tool <em>calls</em> - application code, a model listener, a TL-Script function - which has no
 * context parameter and would otherwise have to have one threaded through every signature in
 * between, exactly as {@code RunFactsBinding} does for the environment block.
 * </p>
 *
 * <p>
 * {@code ToolExecutions#execute} binds it around every tool call, on the thread the tool actually
 * runs on - which is a worker thread for the tools of a concurrent batch - and unbinds it when the
 * call returns. Outside a tool call there is nothing bound, and {@link #current()} answers
 * {@link RunInfo#NONE}: this says "no run", never a stale one from the thread's previous task.
 * </p>
 *
 * @see ToolExecutionContext#runInfo()
 */
public final class RunInfoBinding {

	private static final ThreadLocalBinding<RunInfo> RUN = new ThreadLocalBinding<>();

	private RunInfoBinding() {
		// Static utility.
	}

	/**
	 * Binds {@code info} on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value (or its absence).
	 *
	 * <p>
	 * Prefer try-with-resources. Restoring rather than clearing is what makes this safe on the
	 * cached pools the agent runs on, where a thread may already carry a value from an earlier task.
	 * </p>
	 *
	 * @param info
	 *        What to bind; {@code null} or {@link RunInfo#NONE} binds nothing.
	 */
	public static Binding bind(RunInfo info) {
		RunInfo bound = (info == null || !info.isKnown()) ? null : info;
		return new Binding(RUN.bind(bound));
	}

	/**
	 * The run bound to the current thread; never {@code null}, {@link RunInfo#NONE} when none is
	 * bound.
	 */
	public static RunInfo current() {
		RunInfo bound = RUN.get();
		return bound == null ? RunInfo.NONE : bound;
	}

	/** Handle returned by {@link RunInfoBinding#bind(RunInfo)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<RunInfo>.Handle _handle;

		Binding(ThreadLocalBinding<RunInfo>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
