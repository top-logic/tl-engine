/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.maintenance;

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
import com.top_logic.util.error.TopLogicException;

/**
 * What an operation needs that rewrites the stored history of the running application.
 *
 * <p>
 * Such an operation works on the database behind the {@link PersistencyLayer#getKnowledgeBase()
 * default knowledge base} with plain SQL and changes rows that the running application still holds
 * in its caches and that other sessions and other nodes of a cluster may still read. It therefore
 * runs only in an active maintenance window and, in a cluster, only on the single active node:
 * within a maintenance window no user session works with the data, and no other node reads what is
 * being rewritten.
 * </p>
 *
 * <p>
 * Afterwards the {@link KnowledgeBaseFactory} is restarted, which drops the caches of items,
 * revisions and branches that still describe the rewritten state. The restart ends every session
 * and is therefore scheduled on the {@link SchedulerService} instead of running on the thread that
 * triggered the operation, so that the answer to the request triggering it is still delivered. The
 * maintenance window is a cluster property that is read again on start-up and hence survives the
 * restart.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistencyMaintenance {

	/**
	 * Delay after which the restart of the persistency layer starts, so that the answer to the
	 * request triggering it can still be delivered.
	 */
	private static final long RESTART_DELAY_SECONDS = 2;

	/**
	 * Whether an operation rewriting the stored history may run now.
	 *
	 * @return A message describing the violated precondition, or <code>null</code> if the operation
	 *         may run.
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
	 * Fails unless an operation rewriting the stored history may run now.
	 *
	 * @throws TopLogicException
	 *         If a precondition is violated.
	 *
	 * @see #checkPreconditions()
	 */
	public static void requirePreconditions() {
		ResKey problem = checkPreconditions();
		if (problem != null) {
			throw new TopLogicException(problem);
		}
	}

	/**
	 * The database of the default {@link KnowledgeBase}.
	 *
	 * @throws TopLogicException
	 *         If the application stores its data elsewhere than in a database.
	 */
	public static DBKnowledgeBase defaultKnowledgeBase() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!(kb instanceof DBKnowledgeBase)) {
			throw new TopLogicException(I18NConstants.ERROR_UNSUPPORTED_KNOWLEDGE_BASE);
		}
		return (DBKnowledgeBase) kb;
	}

	/**
	 * Restarts the persistency layer a few seconds from now, dropping the caches that still
	 * describe the rewritten state.
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
			() -> ThreadContextManager.inSystemInteraction(PersistencyMaintenance.class, () -> restart(log)),
			RESTART_DELAY_SECONDS, TimeUnit.SECONDS);
	}

	private static void restart(Log log) {
		try {
			ModuleUtil.INSTANCE.restart(KnowledgeBaseFactory.Module.INSTANCE, null);
			log.info("The persistency layer is up and running again.");
		} catch (RestartException ex) {
			log.error("Restarting the persistency layer failed.", ex);
		}
	}

}
