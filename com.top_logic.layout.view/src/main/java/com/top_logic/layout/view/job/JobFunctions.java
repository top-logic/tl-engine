/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions about a long-running job: what a job says about itself while it works, and
 * what a display reads from what it said.
 *
 * <p>
 * The reporting functions are called on the monitor the body of a job is given as its first
 * argument, so a job written as a TL-Script function reports with
 * <code>$job.jobPhase('read')</code>. The reading functions are called on the state a job publishes
 * on its channel, so a display asks with <code>jobStatus($state)</code> - over no job at all just
 * as well, which is what a channel holds before the first job was started.
 * </p>
 *
 * <pre>
 * job -&gt; files -&gt; x -&gt; {
 *   $job.jobPhases(['read', 'check']);
 *   $job.jobPhase('read');
 *   $job.jobMessage('reading the files');
 *   $job.jobProgress(1, $files.size());
 *   $job.jobPhase('check');
 *   $files.size();
 * }
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #phase(JobMonitor, String)} is called <code>jobPhase</code>.
 */
@ScriptPrefix(JobFunctions.PREFIX)
public class JobFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "job";

	/**
	 * Enters the named step of the job, marking the steps passed over as done.
	 *
	 * <p>
	 * The step is one of those the job announced, either in its configuration or with
	 * <code>jobPhases</code>; naming any other step ends the job with an error. A job that has
	 * nothing to do in a step simply does not enter it.
	 * </p>
	 *
	 * @param job
	 *        The job that enters the step, the first argument of the function doing its work.
	 * @param name
	 *        The name of the step to enter.
	 */
	@Label("Enter a step of the job")
	public static void phase(@Mandatory JobMonitor job, @Mandatory String name) {
		monitor(job).beginPhase(name);
	}

	/**
	 * Replaces the steps the job announces it goes through.
	 *
	 * <p>
	 * For a job that learns only while running what it has to do. The steps are given either as a
	 * list of names, which are then what the reader sees as well, or as a map from the name of a
	 * step to what the reader sees for it - a text or an internationalized one, written as
	 * <code>#('Reading'@en, 'Lesen'@de)</code>. They are announced in the order the list or the map
	 * gives them.
	 * </p>
	 *
	 * <pre>
	 * $job.jobPhases({'read': #('Reading'@en), 'write': #('Writing'@en)});
	 * </pre>
	 *
	 * @param job
	 *        The job announcing its steps, the first argument of the function doing its work.
	 * @param phases
	 *        The steps, as a list of names or as a map from a name to what the reader sees.
	 */
	@Label("Announce the steps of the job")
	public static void phases(@Mandatory JobMonitor job, @Mandatory Object phases) {
		monitor(job).setPhases(asPhases(phases));
	}

	/**
	 * States how much of the work is done, as the two counts the progress is the ratio of.
	 *
	 * @param job
	 *        The job reporting its progress, the first argument of the function doing its work.
	 * @param done
	 *        How much is done.
	 * @param total
	 *        How much there is in all; nothing at all leaves an empty progress rather than a
	 *        division by it.
	 */
	@Label("Report the progress of the job")
	public static void progress(@Mandatory JobMonitor job, @Mandatory double done, @Mandatory double total) {
		monitor(job).progress(done, total);
	}

	/**
	 * States that the job does not know how much of its work is done.
	 *
	 * <p>
	 * The display then shows that something is going on without showing how far it has come, until
	 * the job reports a progress again.
	 * </p>
	 *
	 * @param job
	 *        The job giving up its progress, the first argument of the function doing its work.
	 */
	@Label("Report an unknown progress")
	public static void indeterminate(@Mandatory JobMonitor job) {
		monitor(job).indeterminate();
	}

	/**
	 * States what the job is doing right now.
	 *
	 * @param job
	 *        The job reporting what it does, the first argument of the function doing its work.
	 * @param message
	 *        What the reader sees - a text, or an internationalized one written as
	 *        <code>#('Reading'@en, 'Lesen'@de)</code>; nothing at all says nothing.
	 */
	@Label("Report what the job is doing")
	public static void message(@Mandatory JobMonitor job, @Mandatory Object message) {
		monitor(job).message(asResKey(message));
	}

	/**
	 * Whether the given job is still working, so what it reports still changes.
	 *
	 * @param job
	 *        The state of a job, as its channel holds it; no job at all is not running.
	 * @return Whether the job is still working.
	 */
	@Label("Job is running")
	@SideEffectFree
	public static boolean isRunning(@Mandatory JobState job) {
		return job != null && job.isRunning();
	}

	/**
	 * Whether the given job has ended, however it ended.
	 *
	 * @param job
	 *        The state of a job, as its channel holds it; no job at all has not ended.
	 * @return Whether the job has ended.
	 */
	@Label("Job is finished")
	@SideEffectFree
	public static boolean isFinished(@Mandatory JobState job) {
		return job != null && job.isFinished();
	}

	/**
	 * How the given job stands, as one of the texts <code>running</code>, <code>completed</code>,
	 * <code>failed</code> and <code>cancelled</code>.
	 *
	 * <p>
	 * A text, so that a display switches over it: <code>&lt;case match="'completed'"&gt;</code>.
	 * </p>
	 *
	 * @param job
	 *        The state of a job, as its channel holds it.
	 * @return How the job stands, or nothing at all when there is no job.
	 */
	@Label("Status of the job")
	@SideEffectFree
	public static String status(@Mandatory JobState job) {
		return job == null ? null : job.status().name().toLowerCase();
	}

	/**
	 * What the given job produced.
	 *
	 * @param job
	 *        The state of a job, as its channel holds it.
	 * @return The result of the job, or nothing at all for a job that has not completed.
	 */
	@Label("Result of the job")
	@SideEffectFree
	public static Object result(@Mandatory JobState job) {
		return job == null ? null : job.result();
	}

	/**
	 * Why the given job failed.
	 *
	 * @param job
	 *        The state of a job, as its channel holds it.
	 * @return The message of the failure, or nothing at all for a job that has not failed.
	 */
	@Label("Error of the job")
	@SideEffectFree
	public static ResKey error(@Mandatory JobState job) {
		return job == null ? null : job.error();
	}

	/**
	 * The job to report to, for a function that is called on one.
	 *
	 * @param job
	 *        What the caller handed in.
	 * @return The given monitor.
	 * @throws TopLogicException
	 *         If there is no job to report to.
	 */
	private static JobMonitor monitor(JobMonitor job) {
		if (job == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_JOB_TO_REPORT_TO);
		}
		return job;
	}

	/**
	 * The given script value as the steps of a job.
	 *
	 * @param phases
	 *        A map from the name of a step to what the reader sees for it, or a list of names.
	 * @return The steps, in the order the value gives them.
	 */
	private static List<JobPhase> asPhases(Object phases) {
		if (phases == null) {
			return List.of();
		}
		List<JobPhase> result = new ArrayList<>();
		if (phases instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				String name = asText(entry.getKey());
				ResKey label = asResKey(entry.getValue());
				result.add(label == null ? JobPhase.named(name) : new JobPhase(name, label));
			}
			return result;
		}
		for (Object name : CollectionUtil.asList(phases)) {
			result.add(JobPhase.named(asText(name)));
		}
		return result;
	}

	/**
	 * The given script value as a text the reader sees, or {@code null} for nothing at all.
	 */
	private static ResKey asResKey(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof ResKey key) {
			return key;
		}
		return ResKey.text(asText(value));
	}

	/**
	 * The given script value as the plain text it is written as.
	 */
	private static String asText(Object value) {
		return value == null ? null : value.toString();
	}

}
