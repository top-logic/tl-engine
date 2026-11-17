/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.DropTablesProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;

/**
 * Test of {@link DropTablesProcessor}.
 */
@SuppressWarnings("javadoc")
public class TestDropTablesProcessor extends BasicTestCase {

	private static final String DATA_TABLE = "TEST_DROP_DATA";

	private static final String TARGET_TABLE = "TEST_DROP_TARGET";

	private static final String SOURCE_TABLE = "TEST_DROP_SOURCE";

	private static final String MISSING_TABLE = "TEST_DROP_MISSING";

	private static final String COL_ID = "ID";

	private static final String COL_REF = "REF";

	private ConnectionPool _pool;

	private PooledConnection _connection;

	private DBSchema _schema;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_connection = _pool.borrowWriteConnection();

		_schema = createDBSchema();
		_schema.getTables().add(idTable(DATA_TABLE));
		_schema.getTables().add(idTable(TARGET_TABLE));
		DBTable source = idTable(SOURCE_TABLE);
		source.getColumns().add(column(COL_REF));
		DBForeignKey foreignKey = createForeignKey("FK_DROP_REF");
		foreignKey.getSourceColumnRefs().add(ref(COL_REF));
		foreignKey.setTargetTableRef(tableRef(TARGET_TABLE));
		foreignKey.getTargetColumnRefs().add(ref(COL_ID));
		source.getForeignKeys().add(foreignKey);
		_schema.getTables().add(source);

		DBSchemaUtils.create(_connection, _schema);
		_connection.commit();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_connection.rollback();
			DBSchemaUtils.resetTables(_connection, _schema, false);
			_connection.commit();
		} finally {
			_pool.releaseWriteConnection(_connection);
			super.tearDown();
		}
	}

	/**
	 * The processor does not commit the changes made by the migration before.
	 * 
	 * <p>
	 * Only a missing table is dropped, since <i>H2</i> commits the transaction with each DDL
	 * statement.
	 * </p>
	 */
	public void testNoCommit() throws SQLException {
		insertId(DATA_TABLE, 1);

		BufferingProtocol log = dropTables(MISSING_TABLE);
		assertEquals(Arrays.asList(), log.getErrors());

		_connection.rollback();
		assertEquals("Migration changes must not be committed by the processor.", 0, rowCount(DATA_TABLE));
	}

	/**
	 * Dropping a table that does not exist is not an error.
	 */
	public void testMissingTable() {
		BufferingProtocol log = dropTables(MISSING_TABLE);
		assertEquals(Arrays.asList(), log.getErrors());
		assertFalse(log.getInfos().stream().anyMatch(info -> info.startsWith("Dropped tables")));
	}

	/**
	 * A failing drop of an existing table is reported as error.
	 */
	public void testFailingDrop() throws SQLException {
		// The target table is referenced by a foreign key and cannot be dropped.
		BufferingProtocol log = dropTables(TARGET_TABLE);
		assertFalse("A failed drop must be reported.", log.getErrors().isEmpty());
		assertFalse(log.getInfos().stream().anyMatch(info -> info.startsWith("Dropped tables")));
		assertTrue(DBSchemaUtils.exists(_connection, TARGET_TABLE));
	}

	/**
	 * A successful drop is reported.
	 */
	public void testDrop() throws SQLException {
		BufferingProtocol log = dropTables(SOURCE_TABLE);
		assertEquals(Arrays.asList(), log.getErrors());
		assertTrue(log.getInfos().stream().anyMatch(info -> info.startsWith("Dropped tables")));
		assertFalse(DBSchemaUtils.exists(_connection, SOURCE_TABLE));
	}

	private BufferingProtocol dropTables(String... tables) {
		DropTablesProcessor.Config config = TypedConfiguration.newConfigItem(DropTablesProcessor.Config.class);
		config.setTables(Arrays.asList(tables));
		DropTablesProcessor processor = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		BufferingProtocol log = new BufferingProtocol();
		MigrationContext context = new MigrationContext(new AssertProtocol(), _connection);
		processor.doMigration(context, log, _connection);
		return log;
	}

	private void insertId(String tableName, int id) throws SQLException {
		DBHelper sqlDialect = _connection.getSQLDialect();
		try (Statement stmt = _connection.createStatement()) {
			stmt.executeUpdate("INSERT INTO " + sqlDialect.tableRef(tableName) + " ("
				+ sqlDialect.columnRef(COL_ID) + ") VALUES (" + id + ")");
		}
	}

	private int rowCount(String tableName) throws SQLException {
		try (Statement stmt = _connection.createStatement()) {
			try (ResultSet result =
				stmt.executeQuery("SELECT COUNT(*) FROM " + _connection.getSQLDialect().tableRef(tableName))) {
				assertTrue(result.next());
				return result.getInt(1);
			}
		}
	}

	private static DBTable idTable(String name) {
		DBTable table = createTable(name);
		table.getColumns().add(column(COL_ID));
		DBPrimary primary = createPrimary();
		primary.getColumnRefs().add(ref(COL_ID));
		table.setPrimaryKey(primary);
		return table;
	}

	private static DBColumn column(String name) {
		DBColumn result = createColumn(name);
		result.setType(DBType.INT);
		result.setMandatory(true);
		return result;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestDropTablesProcessor.class));
	}

}
