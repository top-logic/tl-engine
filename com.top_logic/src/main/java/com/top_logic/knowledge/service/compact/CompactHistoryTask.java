/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.db2.HistoryCompaction.Report;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Task discarding the history that is older than a configured retention period.
 *
 * <p>
 * All revisions committed before the cut-off date are collapsed into the newest revision committed
 * at or before that date. The state of that revision and of every newer revision is preserved, all
 * intermediate states are discarded and their rows are physically deleted. The discarded history
 * cannot be restored.
 * </p>
 *
 * <p>
 * The compaction requires an active maintenance window and, in a cluster, that the executing node
 * is the only active one. The task therefore requests the maintenance window from the
 * {@link Scheduler} by default. A task started manually while no maintenance window is active fails
 * with the corresponding message.
 * </p>
 *
 * <p>
 * After the compaction the persistency layer is restarted, which drops the caches that still
 * describe the discarded history and ends all sessions. The restart begins only after the
 * {@link Scheduler} has processed the end of the task, i.e. has checked its result and released
 * its cluster lock. A maintenance window that the {@link Scheduler} has entered for this task is
 * taken over by the task and left once the persistency layer is up and running again after the
 * restart. A maintenance window entered manually before a manually started run stays active.
 * </p>
 *
 * @implNote The compaction itself is performed by
 *           {@link HistoryCompactionOperation#compact(long, Log)}, its progress is written to the
 *           application log through a {@link LogProtocol}. After a successful compaction the task
 *           takes over the maintenance window through
 *           {@link Scheduler#takeOverMaintenanceMode(Task)} and registers the restart through
 *           {@link Scheduler#runAfterTaskDone(Task, Runnable)}; the restart is performed by
 *           {@link PersistencyMaintenance#scheduleRestart(Log, boolean)}, which leaves the
 *           maintenance window only if the {@link Scheduler} had entered it for this task.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class CompactHistoryTask<C extends CompactHistoryTask.Config<?>> extends TaskImpl<C> {

	/**
	 * Configuration options for {@link CompactHistoryTask}.
	 */
	public interface Config<I extends CompactHistoryTask<?>> extends TaskImpl.Config<I> {

		/** Name of the property {@link #getRetentionPeriod()}. */
		String RETENTION_PERIOD = "retention-period";

		/**
		 * Time span for which the history is kept.
		 *
		 * <p>
		 * Everything that is older than this time span is discarded when the task runs.
		 * </p>
		 */
		@Name(RETENTION_PERIOD)
		@Format(MillisFormat.class)
		@Mandatory
		long getRetentionPeriod();

		/**
		 * The compaction must not run while sessions are working with the data, therefore the
		 * maintenance window is entered for this task by default.
		 */
		@Override
		@BooleanDefault(true)
		boolean isNeedingMaintenanceMode();

	}

	private final long _retentionPeriod;

	/**
	 * Creates a {@link CompactHistoryTask} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CompactHistoryTask(InstantiationContext context, C config) {
		super(context, config);

		_retentionPeriod = config.getRetentionPeriod();
	}

	/**
	 * The time span for which the history is kept, in milliseconds.
	 */
	public long getRetentionPeriod() {
		return _retentionPeriod;
	}

	@Override
	public void run() {
		super.run();

		ThreadContext.inSystemContext(CompactHistoryTask.class, this::compactHistory);
	}

	private void compactHistory() {
		getLog().taskStarted();

		long cutOff = System.currentTimeMillis() - _retentionPeriod;
		Log log = new LogProtocol(CompactHistoryTask.class);
		try {
			Report report = HistoryCompactionOperation.compact(cutOff, log);
			scheduleRestart(log);

			getLog().taskEnded(ResultType.SUCCESS,
				I18NConstants.TASK_COMPACTION_DONE__REVISION_DELETED_REWRITTEN_REPINNED_CLEARED_DROPPED.fill(
					Long.valueOf(report.getCompactionRevision()),
					Long.valueOf(report.getDeletedRows()),
					Long.valueOf(report.getRewrittenRows()),
					Long.valueOf(report.getRewrittenPins()),
					Long.valueOf(report.getClearedPins()),
					Long.valueOf(report.getRowsDeletedForDanglingPins())));
		} catch (I18NRuntimeException ex) {
			getLog().taskEnded(ResultType.FAILURE, ex.getErrorKey(), ex);
		} catch (RuntimeException ex) {
			getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
		}
	}

	/**
	 * Schedules the restart of the persistency layer after the {@link Scheduler} has processed the
	 * end of the current run.
	 */
	private void scheduleRestart(Log log) {
		Maybe<Scheduler> scheduler = getScheduler();
		if (scheduler.hasValue()) {
			boolean leaveWindow = scheduler.get().takeOverMaintenanceMode(this);
			scheduler.get().runAfterTaskDone(this,
				() -> HistoryCompactionOperation.scheduleRestart(log, leaveWindow));
		} else {
			HistoryCompactionOperation.scheduleRestart(log, false);
		}
	}

	@Override
	public boolean isNodeLocal() {
		// Rewrites the persistent history; must run only once in the cluster.
		return false;
	}

}
