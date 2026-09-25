/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import java.sql.SQLException;

import com.top_logic.basic.Log;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
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
	 * The compaction runs on the calling thread. The caches of the running knowledge base still
	 * describe the discarded history afterwards and are dropped by {@link #scheduleRestart(Log)}.
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

		try {
			return newCompaction().compactHistory(beforeDate, log);
		} catch (SQLException ex) {
			throw new TopLogicException(I18NConstants.ERROR_COMPACTION_FAILED, ex);
		}
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
	 *
	 * @see PersistencyMaintenance#scheduleRestart(Log)
	 */
	public static void scheduleRestart(Log log) {
		PersistencyMaintenance.scheduleRestart(log);
	}

	/**
	 * Compacts the history and drops the outdated caches afterwards.
	 *
	 * @param beforeDate
	 *        Point in time in milliseconds since the epoch.
	 * @param log
	 *        Receives progress information, the resulting report and the outcome of the restart.
	 * @return What was changed.
	 *
	 * @see #compact(long, Log)
	 * @see #scheduleRestart(Log)
	 */
	public static Report compactAndRestart(long beforeDate, Log log) {
		Report report = compact(beforeDate, log);
		scheduleRestart(log);
		return report;
	}

	/**
	 * Creates the engine operating on the database of the default {@link KnowledgeBase}.
	 */
	private static HistoryCompaction newCompaction() {
		return HistoryCompaction.newInstance(PersistencyMaintenance.defaultKnowledgeBase());
	}

}
