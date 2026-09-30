/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.purge;

import java.sql.SQLException;
import java.util.Collection;

import com.top_logic.basic.Log;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Report;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.util.error.TopLogicException;

/**
 * Runs {@link DeletedObjectPurge} on the {@link PersistencyLayer#getKnowledgeBase() default
 * knowledge base} of the running application.
 *
 * <p>
 * The purge erases the rows of deleted objects from every revision they appear in, so it changes
 * data that a running {@link KnowledgeBase} still holds in its caches and that other sessions and
 * other nodes of a cluster may still read. It therefore runs under the conditions of a
 * {@link PersistencyMaintenance} and is followed by a restart of the persistency layer.
 * </p>
 *
 * <p>
 * The analysis only counts what a purge would remove and needs neither a maintenance window nor a
 * restart. A run that a living object {@link Report#isBlocked() blocks} changes nothing either and
 * answers the blockers, so the caller decides what to do with them.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeletedObjectPurgeOperation {

	/**
	 * Counts what {@link #purge(Collection, Log)} would remove, without changing anything.
	 *
	 * @param seeds
	 *        The objects to remove; their hull is removed with them.
	 * @param log
	 *        Receives progress information and the resulting report.
	 * @return What a purge would remove.
	 */
	public static Report analyze(Collection<ObjectKey> seeds, Log log) {
		try {
			return newPurge().analyze(seeds, log);
		} catch (SQLException ex) {
			throw new TopLogicException(I18NConstants.ERROR_ANALYSIS_FAILED, ex);
		}
	}

	/**
	 * Removes the given objects and their hull from the database.
	 *
	 * <p>
	 * The purge runs on the calling thread. The caches of the running knowledge base still hold the
	 * removed objects afterwards and are dropped by
	 * {@link PersistencyMaintenance#scheduleRestart(Log)}.
	 * </p>
	 *
	 * @param seeds
	 *        The objects to remove; their hull is removed with them.
	 * @param log
	 *        Receives progress information and the resulting report.
	 * @return What was removed, or what blocks the removal.
	 *
	 * @throws TopLogicException
	 *         If a precondition is violated, or if the purge fails.
	 *
	 * @see PersistencyMaintenance#checkPreconditions()
	 */
	public static Report purge(Collection<ObjectKey> seeds, Log log) {
		PersistencyMaintenance.requirePreconditions();

		try {
			return newPurge().purge(seeds, log);
		} catch (SQLException ex) {
			throw new TopLogicException(I18NConstants.ERROR_PURGE_FAILED, ex);
		}
	}

	/**
	 * Removes the given objects and drops the outdated caches afterwards.
	 *
	 * <p>
	 * A run that removed nothing, because it was blocked or because the objects were gone already,
	 * leaves the caches as correct as they were and needs no restart.
	 * </p>
	 *
	 * @param seeds
	 *        The objects to remove; their hull is removed with them.
	 * @param log
	 *        Receives progress information, the resulting report and the outcome of the restart.
	 * @return What was removed, or what blocks the removal.
	 *
	 * @see #purge(Collection, Log)
	 * @see PersistencyMaintenance#scheduleRestart(Log)
	 */
	public static Report purgeAndRestart(Collection<ObjectKey> seeds, Log log) {
		Report report = purge(seeds, log);
		if (!report.isBlocked() && !report.isEmpty()) {
			PersistencyMaintenance.scheduleRestart(log);
		}
		return report;
	}

	/**
	 * Creates the engine operating on the database of the default {@link KnowledgeBase}.
	 */
	private static DeletedObjectPurge newPurge() {
		return DeletedObjectPurge.newInstance(PersistencyMaintenance.defaultKnowledgeBase());
	}

}
