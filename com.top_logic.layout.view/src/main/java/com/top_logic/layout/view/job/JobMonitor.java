/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.List;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.util.ResKey;

/**
 * What a job says about itself while it runs.
 *
 * <p>
 * The body of a job receives a {@link JobMonitor} and reports through it: the phase it enters, how
 * much of the work is done, what it is doing right now. Each report produces a new
 * {@link JobState} on the channel the job publishes to, so what is reported here is what the
 * display shows.
 * </p>
 *
 * <p>
 * Every method of this interface first checks whether the job was cancelled and throws an
 * {@link AbortExecutionException} when it was. A body that reports regularly therefore needs no
 * cancellation check of its own; one that works for a long while without reporting asks with
 * {@link #checkCancelled()}.
 * </p>
 */
public interface JobMonitor {

	/**
	 * Replaces the steps this job announces it goes through.
	 *
	 * <p>
	 * For a job that only learns while running what it has to do. The phase the job is at is kept
	 * when the new list still names it, and is otherwise given up, so the job is between phases
	 * again.
	 * </p>
	 *
	 * @param phases
	 *        The steps, in the order they are worked through.
	 */
	void setPhases(List<JobPhase> phases);

	/**
	 * Enters the named phase.
	 *
	 * <p>
	 * The phases the job passes over by entering a later one count as done, so a job that has
	 * nothing to do in a phase simply does not name it. Naming a phase the job did not announce is
	 * an error that ends the job.
	 * </p>
	 *
	 * @param name
	 *        The {@link JobPhase#name() name} of one of the announced phases.
	 */
	void beginPhase(String name);

	/**
	 * States how much of the work is done as the two counts it is the ratio of.
	 *
	 * @param done
	 *        How much is done.
	 * @param total
	 *        How much there is in all; nothing at all leaves an empty progress rather than a
	 *        division by it.
	 */
	void progress(double done, double total);

	/**
	 * States how much of the work is done as a number between 0 and 1, which a value outside is cut
	 * down to.
	 *
	 * @param fraction
	 *        The part of the work that is done.
	 */
	void fraction(double fraction);

	/**
	 * States that the job does not know how much of its work is done, so its progress has no length
	 * until it says otherwise.
	 */
	void indeterminate();

	/**
	 * States what the job is doing right now.
	 *
	 * @param message
	 *        The text to show, a plain one written as {@link ResKey#text(String)}; {@code null} to
	 *        say nothing.
	 */
	void message(ResKey message);

	/**
	 * Ends the job when it was cancelled, and does nothing otherwise.
	 *
	 * <p>
	 * For a body that works for a long while without reporting anything; every other method of this
	 * interface asks the same question on its own.
	 * </p>
	 *
	 * @throws AbortExecutionException
	 *         If the job was cancelled.
	 */
	void checkCancelled();

}
