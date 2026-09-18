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
 * {@link com.top_logic.basic.AbortExecutionException} out of the monitor, or as an interrupt of
 * whatever it is waiting for.
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
