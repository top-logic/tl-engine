/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import java.sql.SQLException;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.db2.migration.AlterColumnProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;

/**
 * Test of {@link AlterColumnProcessor}.
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}.
 * </p>
 */
public class TestAlterColumnProcessor extends AbstractDBKnowledgeBaseTest {

	/** Abstract table declaring {@link #COLUMN}. */
	private static final String ABSTRACT_TABLE = "AbstractUnit";

	/** Concrete sub table of {@link #ABSTRACT_TABLE} inheriting {@link #COLUMN}. */
	private static final String CONCRETE_TABLE = "Unit";

	/** Further concrete sub table of {@link #ABSTRACT_TABLE} inheriting {@link #COLUMN}. */
	private static final String OTHER_CONCRETE_TABLE = "Currency";

	/** String column declared in {@link #ABSTRACT_TABLE}. */
	private static final String COLUMN = "format";

	private static final int NEW_SIZE = 1234;

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

		storeSchema(configuredSchema());
	}

	@Override
	protected void tearDown() throws Exception {
		_setup.tearDown();
		_setup = null;
		_kb = null;
		_pool = null;
		_kbConfig = null;
		super.tearDown();
	}

	/**
	 * Alters a column declared in an abstract table that is only inherited by the concrete sub
	 * tables.
	 */
	public void testAlterColumnOfAbstractTable() throws Exception {
		assertTrue(((MOClass) _kb.getMORepository().getTypeOrNull(ABSTRACT_TABLE)).isAbstract());
		assertFalse(((MOClass) _kb.getMORepository().getTypeOrNull(CONCRETE_TABLE)).hasDeclaredAttribute(COLUMN));
		assertFalse(Integer.valueOf(NEW_SIZE).equals(storedColumn().getDBSize()));

		AlterColumnProcessor.Config<?> config =
			TypedConfiguration.newConfigItem(AlterColumnProcessor.Config.class);
		set(config, "table", ABSTRACT_TABLE);
		set(config, "column", COLUMN);
		config.setDBSize(NEW_SIZE);

		BufferingProtocol log = migrate(TypedConfigUtil.createInstance(config));
		assertTrue("Stored schema not updated: " + log.getInfos(), log.getInfos().stream().anyMatch(
			info -> info.contains("Updating persistent schema of column '" + COLUMN + "' in table '"
				+ ABSTRACT_TABLE + "'.")));

		assertEquals(Integer.valueOf(NEW_SIZE), storedColumn().getDBSize());
		assertEquals(NEW_SIZE, dbColumn(CONCRETE_TABLE).getSize());
		assertEquals(NEW_SIZE, dbColumn(OTHER_CONCRETE_TABLE).getSize());
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	private BufferingProtocol migrate(AlterColumnProcessor processor) throws SQLException {
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection);
			processor.doMigration(context, log, connection);
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

	/**
	 * The definition of {@link #COLUMN} in {@link #ABSTRACT_TABLE} in the stored schema.
	 */
	private DBColumnType storedColumn() {
		SchemaConfiguration stored;
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			stored = KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		} finally {
			_pool.releaseReadConnection(connection);
		}
		MetaObjectConfig table = (MetaObjectConfig) stored.getMetaObjects().getTypes().get(ABSTRACT_TABLE);
		assertNotNull("No table '" + ABSTRACT_TABLE + "' in stored schema.", table);
		for (AttributeConfig attribute : table.getAttributes()) {
			if (attribute.getAttributeName().equals(COLUMN)) {
				return (DBColumnType) attribute;
			}
		}
		fail("No column '" + COLUMN + "' in table '" + ABSTRACT_TABLE + "' of the stored schema.");
		return null;
	}

	private DBColumn dbColumn(String typeName) throws SQLException {
		MOClass type = (MOClass) _kb.getMORepository().getTypeOrNull(typeName);
		String tableName = type.getDBMapping().getDBName();
		DBTable table = DBSchemaUtils.extractTable(_pool, DBSchemaFactory.createDBSchema(), tableName);
		assertNotNull("No table '" + tableName + "'.", table);
		MOAttribute attribute = type.getAttribute(COLUMN);
		String columnName = attribute.getDbMapping()[0].getDBName();
		for (DBColumn column : table.getColumns()) {
			if (column.getDBName().equalsIgnoreCase(columnName)) {
				return column;
			}
		}
		fail("No column '" + columnName + "' in table '" + tableName + "'.");
		return null;
	}

	private void storeSchema(SchemaConfiguration schema) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, schema);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * The resolved schema of the configuration.
	 */
	private SchemaConfiguration configuredSchema() throws ConfigurationException, SQLException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		SchemaConfiguration resolved =
			context.getInstance(KBUtils.getSchemaConfig(_kbConfig).getConfig()).resolve(context).getConfig();
		return SchemaSetup.withExplicitLayout(resolved, _pool.getSQLDialect());
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
	 * Suite of {@link TestAlterColumnProcessor}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestAlterColumnProcessor.class);
	}

}
