/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.SimpleTestFactory;

/**
 * Test that ensures that {@link DBKnowledgeBaseMigrationTestSetup} can select the Oracle DB as
 * target for the migration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDBKnowledgeBaseMigrationTestSetup extends AbstractDBKnowledgeBaseMigrationTest {

	public void testSourceIsOracle() throws SQLException {
		TestDBKnowledgeBaseTestSetup.assertIsOracle(kb().getConnectionPool());
	}

	public void testTargetIsOracle() throws SQLException {
		TestDBKnowledgeBaseTestSetup.assertIsOracle(kbNode2().getConnectionPool());
	}

	public static Test suite() {
		TestSuite test = new TestSuite();
		if (DatabaseTestSetup.useOnlyDefaultDB()) {
			switch (DatabaseTestSetup.DEFAULT_DB) {
				case ORACLE_DB:
				case ORACLE12_DB:
				case ORACLE19_DB:
					test.addTest(suite(TestDBKnowledgeBaseMigrationTestSetup.class, DatabaseTestSetup.DEFAULT_DB));
					break;
				default:
					test.addTest(SimpleTestFactory.newSuccessfulTest("Skipped, only default DB is tested."));
			}
		} else {
			test.addTest(suite(TestDBKnowledgeBaseMigrationTestSetup.class, DBType.ORACLE_DB));
			test.addTest(suite(TestDBKnowledgeBaseMigrationTestSetup.class, DBType.ORACLE12_DB));
			test.addTest(suite(TestDBKnowledgeBaseMigrationTestSetup.class, DBType.ORACLE19_DB));
		}
		return test;
	}

}
