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

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.db.model.DBColumn;
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
			executeUpdate(connection, alterTable(
				table(_table.getDBName()),
				dropColumn("a")));
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
	public void testModifyColumnType() throws SQLException {
		PooledConnection connection;

		connection = _pool.borrowWriteConnection();
		try {
			executeUpdate(connection, alterTable(
				table(_table.getDBName()),
				modifyColumnType("b", DBType.STRING).setSize(60)));
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
			executeUpdate(connection, alterTable(
				table(_table.getDBName()),
				modifyColumnMandatory("b", DBType.LONG, true)));
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
			executeUpdate(connection, alterTable(
				table(_table.getDBName()),
				addColumn("c", DBType.STRING).setSize(100).setMandatory(true).setBinary(true)
					.setDefaultValue("cDefault")));
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
