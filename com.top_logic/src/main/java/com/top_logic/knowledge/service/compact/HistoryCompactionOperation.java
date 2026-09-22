/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.HistoryCompaction;
import com.top_logic.knowledge.service.db2.HistoryCompaction.Report;
import com.top_logic.util.error.TopLogicException;

/**
 * Runs {@link HistoryCompaction} on the {@link PersistencyLayer#getKnowledgeBase() default
 * knowledge base} of the running application.
 *
 * <p>
 * The compaction deletes rows of revisions that a running {@link KnowledgeBase} still holds in its
 * caches and that other sessions and other nodes of a cluster may still access. It therefore
 * requires an active maintenance window and, in a cluster, that the executing node is the only
 * active one. Within a maintenance window no user session works with the data, and the state
 * visible in the newest revision is not touched by the rewrite, so the compaction runs against the
 * started knowledge base.
 * </p>
 *
 * <p>
 * Afterwards the {@link KnowledgeBaseFactory} is restarted, which drops the caches of items,
 * revisions and branches that still describe the discarded history. The restart ends every session
 * and is therefore scheduled on the {@link SchedulerService} instead of running on the thread that
 * triggered the compaction. The maintenance window is a cluster property that is read again on
 * start-up and hence survives the restart.
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
	 * Delay after which the restart of the persistency layer starts, so that the answer to the
	 * request triggering it can still be delivered.
	 */
	private static final long RESTART_DELAY_SECONDS = 2;

	/**
	 * Whether the preconditions of {@link #compact(long, Log)} are currently met.
	 *
	 * @return A message describing the violated precondition, or <code>null</code> if the
	 *         compaction may run.
	 */
	public static ResKey checkPreconditions() {
		if (!MaintenanceWindowManager.isMaintenanceActive()) {
			return I18NConstants.ERROR_NO_MAINTENANCE_WINDOW;
		}

		ClusterManager clusterManager = ClusterManager.getInstance();
		if (clusterManager.isClusterMode()) {
			List<Long> activeNodes;
			try {
				activeNodes = clusterManager.getActiveNodes();
			} catch (SQLException ex) {
				throw new TopLogicException(I18NConstants.ERROR_CLUSTER_STATE_UNAVAILABLE, ex);
			}
			if (activeNodes.size() != 1) {
				return I18NConstants.ERROR_CLUSTER_NODES_ACTIVE__COUNT.fill(Integer.valueOf(activeNodes.size()));
			}
		}

		return null;
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
		ResKey problem = checkPreconditions();
		if (problem != null) {
			throw new TopLogicException(problem);
		}

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
	 */
	public static void scheduleRestart(Log log) {
		log.info("Restarting the persistency layer in " + RESTART_DELAY_SECONDS + " seconds.");

		SchedulerService.getInstance().schedule(
			() -> ThreadContextManager.inSystemInteraction(HistoryCompactionOperation.class, () -> restart(log)),
			RESTART_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	private static void restart(Log log) {
		try {
			ModuleUtil.INSTANCE.restart(KnowledgeBaseFactory.Module.INSTANCE, null);
			log.info("The persistency layer is up and running with a compacted history.");
		} catch (RestartException ex) {
			log.error("Restarting the persistency layer failed.", ex);
		}
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
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!(kb instanceof DBKnowledgeBase)) {
			throw new TopLogicException(I18NConstants.ERROR_UNSUPPORTED_KNOWLEDGE_BASE);
		}
		return HistoryCompaction.newInstance((DBKnowledgeBase) kb);
	}

}
