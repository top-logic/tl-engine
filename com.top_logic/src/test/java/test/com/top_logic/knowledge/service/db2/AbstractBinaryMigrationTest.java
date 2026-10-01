/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.db.schema.properties.DBPropertiesTableSetup;
import test.com.top_logic.basic.module.TestModuleUtil;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.FileSystemBlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * Base class for tests of migration processors moving binary content.
 *
 * <p>
 * The knowledge base {@link #kb()} is set up with the source types of the migration, the second
 * node {@link #kbNode2()} with the target types. Data is created through {@link #kb()}, migrated,
 * and read through {@link #kbNode2()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractBinaryMigrationTest extends AbstractDBKnowledgeBaseClusterTest {

	static final String OTHER_STORE = "other";

	private File _root;

	private BlobStoreService _service;

	private BlobStoreService _formerService;

	/**
	 * The types of the knowledge base before the migration.
	 */
	protected abstract TypeProvider sourceTypes();

	/**
	 * The types of the knowledge base after the migration.
	 */
	protected abstract TypeProvider targetTypes();

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new MigrationTestSetup(self, sourceTypes(), targetTypes());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir(getClass().getSimpleName());
		_service = newService();
		_formerService = TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _service);
	}

	@Override
	protected void tearDown() throws Exception {
		TestModuleUtil.installNewInstance(BlobStoreService.Module.INSTANCE, _formerService);
		for (BlobStore store : _service.getStores().values()) {
			store.close();
		}
		FileUtilities.deleteR(_root);
		super.tearDown();
	}

	private BlobStoreService newService() {
		BlobStoreService.Config<?> config = TypedConfiguration.newConfigItem(BlobStoreService.Config.class);
		config.setDefaultStore(BlobStoreService.DEFAULT_STORE_NAME);
		for (String name : List.of(BlobStoreService.DEFAULT_STORE_NAME, OTHER_STORE)) {
			FileSystemBlobStore.Config<?> storeConfig =
				TypedConfiguration.newConfigItem(FileSystemBlobStore.Config.class);
			storeConfig.setName(name);
			storeConfig.setRoot(new File(_root, name).getPath());
			config.getStores().put(name, storeConfig);
		}
		return (BlobStoreService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Runs the migration processor configured by the given XML.
	 *
	 * @param processorConfig
	 *        The configuration of the processor with a <code>class</code> attribute.
	 * @param schema
	 *        The schema stored in the database during the migration.
	 * @return The log of the migration.
	 */
	protected BufferingProtocol migrate(String processorConfig, SchemaConfiguration schema) throws Exception {
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<MigrationProcessor> config =
			TypedConfiguration.parse("processor", PolymorphicConfiguration.class,
				CharacterContents.newContent(processorConfig));
		MigrationProcessor processor = TypedConfigUtil.createInstance(config);

		ConnectionPool pool = kb().getConnectionPool();
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			String storedSchema = KBSchemaUtil.loadSchemaRaw(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
			try {
				MigrationContext context = new MigrationContext(log, connection) {
					@Override
					public MORepository getPersistentRepository() {
						return persistentRepository();
					}

					@Override
					public SchemaConfiguration getPersistentSchema() {
						return schema;
					}

					@Override
					public boolean hasBranchSupport() {
						return kb().getMORepository().multipleBranches();
					}
				};
				processor.doMigration(context, log, connection);
				connection.commit();
				assertFalse("Migration failed: " + log.getErrors(), log.hasErrors());
				migrated(connection);
			} finally {
				KBSchemaUtil.storeSchemaRaw(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, storedSchema);
				connection.commit();
			}
		} finally {
			pool.releaseWriteConnection(connection);
		}
		assertFalse("Migration failed: " + log.getErrors(), log.hasErrors());
		return log;
	}

	/**
	 * The types of the database during the migration.
	 */
	protected MORepository persistentRepository() {
		return kb().getMORepository();
	}

	/**
	 * Hook called after the migration with the connection of the migration.
	 */
	protected void migrated(PooledConnection connection) throws Exception {
		// Hook for subclasses.
	}

	protected static void assertContent(byte[] expected, String contentType, String name, BinaryData actual)
			throws IOException {
		assertNotNull(actual);
		assertEquals(name, actual.getName());
		assertEquals(contentType, actual.getContentType());
		assertEquals(expected.length, actual.getSize());
		try (InputStream in = actual.getStream()) {
			assertEquals(expected, StreamUtilities.readStreamContents(in));
		}
	}

	protected static byte[] randomContent(int size) {
		byte[] result = new byte[size];
		new Random(size).nextBytes(result);
		return result;
	}

	/**
	 * The suite of the given test class with the table of database properties holding the stored
	 * schema.
	 */
	protected static Test binaryMigrationSuite(Class<? extends AbstractBinaryMigrationTest> testClass) {
		return suite(testClass, new TestFactory() {
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				return DBPropertiesTableSetup.setup(suite);
			}
		});
	}

	/**
	 * {@link DBKnowledgeBaseClusterTestSetup} whose second node uses different types for the same
	 * tables.
	 */
	static class MigrationTestSetup extends DBKnowledgeBaseClusterTestSetup {

		private final TypeProvider _sourceTypes;

		private final TypeProvider _targetTypes;

		MigrationTestSetup(Test test, TypeProvider sourceTypes, TypeProvider targetTypes) {
			super(test);
			_sourceTypes = sourceTypes;
			_targetTypes = targetTypes;
			addAdditionalTypes(sourceTypes);
		}

		@Override
		protected DBKnowledgeBase setupSecondKB(KnowledgeBaseConfiguration config) throws Exception {
			List<TypeProvider> providers = new ArrayList<>(getAdditionalProvider());
			providers.remove(_sourceTypes);
			providers.add(_targetTypes);
			SchemaSetup schemaSetup = SetupKBHelper.newSchemaSetup(config, providers);
			DBKnowledgeBase kb = new DBKnowledgeBaseAccess(schemaSetup);
			kb.initialize(new AssertProtocol(), config);
			kb.startup(new AssertProtocol());
			return kb;
		}

	}

}
