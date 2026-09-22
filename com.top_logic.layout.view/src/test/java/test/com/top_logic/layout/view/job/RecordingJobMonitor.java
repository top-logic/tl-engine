/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.job.JobMonitor;
import com.top_logic.layout.view.job.JobPhase;

/**
 * A {@link JobMonitor} recording what a job reports, and telling it that it was cancelled once it
 * is asked to.
 *
 * <p>
 * What a test of a job body drives it with: the body is run with this monitor and what it reported
 * is read back from here, in the order it was reported.
 * </p>
 */
class RecordingJobMonitor implements JobMonitor {

	/** The steps the job announced. */
	final List<JobPhase> _announced = new ArrayList<>();

	/** The names of the steps the job entered, in order. */
	final List<String> _entered = new ArrayList<>();

	/** What the job reported about its progress, as the ratio it is. */
	final List<String> _progress = new ArrayList<>();

	/** What the job said about what it is doing, in order. */
	final List<ResKey> _messages = new ArrayList<>();

	/** How often the job said it does not know how much of its work is done. */
	int _indeterminate;

	private boolean _cancelled;

	/** Has this monitor tell the job that it was cancelled. */
	void cancel() {
		_cancelled = true;
	}

	@Override
	public void setPhases(List<JobPhase> phases) {
		checkCancelled();
		_announced.clear();
		_announced.addAll(phases);
	}

	@Override
	public void beginPhase(String name) {
		checkCancelled();
		_entered.add(name);
	}

	@Override
	public void progress(double done, double total) {
		checkCancelled();
		_progress.add((long) done + "/" + (long) total);
	}

	@Override
	public void fraction(double fraction) {
		checkCancelled();
	}

	@Override
	public void indeterminate() {
		checkCancelled();
		_indeterminate++;
	}

	@Override
	public void message(ResKey message) {
		checkCancelled();
		_messages.add(message);
	}

	@Override
	public void checkCancelled() {
		if (_cancelled) {
			throw new AbortExecutionException("The job was cancelled.", null);
		}
	}

	/** The names of the steps the job announced. */
	List<String> phaseNames() {
		List<String> result = new ArrayList<>();
		for (JobPhase phase : _announced) {
			result.add(phase.name());
		}
		return result;
	}

	/** The texts the reader sees for the steps the job announced. */
	List<ResKey> phaseLabels() {
		List<ResKey> result = new ArrayList<>();
		for (JobPhase phase : _announced) {
			result.add(phase.label());
		}
		return result;
	}

	/** What the job said last about what it is doing, {@code null} for a job that said nothing. */
	ResKey lastMessage() {
		return _messages.isEmpty() ? null : _messages.get(_messages.size() - 1);
	}

}
