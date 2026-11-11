/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

/**
 * The handle on a running job that the display holds: what the reader can do about the job while it
 * runs.
 *
 * <p>
 * Every state of a job carries its control, so a button asking to stop the job reaches it from what
 * it displays. Cancelling is cooperative: it asks the job to stop, and the job ends at the next point
 * it reports from. A wait the job declares as an interruptible section of its
 * {@link com.top_logic.basic.thread.CancelScope} - TL-Script {@code sleep()} is one - ends at once;
 * work on the database is never interrupted.
 * </p>
 */
public interface JobControl {

	/**
	 * Whether this job may be cancelled at all.
	 *
	 * <p>
	 * A job that declares itself cancelable is one whose work may be given up half-done; for any
	 * other job {@link #cancel()} does nothing. A cancelable job stops being so when its body
	 * reaches its {@link JobMonitor#uncancelable() point of no return}.
	 * </p>
	 */
	boolean isCancelable();

	/**
	 * Asks the job to stop.
	 *
	 * <p>
	 * The job ends as soon as it notices: the next call it makes to its {@link JobMonitor} throws,
	 * and a wait it runs through
	 * {@link com.top_logic.basic.thread.CancelScope#runInterruptibly(com.top_logic.basic.thread.CancelScope.BlockingCall)}
	 * or {@link com.top_logic.basic.thread.CancelScope#sleep(long)} ends with an
	 * {@link com.top_logic.basic.AbortExecutionException}. No other code of the job is interrupted,
	 * least of all its access to the database. The job ends {@link JobStatus#CANCELLED} either
	 * way; what its body returns after being cancelled - the part of the work it got done - stays
	 * as the {@link JobState#result() result} of the cancelled job. Cancelling a job that is
	 * not {@link #isCancelable() cancelable} - any longer, past the
	 * {@link JobMonitor#uncancelable() point of no return} of its body - or has already finished
	 * does nothing.
	 * </p>
	 */
	void cancel();

}
