/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;


import java.util.Objects;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.RearrangableThreadContextSetup;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceStarter;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Environment;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseSetup;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * {@link TestSetup} setting up a {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class KBSetup extends RearrangableThreadContextSetup {

	/**
	 * Type of the {@link KnowledgeBase} under test.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class KBType {

		/** The MySQL is the default DB-KB, but this may change ... */
		public static final KBType KB_MYSQL = new KBType(DBType.MYSQL_DB, false);

		/** Indicates usage of the MSSQL-based DB-KnowledgeBase */
		public static final KBType KB_MSSQL = new KBType(DBType.MSSQL_DB, false);

		/** Indicates usage of the Oracle-based DB-KnowledgeBase */
		public static final KBType KB_ORACLE = new KBType(DBType.ORACLE_DB, false);

		/** Indicates usage of the Oracle12-based DB-KnowledgeBase */
		public static final KBType KB_ORACLE12 = new KBType(DBType.ORACLE12_DB, false);

		/** Indicates usage of the Oracle19-based DB-KnowledgeBase */
		public static final KBType KB_ORACLE19 = new KBType(DBType.ORACLE19_DB, false);

		/** Unversioned knowledge base on MySQL. */
		public static final KBType KB_MYSQL_UNVERSIONED = new KBType(DBType.MYSQL_DB, true);

		/** Name of H2 database based {@link KnowledgeBase}. */
		public static final KBType KB_H2 = new KBType(DBType.H2_DB, false);

		/** Name of DB2 database based {@link KnowledgeBase}. */
		public static final KBType KB_DB2 = new KBType(DBType.DB2_DB, false);

		/** Name of Postgresql database based {@link KnowledgeBase}. */
		public static final KBType KB_POSTGRESQL = new KBType(DBType.POSTGRESQL_DB, false);

		private final DBType _db;

		private final boolean _unversioned;

		KBType(DBType db, boolean unversioned) {
			_db = db;
			_unversioned = unversioned;
		}

		/**
		 * Type of the database of the {@link KnowledgeBase}.
		 */
		public DBType db() {
			return _db;
		}

		/**
		 * Name of the KB to test.
		 */
		public String externalName() {
			return _db.name().replace("_DB", "_KB") + (_unversioned ? "_UNVERSIONED" : "");
		}

		/**
		 * Whether the {@link KnowledgeBase} is unversioned.
		 */
		public boolean unversioned() {
			return _unversioned;
		}

		@Override
		public int hashCode() {
			return Objects.hash(_db, _unversioned);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KBType other = (KBType) obj;
			return _db == other._db && _unversioned == other._unversioned;
		}
	}

	/**
	 * System property to request testing in an unversioned system by default.
	 */
	public static final String DEFAULT_KB_UNVERSIONED_PROPERTY = "tl_test_defaultKbUnversioned";

    /**
     * The default {@link KnowledgeBase}
     * 
     * @see #getSingleKBTest(Test)
     */
	public static final KBType DEFAULT_KB;

	static {
		boolean unversioned =
			Boolean.valueOf(
				Environment.getSystemPropertyOrEnvironmentVariable(DEFAULT_KB_UNVERSIONED_PROPERTY, "false"));
		DEFAULT_KB = new KBType(DatabaseTestSetup.DEFAULT_DB, unversioned);
	}

    /** set to false to skip creation of the OPF tables */
    private static boolean          createTables = true;

    // Static Members (the KBs are cerated just once its to expensive otherwise)

	/** The KB currently setup, <code>null</code> after {@link #tearDown()} */
	private static KBType _currentType;

	private static MutableInteger setupCnt = new MutableInteger();
	
	/**
	 * Helper class to start the {@link PersistencyLayer}.
	 */
	private ServiceStarter _persistencyLayerStarter = new ServiceStarter(PersistencyLayer.Module.INSTANCE);
    
    /**
     * Get the current installed Knowledgebase,
     */
    public static KnowledgeBase getKnowledgeBase() {
        return PersistencyLayer.getKnowledgeBase();
    }

    /**
	 * Get the currently installed {@link KnowledgeBase}.
	 * 
	 * @return <code>null</code> if no Base is current.
	 */
	public static KBType getKnowledgeBaseIndex() {
		return _currentType;
    }

    /**
     * Create a new DBKnowledgebase and eventually set it up.
     */
	protected static KnowledgeBase createDBKnowledgeBase(KBType type) {
		AssertProtocol protocol = new AssertProtocol();
		KnowledgeBase kb = new DBKnowledgeBase();
		KnowledgeBaseConfiguration config = createKBConfig(type);
		kb.initialize(protocol, config);

		kb.startup(protocol);

		return kb;
    }

	private static KnowledgeBaseConfiguration createKBConfig(KBType type) {
		KnowledgeBaseConfiguration config = TypedConfiguration.newConfigItem(KnowledgeBaseConfiguration.class);
		initKBConfig(config, type);
		return config;
	}

	private static void initKBConfig(KnowledgeBaseConfiguration config, KBType type) {
		config.setName(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		// No automatic refetch: Allow explicitly testing situations, where a refetch has not yet
		// been performed.
		config.setRefetchInterval(-1);
		config.setRefetchTimeout(5000);
		config.setDisableVersioning(type.unversioned());
		config.setSingleNodeOptimization(false);
		// connection pool is switched before
//		config.setConnectionPool(type.db().poolName());
	}

	private void destroyDBKnowledgeBase() {
		KnowledgeBaseFactory kbf = KnowledgeBaseFactory.getInstance();
		kbf.destroyKnowledgeBase(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
	}

    // instance members

	private KBType _type;

    /**
	 * Standard Constructor for Testcases.
	 * 
	 * @param t
	 *        Test to wrap into this Setup.
     * @param kb
	 *        The {@link KnowledgeBase} unter test
	 */
	protected KBSetup(Test t, KBType kb) {
		super(t, setupCnt);
		_type = kb;
    }
    
    @Override
    public Object configKey() {
		return _type;
    }

    @Override
    protected void doSetUp() throws Exception {
		// remove default KnowledgeBase is recreated lazy after this test
		destroyDBKnowledgeBase();
		KnowledgeBase kb = createDBKnowledgeBase(_type);
		installKBToFactory(kb);
		
		_currentType = _type;
		if (PersistencyLayer.Module.INSTANCE.isActive()) {
			ModuleUtil.INSTANCE.restart(PersistencyLayer.Module.INSTANCE, null);
		} else {
			_persistencyLayerStarter.startService();
		}
		
    }

	private void installKBToFactory(KnowledgeBase kb) {
		KnowledgeBaseFactory factory = KnowledgeBaseFactory.getInstance();
		ReflectionUtils.executeMethod(factory, "addKnowledgeBase", new Class[] { KnowledgeBase.class },
			new Object[] { kb });
	}

    @Override
    protected void doTearDown() throws Exception {
		destroyDBKnowledgeBase();
		_currentType = null;

		if (_persistencyLayerStarter.hasStartedService()) {
			_persistencyLayerStarter.stopService();
		} else {
			ModuleUtil.INSTANCE.restart(PersistencyLayer.Module.INSTANCE, null);
		}
    	
    }

	/**
     * Get a KnowledgeBase-enabled Test using only a single KnowledgeBase. <br/>
     * You should only use this method when you are sure that there is no
     * KnowledgeBase specific dependencies that need to be tested. <br/>
     * Which KnowledgeBase is used is not public information and may change
     * without notice.
     * 
     * @param c the test to enable; must not be null
     * @return the KnowledgeBase-enabled Test; never null
     * @see #getKBTest(Class)
     * @see #getSingleKBTest(Test) #author Michael Eriksson
     */
    public static Test getSingleKBTest(Class c) {
        return getSingleKBTest(new TestSuite(c));
    }

    public static Test getSingleKBTest(Class c, TestFactory f) {
		return wrapTLCommonSetups(newTest(c, DEFAULT_KB, f));
    }
    
    /**
     * Get a KnowledgeBase-enabled Test using only a single KnowledgeBase. <br/>
     * You should only use this method when you are sure that there is no
     * KnowledgeBase specific dependencies that need to be tested. <br/>
     * Which KnowledgeBase is used is not public information and may change
     * without notice.
     * 
     * @param t the test to enable; must not be null
     * @return the KnowledgeBase-enabled Test; never null
     * @see #getKBTest(Class)
     * @see #getSingleKBTest(Class) #author Michael Eriksson
     */
    public static Test getSingleKBTest(Test t) {
		return getKBTest(t, DEFAULT_KB);
    }

	/**
	 * Get a KnowledgeBase-enabled Test using the specified KnowledgeBase. <br/>
	 * You should only use this method when you explicitly need to use one specific KnowledgeBase.
	 * Normally you should use the corresponding method without a KnowledgeBase argument.
	 * 
	 * @param c
	 *        the test to enable; must not be null
	 * @param kb
	 *        the KnowledgeBase to use; must be OPF_KB or XML_KB
	 * @return the KnowledgeBase-enabled Test; never null
	 * 
	 * @see #getKBTest(Test, KBType)
	 * @see #getSingleKBTest(Class)
	 * @see #getSingleKBTest(Test)
	 * @see #getKBTest(Class, KBType, TestFactory) If the test case for the given class cannot be
	 *      produced with {@link TestSuite#TestSuite(Class)}
	 */
	public static Test getKBTest(Class c, KBType kb) {
		return wrapTLCommonSetups(newTest(c, kb, DefaultTestFactory.INSTANCE));
    }

	/**
	 * Wraps the test case produced by the given factory for execution with the given knowledge
	 * base.
	 * 
	 * @param testCase
	 *        The test case class that provides the context.
	 * @param kb
	 *        The knowledge base {@link KBType type} to use for the test case.
	 * @param f
	 *        The test factory that produces the test to wrap
	 * @return The wrapped test case.
	 * 
	 * @see #getKBTest(Class, KBType) if the test suite to wrap can be produced with
	 *      {@link TestSuite#TestSuite(Class)}.
	 */
	public static Test getKBTest(Class testCase, KBType kb, TestFactory f) {
		return wrapTLCommonSetups(newTest(testCase, kb, f));
    }

	public static String getKBName(KBType kb) {
		return kb.externalName();
	}

	/**
	 * Get a KnowledgeBase-enabled Test using the specified KnowledgeBase. <br/>
	 * You should only use this method when you explicitly need to use one specific KnowledgeBase.
	 * Normally you should use the corresponding method without a KnowledgeBase argument or
	 * getSingleKBTest.
	 * 
	 * @param t
	 *        the test to enable; must not be null
	 * @param kb
	 *        the KnowledgeBase to use; must be OPF_KB or XML_KB
	 * @return the KnowledgeBase-enabled Test; never null
	 * @see #getKBTest(Class)
	 * @see #getKBTest(Class, KBType)
	 * @see #getSingleKBTest(Class)
	 * @see #getSingleKBTest(Test) #author Michael Eriksson
	 */
	public static Test getKBTest(Test t, KBType kb) {
		return wrapTLCommonSetups(newTest(t, kb));
    }

    /**
	 * Get a KnowledgeBase-enabled Test looping through all available KnowledgeBases.
	 * 
	 * @param c
	 *        the test to enable; must not be null
	 * @return the KnowledgeBase-enabled Test; never null
	 * @see #getKBTest(Class, KBType)
	 * @see #getKBTest(Test, KBType)
	 * @see #getSingleKBTest(Class)
	 * @see #getSingleKBTest(Test) #author Michael Eriksson
	 */
    public static Test getKBTest(Class c) {
		return getKBTest(c, DefaultTestFactory.INSTANCE);
    }

	/**
	 * Wraps the test case produced by the given factory for execution with all
	 * knowledge bases.
	 * 
	 * @param testCase
	 *        The test case class that provides the context.
	 * @param f
	 *        The test factory that produces the test to wrap
	 * @return The suite of wrapped test cases.
	 */
    public static Test getKBTest(Class testCase, TestFactory f) {
		return wrapTLCommonSetups(getKBTestWithoutSetups(testCase, f));
	}

	/**
	 * A variant of {@link #getKBTest(Class, TestFactory)} that <b>does</b> not
	 * {@link #wrapTLCommonSetups(Test)} the setups needed for the {@link KnowledgeBase} around the
	 * test.
	 */
	public static Test getKBTestWithoutSetups(Class testCase, TestFactory f) {
		return getKBTestWithoutSetups(testCase, f, false);
    }

	/**
	 * A variant of {@link #getKBTest(Class, TestFactory)} that <b>does</b> not
	 * {@link #wrapTLCommonSetups(Test)} the setups needed for the {@link KnowledgeBase} around the
	 * test.
	 * 
	 * @param onlyVersionedKB
	 *        If <code>true</code> only versioned {@link KnowledgeBase}'s are tested.
	 */
	public static TestSuite getKBTestWithoutSetups(Class testCase, TestFactory f, boolean onlyVersionedKB) {
		TestSuite theSuite = new TestSuite(testCase.getName());
    	
    	if (DatabaseTestSetup.useOnlyDefaultDB()) {
			theSuite.addTest(newTest(testCase, DEFAULT_KB, f));
    	} else {
			theSuite.addTest(newTest(testCase, KBType.KB_MYSQL, f));
			theSuite.addTest(newTest(testCase, KBType.KB_MSSQL, f));
			theSuite.addTest(newTest(testCase, KBType.KB_H2, f));
			theSuite.addTest(newTest(testCase, KBType.KB_ORACLE, f));
			theSuite.addTest(newTest(testCase, KBType.KB_ORACLE12, f));
			theSuite.addTest(newTest(testCase, KBType.KB_ORACLE19, f));
    		// #8797 licence has been expired
//			theSuite.addTest(newTest(testCase, KBType.KB_DB2, f));
			if (!onlyVersionedKB) {
				theSuite.addTest(newTest(testCase, KBType.KB_MYSQL_UNVERSIONED, f));
			}
    	}
		return theSuite;
	}

	private static Test newTest(Class testCase, KBType type, TestFactory f) {
		Test t = createSuite(f, testCase, type);
		return newTest(t, type);
	}
    
	private static Test newTest(Test test, KBType type) {
		return TestUtils.tryEnrichTestnames(internalCreateKBTest(test, type), getKBName(type));
	}

	private static Test createSuite(TestFactory f, Class testCase, KBType kb) {
		return f.createSuite(testCase, testCase.getName() + " (" + getKBName(kb) + ")");
	}
    
	private static Test internalCreateKBTest(Test t, KBType kb) {
		return wrapKBSpecificSetups(kb, new KBSetup(addNeededSetups(t), kb));
	}

	/**
	 * Returns a Test that contains all tests usually needed for accessing database.
	 */
	protected static Test addNeededSetups(Test t) {
		t = ServiceTestSetup.createSetup(t, KnowledgeBaseSetup.getOptionalDependencies());
		return t;
	}

	/**
	 * Wrap the {@link TestSetup}s around the {@link Test}, that are needed for the
	 * {@link KnowledgeBase}, but not for all TL tests.
	 */
	protected static Test wrapKBSpecificSetups(KBType type, Test test) {
		test =
			ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE, test, KnowledgeBaseFactory.Module.INSTANCE);
		test = ServiceTestSetup.withThreadContext(test);
		test = new KBTableSetup(test, type);
		test = new SwitchDefaultPoolSetup(test, type.db().poolName());
		test = ServiceTestSetup.createSetup(null, test, ConnectionPoolRegistry.Module.INSTANCE);
		return test;
	}

	/**
	 * Wrap the {@link TestSetup}s around the {@link Test} that are needed for (almost) all TL
	 * tests.
	 */
	public static Test wrapTLCommonSetups(Test startKBFactory) {
		return ModuleLicenceTestSetup.setupModule(startKBFactory);
	}

	public static void setCreateTables(boolean value) {
		createTables = value;
	}

	public static boolean shouldCreateTables() {
		return createTables;
	}
	
}