/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.model.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static java.util.Arrays.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLAddColumn;
import com.top_logic.basic.db.sql.SQLAddIndex;
import com.top_logic.basic.db.sql.SQLAlterTable;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLDropColumn;
import com.top_logic.basic.db.sql.SQLDropIndex;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLModifyColumn;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case for {@link SQLAlterTable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestAlterTable extends BasicTestCase {

	/**
	 * Number of rows inserted by {@link #insertRows()}.
	 */
	private static final int ROW_COUNT = 5;

	private DBSchema _schema;

	private ConnectionPool _pool;

	private DBTable _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_schema = DBSchemaFactory.createDBSchema();
		_table = DBSchemaFactory.createTable(TestDBConcurrentAccess.class.getSimpleName());
		_schema.getTables().add(_table);

		DBColumn a = DBSchemaFactory.createColumn("a");
		a.setBinary(true);
		a.setType(DBType.STRING);
		a.setSize(100);
		a.setMandatory(true);
		_table.getColumns().add(a);

		DBColumn b = DBSchemaFactory.createColumn("b");
		b.setType(DBType.LONG);
		_table.getColumns().add(b);

		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();

		DBSchemaUtils.recreateTables(_pool, _schema);
	}

	@Override
	protected void tearDown() throws Exception {
		DBSchemaUtils.resetTables(_pool, _schema, false);

		super.tearDown();
	}

	/**
	 * Tests {@link SQLDropColumn}.
	 */
	public void testDropColumn() throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				dropColumn(table(_table.getDBName()), "a"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedTable = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedTable.getTable(_table.getDBName());
		assertNotNull(alteredTable.getColumn("b"));
		assertNull(alteredTable.getColumn("a"));
	}

	/**
	 * Tests {@link SQLModifyColumn}.
	 */
	public void testRenameColumn() throws SQLException {
		PooledConnection connection;

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				modifyColumnName(table(_table.getDBName()), "b", DBType.LONG, "xxx"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedSchema = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedSchema.getTable(_table.getDBName());
		DBColumn newColumn = alteredTable.getColumn("xxx");
		assertNotNull(newColumn);
		assertEquals(DBType.LONG, newColumn.getType());
	}

	/**
	 * Tests {@link SQLModifyColumn}.
	 */
	public void testModifyColumnType() throws SQLException {
		PooledConnection connection;

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				modifyColumnType(table(_table.getDBName()), "b", DBType.STRING).setSize(60));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedTable = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedTable.getTable(_table.getDBName());
		assertEquals(DBType.STRING, alteredTable.getColumn("b").getType());
	}

	/**
	 * Tests {@link SQLModifyColumn}.
	 */
	public void testModifyColumnMandatory() throws SQLException {
		PooledConnection connection;

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				modifyColumnMandatory(table(_table.getDBName()), "b", DBType.LONG, true));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, insert(
				table(_table.getDBName(), "t"),
				asList("a"),
				asList(
					literalString("v1"))));
			connection.commit();
			fail("No value for mandatory column 'b' given.");
		} catch (SQLException ex) {
			// expected
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedTable = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedTable.getTable(_table.getDBName());
		assertTrue(alteredTable.getColumn("b").isMandatory());
	}

	/**
	 * Tests {@link SQLAddColumn}.
	 */
	public void testAddColumn() throws SQLException {
		PooledConnection connection;

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, insert(
				table(_table.getDBName(), "t"),
				asList("a", "b"),
				asList(
					literalString("v0"),
					literalLong(265L))));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				addColumn(table(_table.getDBName()), "c", DBType.STRING).setSize(100).setMandatory(true).setBinary(true)
					.setDefaultValue("cDefault"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, insert(
				table(_table.getDBName(), "t"),
				asList("a", "b", "c"),
				asList(
					literalString("v1"),
					literalLong(1568714L),
					literalString("v2"))));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		SQLStatement query = select(
			asList(columnDef("a"), columnDef("b"), columnDef("c")),
			table(_table.getDBName()),
			SQLBoolean.TRUE,
			orders(order(true, column("a"))));
		connection = _pool.borrowReadConnection();
		try {
			try (ResultSet result = executeQuery(connection, query)) {
				assertTrue(result.next());
				assertEquals("v1", result.getString("a"));
				assertEquals(1568714L, result.getLong("b"));
				assertEquals("v2", result.getString("c"));
				assertTrue(result.next());
				assertEquals("v0", result.getString("a"));
				assertEquals(265L, result.getLong("b"));
				assertEquals("cDefault", result.getString("c"));

				assertFalse(result.next());
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}

	}

	/**
	 * Tests {@link SQLModifyColumn} renaming a column of a table with content.
	 */
	public void testRenameColumnWithContent() throws SQLException {
		insertRows();

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				modifyColumnName(table(_table.getDBName()), "b", DBType.LONG, "xxx"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedSchema = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedSchema.getTable(_table.getDBName());
		assertNull(alteredTable.getColumn("b"));
		assertEquals(DBType.LONG, alteredTable.getColumn("xxx").getType());

		connection = _pool.borrowReadConnection();
		try {
			try (ResultSet result = executeQuery(connection, selectRows("xxx"))) {
				for (int n = 0; n < ROW_COUNT; n++) {
					assertTrue(result.next());
					assertEquals(rowName(n), result.getString("a"));
					assertEquals(rowValue(n), result.getLong("xxx"));
				}
				assertFalse(result.next());
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Tests {@link SQLModifyColumn} changing the type of a non-mandatory column of a table with
	 * content.
	 */
	public void testModifyColumnTypeWithContent() throws SQLException {
		boolean detectsBinary = detectsBinary();
		insertRows();
		insertRow(rowName(ROW_COUNT), literalNull(DBType.LONG));

		executeAlter(modifyColumnType(table(_table.getDBName()), "b", DBType.STRING).setSize(60));

		DBColumn b = analyzeTable().getColumn("b");
		assertEquals(DBType.STRING, b.getType());
		assertFalse(b.isMandatory());
		if (detectsBinary) {
			assertFalse(b.isBinary());
		}
		assertValues("b", n -> n < ROW_COUNT ? Long.toString(rowValue(n)) : null, ROW_COUNT + 1);
	}

	/**
	 * Tests {@link SQLModifyColumn} changing the type of a mandatory column of a table with
	 * content.
	 */
	public void testModifyMandatoryColumnTypeWithContent() throws SQLException {
		executeAlter(modifyColumnMandatory(table(_table.getDBName()), "b", DBType.LONG, true));
		insertRows();

		executeAlter(
			modifyColumnType(table(_table.getDBName()), "b", DBType.STRING).setSize(60).setMandatory(true));

		DBColumn b = analyzeTable().getColumn("b");
		assertEquals(DBType.STRING, b.getType());
		assertTrue(b.isMandatory());
		assertValues("b", n -> Long.toString(rowValue(n)), ROW_COUNT);
	}

	/**
	 * Tests {@link SQLModifyColumn} changing the type of a column of a table with content to a
	 * binary string.
	 */
	public void testModifyColumnTypeToBinaryWithContent() throws SQLException {
		boolean detectsBinary = detectsBinary();
		insertRows();

		executeAlter(
			modifyColumnType(table(_table.getDBName()), "b", DBType.STRING).setSize(60).setBinary(true));

		DBColumn b = analyzeTable().getColumn("b");
		assertEquals(DBType.STRING, b.getType());
		assertFalse(b.isMandatory());
		if (detectsBinary) {
			assertTrue(b.isBinary());
		}
		assertValues("b", n -> Long.toString(rowValue(n)), ROW_COUNT);
	}

	/**
	 * Tests {@link SQLModifyColumn} changing a mandatory binary string column of a table with
	 * content to a non-binary string.
	 */
	public void testModifyColumnTypeFromBinaryWithContent() throws SQLException {
		boolean detectsBinary = detectsBinary();
		insertRows();

		executeAlter(
			modifyColumnType(table(_table.getDBName()), "a", DBType.STRING).setSize(100).setMandatory(true));

		DBColumn a = analyzeTable().getColumn("a");
		assertEquals(DBType.STRING, a.getType());
		assertTrue(a.isMandatory());
		if (detectsBinary) {
			assertFalse(a.isBinary());
		}
		assertValues("b", n -> Long.toString(rowValue(n)), ROW_COUNT);
	}

	/**
	 * Tests that an index containing a column is kept when changing the type of the column in a
	 * table with content.
	 */
	public void testModifyColumnTypeKeepsIndex() throws SQLException {
		executeAlter(addIndex(table(_table.getDBName()), "testIndex", true, "b", "a"));
		insertRows();
		assertTestIndex("Index not extracted.");

		executeAlter(modifyColumnType(table(_table.getDBName()), "b", DBType.STRING).setSize(60));

		assertTestIndex("Index lost by type change.");
		assertValues("b", n -> Long.toString(rowValue(n)), ROW_COUNT);

		try {
			insertRow(rowName(0), literalString(Long.toString(rowValue(0))));
			fail("Unique index violation!");
		} catch (SQLException ex) {
			// expected
		}
	}

	/**
	 * Tests {@link SQLModifyColumn} making a column of a table with content mandatory.
	 */
	public void testModifyColumnMandatoryWithContent() throws SQLException {
		insertRows();

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				modifyColumnMandatory(table(_table.getDBName()), "b", DBType.LONG, true));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedTable = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedTable.getTable(_table.getDBName());
		assertTrue(alteredTable.getColumn("b").isMandatory());

		connection = _pool.borrowReadConnection();
		try {
			try (ResultSet result = executeQuery(connection, selectRows("b"))) {
				for (int n = 0; n < ROW_COUNT; n++) {
					assertTrue(result.next());
					assertEquals(rowName(n), result.getString("a"));
					assertEquals(rowValue(n), result.getLong("b"));
				}
				assertFalse(result.next());
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Tests {@link SQLDropColumn} on a table with content.
	 */
	public void testDropColumnWithContent() throws SQLException {
		insertRows();

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection,
				dropColumn(table(_table.getDBName()), "b"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		DBSchema analyzedTable = DBSchemaUtils.extractTable(_pool, _table.getDBName());
		DBTable alteredTable = analyzedTable.getTable(_table.getDBName());
		assertNull(alteredTable.getColumn("b"));

		SQLStatement query = select(
			asList(columnDef("a")),
			table(_table.getDBName()),
			SQLBoolean.TRUE,
			orders(order(false, column("a"))));
		connection = _pool.borrowReadConnection();
		try {
			try (ResultSet result = executeQuery(connection, query)) {
				for (int n = 0; n < ROW_COUNT; n++) {
					assertTrue(result.next());
					assertEquals(rowName(n), result.getString("a"));
				}
				assertFalse(result.next());
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Fills the test table with {@link #ROW_COUNT} rows.
	 */
	private void insertRows() throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			for (int n = 0; n < ROW_COUNT; n++) {
				executeUpdate(connection, insert(
					table(_table.getDBName(), "t"),
					asList("a", "b"),
					asList(
						literalString(rowName(n)),
						literalLong(rowValue(n)))));
			}
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Inserts a single row into the test table.
	 */
	private void insertRow(String a, SQLExpression b) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, insert(
				table(_table.getDBName(), "t"),
				asList("a", "b"),
				asList(
					literalString(a),
					b)));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Executes the given schema modification.
	 */
	private void executeAlter(SQLStatement alter) throws SQLException {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, alter);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * The test table as currently defined in the database.
	 */
	private DBTable analyzeTable() throws SQLException {
		return DBSchemaUtils.extractTable(_pool, _table.getDBName()).getTable(_table.getDBName());
	}

	/**
	 * Asserts that the test table has the unique index "testIndex" on the columns "b" and "a".
	 */
	private void assertTestIndex(String message) throws SQLException {
		DBTable table = analyzeTable();
		DBIndex index = table.getIndices().stream()
			.filter(i -> "testIndex".equalsIgnoreCase(i.getName()))
			.findFirst()
			.orElse(null);
		assertNotNull(message + " Extracted indices: " + table.getIndices().stream()
			.map(i -> i.getName() + i.getColumns().stream().map(DBColumn::getName).collect(Collectors.toList()))
			.collect(Collectors.toList()), index);
		assertTrue(index.isUnique());
		assertEquals(asList("b", "a"), index.getColumns().stream().map(DBColumn::getName).collect(Collectors.toList()));
	}

	/**
	 * Whether the schema extraction of the database reports the binary flag of string columns.
	 * 
	 * <p>
	 * Must be called before modifying column "a", which is created binary.
	 * </p>
	 */
	private boolean detectsBinary() throws SQLException {
		return analyzeTable().getColumn("a").isBinary();
	}

	/**
	 * Asserts that the table contains exactly the given number of rows with the names
	 * {@link #rowName(int)} and the given values in the given column.
	 */
	private void assertValues(String valueColumn, IntFunction<String> expectedValue, int rowCount)
			throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			try (ResultSet result = executeQuery(connection, selectRows(valueColumn))) {
				for (int n = 0; n < rowCount; n++) {
					assertTrue(result.next());
					assertEquals(rowName(n), result.getString("a"));
					assertEquals(expectedValue.apply(n), result.getString(valueColumn));
				}
				assertFalse(result.next());
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Query selecting column "a" and the given column of all rows, ordered by "a".
	 */
	private SQLStatement selectRows(String valueColumn) {
		return select(
			asList(columnDef("a"), columnDef(valueColumn)),
			table(_table.getDBName()),
			SQLBoolean.TRUE,
			orders(order(false, column("a"))));
	}

	private static String rowName(int n) {
		return "row" + n;
	}

	private static long rowValue(int n) {
		return 1000L * n + 42L;
	}

	/**
	 * Test {@link SQLAddIndex}, {@link SQLDropIndex}.
	 */
	public void testIndex() throws SQLException {
		PooledConnection connection;

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, addIndex(
				table(_table.getDBName()),
				"testIndex",
				true,
				"b", "a"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, insert(
				table(_table.getDBName(), "t"),
				asList("a", "b"),
				asList(
					literalString("v1"),literalLong(42))));
			connection.commit();
			try {
				executeUpdate(connection, insert(
					table(_table.getDBName(), "t"),
					asList("a", "b"),
					asList(
						literalString("v1"), literalLong(42))));
				connection.commit();
				fail("Unique index violation!");
			} catch (SQLException ex) {
				// expected
			}
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, dropIndex(
				table(_table.getDBName()),
				"testIndex"));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		
		connection = _pool.borrowWriteConnection();
		try {
			// Index was removed, inserting same values must be successful.
			executeUpdate(connection, insert(
				table(_table.getDBName(), "t"),
				asList("a", "b"),
				asList(
					literalString("v1"), literalLong(42))));
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	private ResultSet executeQuery(PooledConnection readConnection, SQLStatement query) throws SQLException {
		SQLQuery<SQLStatement> select = query(query);
		CompiledStatement stmt = select.toSql(readConnection.getSQLDialect());
		return stmt.executeQuery(readConnection);
	}

	private void executeUpdate(PooledConnection writeConnection, SQLStatement update) throws SQLException {
		SQLQuery<?> alterStmt = query(update);
		CompiledStatement stmt = alterStmt.toSql(writeConnection.getSQLDialect());
		stmt.executeUpdate(writeConnection);
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestAlterTable.class));
	}

}
