/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess;
import test.com.top_logic.basic.TestingConnectionPoolRegistryAccess.PoolRef;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.knowledge.service.InitialTableSetup;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationLock;
import com.top_logic.knowledge.service.migration.MigrationService;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.Version;
import com.top_logic.knowledge.service.migration.processors.AlignTableLayoutProcessor;

/**
 * Test of the {@link MigrationService} taking the {@link MigrationLock} only for a start that
 * changes the database.
 *
 * <p>
 * The test starts a {@link MigrationService} on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}. Before each test, the stored schema is the
 * application schema and the maximal versions of all migration modules are stored, so that a
 * start has nothing to do. Each test enters the {@link MigrationLock} of another node before the
 * start: A start that has nothing to do completes without touching the lock, a start with pending
 * changes waits until the lock has been deleted and releases it after the migration.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPendingMigrationChanges extends AbstractDBKnowledgeBaseTest {

	/** Type whose definition is changed in the stored schema. */
	private static final String LABEL_TYPE = "ObjectLabel";

	/** Value of the {@link MigrationLock#LOCK_PROPERTY} entered by another node. */
	private static final String OTHER_NODE = "other node since 2026-01-01T00:00:00Z";

	/** Milliseconds a start waiting for the lock is observed before the lock is deleted. */
	private static final long WAIT_TIME = 2500;

	/** Milliseconds to wait for a start to complete. */
	private static final long START_TIMEOUT = 60000;

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private DBProperties _properties;

	private SetupKBHelper _setup;

	/** The application schema, as compared with the stored schema by the migration. */
	private SchemaConfiguration _applicationSchema;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_properties = new DBProperties(_pool);
		_setup = new SetupKBHelper(_kbConfig);
		_setup.setup();

		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			_applicationSchema = new MigrationContext(log, connection).getApplicationSchema();
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, _applicationSchema);
			MigrationUtil.updateStoredVersions(log, connection, MigrationUtil.maximalVersions(log));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), List.of(), log.getErrors());
		assertFalse("No migration versions to store.", MigrationUtil.maximalVersions(log).isEmpty());

		_properties.setProperty(MigrationLock.LOCK_PROPERTY, OTHER_NODE);
	}

	@Override
	protected void tearDown() throws Exception {
		_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);
		_setup.tearDown();
		_setup = null;
		_properties = null;
		_pool = null;
		_kbConfig = null;
		_applicationSchema = null;
		super.tearDown();
	}

	/**
	 * A start without pending changes neither waits for the lock of another node nor creates a
	 * lock.
	 */
	public void testNothingPending() throws Exception {
		String storedSchema = storedSchemaRaw();
		Map<String, Version> storedVersions = storedVersions();

		AtomicBoolean done = new AtomicBoolean();
		TestFuture start = inParallel(() -> {
			startMigration();
			done.set(true);
		});
		try {
			start.check(START_TIMEOUT);
			assertTrue("Start did not complete.", done.get());
			assertEquals("Start touched the lock of the other node.", OTHER_NODE, lockProperty());
		} finally {
			// Let a start waiting for the lock terminate.
			_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);
		}

		assertEquals("Stored schema changed.", storedSchema, storedSchemaRaw());
		assertEquals("Stored versions changed.", storedVersions, storedVersions());
	}

	/**
	 * A migration module without stored version is a pending change: Its version is stored under
	 * the lock.
	 */
	public void testMissingVersion() throws Exception {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationUtil.dropStoredVersions(new BufferingProtocol(), connection);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertTrue(storedVersions().isEmpty());

		startUnderLock(() -> storedVersions().isEmpty());

		Map<String, Version> storedVersions = storedVersions();
		for (Version version : MigrationUtil.maximalVersions(new BufferingProtocol())) {
			assertEquals("Version of module '" + version.getModule() + "' not stored.", version.getName(),
				storedVersions.get(version.getModule()).getName());
		}
	}

	/**
	 * A type defined differently in the stored and the application schema is a pending change: The
	 * changed definition is adopted under the lock.
	 */
	public void testChangedType() throws Exception {
		Decision configured = labelConfig(_applicationSchema).isVersioned();
		Decision changed = configured == Decision.FALSE ? Decision.TRUE : Decision.FALSE;

		SchemaConfiguration stored = TypedConfiguration.copy(_applicationSchema);
		MetaObjectConfig storedLabel = labelConfig(stored);
		storedLabel.update(storedLabel.descriptor().getProperty(MetaObjectConfig.VERSIONED_PROPERTY), changed);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, stored);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals(changed, labelConfig(storedSchema()).isVersioned());

		startUnderLock(() -> labelConfig(storedSchema()).isVersioned() == changed);

		assertEquals("Changed type not adopted.", configured, labelConfig(storedSchema()).isVersioned());
	}

	/**
	 * A changed table layout is a pending change: The tables are aligned with the layout of the
	 * application schema under the lock.
	 */
	public void testLayoutChange() throws Exception {
		boolean configured = _applicationSchema.getBranchLast().booleanValue();
		SchemaConfiguration other = TypedConfiguration.copy(_applicationSchema);
		other.setBranchLast(Boolean.valueOf(!configured));

		// Convert the database to the other layout.
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return other;
				}
			};
			TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(AlignTableLayoutProcessor.Config.class))
				.doMigration(context, log, connection);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), List.of(), log.getErrors());
		assertEquals(Boolean.valueOf(!configured), storedSchema().getBranchLast());

		startUnderLock(() -> storedSchema().getBranchLast().booleanValue() != configured);

		assertEquals("Layout not aligned.", Boolean.valueOf(configured), storedSchema().getBranchLast());
	}

	/**
	 * Starts the migration while the lock of another node is entered: Checks that the start waits
	 * for the lock without changing the database, deletes the lock, and checks that the start
	 * completes and releases the lock.
	 *
	 * @param unchanged
	 *        Whether the database is still unchanged.
	 */
	private void startUnderLock(Check unchanged) throws Exception {
		AtomicBoolean done = new AtomicBoolean();
		TestFuture start = inParallel(() -> {
			startMigration();
			done.set(true);
		});
		try {
			Thread.sleep(WAIT_TIME);
			assertFalse("Start with pending changes did not wait for the lock.", done.get());
			assertTrue("Database changed without lock.", unchanged.test());
			assertEquals(OTHER_NODE, lockProperty());
		} finally {
			_properties.setProperty(MigrationLock.LOCK_PROPERTY, null);
		}

		start.check(START_TIMEOUT);
		assertTrue("Start did not complete.", done.get());
		assertNull("Lock not released.", lockProperty());
		assertFalse("Database not changed.", unchanged.test());
	}

	/**
	 * Starts a {@link MigrationService} on {@link DatabaseTestSetup#MIGRATION_POOL_NAME} and
	 * completes the start of the application by {@link MigrationService#applicationStarted()}.
	 *
	 * <p>
	 * The test setup disables the automatic migration with the system property
	 * {@link InitialTableSetup#NO_DATABASE_TABLE_SETUP}, the property is cleared for the start.
	 * </p>
	 */
	private static void startMigration() throws ConfigurationException {
		String noTableSetup = System.clearProperty(InitialTableSetup.NO_DATABASE_TABLE_SETUP);
		try {
			startMigrationService();
		} finally {
			if (noTableSetup != null) {
				System.setProperty(InitialTableSetup.NO_DATABASE_TABLE_SETUP, noTableSetup);
			}
		}
	}

	private static void startMigrationService() throws ConfigurationException {
		MigrationService.Config config = (MigrationService.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(MigrationService.class);

		StartableMigrationService service;
		PoolRef defaultPool = TestingConnectionPoolRegistryAccess.setupDefaultPool(DatabaseTestSetup.MIGRATION_POOL_NAME);
		try {
			service = new StartableMigrationService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		} finally {
			TestingConnectionPoolRegistryAccess.restoreDefaultPool(defaultPool);
		}
		service.start();
		service.applicationStarted();
	}

	private String lockProperty() {
		return _properties.getProperty(MigrationLock.LOCK_PROPERTY);
	}

	private Map<String, Version> storedVersions() throws Exception {
		Collection<Version> versions = MigrationUtil.maximalVersions(new BufferingProtocol());
		List<String> modules = versions.stream().map(Version::getModule).toList();
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return MigrationUtil.readStoredVersions(connection, modules);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private SchemaConfiguration storedSchema() {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private String storedSchemaRaw() {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return KBSchemaUtil.loadSchemaRaw(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private static MetaObjectConfig labelConfig(SchemaConfiguration schema) {
		MetaObjectConfig result = (MetaObjectConfig) schema.getMetaObjects().getTypes().get(LABEL_TYPE);
		assertNotNull("No type '" + LABEL_TYPE + "'.", result);
		return result;
	}

	private static KnowledgeBaseConfiguration migrationKBConfig() throws ConfigurationException {
		KnowledgeBaseFactoryConfig factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
			.getServiceConfiguration(KnowledgeBaseFactory.class);
		KnowledgeBaseConfiguration result = TypedConfiguration.copy(
			factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME));
		result.update(result.descriptor().getProperty(KnowledgeBaseConfiguration.CONNECTION_POOL_PROPERTY),
			DatabaseTestSetup.MIGRATION_POOL_NAME);
		return result;
	}

	/**
	 * Condition on the state of the database.
	 */
	private interface Check {

		/**
		 * Whether the condition holds.
		 */
		boolean test() throws Exception;

	}

	/**
	 * {@link MigrationService} that can be started by the test.
	 */
	private static class StartableMigrationService extends MigrationService {

		StartableMigrationService(InstantiationContext context, Config config) {
			super(context, config);
		}

		void start() {
			startUp();
		}

	}

	/**
	 * Suite of {@link TestPendingMigrationChanges}.
	 */
	public static Test suite() {
		return suiteNeedsBranches(TestPendingMigrationChanges.class);
	}

}
