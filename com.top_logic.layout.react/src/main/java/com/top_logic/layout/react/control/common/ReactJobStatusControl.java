/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Displays what is known about a long-running job, via the {@code TLJobStatus} React component: the
 * steps it goes through, how far it has come, how long it has been running, and what it says about
 * what it is doing.
 *
 * <p>
 * The control is told a {@link JobDisplay} and nothing else: one description of the job with every
 * text already resolved, which it publishes to the client in a single patch, so the reader never
 * sees a phase belonging to one report beside the progress of another. A control told no job at all
 * displays nothing.
 * </p>
 *
 * <p>
 * The elapsed time is counted by the client, from {@link #STARTED_AT} against {@link #SERVER_NOW} -
 * the server's own clock reading at the moment it sent the report, letting the client correct for a
 * browser clock that is off. A job that has ended states its {@link #FINISHED_AT}, and the elapsed
 * time stops there rather than following the clock on.
 * </p>
 *
 * <p>
 * A job that may be stopped says so in {@link #CANCELABLE}, and the {@link #CANCEL_COMMAND} carries
 * that request back to the job. It is a gesture of the reader about the job, not about the chrome,
 * so it is recorded like any other command.
 * </p>
 *
 * <p>
 * A display given a {@link #setCloseAction(Runnable) close action} offers the reader to put away
 * a job that has ended, stating so in {@link #CLOSABLE}; the {@link #CLOSE_COMMAND} runs the
 * action. What closing means is up to the one who gives the action - the control only displays
 * the job it is told.
 * </p>
 */
public class ReactJobStatusControl extends ReactControl {

	private static final String REACT_MODULE = "TLJobStatus";

	/**
	 * State key for how the job stands, as the external name of a {@link JobDisplay.Status}, or
	 * {@code null} for no job at all.
	 */
	public static final String STATUS = "status";

	/**
	 * State key for the steps of the job, each an object of {@link #PHASE_LABEL} and
	 * {@link #PHASE_STATUS}; empty for a job that does not name its steps.
	 */
	public static final String PHASES = "phases";

	/** Key of a phase's text within an entry of {@link #PHASES}. */
	public static final String PHASE_LABEL = "label";

	/**
	 * Key of where a phase stands within an entry of {@link #PHASES}, as the external name of a
	 * {@link JobDisplay.PhaseState}.
	 */
	public static final String PHASE_STATUS = "status";

	/**
	 * State key for how much of the work is done, between 0 and 1, or {@code null} for the
	 * indeterminate bar.
	 */
	public static final String FRACTION = "fraction";

	/** State key for what the job says about what it is doing, or {@code null} for nothing. */
	public static final String MESSAGE = "message";

	/** State key for when the job was started, as epoch milliseconds. */
	public static final String STARTED_AT = "startedAt";

	/** State key for when the job ended, as epoch milliseconds, or {@code null} while it runs. */
	public static final String FINISHED_AT = "finishedAt";

	/**
	 * State key for the server's clock at the moment it sent the report, as epoch milliseconds; the
	 * reference the client measures the offset of its own clock against.
	 */
	public static final String SERVER_NOW = "serverNow";

	/** State key for the text naming what the job produced, or {@code null} for nothing to show. */
	public static final String RESULT = "result";

	/** State key for why the job failed, or {@code null} for a job that did not. */
	public static final String ERROR = "error";

	/**
	 * State key for the individual problems behind the {@link #ERROR}, a list of texts; empty for a
	 * failure without details and for a job that did not fail.
	 */
	public static final String ERROR_DETAILS = "errorDetails";

	/** State key for whether the reader may ask the job to stop. */
	public static final String CANCELABLE = "cancelable";

	/** The {@link ReactCommandHandler} asking the displayed job to stop. */
	public static final String CANCEL_COMMAND = "cancel";

	/**
	 * State key for whether the reader may close the display: a {@link #setCloseAction(Runnable)
	 * close action} is given and the displayed job has ended.
	 */
	public static final String CLOSABLE = "closable";

	/** The {@link ReactCommandHandler} running the {@link #setCloseAction(Runnable) close action}. */
	public static final String CLOSE_COMMAND = "close";

	private JobDisplay _job;

	/**
	 * @see #setCloseAction(Runnable)
	 */
	private Runnable _closeAction;

	/**
	 * Creates a {@link ReactJobStatusControl} displaying the given job.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param job
	 *        What is known about the job, or {@code null} for a display without a job, which shows
	 *        nothing.
	 */
	public ReactJobStatusControl(ReactContext context, JobDisplay job) {
		super(context, null, REACT_MODULE);
		publish(job);
	}

	/**
	 * Displays what is now known about the job.
	 *
	 * @param job
	 *        The report to display, or {@code null} to display no job at all.
	 */
	public void setJob(JobDisplay job) {
		Object tx = beginUpdate();
		publish(job);
		commitUpdate(tx);
	}

	/**
	 * The job currently displayed, or {@code null} while none is.
	 */
	public JobDisplay getJob() {
		return _job;
	}

	/**
	 * Sets what is done when the reader closes the display of a job that has ended.
	 *
	 * @param closeAction
	 *        The action putting the display away, or {@code null} for a display that offers no
	 *        closing.
	 */
	public void setCloseAction(Runnable closeAction) {
		_closeAction = closeAction;
		putState(CLOSABLE, Boolean.valueOf(isClosable(_job)));
	}

	/**
	 * Runs the {@link #setCloseAction(Runnable) close action}.
	 *
	 * @implNote Ignored while the displayed job runs: the button is offered only for a job that has
	 *           ended, but a click on its way to the server crosses the report of a job started
	 *           anew, which must not be put away.
	 */
	@ReactCommandHandler(CLOSE_COMMAND)
	void handleClose() {
		if (isClosable(_job)) {
			_closeAction.run();
		}
	}

	/**
	 * Whether the reader may close the display of the given job.
	 */
	private boolean isClosable(JobDisplay job) {
		return _closeAction != null && job != null && !job.isRunning();
	}

	/**
	 * Asks the displayed job to stop.
	 *
	 * @implNote Ignored for a job that is not {@link JobDisplay#isCancelable() cancelable}: the
	 *           button is gone as soon as the job ends, but a click on its way to the server
	 *           crosses the report that ended the job.
	 */
	@ReactCommandHandler(CANCEL_COMMAND)
	void handleCancel() {
		JobDisplay job = _job;
		if (job != null && job.isCancelable()) {
			job.cancel().run();
		}
	}

	/**
	 * Writes the given report into the state the client renders from.
	 */
	private void publish(JobDisplay job) {
		_job = job;
		putState(STATUS, job == null ? null : job.status().getExternalName());
		putState(PHASES, phases(job));
		putState(FRACTION, job == null ? null : job.fraction());
		putState(MESSAGE, job == null ? null : job.message());
		putState(STARTED_AT, millis(job == null ? null : job.startedAt()));
		putState(FINISHED_AT, millis(job == null ? null : job.finishedAt()));
		// Reference point for the client's clock-offset correction, taken together with the
		// timestamps of the job so that all of them describe the same clock.
		putState(SERVER_NOW, job == null ? null : Long.valueOf(System.currentTimeMillis()));
		putState(RESULT, job == null ? null : job.result());
		putState(ERROR, job == null ? null : job.error());
		putState(ERROR_DETAILS, job == null ? List.of() : job.errorDetails());
		putState(CANCELABLE, Boolean.valueOf(job != null && job.isCancelable()));
		putState(CLOSABLE, Boolean.valueOf(isClosable(job)));
	}

	/**
	 * The steps of the given job as the client reads them, an empty list for no job at all.
	 */
	private static List<Map<String, Object>> phases(JobDisplay job) {
		if (job == null) {
			return List.of();
		}
		List<Map<String, Object>> result = new ArrayList<>(job.phases().size());
		for (JobDisplay.Phase phase : job.phases()) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(PHASE_LABEL, phase.label());
			entry.put(PHASE_STATUS, phase.state().getExternalName());
			result.add(entry);
		}
		return result;
	}

	/**
	 * The given instant as epoch milliseconds, {@code null} for no instant at all.
	 */
	private static Long millis(Date time) {
		return time == null ? null : Long.valueOf(time.getTime());
	}

}
