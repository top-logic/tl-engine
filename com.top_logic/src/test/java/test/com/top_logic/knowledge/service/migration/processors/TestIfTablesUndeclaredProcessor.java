/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeTypeFactory;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.processors.IfTablesUndeclaredProcessor;

/**
 * Test of {@link IfTablesUndeclaredProcessor}.
 *
 * <p>
 * The stored schema contains the type {@link #LEFTOVER_TYPE} of {@link #LEFTOVER_META} in addition
 * to the schema of the application, as in a database created with a module that has handed this
 * type over to a module the application does not contain.
 * </p>
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}.
 * </p>
 */
public class TestIfTablesUndeclaredProcessor extends AbstractDBKnowledgeBaseTest {

	/** Declaration of {@link #LEFTOVER_TYPE}. */
	private static final String LEFTOVER_META = "webinf://kbase/TestIfTablesUndeclaredMeta.xml";

	/** Type of the stored schema the application schema does not declare. */
	private static final String LEFTOVER_TYPE = "UndeclaredLeftover";

	/** Type of the application schema, with contents. */
	private static final String GROUP_TYPE = "Group";

	/** Mandatory attribute of {@link #GROUP_TYPE}. */
	private static final String GROUP_NAME = "name";

	/** Name that is no type at all. */
	private static final String UNKNOWN_TYPE = "NoSuchType";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	private SchemaConfiguration _applicationSchema;

	private DBSchema _leftoverSchema;

	private MOClass _leftoverType;

	private DBTable _leftoverTable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

		try (Transaction tx = _kb.beginTransaction()) {
			KnowledgeObject group = _kb.createKnowledgeObject(GROUP_TYPE);
			group.setAttributeValue(GROUP_NAME, "if-tables-undeclared-group");
			tx.commit();
		}

		_applicationSchema = resolve(applicationSchemaConfig());

