/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.SimpleTestFactory;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test that {@link DBKnowledgeBaseTestSetup} is able to actually select the Oracle database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDBKnowledgeBaseTestSetup extends AbstractDBKnowledgeBaseTest {

	public void testUsingOracle() throws SQLException {
		assertIsOracle(kb().getConnectionPool());
	}

	public static void assertIsOracle(ConnectionPool pool) throws SQLException {
		PooledConnection connection = pool.borrowReadConnection();
		try {
			Statement statement = connection.createStatement();
			try {
				String imp = statement.getClass().getName();
				assertTrue(imp, imp.contains("oracle"));
			} finally {
				statement.close();
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	public static Test suite() {
		if (DatabaseTestSetup.useOnlyDefaultDB()) {
			switch (DatabaseTestSetup.DEFAULT_DB) {
				case ORACLE_DB:
					return suite(TestDBKnowledgeBaseTestSetup.class, DBType.ORACLE_DB);
				case ORACLE12_DB:
					return suite(TestDBKnowledgeBaseTestSetup.class, DBType.ORACLE12_DB);
				case ORACLE19_DB:
					return suite(TestDBKnowledgeBaseTestSetup.class, DBType.ORACLE19_DB);
				default:
					return SimpleTestFactory.newSuccessfulTest("Skipped testing with Oracle.");
			}
		}

		TestSuite test = new TestSuite();
		test.addTest(suite(TestDBKnowledgeBaseTestSetup.class, DBType.ORACLE_DB));
		test.addTest(suite(TestDBKnowledgeBaseTestSetup.class, DBType.ORACLE12_DB));
		test.addTest(suite(TestDBKnowledgeBaseTestSetup.class, DBType.ORACLE19_DB));
		return test;
	}

}
