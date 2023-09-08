/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.persist;

import java.sql.SQLException;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.dob.persist.EncryptedDataManager;

/** Testcase for the {@link com.top_logic.dob.persist.EncryptedDataManager}.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestEncryptedDataManager extends AbstractDataManagerTest {

    private static final TestFactory TEST_FACTORY = new TestFactory() {
		@Override
		public Test createSuite(Class testCase, String suiteName) {
			TestSuite suite = new ActiveTestSuite(suiteName);
	        suite.addTestSuite(TestEncryptedDataManager.class);
	        suite.addTest(TestUtils.tryEnrichTestnames(new TestEncryptedDataManager("testMassStore"), "ActiveTestSuite"));
			return suite;
		}
    };

    private static final DataManagerFactory ENCRYPTED_DATA_MANAGER_FACTORY = new DataManagerFactory() {
		@Override
		public DataManager createDataManager(ConnectionPool currentDB) throws SQLException {
			return DataManagerFactory.createDataManager(EncryptedDataManager.class);
		}
    };

	/**
     * Constructor for TestEncryptedDataManager.
     * 
     * @param name The function to execute for testing.
     */
    public TestEncryptedDataManager(String name) {
        super(name);
    }

    /**
     * the suite of tests to perform
     */
    public static Test suite() {
        return suite(TestEncryptedDataManager.class, ENCRYPTED_DATA_MANAGER_FACTORY, TEST_FACTORY);
    }

    /**
     * main function for direct Testing.
     */
    public static void main (String[] args) {
        
        SHOW_TIME = true;   // Show times when calling logTime()
        
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
