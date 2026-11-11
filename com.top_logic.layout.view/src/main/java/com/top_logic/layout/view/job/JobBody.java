/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.List;

/**
 * The work a long-running job does.
 *
 * <p>
 * Run on a worker thread, outside the request that started it and outside any transaction: what the
 * body produces is handed back to the caller, which decides what to persist. The body reports what
 * it is doing through the {@link JobMonitor} it is given, and ends by returning its result, by
 * failing with an exception, or by being cancelled - which reaches it as an
 * {@link com.top_logic.basic.AbortExecutionException} out of the monitor, or out of a wait it
 * declared as interruptible.
 * </p>
 *
 * <p>
 * The body runs in the {@link com.top_logic.basic.thread.CancelScope} of its job, which a cancel
 * of the job cancels. A cancel interrupts the thread of the body only inside an interruptible
 * section of that scope: a blocking wait - a request over the network, a future, a sleep - is
 * wrapped in
 * {@link com.top_logic.basic.thread.CancelScope#runInterruptibly(com.top_logic.basic.thread.CancelScope.BlockingCall)}
 * or {@link com.top_logic.basic.thread.CancelScope#sleep(long)} to end at once on a cancel; access
 * to the database never is, since an interrupt reaching it closes the database file.
 * </p>
 *
 * <p>
 * A cancel stops the body only at the points it reports from, never in the middle of a commit. A
 * body that ends with a commit therefore declares it as its
 * {@link JobMonitor#uncancelable() point of no return}: a cancel asked for before that point ends
 * the job, and none is accepted after it, so the job never ends as cancelled with its work saved.
 * </p>
 */
public interface JobBody {

	/**
	 * Does the work of the job.
	 *
	 * @param job
	 *        What the body reports its progress to, and asks about its cancellation.
	 * @param arguments
	 *        The values the job was started with, in the order the caller declared them.
	 * @return The result of the job, handed to whatever waits for it.
	 * @throws Exception
	 *         If the work fails. An {@link com.top_logic.basic.exception.I18NRuntimeException}
	 *         carries its own message to the reader; any other failure is logged and reported
	 *         generically.
	 */
	Object run(JobMonitor job, List<Object> arguments) throws Exception;

}
