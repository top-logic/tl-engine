/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * What is known about a long-running job at one moment: an immutable snapshot.
 *
 * <p>
 * A job publishes its state by handing a new snapshot to the channel it reports on; nothing about a
 * snapshot already published changes afterwards. The display therefore reads a job the way it reads
 * any other value of a channel - a condition over it, a derived channel from it and a listener on
 * it all see one consistent picture of the job.
 * </p>
 *
 * @param status
 *        Whether the job still runs, and how it ended when it does not.
 * @param phases
 *        The steps the job announced it goes through, in order; empty for a job that does not name
 *        its steps.
 * @param currentPhase
 *        The index in {@link #phases()} of the phase the job is at, {@code -1} before it entered
 *        the first one and the number of phases once it has passed the last one.
 * @param fraction
 *        How much of the work is done, between 0 and 1, or {@code null} when the job does not know
 *        - an indeterminate progress the display sweeps rather than fills.
 * @param message
 *        What the job says about what it is doing right now, or {@code null} for nothing; a plain
 *        text is written as {@link ResKey#text(String)}.
 * @param startedAt
 *        When the job was started, the instant an elapsed time is counted from.
 * @param finishedAt
 *        When the job ended, or {@code null} while it runs.
 * @param result
 *        What the job produced, only for a {@link JobStatus#COMPLETED} one; {@code null} otherwise.
 * @param error
 *        Why the job failed, only for a {@link JobStatus#FAILED} one; {@code null} otherwise.
 * @param control
 *        The handle the display cancels the job through, {@code null} for a job that is not
 *        observed by one.
 */
public record JobState(JobStatus status, List<JobPhase> phases, int currentPhase, Double fraction,
		ResKey message, Date startedAt, Date finishedAt, Object result, ResKey error, JobControl control) {

	/**
	 * Creates a {@link JobState}, holding on to the phases as a list of its own.
	 */
	public JobState {
		phases = phases == null ? List.of() : List.copyOf(phases);
	}

	/**
	 * Whether the job is still working, so this is not the last word about it.
	 */
	public boolean isRunning() {
		return status == JobStatus.RUNNING;
	}

	/**
	 * Whether the job has ended, however it ended.
	 */
	public boolean isFinished() {
		return status.isFinished();
	}

	/**
	 * Whether the job does not know how much of its work is done, so its progress has no length.
	 */
	public boolean isIndeterminate() {
		return fraction == null;
	}

	/**
	 * The phase the job is at, or {@code null} before it entered the first one, after it passed the
	 * last one, and for a job that does not name its steps.
	 */
	public JobPhase phase() {
		return currentPhase >= 0 && currentPhase < phases.size() ? phases.get(currentPhase) : null;
	}

	/**
	 * Where the phase at the given index stands relative to the phase the job is at.
	 *
	 * @param index
	 *        The position in {@link #phases()}.
	 * @return {@link PhaseStatus#DONE} for a phase the job has passed - one it ran and one it
	 *         skipped alike - {@link PhaseStatus#ACTIVE} for the one it is at, and
	 *         {@link PhaseStatus#PENDING} for one it has not reached.
	 */
	public PhaseStatus phaseStatus(int index) {
		if (index < currentPhase) {
			return PhaseStatus.DONE;
		}
		return index == currentPhase ? PhaseStatus.ACTIVE : PhaseStatus.PENDING;
	}

	/**
	 * Whether the reader may ask this job to stop right now.
	 */
	public boolean isCancelable() {
		return isRunning() && control != null && control.isCancelable();
	}

}
