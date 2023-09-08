/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.persist;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.Logger;

/** Testcase for the {@link com.top_logic.dob.persist.NewDataManager}.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestNewDataManager extends AbstractDataManagerTest {

    private static final TestFactory TEST_FACTORY = new TestFactory() {
		@Override
		public Test createSuite(Class testCase, String suiteName) {
			TestSuite suite = new ActiveTestSuite(suiteName);
	        suite.addTestSuite(TestNewDataManager.class);
	        suite.addTest(TestUtils.tryEnrichTestnames(new TestNewDataManager("testMassStore"), "ActiveTestSuite"));
			return suite;
		}
    };

	/**
     * Constructor for TestNewDataManager.
     */
    public TestNewDataManager(String name) {
        super(name);
    }

    /**
     * the suite of tests to perform
     */
    public static Test suite() {
        return suite(TestNewDataManager.class, DataManagerFactory.NEW_DATA_MANAGER_DEFAULT_INSTANCE, TEST_FACTORY);
    }

    /**
     * main function for direct Testing.
     */
    public static void main (String[] args) {
        SHOW_TIME = true;
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}