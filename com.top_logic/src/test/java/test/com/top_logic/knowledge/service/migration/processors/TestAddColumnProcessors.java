/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.SetupKBHelper;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.AddDatabaseColumnProcessor;
import com.top_logic.knowledge.service.migration.processors.AddMOReferenceProcessor;
import com.top_logic.knowledge.service.migration.processors.AddPrimitiveMOAttributeProcessor;

/**
 * Test of the migration processors adding columns to a table with content:
 * {@link AddPrimitiveMOAttributeProcessor}, {@link AddMOReferenceProcessor}, and
 * {@link AddDatabaseColumnProcessor}.
 *
 * <p>
 * Mandatory columns must be filled in existing rows and must not keep a database default.
 * </p>
 *
 * <p>
 * The test works on a separate knowledge base in the database
 * {@link DatabaseTestSetup#MIGRATION_POOL_NAME}.
 * </p>
 */
public class TestAddColumnProcessors extends AbstractDBKnowledgeBaseTest {

	/** Table with content (see <code>KBDataUnit.xml</code>). */
	private static final String TABLE = "Unit";

	private KnowledgeBaseConfiguration _kbConfig;

	private ConnectionPool _pool;

	private SetupKBHelper _setup;

	private DBKnowledgeBase _kb;

	private Map<String, Boolean> _initialColumns;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_kbConfig = migrationKBConfig();
		_pool = ConnectionPoolRegistry.getConnectionPool(_kbConfig.getConnectionPool());
		_setup = new SetupKBHelper(_kbConfig);
		_kb = _setup.setup();

