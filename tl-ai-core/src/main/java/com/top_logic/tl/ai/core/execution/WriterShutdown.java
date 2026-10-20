/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.Logger;

/**
 * Stops an executor whose tasks write the knowledge base, without interrupting the task it runs.
 *
 * <p>
 * {@link ExecutorService#shutdownNow()} interrupts the running task, and an interrupt that reaches a
 * thread while it reads the database file closes H2's file channel - for every thread of the
 * application, for good. A service that is restarted while its writer commits would take the
 * database with it. This lets the running and the queued tasks finish instead, waiting a bounded
 * time for them.
 * </p>
 */
public final class WriterShutdown {

	/** How long {@link #stop(ExecutorService, String)} waits for the tasks to finish. */
	private static final long WAIT_SECONDS = 10;

	private WriterShutdown() {
		// Static helper.
	}

	/**
	 * Accepts no more tasks, and waits up to {@value #WAIT_SECONDS} seconds for those submitted to
	 * finish. A task that takes longer is left to finish on its own; nothing is interrupted.
	 *
	 * @param executor
	 *        The executor to stop.
	 * @param what
	 *        What the executor writes, for the log.
	 */
	public static void stop(ExecutorService executor, String what) {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS)) {
				Logger.warn("The " + what + " writer did not finish within " + WAIT_SECONDS
					+ " seconds; it is left to finish on its own.", WriterShutdown.class);
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
