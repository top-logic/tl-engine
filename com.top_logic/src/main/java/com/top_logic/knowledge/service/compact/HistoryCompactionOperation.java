/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.Log;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.HistoryCompaction;
import com.top_logic.knowledge.service.db2.HistoryCompaction.Report;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.util.error.TopLogicException;

/**
 * Runs {@link HistoryCompaction} on the {@link PersistencyLayer#getKnowledgeBase() default
 * knowledge base} of the running application.
 *
 * <p>
 * The compaction deletes rows of revisions that a running {@link KnowledgeBase} still holds in its
 * caches and that other sessions and other nodes of a cluster may still access. It therefore runs
 * under the conditions of a {@link PersistencyMaintenance}: in an active maintenance window, on the
 * single active node of a cluster, and followed by a restart of the {@link KnowledgeBaseFactory}
 * that drops the caches still describing the discarded history. The state visible in the newest
 * revision is not touched by the rewrite, so the compaction runs against the started knowledge
 * base.
 * </p>
 *
 * <p>
 * The analysis only counts what a compaction would change and needs neither a maintenance window
 * nor a restart.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoryCompactionOperation {

	/**
	 * Number of years {@link #defaultBeforeDate()} lies back in time.
	 */
	private static final int DEFAULT_AGE_YEARS = 1;

	/**
	 * The point in time before which an administrator compacts the history unless choosing
	 * another one: one year ago.
	 */
	public static Date defaultBeforeDate() {
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.add(Calendar.YEAR, -DEFAULT_AGE_YEARS);
		return calendar.getTime();
	}

	/**
	 * Whether the preconditions of {@link #compact(long, Log)} are currently met.
	 *
	 * @return A message describing the violated precondition, or <code>null</code> if the
	 *         compaction may run.
	 *
	 * @see PersistencyMaintenance#checkPreconditions()
	 */
	public static ResKey checkPreconditions() {
		return PersistencyMaintenance.checkPreconditions();
	}

	/**
	 * Counts what {@link #compact(long, Log)} would change, without changing anything.
	 *
	 * @param beforeDate
	 *        Point in time in milliseconds since the epoch.
	 * @param log
	 *        Receives progress information and the resulting report.
	 * @return What a compaction would change.
	 */
	public static Report analyze(long beforeDate, Log log) {
		try {
			return newCompaction().analyzeHistory(beforeDate, log);
		} catch (SQLException ex) {
			throw new TopLogicException(I18NConstants.ERROR_ANALYSIS_FAILED, ex);
		}
	}

	/**
	 * Collapses all revisions committed before the given point in time into the newest revision
	 * committed at or before that point in time.
	 *
	 * <p>
	 * The compaction runs on the calling thread. The current state of the objects the running
	 * knowledge base holds in its cache is adapted to the rewritten rows (see
	 * {@link DBKnowledgeBase#historyCompacted(long)}), so that objects can still be changed, e.g.
	 * the sessions ended by the restart can still record their logout. Its other caches still
	 * describe the discarded history afterwards and are dropped by
	 * {@link #scheduleRestart(Log, boolean)}.
	 * </p>
	 *
	 * @param beforeDate
	 *        Point in time in milliseconds since the epoch.
	 * @param log
	 *        Receives progress information and the resulting report.
	 * @return What was changed.
	 *
	 * @throws TopLogicException
	 *         If a precondition is violated, or if the compaction fails.
	 *
	 * @see #checkPreconditions()
	 */
	public static Report compact(long beforeDate, Log log) {
		PersistencyMaintenance.requirePreconditions();

		DBKnowledgeBase kb = PersistencyMaintenance.defaultKnowledgeBase();
		Report report;
		try {
			report = HistoryCompaction.newInstance(kb).compactHistory(beforeDate, log);
		} catch (SQLException ex) {
			throw new TopLogicException(I18NConstants.ERROR_COMPACTION_FAILED, ex);
		}
		if (report.getCompactionRevision() > 0) {
			kb.historyCompacted(report.getCompactionRevision());
		}
		return report;
	}

	/**
	 * Restarts the persistency layer a few seconds from now, dropping the caches that still
	 * describe the discarded history.
	 *
	 * <p>
	 * The restart tears down every session and every service depending on the
	 * {@link KnowledgeBaseFactory}. It therefore runs on a {@link SchedulerService} thread in a
	 * system interaction, never on the thread requesting it.
	 * </p>
	 *
	 * @param log
	 *        Receives the outcome of the restart.
	 * @param leaveMaintenanceWindow
	 *        Whether the maintenance window is left after the persistency layer is up and running
	 *        again; <code>true</code> only for a maintenance window that was entered for the
	 *        compaction, never for one an administrator entered manually. After a failed restart the
	 *        maintenance window stays active.
	 *
	 * @see PersistencyMaintenance#scheduleRestart(Log, boolean)
	 */
	public static void scheduleRestart(Log log, boolean leaveMaintenanceWindow) {
		PersistencyMaintenance.scheduleRestart(log, leaveMaintenanceWindow);
	}

	/**
	 * Creates the engine operating on the database of the default {@link KnowledgeBase}.
	 */
	private static HistoryCompaction newCompaction() {
		return HistoryCompaction.newInstance(PersistencyMaintenance.defaultKnowledgeBase());
	}

}