		storeSchema(configuredSchema());
		_initialColumns = columns();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			dropAddedColumns();
		} finally {
			// Shut down the knowledge base even if a column could not be dropped, so that a failure
			// does not break the setup of the following tests.
			try {
				_setup.tearDown();
			} finally {
				_setup = null;
				_kb = null;
				_pool = null;
				_kbConfig = null;
				_initialColumns = null;
				super.tearDown();
			}
		}
	}

	/**
	 * Adds a mandatory primitive attribute to a table with content.
	 */
	public void testAddMandatoryPrimitiveAttribute() throws Exception {
		assertTrue("Test requires a table with content.", rowCount() > 0);
		Map<String, Boolean> before = columns();

		migrate(processor("add-mo-attribute", AddPrimitiveMOAttributeProcessor.Config.class,
			"<add-mo-attribute table='" + TABLE + "'>"
				+ "<mo_attribute att_name='testLong' att_type='Long' mandatory='true'/>"
				+ "</add-mo-attribute>"));

		assertAddedColumns(before, 1, true);
	}

	/**
	 * Adds a mandatory polymorphic reference to a table with content.
	 */
	public void testAddMandatoryReference() throws Exception {
		assertTrue("Test requires a table with content.", rowCount() > 0);
		Map<String, Boolean> before = columns();

		migrate(processor("add-mo-reference", AddMOReferenceProcessor.Config.class,
			"<add-mo-reference table='" + TABLE + "'>"
				+ "<reference att_name='testRef' target-type='" + TABLE + "' mandatory='true'"
				+ " monomorphic='false' branch-global='false' use-default-index='false'/>"
				+ "</add-mo-reference>"));

		// At least type and identifier column.
		Map<String, Boolean> added = assertAddedColumns(before, -1, true);
		assertTrue("Columns added: " + added, added.size() >= 2);
	}

	/**
	 * Adds a mandatory database column with a fill value to a table with content.
	 */
	public void testAddMandatoryDatabaseColumn() throws Exception {
		assertTrue("Test requires a table with content.", rowCount() > 0);
		Map<String, Boolean> before = columns();

		AddDatabaseColumnProcessor.Config config =
			TypedConfiguration.newConfigItem(AddDatabaseColumnProcessor.Config.class);
		config.setTable(dbTableName());
		config.setColumn("TEST_FILLED");
		config.setDBType(DBType.STRING);
		config.setSize(Long.valueOf(50));
		config.setMandatory(Boolean.TRUE);
		config.setDefaultValue(TypedConfiguration.createConfigItemForImplementationClass(FillValue.class));
		migrate(TypedConfigUtil.createInstance(config));

		assertAddedColumns(before, 1, true);
		try (PooledConnectionHandle handle = new PooledConnectionHandle();
				Statement stmt = handle._connection.createStatement();
				ResultSet result = stmt.executeQuery("SELECT COUNT(*) FROM " + tableRef() + " WHERE "
					+ dialect().columnRef(columnName(before, "TEST_FILLED")) + " <> '" + FillValue.VALUE + "'")) {
			assertTrue(result.next());
			assertEquals("Rows not filled.", 0, result.getInt(1));
		}
	}

	@SuppressWarnings("unchecked")
	private static MigrationProcessor processor(String tag, Class<?> configType, String xml)
			throws ConfigurationException {
		return TypedConfigUtil.createInstance(
			(PolymorphicConfiguration<? extends MigrationProcessor>) TypedConfiguration.parse(tag, configType,
				CharacterContents.newContent(xml)));
	}

	/**
	 * Fill value for {@link AddDatabaseColumnProcessor.Config#getDefaultValue()}.
	 */
	public static class FillValue implements Supplier<String> {

		/** The value supplied. */
		public static final String VALUE = "filled";

		@Override
		public String get() {
			return VALUE;
		}
	}

	private String columnName(Map<String, Boolean> before, String name) throws SQLException {
		for (String column : columns().keySet()) {
			if (!before.containsKey(column) && column.equalsIgnoreCase(name)) {
				return column;
			}
		}
		fail("No column '" + name + "' added.");
		return null;
	}

	/**
	 * Asserts that the columns added since the given state are filled in all rows, have the given
	 * mandatory state, and have no database default.
	 *
	 * @param expectedCount
	 *        The expected number of added columns, or <code>-1</code> to not check it.
	 * @return The added columns, mapped to whether they are nullable.
	 */
	private Map<String, Boolean> assertAddedColumns(Map<String, Boolean> before, int expectedCount,
			boolean mandatory) throws SQLException {
		Map<String, Boolean> added = new LinkedHashMap<>(columns());
		added.keySet().removeAll(before.keySet());
		if (expectedCount >= 0) {
			assertEquals("Columns added: " + added, expectedCount, added.size());
		}
		for (Map.Entry<String, Boolean> entry : added.entrySet()) {
			String column = entry.getKey();
			assertEquals("Nullable state of column '" + column + "'.", !mandatory, entry.getValue().booleanValue());
			assertNoDefault(column);
			assertEquals("Rows with null in column '" + column + "'.", 0, nullCount(column));
		}
		return added;
	}

	/**
	 * Drops the columns added by a test, since the database is shared with other tests.
	 */
	private void dropAddedColumns() throws SQLException {
		if (_initialColumns == null) {
			// Set up failed before the columns were recorded.
			return;
		}
		SQLException failure = null;
		PooledConnection connection = _pool.borrowWriteConnection();
		try (Statement stmt = connection.createStatement()) {
			for (String column : columns().keySet()) {
				if (_initialColumns.containsKey(column)) {
					continue;
				}
				String sql = "ALTER TABLE " + tableRef() + " DROP COLUMN " + dialect().columnRef(column);
				try {
					stmt.execute(sql);
					connection.commit();
				} catch (SQLException ex) {
					connection.rollback();
					SQLException columnFailure = new SQLException("Failed to drop column: " + sql, ex);
					if (failure == null) {
						failure = columnFailure;
					} else {
						failure.addSuppressed(columnFailure);
					}
				}
			}
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		if (failure != null) {
			throw failure;
		}
	}

	private int rowCount() throws SQLException {
		try (PooledConnectionHandle handle = new PooledConnectionHandle();
				Statement stmt = handle._connection.createStatement();
				ResultSet result = stmt.executeQuery("SELECT COUNT(*) FROM " + tableRef())) {
			assertTrue(result.next());
			return result.getInt(1);
		}
	}

	private int nullCount(String column) throws SQLException {
		try (PooledConnectionHandle handle = new PooledConnectionHandle();
				Statement stmt = handle._connection.createStatement();
				ResultSet result = stmt.executeQuery("SELECT COUNT(*) FROM " + tableRef() + " WHERE "
					+ dialect().columnRef(column) + " IS NULL")) {
			assertTrue(result.next());
			return result.getInt(1);
		}
	}

	/**
	 * The columns of the table as reported by the database, mapped to whether they are nullable.
	 */
	private Map<String, Boolean> columns() throws SQLException {
		Map<String, Boolean> result = new LinkedHashMap<>();
		try (PooledConnectionHandle handle = new PooledConnectionHandle();
				ResultSet columns = metaData(handle).getColumns(null, null, dbTableName(), null)) {
			while (columns.next()) {
				result.put(columns.getString("COLUMN_NAME"),
					Boolean.valueOf(columns.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls));
			}
		}
		assertFalse("No columns found for table '" + dbTableName() + "'.", result.isEmpty());
		return result;
	}

	/**
	 * Asserts that the given column has no database default.
	 *
	 * <p>
	 * A default of <code>NULL</code> (as reported by H2 for a column created with its null
	 * specification <code>DEFAULT NULL</code>) is no default value, since an omitted value is
	 * <code>null</code> anyway.
	 * </p>
	 */
	private void assertNoDefault(String column) throws SQLException {
		String columnDefault = columnDefault(column);
		if (columnDefault != null && !"NULL".equalsIgnoreCase(columnDefault.trim())) {
			fail("Database default '" + columnDefault + "' of column '" + column + "'.");
		}
	}

	private String columnDefault(String column) throws SQLException {
		try (PooledConnectionHandle handle = new PooledConnectionHandle();
				ResultSet columns = metaData(handle).getColumns(null, null, dbTableName(), column)) {
			assertTrue("No column '" + column + "'.", columns.next());
			return columns.getString("COLUMN_DEF");
		}
	}

	private DatabaseMetaData metaData(PooledConnectionHandle handle) throws SQLException {
		return handle._connection.getMetaData();
	}

	private String dbTableName() {
		return ((MOClass) _kb.getMORepository().getTypeOrNull(TABLE)).getDBMapping().getDBName();
	}

	private String tableRef() throws SQLException {
		return dialect().tableRef(dbTableName());
	}

	private DBHelper dialect() throws SQLException {
		return _pool.getSQLDialect();
	}

	private void migrate(MigrationProcessor processor) throws SQLException {
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
	 * Read connection of {@link TestAddColumnProcessors#_pool} released on close.
	 */
	private final class PooledConnectionHandle implements AutoCloseable {

		final PooledConnection _connection = _pool.borrowReadConnection();

		@Override
		public void close() {
			_pool.releaseReadConnection(_connection);
		}
	}

	/**
	 * Suite of {@link TestAddColumnProcessors}.
	 *
	 * <p>
	 * Like the other tests of migration processors, the test runs on the default database only:
	 * Setting up the knowledge base anew for each test does not work on all databases (e.g. MS SQL
	 * Server).
	 * </p>
	 */
	public static Test suite() {
		return suiteDefaultDB(TestAddColumnProcessors.class);
	}

}
