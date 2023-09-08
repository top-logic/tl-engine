/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.flexdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;

/**
 * Rolls back the knowledge base to a given revision number.
 * 
 * <p>
 * Note: Storages are not included.
 * </p>
 * 
 * <p>
 * Note: Branch rollback not supported.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RollbackOperation extends DBOperation {

	private static final long INFINITY = Revision.CURRENT_REV;

	private long _cutAfter;

	/**
	 * Creates a {@link RollbackOperation}.
	 * 
	 * @param log
	 *        See {@link #log()}.
	 * @param kbName
	 *        See {@link #kbName()}.
	 * @param cutAfter
	 *        All revisions with a greater number are removed from the database.
	 */
	public RollbackOperation(Protocol log, String kbName, long cutAfter) {
		super(log, kbName);
		_cutAfter = cutAfter;
	}

	@Override
	protected void operate() throws SQLException {
		for (MOKnowledgeItem type : concreteTypes()) {
			dropAllAfter(type.getDBName());
		}
		dropAllAfter("flex_data");

		final long lastExisitingBranch = dropBranchTables();
		updateSequenceTable(lastExisitingBranch);

		dropSystemTableAfter("REVISION");
		dropSystemTableAfter("REVISION_XREF");
		connection().commit();
	}

	private void updateSequenceTable(long lastExisitingBranch) throws SQLException {

		if (lastExisitingBranch != -1) {
			exec("UPDATE SEQUENCE SET VALUE = " + lastExisitingBranch + " WHERE ID = 'branch';");
			System.out.println("Updated system table SEQUENCE: Set branch = " + lastExisitingBranch);
		}
		exec("UPDATE SEQUENCE SET VALUE = " + _cutAfter + " WHERE ID = 'rev';");
		System.out.println("Updated system table SEQUENCE: Set rev = " + _cutAfter);
	}

	private long dropBranchTables() throws SQLException {
		final long firstDroppedBranchID;
		final Statement stmt = connection().createStatement();
		try {
			final ResultSet droppedBranches =
				stmt.executeQuery("SELECT BRANCH FROM BRANCH WHERE REV > " + _cutAfter
					+ " ORDER BY BRANCH LIMIT 1;");
			try {
				if (!droppedBranches.first()) {
					// all branches before cut revision
					System.out.println("No branch created after revision " + _cutAfter);
					return -1;
				}
				firstDroppedBranchID = droppedBranches.getLong("BRANCH");
			} finally {
				droppedBranches.close();
			}
		} finally {
			stmt.close();
		}

		long lastExisitingBranch = firstDroppedBranchID - 1;
		exec("DELETE FROM BRANCH WHERE BRANCH > " + lastExisitingBranch);
		exec("DELETE FROM BRANCH_SWITCH WHERE BRANCH > " + lastExisitingBranch);
		System.out.println("Dropped branches created after " + _cutAfter + " (all branches with id greater than "
			+ lastExisitingBranch + ")");
		return lastExisitingBranch;
	}

	private void dropAllAfter(final String table) throws SQLException {
		exec("DELETE FROM " + table + " WHERE REV_MIN > " + _cutAfter);
		exec("UPDATE " + table + " SET rev_max = " + INFINITY
			+ " WHERE REV_MAX >= " + _cutAfter + " AND REV_MAX < " + INFINITY);
		System.out.println("Cut table " + table + " to " + _cutAfter);
	}

	private void dropSystemTableAfter(String table) throws SQLException {
		exec("DELETE FROM " + table + " WHERE REV > " + _cutAfter);
		System.out.println("Cut system table " + table + " to " + _cutAfter);
	}

}