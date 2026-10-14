/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.ChannelPublisher;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.error.TopLogicException;

/**
 * Runs a {@link JobBody} on a worker thread and publishes what it reports as {@link JobState}
 * snapshots on a {@link ViewChannel}.
 *
 * <p>
 * The job leaves the request that started it: the first snapshot is written on the starting
 * thread, so the channel holds the running job before the request returns, and the body then runs
 * in the sub-session of that request on a thread of the {@link SchedulerService}. Every later
 * snapshot is handed to a {@link ChannelPublisher}, which writes it asynchronously on a scheduler
 * thread, in that sub-session and under the interaction of the window the job belongs to. Writing
 * the channel, the controls that update from it and the updates delivered to the browser are thus
 * serialized against the requests of the same session exactly like a command is, and the body
 * never waits for the display.
 * </p>
 *
 * <p>
 * A job that reports faster than the display can follow is throttled to one snapshot per update
 * interval: a report arriving too early is held back, and a later one replaces it, so the display
 * shows the state the job has reached and the last report of a burst is never lost. The snapshot
 * that ends the job is delivered without waiting for the interval, and with it the runner tells
 * whoever waits for the job that it has ended - within the same interaction, so that the work
 * following the job runs where a command runs. It is delivered even when the window is gone, so
 * that nothing waiting for the job is left hanging.
 * </p>
 *
 * <p>
 * Snapshots reach the channel in the order they were taken: a snapshot is handed to the publisher
 * while the state it was taken from is locked, and the publisher delivers in the order it was
 * handed the values, so a snapshot taken before the job ended is never delivered after the one that
 * ends it. Once that one has been delivered, the publisher is closed.
 * </p>
 *
 * @see JobMonitor
 * @see JobControl
 */
public class JobRunner implements JobControl, JobMonitor {

	private final ViewChannel _target;

	private final boolean _cancelable;

	private final Consumer<JobState> _onFinished;

	/** Delivers the snapshots taken after the first one. */
	private final ChannelPublisher _publisher;

	/** The sub-session of the request that started the job, the one the body runs in. */
	private final SubSessionContext _subSession;

	/** Guards everything the snapshot is built from. */
	private final Object _lock = new Object();

	private final Date _startedAt = new Date();

	private List<JobPhase> _phases;

	private JobStatus _status = JobStatus.RUNNING;

	private int _currentPhase = -1;

	private Double _fraction;

	private ResKey _message;

	private Date _finishedAt;

	private Object _result;

	private ResKey _error;

	private Thread _worker;

	private volatile boolean _cancelled;

	/**
	 * Creates a {@link JobRunner}.
	 *
	 * <p>
	 * Called from the request that starts the job, whose sub-session the job runs and publishes
	 * in.
	 * </p>
	 *
	 * @param context
	 *        The context of the window the job belongs to; its
	 *        {@link ReactContext#getWindowRegistry() window registry} is where the snapshots are
	 *        published under. Without a registry - headless - the channel is written directly.
	 * @param target
	 *        The channel receiving the {@link JobState} snapshots.
	 * @param phases
	 *        The steps the job announces it goes through, empty for a job that does not name its
	 *        steps. The body may replace them with {@link #setPhases(List)}.
	 * @param cancelable
	 *        Whether the reader may ask this job to stop, see {@link #cancel()}.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two published snapshots; zero to publish
	 *        every report.
	 * @param onFinished
	 *        Told the terminal snapshot once the job has ended, from within the interaction the
	 *        snapshot was published in. May be {@code null}.
	 */
	public JobRunner(ReactContext context, ViewChannel target, List<JobPhase> phases, boolean cancelable,
			long updateInterval, Consumer<JobState> onFinished) {
		_target = target;
		_phases = List.copyOf(phases);
		_cancelable = cancelable;
		_onFinished = onFinished;
		_subSession = ThreadContextManager.getSubSession();
		_publisher = new ChannelPublisher(context, updateInterval, value -> apply((JobState) value), true);
	}

