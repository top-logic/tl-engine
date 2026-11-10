/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.util;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.postgresql.PGConnection;
import org.postgresql.jdbc.AutoSave;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case for {@link DBSchemaUtils}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDBSchemaUtils extends BasicTestCase {

	public void testExistence() throws SQLException {
		DBSchema schema = createDBSchema();
		DBTable table = createTable("TestExistence");
		DBColumn foo = createColumn("foo");
		foo.setSize(128L);
		foo.setType(DBType.STRING);
		table.getColumns().add(foo);

		DBColumn bar = createColumn("bar");
		bar.setType(DBType.INT);
		table.getColumns().add(bar);

		schema.getTables().add(table);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();

		PooledConnection read = pool.borrowReadConnection();
		try {
			assertFalse(DBSchemaUtils.exists(read, table));
		} finally {
			pool.releaseReadConnection(read);
		}

		PooledConnection write = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.create(write, table);
		} finally {
			pool.releaseWriteConnection(write);
		}

		PooledConnection read2 = pool.borrowReadConnection();
		try {
			assertTrue(DBSchemaUtils.exists(read2, table));
		} finally {
			pool.releaseReadConnection(read2);
		}

		PooledConnection write2 = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.resetTables(write2, schema, true);
		} finally {
			pool.releaseWriteConnection(write2);
		}

		PooledConnection read3 = pool.borrowReadConnection();
		try {
			assertTrue(DBSchemaUtils.exists(read3, table));
		} finally {
			pool.releaseReadConnection(read3);
		}

		PooledConnection write3 = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.resetTables(write3, schema, false);
		} finally {
			pool.releaseWriteConnection(write3);
		}

		PooledConnection read4 = pool.borrowReadConnection();
		try {
			assertFalse(DBSchemaUtils.exists(read4, table));
		} finally {
			pool.releaseReadConnection(read4);
		}
	}

	private static final String TARGET_TABLE = "TEST_TOLERATED_TARGET";

	private static final String SOURCE_TABLE = "TEST_TOLERATED_SOURCE";

	private static final String MISSING_TABLE = "TEST_TOLERATED_MISSING";

	private static final String COL_ID = "ID";

	private static final String COL_REF = "REF";

	/**
	 * Test that checking for and dropping missing tables and foreign keys, and a failing statement
	 * protected by {@link DBHelper#setSavepoint(java.sql.Connection)}, do not abort the
	 * surrounding transaction.
	 * 
	 * <p>
	 * In <i>PostgreSQL</i> without the driver option <code>autosave=always</code>, a failing
	 * statement aborts the whole transaction and the commit acts as rollback. On
	 * <i>PostgreSQL</i>, the test therefore runs with each {@link AutoSave} mode, set on the
	 * connection.
	 * </p>
	 */
	public void testToleratedFailuresKeepTransaction() throws SQLException {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			if (connection.isWrapperFor(PGConnection.class)) {
				PGConnection pgConnection = connection.unwrap(PGConnection.class);
				AutoSave before = pgConnection.getAutosave();
				try {
					for (AutoSave mode : AutoSave.values()) {
						pgConnection.setAutosave(mode);
						checkToleratedFailuresKeepTransaction(connection, "autosave=" + mode + ": ");
					}
				} finally {
					pgConnection.setAutosave(before);
				}
			} else {
				checkToleratedFailuresKeepTransaction(connection, "");
			}
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private void checkToleratedFailuresKeepTransaction(PooledConnection connection, String context)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		DBSchema existing = createDBSchema();
		DBTable target = idTable(TARGET_TABLE);
		existing.getTables().add(target);
		DBTable source = idTable(SOURCE_TABLE);
		source.getColumns().add(column(COL_REF, DBType.INT));
		DBForeignKey foreignKey = createForeignKey("FK_REF");
		foreignKey.getSourceColumnRefs().add(ref(COL_REF));
		foreignKey.setTargetTableRef(tableRef(TARGET_TABLE));
		foreignKey.getTargetColumnRefs().add(ref(COL_ID));
		source.getForeignKeys().add(foreignKey);
		existing.getTables().add(source);

		DBSchema missing = createDBSchema();
		DBTable missingTable = idTable(MISSING_TABLE);
		missingTable.getColumns().add(column(COL_REF, DBType.INT));
		DBForeignKey missingKey = createForeignKey("FK_MISSING");
		missingKey.getSourceColumnRefs().add(ref(COL_REF));
		missingKey.setTargetTableRef(tableRef(TARGET_TABLE));
		missingKey.getTargetColumnRefs().add(ref(COL_ID));
		missingTable.getForeignKeys().add(missingKey);
		missing.getTables().add(missingTable);

		DBSchemaUtils.create(connection, existing);
		connection.commit();
		try {
			insertId(connection, 1);
			assertTrue(context, DBSchemaUtils.exists(connection, TARGET_TABLE));
			assertFalse(context, DBSchemaUtils.exists(connection, MISSING_TABLE));
			assertFalse(context + "Missing table must be reported.",
				DBSchemaUtils.tryResetTable(connection, MISSING_TABLE, false));
			insertId(connection, 2);
			assertFalse(context + "Missing table must be reported.",
				DBSchemaUtils.resetTables(connection, missing, false));
			insertId(connection, 3);
			failProtectedBySavepoint(connection, context);
			insertId(connection, 4);
			connection.commit();

			assertEquals(context + "Rows of the transaction lost.", 4, idCount(connection));
			assertTrue(context, DBSchemaUtils.existsForeignKey(connection, sqlDialect, SOURCE_TABLE,
				sqlDialect.qualifiedName(SOURCE_TABLE, foreignKey.getDBName())));
			assertFalse(context, DBSchemaUtils.existsForeignKey(connection, sqlDialect, SOURCE_TABLE, "FK_NONE"));
		} finally {
			// Do not keep a pending transaction, if the test failed.
			connection.rollback();

			assertTrue(context + "Existing tables and foreign keys must be dropped.",
				DBSchemaUtils.resetTables(connection, existing, false));
			assertFalse(context, DBSchemaUtils.exists(connection, TARGET_TABLE));
			assertFalse(context, DBSchemaUtils.exists(connection, SOURCE_TABLE));
		}
	}

	/**
	 * Executes a failing statement protected by a savepoint, as code tolerating a failure does.
	 */
	private static void failProtectedBySavepoint(PooledConnection connection, String context) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		Savepoint savePoint = sqlDialect.setSavepoint(connection);
		try (Statement stmt = connection.createStatement()) {
			stmt.executeQuery("SELECT * FROM " + sqlDialect.tableRef(MISSING_TABLE)).close();
			fail(context + "Table must not exist: " + MISSING_TABLE);
		} catch (SQLException ex) {
			sqlDialect.rollback(connection, savePoint);
		} finally {
			sqlDialect.releaseSavepoint(connection, savePoint);
		}
	}

	private static DBTable idTable(String name) {
		DBTable table = createTable(name);
		table.getColumns().add(column(COL_ID, DBType.INT));
		DBPrimary primary = createPrimary();
		primary.getColumnRefs().add(ref(COL_ID));
		table.setPrimaryKey(primary);
		return table;
	}

	private static void insertId(PooledConnection connection, int id) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate("INSERT INTO " + sqlDialect.tableRef(TARGET_TABLE) + " ("
				+ sqlDialect.columnRef(COL_ID) + ") VALUES (" + id + ")");
		}
	}

	private static int idCount(PooledConnection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet result =
				stmt.executeQuery("SELECT COUNT(*) FROM " + connection.getSQLDialect().tableRef(TARGET_TABLE))) {
				assertTrue(result.next());
				return result.getInt(1);
			}
		}
	}

	private static final String KEYS_TABLE = "TEST_ALIGN_KEYS";

	private static final String COL_A = "COL_A";

	private static final String COL_B = "COL_B";

	private static final String COL_C = "COL_C";

	/** Index whose columns are reordered. */
	private static final String IDX_CHANGED = "IDX_CHANGED";

	/** Index with the same definition before and after. */
	private static final String IDX_SAME = "IDX_SAME";

	/** Index only present in the current definition. */
	private static final String IDX_CURRENT = "IDX_CURRENT";

	/** Index only present in the target definition. */
	private static final String IDX_TARGET = "IDX_TARGET";

	private static final int ROWS = 5;

	/** Name of an index that does not follow the naming of the table definition. */
	private static final String OTHER_INDEX_NAME = "TEST_ALIGN_OTHER_NAME";

	/**
	 * Test of {@link DBSchemaUtils#alignKeys(com.top_logic.basic.Log, PooledConnection, DBTable, DBTable)}.
	 */
	public void testAlignKeys() throws SQLException {
		DBTable current = keysTable(false);
		DBTable target = keysTable(true);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		createKeysTable(pool, current);
		try {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				assertTrue(DBSchemaUtils.alignKeys(new AssertProtocol(), connection, current, target));
				connection.commit();

				assertFalse("Already aligned.",
					DBSchemaUtils.alignKeys(new AssertProtocol(), connection, target, target));
			} finally {
				pool.releaseWriteConnection(connection);
			}

			DBTable extracted = DBSchemaUtils.extractTable(pool, DBSchemaFactory.createDBSchema(), KEYS_TABLE);
			assertEquals(Arrays.asList(COL_B, COL_A), columnNames(extracted.getPrimaryKey()));
			assertEquals(Arrays.asList(COL_C, COL_A), columnNames(index(pool, extracted, IDX_CHANGED)));
			assertEquals(Arrays.asList(COL_C), columnNames(index(pool, extracted, IDX_SAME)));
			assertEquals("Index only in the current definition is kept.", Arrays.asList(COL_C, COL_B),
				columnNames(index(pool, extracted, IDX_CURRENT)));
			assertNull("Index only in the target definition is not created.",
				findIndex(pool, extracted, IDX_TARGET));
			assertEquals(ROWS, rowCount(pool));
		} finally {
			dropKeysTable(pool);
		}
	}

	/**
	 * Test of {@link DBSchemaUtils#alignKeys(com.top_logic.basic.Log, PooledConnection, DBTable, DBTable)}
	 * for a database containing an index of the current definition under another name.
	 */
	public void testAlignKeysIndexUnderOtherName() throws SQLException {
		DBTable current = keysTable(false);
		DBTable target = keysTable(true);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		createKeysTable(pool, current);
		try {
			renameIndex(pool, IDX_CHANGED, OTHER_INDEX_NAME, COL_A, COL_C);

			PooledConnection connection = pool.borrowWriteConnection();
			try {
				assertTrue(DBSchemaUtils.alignKeys(new AssertProtocol(), connection, current, target));
				connection.commit();
			} finally {
				pool.releaseWriteConnection(connection);
			}

			DBTable extracted = DBSchemaUtils.extractTable(pool, DBSchemaFactory.createDBSchema(), KEYS_TABLE);
			assertEquals(Arrays.asList(COL_C, COL_A), columnNames(index(pool, extracted, IDX_CHANGED)));
			for (DBIndex index : extracted.getIndices()) {
				assertFalse("Index under other name dropped.", index.getName().equalsIgnoreCase(OTHER_INDEX_NAME));
			}
			assertEquals(ROWS, rowCount(pool));
		} finally {
			dropKeysTable(pool);
		}
	}

	/**
	 * Test of {@link DBSchemaUtils#alignKeys(com.top_logic.basic.Log, PooledConnection, DBTable, DBTable)}
	 * for a database missing an index of the current definition.
	 */
	public void testAlignKeysMissingIndex() throws SQLException {
		DBTable current = keysTable(false);
		DBTable target = keysTable(true);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		createKeysTable(pool, current);
		try {
			dropIndex(pool, IDX_CHANGED);

			PooledConnection connection = pool.borrowWriteConnection();
			try {
				assertTrue(DBSchemaUtils.alignKeys(new AssertProtocol(), connection, current, target));
				connection.commit();
			} finally {
				pool.releaseWriteConnection(connection);
			}

			DBTable extracted = DBSchemaUtils.extractTable(pool, DBSchemaFactory.createDBSchema(), KEYS_TABLE);
			assertEquals(Arrays.asList(COL_B, COL_A), columnNames(extracted.getPrimaryKey()));
			assertEquals("Missing index is created in its target form.", Arrays.asList(COL_C, COL_A),
				columnNames(index(pool, extracted, IDX_CHANGED)));
			assertEquals(ROWS, rowCount(pool));
		} finally {
			dropKeysTable(pool);
		}
	}

	/**
	 * Test of
	 * {@link DBSchemaUtils#rebuildTable(com.top_logic.basic.Log, PooledConnection, DBTable, DBTable)}.
	 */
	public void testRebuildTable() throws SQLException {
		DBTable current = keysTable(false);
		DBTable target = keysTable(true);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		createKeysTable(pool, current);
		try {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				DBSchemaUtils.rebuildTable(new AssertProtocol(), connection, current, target);
				connection.commit();
			} finally {
				pool.releaseWriteConnection(connection);
			}

			DBTable extracted = DBSchemaUtils.extractTable(pool, DBSchemaFactory.createDBSchema(), KEYS_TABLE);
			assertEquals(Arrays.asList(COL_B, COL_A), columnNames(extracted.getPrimaryKey()));
			assertEquals(Arrays.asList(COL_C, COL_A), columnNames(index(pool, extracted, IDX_CHANGED)));
			assertEquals(Arrays.asList(COL_C), columnNames(index(pool, extracted, IDX_SAME)));
			assertEquals("Index only in the current definition is recreated.", Arrays.asList(COL_C, COL_B),
				columnNames(index(pool, extracted, IDX_CURRENT)));
			assertEquals("Index of the target definition is created.", Arrays.asList(COL_B),
				columnNames(index(pool, extracted, IDX_TARGET)));
			assertEquals(ROWS, rowCount(pool));

			PooledConnection read = pool.borrowReadConnection();
			try {
				assertFalse("Temporary table removed.", DBSchemaUtils.exists(read,
					DBSchemaUtils.REBUILD_TABLE_PREFIX + Integer.toHexString(KEYS_TABLE.hashCode()).toUpperCase()));
			} finally {
				pool.releaseReadConnection(read);
			}
		} finally {
			dropKeysTable(pool);
		}
	}

	private static DBTable keysTable(boolean target) {
		DBSchema schema = createDBSchema();
		DBTable table = createTable(KEYS_TABLE);
		table.getColumns().add(column(COL_A, DBType.INT));
		table.getColumns().add(column(COL_B, DBType.INT));
		DBColumn c = column(COL_C, DBType.STRING);
		c.setSize(20);
		c.setMandatory(false);
		table.getColumns().add(c);

		DBPrimary primary = createPrimary();
		if (target) {
			primary.getColumnRefs().addAll(Arrays.asList(ref(COL_B), ref(COL_A)));
		} else {
			primary.getColumnRefs().addAll(Arrays.asList(ref(COL_A), ref(COL_B)));
		}
		table.setPrimaryKey(primary);

		if (target) {
			table.getIndices().add(index(IDX_CHANGED, COL_C, COL_A));
		} else {
			table.getIndices().add(index(IDX_CHANGED, COL_A, COL_C));
		}
		table.getIndices().add(index(IDX_SAME, COL_C));
		if (target) {
			table.getIndices().add(index(IDX_TARGET, COL_B));
		} else {
			table.getIndices().add(index(IDX_CURRENT, COL_C, COL_B));
		}
		schema.getTables().add(table);
		return table;
	}

	private static DBColumn column(String name, DBType type) {
		DBColumn result = createColumn(name);
		result.setType(type);
		result.setMandatory(true);
		return result;
	}

	private static DBIndex index(String name, String... columns) {
		DBIndex result = createIndex(name);
		for (String column : columns) {
			result.getColumnRefs().add(ref(column));
		}
		return result;
	}

	private static void createKeysTable(ConnectionPool pool, DBTable table) throws SQLException {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			if (DBSchemaUtils.exists(connection, table)) {
				DBSchemaUtils.resetTable(connection, KEYS_TABLE, false);
			}
			DBSchemaUtils.create(connection, table);
			DBHelper sqlDialect = connection.getSQLDialect();
			try (Statement stmt = connection.createStatement()) {
				for (int n = 0; n < ROWS; n++) {
					stmt.executeUpdate("INSERT INTO " + sqlDialect.tableRef(KEYS_TABLE) + " ("
						+ sqlDialect.columnRef(COL_A) + ", " + sqlDialect.columnRef(COL_B) + ", "
						+ sqlDialect.columnRef(COL_C) + ") VALUES (" + n + ", " + (ROWS - n) + ", 'v" + n + "')");
				}
			}
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private static void dropIndex(ConnectionPool pool, String indexName) throws SQLException {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			try (Statement stmt = connection.createStatement()) {
				stmt.execute(sqlDialect.dropIndex(sqlDialect.qualifiedName(KEYS_TABLE, indexName), KEYS_TABLE));
			}
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private static void renameIndex(ConnectionPool pool, String indexName, String dbName, String... columns)
			throws SQLException {
		dropIndex(pool, indexName);
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			StringBuilder columnList = new StringBuilder();
			for (String column : columns) {
				if (columnList.length() > 0) {
					columnList.append(", ");
				}
				columnList.append(sqlDialect.columnRef(column));
			}
			try (Statement stmt = connection.createStatement()) {
				stmt.execute("CREATE INDEX " + sqlDialect.columnRef(dbName) + " ON " + sqlDialect.tableRef(KEYS_TABLE)
					+ " (" + columnList + ")");
			}
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private static void dropKeysTable(ConnectionPool pool) throws SQLException {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			DBSchemaUtils.resetTable(connection, KEYS_TABLE, false);
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private static int rowCount(ConnectionPool pool) throws SQLException {
		PooledConnection connection = pool.borrowReadConnection();
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet result =
				stmt.executeQuery("SELECT COUNT(*) FROM " + connection.getSQLDialect().tableRef(KEYS_TABLE))) {
				assertTrue(result.next());
				return result.getInt(1);
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private static DBIndex index(ConnectionPool pool, DBTable extracted, String indexName) throws SQLException {
		DBIndex result = findIndex(pool, extracted, indexName);
		assertNotNull("No index '" + indexName + "' in " + extracted.getIndices(), result);
		return result;
	}

	private static DBIndex findIndex(ConnectionPool pool, DBTable extracted, String indexName)
			throws SQLException {
		String dbName = pool.getSQLDialect().qualifiedName(KEYS_TABLE, indexName);
		for (DBIndex index : extracted.getIndices()) {
			if (index.getName().equalsIgnoreCase(dbName)) {
				return index;
			}
		}
		return null;
	}

	private static List<String> columnNames(DBIndex index) {
		List<String> result = new ArrayList<>();
		for (DBColumnRef ref : index.getColumnRefs()) {
			result.add(ref.getName().toUpperCase());
		}
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestDBSchemaUtils.class));
	}

}