		SchemaConfiguration storedSchema = resolve(withLeftover(applicationSchemaConfig()));
		_leftoverSchema = DBSchemaFactory.createDBSchema();
		_leftoverType = type(storedSchema, LEFTOVER_TYPE);
		_leftoverTable = SchemaSetup.createTable(_leftoverType);
		_leftoverSchema.getTables().add(_leftoverTable);
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			DBSchemaUtils.create(connection, _leftoverTable);
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, storedSchema);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertTrue(leftoverTableExists());
		assertTrue(storedTypes().contains(LEFTOVER_TYPE));
	}

	@Override
	protected void tearDown() throws Exception {
		if (leftoverTableExists()) {
			DBSchemaUtils.dropTables(_pool, _leftoverSchema);
		}
		_setup.tearDown();
		_setup = null;
		_kb = null;
		_pool = null;
		_kbConfig = null;
		_applicationSchema = null;
		_leftoverTable = null;
		_leftoverType = null;
		_leftoverSchema = null;
		super.tearDown();
	}

	/**
	 * The application declares none of the tables: the processors run, the nested
	 * {@link DeleteTableProcessor} deletes the type together with its table, so that the automatic
	 * schema migration has nothing to recover.
	 */
	public void testUndeclaredRunsProcessors() throws Exception {
		BufferingProtocol log = migrate(_applicationSchema, List.of(LEFTOVER_TYPE), LEFTOVER_TYPE);

		assertFalse("Table not dropped.", leftoverTableExists());
		Set<String> stored = storedTypes();
		assertFalse("Type not removed from the stored schema.", stored.contains(LEFTOVER_TYPE));
		stored.removeAll(_applicationSchema.getMetaObjects().getTypes().keySet());
		assertEquals("Types the automatic schema migration would recover.", Set.of(), stored);
		assertContains(log.getInfos(), "Deleted table ");
	}

	/**
	 * The application declares the table: no processor runs, table and contents are kept.
	 */
	public void testDeclaredSkipsProcessors() throws Exception {
		SchemaConfiguration withLeftover = resolve(withLeftover(applicationSchemaConfig()));
		String storedBefore = storedSchemaRaw();

		BufferingProtocol log = migrate(withLeftover, List.of(LEFTOVER_TYPE), LEFTOVER_TYPE);

		assertTrue("Declared table dropped.", leftoverTableExists());
		assertEquals("Stored schema changed.", storedBefore, storedSchemaRaw());
		assertContains(log.getInfos(), "Table '" + LEFTOVER_TYPE + "' is declared by the application");
	}

	/**
	 * A name the stored schema does not know counts as undeclared: the processors run without
	 * error.
	 */
	public void testUnknownNameCountsAsUndeclared() throws Exception {
		migrate(_applicationSchema, List.of(UNKNOWN_TYPE), LEFTOVER_TYPE);

		assertFalse("Table not dropped.", leftoverTableExists());
		assertFalse(storedTypes().contains(LEFTOVER_TYPE));
		assertFalse(storedTypes().contains(UNKNOWN_TYPE));
	}

	/**
	 * Several tables, one of them declared by the application: no processor runs.
	 */
	public void testOneDeclaredSkipsProcessors() throws Exception {
		String storedBefore = storedSchemaRaw();

		BufferingProtocol log =
			migrate(_applicationSchema, List.of(LEFTOVER_TYPE, GROUP_TYPE), LEFTOVER_TYPE, GROUP_TYPE);

		assertTrue("Table dropped.", leftoverTableExists());
		assertEquals("Stored schema changed.", storedBefore, storedSchemaRaw());
		assertEquals(1, rowCount(dbTableName(GROUP_TYPE)));
		assertContains(log.getInfos(), "Table '" + GROUP_TYPE + "' is declared by the application");
	}

	/**
	 * Executes an {@link IfTablesUndeclaredProcessor} with the condition tables
	 * <code>conditionTables</code> and a nested {@link DeleteTableProcessor} deleting
	 * <code>deleteTables</code>.
	 * 
	 * <p>
	 * The {@link MigrationContext#revalidateXRef(com.top_logic.basic.Log, PooledConnection) revision
	 * cross reference} of {@link #LEFTOVER_TYPE} is invalidated before, and rebuilt after the
	 * processor, so that a dropped table must not be part of the rebuild.
	 * </p>
	 */
	private BufferingProtocol migrate(SchemaConfiguration applicationSchema, List<String> conditionTables,
			String... deleteTables) throws SQLException {
		IfTablesUndeclaredProcessor.Config config =
			TypedConfiguration.newConfigItem(IfTablesUndeclaredProcessor.Config.class);
		for (String table : conditionTables) {
			config.getTables().add(tableRef(table));
		}
		DeleteTableProcessor.Config deleteTable = TypedConfiguration.newConfigItem(DeleteTableProcessor.Config.class);
		for (String table : deleteTables) {
			deleteTable.getTables().add(tableRef(table));
		}
		config.getProcessors().add(deleteTable);
		IfTablesUndeclaredProcessor processor = TypedConfigUtil.createInstance(config);

		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public SchemaConfiguration getApplicationSchema() {
					return applicationSchema;
				}
			};
			// As a processor deleting the objects of the table does before the table is dropped.
			context.invalidateXRef(_leftoverType);
			processor.doMigration(context, log, connection);
			context.revalidateXRef(log, connection);
			if (log.hasErrors()) {
				connection.rollback();
			} else {
				connection.commit();
			}
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		assertEquals("Errors: " + log.getErrors(), 0, log.getErrors().size());
		return log;
	}

	private static DeleteTableProcessor.Config.TableRef tableRef(String table) {
		DeleteTableProcessor.Config.TableRef ref =
			TypedConfiguration.newConfigItem(DeleteTableProcessor.Config.TableRef.class);
		ref.update(ref.descriptor().getProperty(MetaObjectName.OBJECT_NAME_ATTRIBUTE), table);
		return ref;
	}

	private SchemaConfiguration applicationSchemaConfig() {
		return TypedConfiguration.copy(KBUtils.getSchemaConfig(_kbConfig).getConfig());
	}

	private static SchemaConfiguration withLeftover(SchemaConfiguration schema) {
		ResourceDeclaration declaration = TypedConfiguration.newConfigItem(ResourceDeclaration.class);
		declaration.setResource(LEFTOVER_META);
		schema.getDeclarations().add(declaration);
		return schema;
	}

	/**
	 * The resolved schema with the layout options of the test database.
	 */
	private SchemaConfiguration resolve(SchemaConfiguration schema) throws ConfigurationException, SQLException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		SchemaConfiguration resolved = context.getInstance(schema).resolve(context).getConfig();
		return SchemaSetup.withExplicitLayout(resolved, _pool.getSQLDialect());
	}

	private MOClass type(SchemaConfiguration schema, String typeName) throws Exception {
		SchemaSetup setup = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(schema);
		MORepository types = setup.createMORepository(_pool.getSQLDialect(), new DBKnowledgeTypeFactory());
		return (MOClass) types.getTypeOrNull(typeName);
	}

	private boolean leftoverTableExists() throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return DBSchemaUtils.exists(connection, _leftoverTable);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private int rowCount(String dbTableName) throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try (Statement stmt = connection.createStatement();
				ResultSet result = stmt.executeQuery(
					"SELECT COUNT(*) FROM " + connection.getSQLDialect().tableRef(dbTableName))) {
			assertTrue(result.next());
			return result.getInt(1);
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	private String dbTableName(String typeName) {
		return ((MOClass) _kb.getMORepository().getTypeOrNull(typeName)).getDBMapping().getDBName();
	}

	private Set<String> storedTypes() {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			return new TreeSet<>(KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME)
				.getMetaObjects().getTypes().keySet());
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

	private static void assertContains(List<String> infos, String part) {
		assertTrue("Missing info '" + part + "': " + infos, infos.stream().anyMatch(info -> info.contains(part)));
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
	 * Suite of {@link TestIfTablesUndeclaredProcessor}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestIfTablesUndeclaredProcessor.class);
	}

}
