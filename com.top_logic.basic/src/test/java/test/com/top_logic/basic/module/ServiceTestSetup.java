/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

/**
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;

public class ServiceTestSetup extends RearrangableTestSetup {
	
	/**
	 * @see #getSetupCnt(BasicRuntimeModule)
	 */
	private static final Map<BasicRuntimeModule<?>, MutableInteger> setupCnts = new HashMap<>();

	/**
	 * Helper class to start the desired service
	 */
	private final ServiceStarter _serviceSetup;

	protected ServiceTestSetup(TestSetupDecorator decorator, Test test, BasicRuntimeModule<?> module) {
		super(decorator, test, getSetupCnt(module));
		setName(getClass().getSimpleName() + " " + module.getClass().getName());
		_serviceSetup = new ServiceStarter(module);
	}

	/**
	 * Returns the setup counter for the given {@link BasicRuntimeModule module}
	 * . It is not enough to have one setup counter for every instance of the
	 * same class, as for different modules the setup is potentially needed more
	 * than once.
	 */
	private static MutableInteger getSetupCnt(BasicRuntimeModule<?> module) {
		MutableInteger cnt = setupCnts.get(module);
		if (cnt != null) {
			return cnt;
		}
		MutableInteger newSetupCnt = new MutableInteger();
		setupCnts.put(module, newSetupCnt);
		return newSetupCnt;
	}

	@Override
	public Object configKey() {
		return _serviceSetup.getServiceToStart();
	}

	@Override
	protected void doSetUp() throws Exception {
		_serviceSetup.startService();
	}

	@Override
	protected void doTearDown() throws Exception {
		try {
			_serviceSetup.stopService();
		} catch (IllegalStateException ex) {
			throw new IllegalStateException("Failed to shutdown service started for tests: " + TestUtils.computeTestName(getTest()), ex);
		}
	}

	public static Test startConnectionPool(Test inner) {
		return startConnectionPool(inner, DatabaseTestSetup.DEFAULT_DB);
	}

	public static Test startConnectionPool(Test inner, DBType dbType) {
		return DatabaseTestSetup.getDBTest(inner, dbType);
	}
	
	/**
	 * Ensures that the {@link ThreadContextManager} is accessible in the given test.
	 */
	public static Test withThreadContext(Test innerTest) {
		return createSetup(innerTest, ThreadContextManager.Module.INSTANCE);
	}

	public static Test createSetup(TestSetupDecorator decor, Test innerTest, BasicRuntimeModule<?> module) {
		return new ServiceTestSetup(decor, innerTest, module);
	}
	
	public static Test createSetup(TestSetupDecorator decor, Class<? extends Test> testClass, BasicRuntimeModule<?> module) {
		return createSetup(decor, createTestSuite(testClass, module), module);
	}
	
	public static Test createSetup(TestSetupDecorator decor, Class<? extends Test> testClass, BasicRuntimeModule<?>... modules) {
		Test innerTest = new TestSuite(testClass);
		return createSetup(decor, innerTest, modules);
	}
	
	public static Test createSetup(TestSetupDecorator decor, Test innerTest, BasicRuntimeModule<?>... modules) {
		for (int i = modules.length - 1; i >=0 ; i --) {
			innerTest = createSetup(decor, innerTest, modules[i]);
		}
		return innerTest;
	}

	public static Test createSetup(Test innerTest, BasicRuntimeModule<?> module) {
		return createSetup(null, innerTest, module);
	}
	
	public static Test createSetup(Class<? extends Test> testClass, BasicRuntimeModule<?> module) {
		return createSetup(createTestSuite(testClass, module), module);
	}

	public static Test createSetup(Test innerTest, BasicRuntimeModule<?>... modules) {
		return createSetup(null, innerTest, modules);
	}

	public static Test createSetup(Class<? extends Test> testClass, BasicRuntimeModule<?>... modules) {
		return createSetup(null, testClass, modules);
	}

	private static TestSuite createTestSuite(Class<? extends Test> testClass, BasicRuntimeModule<?> module) {
		return createTestSuite(testClass, testClass.getName(), module);
	}

	private static TestSuite createTestSuite(Class<? extends Test> testClass, String suiteName, BasicRuntimeModule<?> module) {
		String name = createSuiteName(suiteName, module);
		TestSuite suite = new TestSuite(testClass);
		suite.setName(name);
		return suite;
	}

	private static String createSuiteName(String suiteName, BasicRuntimeModule<?> module) {
		return suiteName + " (" + module + ")";
	}
	
	public static TestFactory createStarterFactory(final BasicRuntimeModule<?> module) {
		return new TestFactory() {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				Test innerTest = ServiceTestSetup.createTestSuite(testCase, suiteName, module);
				return ServiceTestSetup.createSetup(innerTest, module);
			}
		};
	}

	public static TestFactory createStarterFactoryForModules(final BasicRuntimeModule<?>... module) {
		return new TestFactory() {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test innerTest = suite;
				return ServiceTestSetup.createSetup(innerTest, module);
			}
		};
	}
	
	public static TestFactory createStarterFactoryForModules(final TestFactory innerFactory, final BasicRuntimeModule<?>... module) {
		return createStarterFactoryForModules(null, innerFactory, module);
	}

	public static TestFactory createStarterFactoryForModules(final TestSetupDecorator decorator,
			TestFactory innerFactory, final BasicRuntimeModule<?>... module) {
		return new TestFactoryProxy(innerFactory) {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				Test innerTest = super.createSuite(testCase, suiteName);
				return ServiceTestSetup.createSetup(decorator, innerTest, module);
			}
		};
	}
	
	public static TestFactory createStarterFactory(final BasicRuntimeModule<?> module, TestFactory innerFactory) {
		return new TestFactoryProxy(innerFactory) {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				Test innerTest = super.createSuite(testCase, ServiceTestSetup.createSuiteName(suiteName, module));
				return ServiceTestSetup.createSetup(innerTest, module);
			}
		};
	}

}
