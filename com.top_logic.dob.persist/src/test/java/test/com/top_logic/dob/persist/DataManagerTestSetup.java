/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.persist;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.RearrangableThreadContextSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.db.schema.SelectiveSchemaTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.dob.persist.DataManager;

/**
 * Test setup to install a {@link DataManager} singleton.
 * 
 * @see #getDMTest(Class, TestFactory) Creating a test suite that is run with
 *      the default data manager.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DataManagerTestSetup extends RearrangableThreadContextSetup {
	
	private static String DM_SCHEMA_NAME = "dataManager";

	private static MutableInteger setupCnt = new MutableInteger();
	
	private final DataManagerFactory dmFactory;
	private DataManager globalManager;

	protected DataManagerTestSetup(Test test, DataManagerFactory dmFactory) {
		super(test, setupCnt);
		this.dmFactory = dmFactory;
	}
	
	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), this.dmFactory);
	}
	
	private static DataManager setupInstance(DataManager dataManager) {
		return TestModuleUtil.installNewInstance(DataManager.Module.INSTANCE, dataManager);
	}
	
	@Override
	public final void doSetUp() throws Exception {
        // Install data manager for test.
        DataManager dataManager = dmFactory.createDataManager(ConnectionPoolRegistry.getDefaultConnectionPool());
		this.globalManager = setupInstance(dataManager);
	}
	
	@Override
	public final void doTearDown() throws Exception {
		// Let the tables in place for later analysis.
		
		// Reestablish former data manager.
		setupInstance(this.globalManager);
	}
	
	public static Test getSingleDMTest(Test test) {
		return DatabaseTestSetup.getSingleDBTest(test);
	}

	/**
	 * Wraps the given test with a {@link DataManagerTestSetup} and a
	 * {@link DatabaseTestSetup} for the given database.
	 * 
	 * @param test
	 *        The test to wrap.
	 * @param dbType
	 *        The database to use, see {@link DatabaseTestSetup}.
	 * @return the wrapped test.
	 */
	public static Test getDMTest(Test test, DBType dbType) {
		return DatabaseTestSetup.getDBTest(createDataManagerTestSetup(test, DataManagerFactory.NEW_DATA_MANAGER_DEFAULT_INSTANCE), dbType);
	}

	/**
	 * Creates a test suite from the given class to run with the default
	 * {@link DataManager} and the given database.
	 * 
	 * @param testCase
	 *        the class to create the test suite from
	 * @param dbType
	 *        the database to use, see {@link DatabaseTestSetup}.
	 * @return the wrapped test suite.
	 */
	public static Test getDMTest(Class testCase, DBType dbType) {
		return DatabaseTestSetup.getDBTest(createDataManagerTestSetup(new TestSuite(testCase), DataManagerFactory.NEW_DATA_MANAGER_DEFAULT_INSTANCE), dbType);
	}

	/**
	 * Creates a default {@link TestSuite} for the given class to run with the
	 * default data manager on all databases.
	 * 
	 * @param testCase
	 *        The test class
	 * @return The wrapped test suite.
	 */
	public static Test getDMTest(Class testCase) {
		return getDMTest(testCase, DataManagerFactory.NEW_DATA_MANAGER_DEFAULT_INSTANCE, DefaultTestFactory.INSTANCE);
	}
	
	/**
	 * Creates a {@link TestSuite} for the given class from the given test
	 * factory to run with the default data manager on all databases.
	 * 
	 * @param testCase
	 *        The test class
	 * @param f
	 *        The test factory that produces the raw test suite.
	 * @return The wrapped test suite.
	 */
	public static Test getDMTest(Class testCase, final TestFactory f) {
		return getDMTest(testCase, DataManagerFactory.NEW_DATA_MANAGER_DEFAULT_INSTANCE, f);
	}

	/**
	 * Creates a {@link TestSuite} for the given class from the given test
	 * factory to run with the data manager produced by the given
	 * {@link DataManagerFactory} on all databases.
	 * 
	 * @param testCase
	 *        The test class
	 * @param dmFactory
	 *        The factory that produces a {@link DataManager}.
	 * @param f
	 *        The test factory that produces the raw test suite.
	 * @return The wrapped test suite.
	 */
	public static Test getDMTest(Class testCase, final DataManagerFactory dmFactory, TestFactory f) {
		return DatabaseTestSetup.getDBTest(testCase, new TestFactoryProxy(f) {
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				return createDataManagerTestSetup(super.createSuite(testCase, suiteName), dmFactory);
			}
		});
	}

	public static Test createDataManagerTestSetup(Test test, DataManagerFactory dmFactory) {
		test = new DataManagerTestSetup(test, dmFactory);
		test = ServiceTestSetup.createSetup(test, DataManager.Module.INSTANCE);
		test = new SelectiveSchemaTestSetup(test, DM_SCHEMA_NAME);
		return test;
	}

}