	/**
	 * Publishes the running job and starts its body on a worker thread.
	 *
	 * <p>
	 * Called from the request that starts the job: the first snapshot is written to the channel
	 * before this returns.
	 * </p>
	 *
	 * @param body
	 *        The work to do.
	 * @param arguments
	 *        The values to hand to the body.
	 */
	public void start(JobBody body, List<Object> arguments) {
		JobState initial;
		synchronized (_lock) {
			initial = snapshot();
		}
		apply(initial);

		SchedulerService.getInstance().execute(() -> work(body, arguments));
	}

	private void work(JobBody body, List<Object> arguments) {
		synchronized (_lock) {
			_worker = Thread.currentThread();
		}
		if (_cancelled) {
			// Cancelled before the worker got hold of the job: the interrupt it would have received
			// reaches the body all the same.
			Thread.currentThread().interrupt();
		}
		try {
			SubSessionContext subSession = _subSession;
			if (subSession == null) {
				run(body, arguments);
			} else {
				ThreadContextManager.inContext(subSession, () -> run(body, arguments));
			}
		} finally {
			synchronized (_lock) {
				_worker = null;
			}
			// The thread goes back to the pool, so the interrupt of a cancelled job must not
			// outlive the job.
			Thread.interrupted();
		}
	}

	/**
	 * Runs the body and ends the job with what it produced.
	 *
	 * <p>
	 * A job that was asked to stop ends as cancelled, on both ways out of the body: the request to
	 * stop decides how the job ended, whatever the body does on its way out. A body that is woken
	 * from what it waits for reaches the outside in whatever shape the layer it was stopped in gives
	 * it - an interrupted read as the I/O failure it raises, an abort of a script as the failure of
	 * the expression it was raised in - and none of these shapes is a failure the reader is to be
	 * told about.
	 * </p>
	 *
	 * <p>
	 * A body that abandons its work on its own - raising an {@link AbortExecutionException}, or
	 * being interrupted by something other than this job - ends the job as cancelled as well; that
	 * is what such a failure says, wherever in the chain of causes it appears.
	 * </p>
	 *
	 * <p>
	 * How the body ended is decided first and the job is ended afterwards, outside the catch: what
	 * ending the job sets off - the last snapshot, and the work waiting for the job taken up again
	 * - is no longer the body's doing, and a failure of that work must not be read as a failure of
	 * the body.
	 * </p>
	 */
	private void run(JobBody body, List<Object> arguments) {
		JobStatus status;
		Object result = null;
		ResKey error = null;
		try {
			Object produced = body.run(this, arguments);
			if (_cancelled) {
				status = JobStatus.CANCELLED;
			} else {
				status = JobStatus.COMPLETED;
				result = produced;
			}
		} catch (Throwable ex) {
			if (_cancelled || isAbort(ex)) {
				status = JobStatus.CANCELLED;
			} else if (ex instanceof I18NRuntimeException failure) {
				status = JobStatus.FAILED;
				error = failure.getErrorKey();
			} else {
				Logger.error("Background job failed.", ex, JobRunner.class);
				String message = ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage();
				status = JobStatus.FAILED;
				error = I18NConstants.ERROR_JOB_FAILED__MESSAGE.fill(message);
			}
		}
		finish(status, result, error);
	}

