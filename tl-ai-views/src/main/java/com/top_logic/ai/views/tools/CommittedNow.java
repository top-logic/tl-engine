/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.util.TLContextManager;

/**
 * Runs a change of view files and its change-set record in a transaction of their own, committed
 * when the call returns.
 *
 * <p>
 * A view file is written when the tool runs; nothing can take it back. Its record must therefore
 * not wait for the turn's transaction, which a later tool call can roll back and leave a written
 * file without its record. That transaction is bound to the agent thread, so a transaction begun
 * there would only nest into it: the work runs on a thread of its own instead, while the caller
 * waits. Its interaction is not one of the run's sub-session, whose interaction on the agent thread
 * stays open meanwhile: it runs in a sub-session derived from the run's, so that it checks and
 * reads with the user's rights, and its commit does not move the session revision of the waiting
 * caller. The caller reads what was committed once it has caught up with it, see
 * {@link ToolExecutionContext#catchUpWithCommits()}.
 * </p>
 */
final class CommittedNow {

	private CommittedNow() {
		// Static utility.
	}

	/**
	 * Runs {@code action} in its own transaction and returns its result.
	 *
	 * <p>
	 * The result crosses threads: return values, not objects to keep working with.
	 * </p>
	 */
	static <T> T run(Supplier<T> action) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		ComputationEx2<T, RuntimeException, RuntimeException> job = () -> KBUtils.inTransaction(action);
		FutureTask<T> task = new FutureTask<>(() -> subSession == null
			? ThreadContextManager.inContext(null, job)
			: ThreadContextManager.inDerivedContext(subSession, job));
		Thread thread = new Thread(task, "view-change");
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get();
		} catch (ExecutionException ex) {
			if (ex.getCause() instanceof RuntimeException cause) {
				throw cause;
			}
			if (ex.getCause() instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException(ex.getCause());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while writing a view change.", ex);
		}
	}
}
