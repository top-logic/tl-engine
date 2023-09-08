/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.sql.PreparedStatement;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl;

import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * The class {@link TestPreparedStatementCaching} tests whether the database
 * driver reuse {@link PreparedStatement}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPreparedStatementCaching extends AbstractDBKnowledgeBaseTest {

	public void testCaching() throws Exception {
		SHOW_TIME = true;

		/* install some data */
		for (int i = 0; i < 100; i++) {
			KnowledgeObject b = newB("A1_" + i);
			setA2(b, "A2_" + i);
		}
		assertTrue(kb().commit());

		PooledConnection borrowedWriteConnection = ConnectionPoolRegistry.getDefaultConnectionPool().borrowWriteConnection();
		try {
			// initialize Java classes
			borrowedWriteConnection.prepareStatement(("SELECT * FROM " + B_NAME)).close();
			borrowedWriteConnection.prepareStatement("SELECT * FROM " + B_NAME).close();

			String sql =
				"UPDATE " + B_NAME + " SET " + BasicTypes.REV_MAX_DB_NAME + "=? " + " WHERE " + A1_NAME
					+ "=? " + "AND "
					+ A2_NAME + "=? AND " + BasicTypes.REV_MIN_DB_NAME + "<=?";

			int numberPrepStatements = 500;

			// test getting PreparedStatements for different SQL
			String[] differentSqls = new String[numberPrepStatements];
			differentSqls[0] = sql;
			for (int index = 1, length = differentSqls.length; index < length; index++) {
				differentSqls[index] =
					differentSqls[index - 1] + " AND (?=" + KnowledgeBaseTestScenarioImpl.A1.getDBName() + ")";
			}
			PreparedStatement[] differentPrepStmts = new PreparedStatement[numberPrepStatements];

			startTime();
			for (int index = 0, length = differentPrepStmts.length; index < length; index++) {
				differentPrepStmts[index] = borrowedWriteConnection.prepareStatement(differentSqls[index]);
				differentPrepStmts[index].close();
			}
			logTime("getting " + numberPrepStatements + " times different statements:");

			// test getting PreparedStatements for the same SQL
			String[] sameSqls = new String[numberPrepStatements];
			for (int index = 0, length = sameSqls.length; index < length; index++) {
				sameSqls[index] = new String(differentSqls[differentSqls.length - 1]);
			}
			PreparedStatement[] samePrepStmts = new PreparedStatement[numberPrepStatements];

			startTime();
			for (int index = 0, length = samePrepStmts.length; index < length; index++) {
				samePrepStmts[index] = borrowedWriteConnection.prepareStatement(sameSqls[index]);
				samePrepStmts[index].close();
			}
			logTime("getting " + numberPrepStatements + " times same statements:");

			// test getting PreparedStatements for the same SQL via
			// PrepStatementCache
			startTime();
			PreparedStatement[] samePrepStmtsViaPrepStatementCache = new PreparedStatement[numberPrepStatements];
			for (int index = 0, length = samePrepStmtsViaPrepStatementCache.length; index < length; index++) {
				samePrepStmtsViaPrepStatementCache[index] = borrowedWriteConnection.prepareStatement(sameSqls[index]);
				samePrepStmtsViaPrepStatementCache[index].close();
			}
			logTime("getting " + numberPrepStatements + " times same statements via PrepStatementCache:");
			
		} finally {
			ConnectionPoolRegistry.getDefaultConnectionPool().releaseWriteConnection(borrowedWriteConnection);
		}
	}

	public static Test suite() {
		return suite(TestPreparedStatementCaching.class);
	}

}
