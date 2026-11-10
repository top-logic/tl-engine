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
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.CancelScope;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.view.channel.ChannelPublisher;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.error.ErrorHandlingHelper;
import com.top_logic.util.error.TopLogicException;

/**
 * Runs a {@link JobBody} on a worker thread and publishes what it reports as {@link JobState}
 * snapshots on a {@link ViewChannel}.
 *
 * <p>
 * The job leaves the request that started it: the first snapshot is written on the starting
 * thread, so the channel holds the running job before the request returns, and the body then runs
 * on a thread of the {@link SchedulerService}. Since the window keeps serving its requests while the
 * body runs, the body does not share the sub-session of the starting request: it runs in a
 * sub-session derived from it (see
 * {@link ThreadContextManager#inDerivedContext(SubSessionContext, InContext)}), with the same
 * user, locale and time zone, but a session revision of its own that a commit of the window does not
 * invalidate. Every later snapshot is handed to a {@link ChannelPublisher}, which writes it
 * asynchronously on a scheduler thread, in the sub-session of the window and under the interaction
 * of the window the job belongs to. Writing the channel, the controls that update from it and the
 * updates delivered to the browser are thus serialized against the requests of the same session
 * exactly like a command is, and the body never waits for the display.
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
 * <p>
 * Cancelling the job cancels the {@link CancelScope} the body runs in, and never interrupts the
 * worker thread at an arbitrary point: the body may be reading or committing to the database, and
 * an interrupt reaching such a read closes the database file. Only a wait the body declares as an
 * interruptible section of its scope - {@link CancelScope#runInterruptibly(CancelScope.BlockingCall)},
 * {@link CancelScope#sleep(long)}, which TL-Script {@code sleep()} uses - is woken; everything else
 * ends at the next report or {@link #checkCancelled()} of the body.
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

	/**
	 * The sub-session of the request that started the job, the one the body runs in a sub-session
	 * derived from.
	 */
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

	private List<ResKey> _errorDetails = List.of();

	/**
	 * The scope the body runs in; cancelling the job cancels it.
	 */
	private final CancelScope _scope = new CancelScope() {
		@Override
		protected String cancelledMessage() {
			return "The job was cancelled.";
		}
	};

	/**
	 * Creates a {@link JobRunner}.
	 *
	 * <p>
	 * Called from the request that starts the job, whose sub-session the job publishes in and runs
	 * in a sub-session derived from.
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
		try {
			SubSessionContext subSession = _subSession;
			if (subSession == null) {
				run(body, arguments);
			} else {
				ThreadContextManager.inDerivedContext(subSession, () -> run(body, arguments));
			}
		} finally {
			// The scope takes back every interrupt it sends. An interrupt from elsewhere that the
			// body left set must not reach the next task of the pooled thread, which may well read
			// the database.
			Thread.interrupted();
		}
	}

	/**
	 * Runs the body and ends the job with what it produced.
	 *
	 * <p>
	 * The body runs with the {@link CancelScope} of this job bound to its thread, so that the waits
	 * it declares as interruptible find it through {@link CancelScope#current()}. A job cancelled
	 * before its body got to run ends as cancelled without running it.
	 * </p>
	 *
	 * <p>
	 * A job that was asked to stop ends as cancelled, on both ways out of the body: the request to
	 * stop decides how the job ended, whatever the body does on its way out. A body whose wait is
	 * ended by the cancel reaches the outside in whatever shape the layer it was stopped in gives it
	 * - an abort of a script as the failure of the expression it was raised in, a failed request as
	 * the I/O failure it raises - and none of these shapes is a failure the reader is to be told
	 * about.
	 * </p>
	 *
	 * <p>
	 * A body that abandons its work on its own - raising an {@link AbortExecutionException}, or
	 * being interrupted by something other than this job - ends the job as cancelled as well; that
	 * is what such a failure says, wherever in the chain of causes it appears.
	 * </p>
	 *
	 * <p>
	 * A failure that carries a message for the user ends the job with the
	 * {@link CommandErrors#messages(Throwable) messages} a failed command shows: the first as the
	 * {@link JobState#error() error}, the others - e.g. the individual problems behind a rejected
	 * commit - as its {@link JobState#errorDetails() details}. Any other failure ends the job with a
	 * generic error naming its technical message.
	 * </p>
	 *
	 * <p>
	 * Every failure is logged, the one the user can act on as a warning with its messages, an
	 * {@link ErrorHandlingHelper#isInternalError(Throwable) internal error} as an error: a job runs
	 * in the background, the window that started it may be gone when it fails, so the log is the
	 * only lasting record of the failure.
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
		List<ResKey> errorDetails = List.of();
		try (var binding = CancelScope.bind(_scope)) {
			_scope.checkCancelled();
			Object produced = body.run(this, arguments);
			if (_scope.isCancelled()) {
				status = JobStatus.CANCELLED;
			} else {
				status = JobStatus.COMPLETED;
				result = produced;
			}
		} catch (Throwable ex) {
			if (_scope.isCancelled() || isAbort(ex)) {
				status = JobStatus.CANCELLED;
			} else {
				status = JobStatus.FAILED;
				if (CommandErrors.findI18NFailure(ex) != null) {
					List<ResKey> messages = CommandErrors.messages(ex);
					error = messages.get(0);
					errorDetails = messages.subList(1, messages.size());
				} else {
					String message = ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage();
					error = I18NConstants.ERROR_JOB_FAILED__MESSAGE.fill(message);
				}
				log(ex);
			}
		}
		finish(status, result, error, errorDetails);
	}

	/**
	 * Writes the log entry for a failure of the body.
	 */
	private static void log(Throwable ex) {
		if (ErrorHandlingHelper.isInternalError(ex)) {
			Logger.error("Background job failed.", ex, JobRunner.class);
		} else {
			Logger.warn("Background job failed: " + CommandErrors.text(ex), ex, JobRunner.class);
		}
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
		synchronized (_lock) {
			if (!_cancelable || _status != JobStatus.RUNNING) {
				return;
			}
		}
		_scope.cancel();
	}

	@Override
	public void checkCancelled() {
		_scope.checkCancelled();
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
	 * @param errorDetails
	 *        The messages explaining the error, see {@link JobState#errorDetails()}.
	 */
	private void finish(JobStatus status, Object result, ResKey error, List<ResKey> errorDetails) {
		synchronized (_lock) {
			if (_status != JobStatus.RUNNING) {
				return;
			}
			_status = status;
			_result = result;
			_error = error;
			_errorDetails = errorDetails;
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
			_result, _error, _errorDetails, this);
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