	/**
	 * Whether the given failure says that the work was abandoned rather than that it failed.
	 *
	 * <p>
	 * How a body that catches failures of its own tells the two apart: work that was abandoned is
	 * passed on, a failure is dealt with.
	 * </p>
	 *
	 * @param failure
	 *        What the body threw.
	 * @return Whether an {@link AbortExecutionException} or an {@link InterruptedException} stands
	 *         anywhere in the chain of causes, which is where it ends up once a layer above wraps
	 *         it into a failure of its own.
	 */
	public static boolean isAbort(Throwable failure) {
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			if (cause instanceof AbortExecutionException || cause instanceof InterruptedException) {
				return true;
			}
			if (cause.getCause() == cause) {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isCancelable() {
		return _cancelable;
	}

	@Override
	public void cancel() {
		Thread worker;
		synchronized (_lock) {
			if (!_cancelable || _status != JobStatus.RUNNING) {
				return;
			}
			_cancelled = true;
			worker = _worker;
		}
		if (worker != null) {
			worker.interrupt();
		}
	}

	@Override
	public void checkCancelled() {
		if (_cancelled) {
			throw new AbortExecutionException("The job was cancelled.", null);
		}
	}

	@Override
	public void setPhases(List<JobPhase> phases) {
		checkCancelled();
		report(() -> {
			JobPhase current = _currentPhase >= 0 && _currentPhase < _phases.size() ? _phases.get(_currentPhase) : null;
			_phases = List.copyOf(phases);
			_currentPhase = current == null ? -1 : indexOf(current.name());
		});
	}

	@Override
	public void beginPhase(String name) {
		checkCancelled();
		report(() -> {
			int index = indexOf(name);
			if (index < 0) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_JOB_PHASE__NAME_PHASES.fill(name, phaseNames()));
			}
			_currentPhase = index;
		});
	}

	@Override
	public void progress(double done, double total) {
		fraction(total <= 0 ? 0d : done / total);
	}

	@Override
	public void fraction(double fraction) {
		checkCancelled();
		Double value = Double.valueOf(Math.max(0d, Math.min(1d, fraction)));
		report(() -> _fraction = value);
	}

	@Override
	public void indeterminate() {
		checkCancelled();
		report(() -> _fraction = null);
	}

	@Override
	public void message(ResKey message) {
		checkCancelled();
		report(() -> _message = message);
	}

	/**
	 * Applies the given change to the state of the job and publishes the result, which the
	 * publisher holds back when another snapshot was delivered too recently.
	 */
	private void report(Runnable change) {
		synchronized (_lock) {
			if (_status != JobStatus.RUNNING) {
				return;
			}
			change.run();
			_publisher.publish(snapshot());
		}
	}

	/**
	 * Ends the job, unless it has already ended.
	 *
	 * @param status
	 *        How the job ended.
	 * @param result
	 *        What it produced, for a completed one.
	 * @param error
	 *        Why it failed, for a failed one.
	 */
	private void finish(JobStatus status, Object result, ResKey error) {
		synchronized (_lock) {
			if (_status != JobStatus.RUNNING) {
				return;
			}
			_status = status;
			_result = result;
			_error = error;
			_finishedAt = new Date();
			if (status == JobStatus.COMPLETED && !_phases.isEmpty()) {
				_currentPhase = _phases.size();
			}
			_publisher.publishNow(snapshot());
		}
	}

	/**
	 * Writes the snapshot to the channel and, for the one that ends the job, tells whoever waits
	 * for it.
	 *
	 * <p>
	 * The channel holds the snapshot whatever happens afterwards, so the display shows how the job
	 * ended even when the work taken up after it fails; that failure is left to the one waiting for
	 * the job to report.
	 * </p>
	 *
	 * <p>
	 * The snapshot that ends the job is the last one: nothing is reported after it, so the
	 * publisher is closed once it has been delivered.
	 * </p>
	 */
	private void apply(JobState state) {
		try {
			_target.set(state);
		} finally {
			if (state.isFinished()) {
				try {
					if (_onFinished != null) {
						_onFinished.accept(state);
					}
				} finally {
					_publisher.close();
				}
			}
		}
	}

	/** Called while holding {@link #_lock}. */
	private JobState snapshot() {
		return new JobState(_status, _phases, _currentPhase, _fraction, _message, _startedAt, _finishedAt,
			_result, _error, this);
	}

	/** Called while holding {@link #_lock}. */
	private int indexOf(String name) {
		for (int n = 0; n < _phases.size(); n++) {
			if (_phases.get(n).name().equals(name)) {
				return n;
			}
		}
		return -1;
	}

	/** Called while holding {@link #_lock}. */
	private String phaseNames() {
		StringBuilder result = new StringBuilder();
		for (JobPhase phase : _phases) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(phase.name());
		}
		return result.toString();
	}

}
