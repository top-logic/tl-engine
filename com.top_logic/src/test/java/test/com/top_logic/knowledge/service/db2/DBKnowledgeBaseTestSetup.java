/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess.PoolRef;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceStarter;
import test.com.top_logic.basic.module.TestModuleUtil;
import test.com.top_logic.basic.sql.TestingConnectionPool;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * {@link TestSetup} that installs a {@link KnowledgeBase} for testing using the
 * {@link KnowledgeBaseTestScenarioConstants}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBKnowledgeBaseTestSetup extends LocalTestSetup {

	/**
	 * Name of the {@link TypeSystem} which is used for Tests using a
	 * {@link DBKnowledgeBaseTestSetup}.
	 */
	public static final String TEST_TYPES = "TestTypes";

	private KnowledgeBaseFactory globalKBFactoryInstance;

	/**
	 * Helper class to start the {@link PersistencyLayer}
	 */
	private ServiceStarter _persistencyLayerStarter = new ServiceStarter(PersistencyLayer.Module.INSTANCE);

	/**
	 * whether the {@link PersistencyLayer} was restarted.
	 */
	private boolean _persistencyLayerRestarted;

	private PoolRef _globalConnectionPool;

	private final List<TypeProvider> _additionalProvider;

	private SetupKBHelper _setupKBHelper;

	protected static class DBKnowledgeBaseAccess extends DBKnowledgeBase {
		public DBKnowledgeBaseAccess(SchemaSetup setup) {
			super(setup);
		}
		
		@Override
		protected void onAutoRollback() {
			// May hide the real problem in a test. If the test does commit or
			// rollback, comment out and try again.
//			fail("Test did neither commit nor rollback.");
		}
		
	}

	public DBKnowledgeBaseTestSetup(Test test, KnowledgeBaseTestScenario scenario) {
		super(ThreadContextDecorator.INSTANCE, test);
		_additionalProvider = new ArrayList<>(scenario.getTestTypes());
	}

	public DBKnowledgeBaseTestSetup(Test test) {
		this(test, KnowledgeBaseTestScenarioImpl.INSTANCE);
	}

	public static void simulateConnectionBreakdown() throws SQLException {
		((TestingConnectionPool) kb().getConnectionPool()).simulateReadConnectionBreakdown();
	}
	
	@Override
	public void setUpLocal() throws Exception {
		super.setUpLocal();

		String kbName = "test";
		KnowledgeBaseConfiguration config = createKBConfigItem(kbName, getTestDB());
		_setupKBHelper = new SetupKBHelper(config);
		_setupKBHelper.addAdditionalTypes(_additionalProvider);
		final KnowledgeBase kb = _setupKBHelper.setup();
		_globalConnectionPool = TestingConnectionPoolRegistryAccess.setupDefaultPool(config.getConnectionPool());
		
		KnowledgeBaseFactoryConfig knowledgeBaseConfig =
			TypedConfiguration.newConfigItem(KnowledgeBaseFactoryConfig.class);
		KnowledgeBaseFactory testFactory =
			new KnowledgeBaseFactory(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, knowledgeBaseConfig) {
			
			@Override
			protected KnowledgeBase initKnowledgeBase(String knowledgeBaseName, Protocol notUsed) {
					return kb;
			}
		};
		boolean success = false;
		this.globalKBFactoryInstance = KnowledgeBaseFactoryAccess.installInstance(testFactory);
		try {
			if (_persistencyLayerStarter.getServiceToStart().isActive()) {
				/* If the {@link PersistencyLayer} is already started it has the wrong {@link
				 * KnowledgeBase} as {@link PersistencyLayer#getKnowledgeBase() knowledge base}, as
				 * it has taken it from the old {@link KnowledgeBaseFactory}. Restarting forces it
				 * to take the {@link KnowledgeBase} from the "new" {@link KnowledgeBaseFactory}. */
				ModuleUtil.INSTANCE.restart(_persistencyLayerStarter.getServiceToStart(), null);
				_persistencyLayerRestarted = true;
			} else {
				_persistencyLayerStarter.startService();
			}
			success = true;
		} finally {
			/* Ensure that original KBFactory is installed when terminated abnormally, because no
			 * teardown is called. */
			if (!success) {
				KnowledgeBaseFactoryAccess.installInstance(globalKBFactoryInstance);
			}

		}
	}

	/**
	 * For subclasses to easier changing / adding further KnowledgeBase-XMLs to load. <br/>
	 * <b>If subclasses overwrite this, they have to call super. This is not an empty hook!</b>
	 */
	protected KnowledgeBaseConfiguration createKBConfigItem(String name, String testDB) throws ConfigurationException {
		Map<String, String> intialValues = createKBConfig(name, testDB);
		return TypedConfiguration.newConfigItem(KnowledgeBaseConfiguration.class, intialValues.entrySet());
	}

	/**
	 * For subclasses to easier changing / adding further KnowledgeBase-XMLs to load. <br/>
	 * <b>If subclasses overwrite this, they have to call super. This is not an empty hook!</b>
	 */
	protected Map<String, String> createKBConfig(String name, String connectionPool) {
		Map<String, String> config = new HashMap<>();
		config.put(KnowledgeBaseConfiguration.NAME_ATTRIBUTE, name);
		// No automatic refetch: Allow explicitly testing situations, where a refetch has not yet
		// been performed.
		config.put(KnowledgeBaseConfiguration.REFETCH_INTERVAL_PROPERTY, "-1");
		config.put(KnowledgeBaseConfiguration.REFETCH_TIMEOUT_PROPERTY, "5000");
		config.put(KnowledgeBaseConfiguration.CONNECTION_POOL_PROPERTY, connectionPool);
		config.put(KnowledgeBaseConfiguration.TYPE_SYSTEM_PROPERTY, TEST_TYPES);
		config.put(KnowledgeBaseConfiguration.SINGLE_NODE_OPTIMIZATION_PROPERTY, Boolean.toString(false));
		return config;
	}
	
	@Override
	public void tearDownLocal() throws Exception {
		if (_persistencyLayerRestarted) {
			/*
			 * Must restart again to get the {@link KnowledgeBase} from the correct {@link
			 * KnowledgeBaseFactory}. Therefore it is necessary to install previous KBFactory before
			 * restarting {@link PersistencyLayer}.
			 */
			KnowledgeBaseFactoryAccess.installInstance(globalKBFactoryInstance);
			ModuleUtil.INSTANCE.restart(_persistencyLayerStarter.getServiceToStart(), null);
		} else {
			_persistencyLayerStarter.stopService();
			KnowledgeBaseFactoryAccess.installInstance(globalKBFactoryInstance);
		}

		_setupKBHelper.tearDown();

		TestingConnectionPoolRegistryAccess.restoreDefaultPool(_globalConnectionPool);
		this._globalConnectionPool = null;

		super.tearDownLocal();
	}

	protected String getTestDB() {
		return ConnectionPoolRegistry.DEFAULT_POOL_NAME;
	}
	
	/**
	 * Ensures that the {@link #kb()} contain all types created by the {@link TypeProvider}.
	 */
	public void addAdditionalTypes(TypeProvider provider) {
		if (_setupKBHelper != null) {
			throw new IllegalStateException("Setup already run.");
		}
		_additionalProvider.add(provider);
	}

	/**
	 * All {@link TypeProvider} which add types to the {@link #kb() KnowledgeBase}.
	 */
	protected List<TypeProvider> getAdditionalProvider() {
		return _additionalProvider;
	}

	private static class KnowledgeBaseFactoryAccess {
		public static KnowledgeBaseFactory installInstance(KnowledgeBaseFactory factory) {
			return TestModuleUtil.installNewInstance(KnowledgeBaseFactory.Module.INSTANCE, factory);
		}
	}

	/**
	 * Access to the {@link KnowledgeBase} for testing.
	 */
	public static DBKnowledgeBase kb() {
		return (DBKnowledgeBase) PersistencyLayer.getKnowledgeBase();
	}

}
