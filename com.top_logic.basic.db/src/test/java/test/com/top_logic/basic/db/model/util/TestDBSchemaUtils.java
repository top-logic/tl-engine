/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.util;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
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
