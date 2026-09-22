/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

/**
 * Where a single phase of a job stands relative to the phase the job is in.
 *
 * <p>
 * Positional: it says whether the job has passed a phase, is at it, or has not reached it. Whether
 * the phase the job is at is still being worked on is what the {@link JobStatus} of the job says -
 * the phase a failed job stopped in is the {@link #ACTIVE} one.
 * </p>
 */
public enum PhaseStatus {

	/** The job has passed this phase - it ran, or it was skipped. */
	DONE,

	/** The phase the job is at. */
	ACTIVE,

	/** The job has not reached this phase. */
	PENDING

}
