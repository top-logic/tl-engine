/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * What a {@link ReactJobStatusControl} displays about a long-running job: one immutable
 * description, in the terms the display speaks.
 *
 * <p>
 * Everything is resolved already - the texts are the ones the reader sees, and what may be done
 * about the job is the {@link #cancel()} handler rather than a handle on the job. Whoever follows a
 * job hands the display such a description whenever the job reports, so the control needs to know
 * neither where the job runs nor what it works on.
 * </p>
 *
 * @param status
 *        Whether the job still runs, and how it ended when it does not.
 * @param phases
 *        The steps the job goes through, in order, each with where it stands; empty for a job that
 *        does not name its steps.
 * @param fraction
 *        How much of the work is done, between 0 and 1, or {@code null} when that is unknown - a
 *        bar the client sweeps rather than fills.
 * @param message
 *        What the job says about what it is doing right now, or {@code null} for nothing.
 * @param startedAt
 *        When the job was started, the instant the elapsed time is counted from, or {@code null}
 *        for a display without one.
 * @param finishedAt
 *        When the job ended, or {@code null} while it runs - the elapsed time then keeps running.
 * @param result
 *        What the job produced, as the text naming it, or {@code null} for a job that produced
 *        nothing to show.
 * @param error
 *        Why the job failed, or {@code null} for a job that did not.
 * @param cancel
 *        What asking the job to stop does, or {@code null} for a job the reader may not stop.
 */
public record JobDisplay(Status status, List<Phase> phases, Double fraction, String message, Date startedAt,
		Date finishedAt, String result, String error, Runnable cancel) {

	/**
	 * How a displayed job stands: still working, or ended in one of the three ways a job ends.
	 */
	public enum Status implements ExternallyNamed {

		/** The job is still working. */
		RUNNING("running"),

		/** The job ran to its end. */
		COMPLETED("completed"),

		/** The job stopped with an error. */
		FAILED("failed"),

		/** The job was stopped before it reached its end. */
		CANCELLED("cancelled");

		private final String _externalName;

		Status(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Where a single phase stands relative to the phase the job is at.
	 */
	public enum PhaseState implements ExternallyNamed {

		/** The job has passed this phase. */
		DONE("done"),

		/** The phase the job is at. */
		ACTIVE("active"),

		/** The job has not reached this phase. */
		PENDING("pending");

		private final String _externalName;

		PhaseState(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * One of the steps the displayed job goes through.
	 *
	 * @param label
	 *        What the reader sees for this step.
	 * @param state
	 *        Where the step stands relative to the step the job is at.
	 */
	public record Phase(String label, PhaseState state) {
		// Pure value type.
	}

	/**
	 * Creates a {@link JobDisplay}, holding on to the phases as a list of its own.
	 */
	public JobDisplay {
		phases = phases == null ? List.of() : List.copyOf(phases);
	}

	/**
	 * Whether the job still works, so what is displayed about it still changes.
	 */
	public boolean isRunning() {
		return status == Status.RUNNING;
	}

	/**
	 * Whether the reader may ask this job to stop right now: a running job that offers a
	 * {@link #cancel()} handler.
	 */
	public boolean isCancelable() {
		return isRunning() && cancel != null;
	}

}
