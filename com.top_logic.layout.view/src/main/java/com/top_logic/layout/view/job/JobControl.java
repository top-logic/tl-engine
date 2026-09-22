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
 * it displays. Cancelling is cooperative: it asks the job to stop and interrupts what it is waiting
 * for, and the job ends at the next point it reports from.
 * </p>
 */
public interface JobControl {

	/**
	 * Whether this job may be cancelled at all.
	 *
	 * <p>
	 * A job that declares itself cancelable is one whose work may be given up half-done; for any
	 * other job {@link #cancel()} does nothing.
	 * </p>
	 */
	boolean isCancelable();

	/**
	 * Asks the job to stop.
	 *
	 * <p>
	 * The job ends as soon as it notices: the next call it makes to its {@link JobMonitor} throws,
	 * and what it waits for is interrupted. A result it produces after being cancelled is
	 * discarded, so the job ends {@link JobStatus#CANCELLED} either way. Cancelling a job that is
	 * not {@link #isCancelable() cancelable} or has already finished does nothing.
	 * </p>
	 */
	void cancel();

}
