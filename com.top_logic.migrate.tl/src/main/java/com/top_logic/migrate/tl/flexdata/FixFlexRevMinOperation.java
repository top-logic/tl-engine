/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.flexdata;

import static com.top_logic.knowledge.service.db2.AbstractFlexDataManager.*;

import java.sql.SQLException;

import com.top_logic.basic.Protocol;
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
public class FixFlexRevMinOperation extends DBOperation {

	private long _migrateRev;

	/**
	 * Creates a {@link FixFlexRevMinOperation}.
	 * 
	 * @param log
	 *        See {@link #log()}.
	 * @param kbName
	 *        See {@link #kbName()}.
	 * @param migrateRev
	 *        The revision caused by the DB1 DB2 migration. This one is the smallest one in the flex
	 *        data table.
	 */
	public FixFlexRevMinOperation(Protocol log, String kbName, long migrateRev) {
		super(log, kbName);
		_migrateRev = migrateRev;
	}

	@Override
	protected void operate() throws SQLException {
		for (MOKnowledgeItem type : concreteTypes()) {
			fixFlexData(type);
		}
		connection().commit();
	}

	private void fixFlexData(final MOKnowledgeItem type) throws SQLException {
		String sql = "" +
			"UPDATE " +
			"  " + FLEX_DATA_DB_NAME + " f " +
			"JOIN " +
			"  " + type.getDBName() + " i " +
			"ON " +
			"  i.BRANCH = f.BRANCH " +
			"  AND i.IDENTIFIER = f.IDENTIFIER " +
			"  AND i.REV_MIN <= f.REV_MIN " +
			"  AND i.REV_MAX >= f.REV_MIN " +
			"SET " +
			"  f.REV_MIN = i.REV_CREATE " +
			"WHERE " +
			"  f.TYPE = '" + type.getName() + "' " +
			"  AND f.REV_MIN = " + _migrateRev;

		System.out.println(sql);
		exec(sql);
	}

}
