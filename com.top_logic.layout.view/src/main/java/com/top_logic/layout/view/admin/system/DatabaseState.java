/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * What the administration shows about the database of a {@link KnowledgeBase}: the database
 * product, and the range of revisions its history still holds.
 *
 * @param product
 *        Name of the database product, <code>null</code> if the data is not stored in a database
 *        or the database does not tell.
 * @param productVersion
 *        Version of the database product, <code>null</code> if unknown.
 * @param driver
 *        Name and version of the JDBC driver, <code>null</code> if unknown.
 * @param sqlDialect
 *        The class name of the SQL dialect the application speaks with the database,
 *        <code>null</code> if unknown.
 * @param lastRevision
 *        Commit number of the newest revision, <code>0</code> if nothing was committed yet,
 *        <code>-1</code> without a history.
 * @param lastRevisionDate
 *        Commit time of the newest revision, <code>null</code> if there is none.
 * @param firstRevision
 *        Commit number of the oldest revision the history still holds: {@link Revision#FIRST_REV}
 *        unless the history was compacted, then the revision it was compacted into;
 *        <code>0</code> if nothing was committed yet, <code>-1</code> without a history.
 * @param firstRevisionDate
 *        Commit time of the oldest revision, <code>null</code> if there is none.
 *
 * @see HistoryManager#getFirstRevision()
 */
public record DatabaseState(String product, String productVersion, String driver, String sqlDialect,
		long lastRevision, Date lastRevisionDate, long firstRevision, Date firstRevisionDate) {

	/** Value of {@link #lastRevision()} and {@link #firstRevision()} without a history. */
	public static final long NO_HISTORY = -1;

	/**
	 * Reads the state of the database of the given {@link KnowledgeBase} now.
	 */
	public static DatabaseState of(KnowledgeBase kb) {
		String product = null;
		String productVersion = null;
		String driver = null;
		String sqlDialect = null;
		if (kb instanceof DBKnowledgeBase db) {
			ConnectionPool pool = db.getConnectionPool();
			PooledConnection connection = pool.borrowReadConnection();
			try {
				DatabaseMetaData meta = connection.getMetaData();
				product = meta.getDatabaseProductName();
				productVersion = meta.getDatabaseProductVersion();
				driver = meta.getDriverName() + " " + meta.getDriverVersion();
				sqlDialect = pool.getSQLDialect().getClass().getName();
			} catch (SQLException ex) {
				Logger.error("Cannot read the meta data of the database.", ex, DatabaseState.class);
			} finally {
				pool.releaseReadConnection(connection);
			}
		}

		HistoryManager history = kb.getHistoryManager();
		if (!history.hasHistory()) {
			return new DatabaseState(product, productVersion, driver, sqlDialect, NO_HISTORY, null, NO_HISTORY,
				null);
		}
		long last = history.getLastRevision();
		long first = history.getFirstRevision();
		return new DatabaseState(product, productVersion, driver, sqlDialect,
			last, commitDate(history, last), first, commitDate(history, first));
	}

	/**
	 * The commit time of the revision with the given commit number, <code>null</code> for no
	 * committed revision.
	 */
	private static Date commitDate(HistoryManager history, long commitNumber) {
		if (commitNumber < Revision.FIRST_REV) {
			return null;
		}
		return new Date(history.getRevision(commitNumber).getDate());
	}

}
