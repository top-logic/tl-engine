/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

/**
 * How a long-running job ended, or that it has not ended yet.
 *
 * <p>
 * A job passes from {@link #RUNNING} to exactly one of the three terminal values and stays there:
 * what the display shows about a finished job - the result, the error, the phase it stopped in -
 * does not change any more.
 * </p>
 */
public enum JobStatus {

	/** The job is still working; its progress and phase still change. */
	RUNNING,

	/** The job ran to its end and produced its result. */
	COMPLETED,

	/** The job stopped with an error, which the state names. */
	FAILED,

	/** The job was cancelled, so it produced neither a result nor an error. */
	CANCELLED;

	/**
	 * Whether this is one of the terminal values, so nothing about the job changes any more.
	 */
	public boolean isFinished() {
		return this != RUNNING;
	}

}
