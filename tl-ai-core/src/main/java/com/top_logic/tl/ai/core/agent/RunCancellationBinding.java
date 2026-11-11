/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.thread.BindingSnapshot;
import com.top_logic.basic.thread.CancelScope;
import com.top_logic.basic.thread.CancelScope.BlockingCall;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Thread-local binding of the {@link CancellationToken} of the agent run executing on the current
 * thread.
 *
 * <p>
 * A run is cancelled by cancelling its token, never by interrupting its thread: an interrupt that
 * reaches a thread while it reads the database file closes H2's file channel, and with it the
 * database of the whole application. Code that waits for something only an interrupt can end early
 * - an HTTP request, a future of another thread - waits through {@link #whileBlocked(BlockingCall)},
 * which lets a cancellation interrupt exactly that wait. Code that needs no context to be handed in
 * finds the run's token here.
 * </p>
 *
 * <p>
 * The token is also bound as the thread's {@link CancelScope#current() cancel scope}, so code that
 * knows nothing of agents and waits through {@link CancelScope#runInterruptibly(BlockingCall)} or
 * {@link CancelScope#sleep(long)} is ended by the cancellation of the run. Both bindings are {@link ThreadLocalBinding}s, so {@link BindingSnapshot}
 * carries them onto tool worker threads together with the other bindings of the run.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.execution.AgentEnvironment
 */
public class RunCancellationBinding {

	private static final ThreadLocalBinding<CancellationToken> TOKEN = new ThreadLocalBinding<>();

	/**
	 * Binds the given token on the current thread, as the run's token and as the thread's
	 * {@link CancelScope#current() cancel scope}, and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound values (or absence).
	 */
	public static Binding bind(CancellationToken token) {
		ThreadLocalBinding<CancellationToken>.Handle tokenHandle = TOKEN.bind(token);
		ThreadLocalBinding<CancelScope>.Handle scopeHandle = CancelScope.bind(token);
		return new Binding(tokenHandle, scopeHandle);
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
	 * cancelled.
	 * </p>
	 */
	public static boolean isRunCancelled() {
		CancellationToken token = TOKEN.get();
		return token != null && token.isCancelled();
	}

	/**
	 * Runs a blocking call that the cancellation of the current thread's run is to end.
	 *
	 * <p>
	 * {@link CancellationToken#whileBlocked(BlockingCall)} of the bound token; without a run on this
	 * thread the call simply runs. The call must not touch the knowledge base.
	 * </p>
	 *
	 * @see CancellationToken#whileBlocked(BlockingCall)
	 */
	public static <T, E extends Exception> T whileBlocked(BlockingCall<T, E> call) throws E, InterruptedException {
		CancellationToken token = TOKEN.get();
		return token == null ? call.call() : token.whileBlocked(call);
	}

	/**
	 * Waits for a future of another thread, so that the cancellation of the current thread's run
	 * ends the wait.
	 *
	 * @see CancellationToken#await(Future, long, TimeUnit)
	 */
	public static <T> T await(Future<T> future, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		CancellationToken token = TOKEN.get();
		return token == null ? future.get(timeout, unit) : token.await(future, timeout, unit);
	}

	/**
	 * Handle returned by {@link RunCancellationBinding#bind(CancellationToken)}.
	 */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<CancellationToken>.Handle _tokenHandle;

		private final ThreadLocalBinding<CancelScope>.Handle _scopeHandle;

		Binding(ThreadLocalBinding<CancellationToken>.Handle tokenHandle,
				ThreadLocalBinding<CancelScope>.Handle scopeHandle) {
			_tokenHandle = tokenHandle;
			_scopeHandle = scopeHandle;
		}

		@Override
		public void close() {
			_scopeHandle.close();
			_tokenHandle.close();
		}
	}
}
