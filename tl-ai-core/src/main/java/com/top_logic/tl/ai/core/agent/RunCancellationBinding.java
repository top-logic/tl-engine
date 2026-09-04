/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.binding.BindingSnapshot;
import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Thread-local binding of the {@link CancellationToken} of the agent run executing on the current
 * thread.
 *
 * <p>
 * A run is cancelled by cancelling its token and then interrupting its thread. Code interrupted
 * further down the stack sees only a bare {@link InterruptedException} and cannot tell a
 * user-requested cancellation from an interrupt with any other cause (a tool call that ran into
 * its timeout, a shutdown, a bug). This binding supplies the missing marker: while
 * {@link #isRunCancelled()} holds, an interrupt on this thread is the expected consequence of the
 * user cancelling the run and may be handled as such; otherwise it is a genuine failure and must
 * still be reported as one.
 * </p>
 *
 * <p>
 * Registered with the binding registry, so {@link BindingSnapshot} carries it onto tool worker
 * threads together with the other bindings of the run.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.execution.AgentEnvironment
 */
public class RunCancellationBinding {

	private static final ThreadLocalBinding<CancellationToken> TOKEN = new ThreadLocalBinding<>();

	/**
	 * Binds the given token on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value (or absence).
	 */
	public static Binding bind(CancellationToken token) {
		return new Binding(TOKEN.bind(token));
	}

	/**
	 * The {@link CancellationToken} of the run executing on the current thread, or {@code null} if
	 * the thread does not run an agent.
	 */
	public static CancellationToken getToken() {
		return TOKEN.get();
	}

	/**
	 * Whether the current thread runs an agent whose cancellation has been requested.
	 *
	 * <p>
	 * {@code false} for a thread that runs no agent at all, and for a run that has not been
	 * cancelled. An {@link InterruptedException} observed while this holds is expected; one
	 * observed while it does not is a failure.
	 * </p>
	 */
	public static boolean isRunCancelled() {
		CancellationToken token = TOKEN.get();
		return token != null && token.isCancelled();
	}

	/**
	 * Handle returned by {@link RunCancellationBinding#bind(CancellationToken)}.
	 */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<CancellationToken>.Handle _handle;

		Binding(ThreadLocalBinding<CancellationToken>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
