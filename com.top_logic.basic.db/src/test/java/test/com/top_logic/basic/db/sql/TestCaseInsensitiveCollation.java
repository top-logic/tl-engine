/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Test case verifying that a non-binary {@link DBType#STRING} column compares equal
 * case-insensitively at the actual database level, while a binary {@link DBType#STRING} column
 * compares equal only case-sensitively.
 *
 * <p>
 * Ticket #29425: Non-binary string columns must be case-insensitive at the database level (e.g. to
 * support case-insensitive matching of names or search terms), whereas binary string columns must
 * keep their exact, case-sensitive comparison semantics. This is verified against a real table
 * created in the actual configured database (H2 by default; MySQL, MSSQL, Oracle, PostgreSQL, or
 * DB2 when those servers are configured for the multi-database test suite), not just against the
 * generated DDL string.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCaseInsensitiveCollation extends BasicTestCase {

	private static final String TABLE_NAME = "TestCaseInsensitiveCollation";

	private static final String CI_COLUMN = "CI_COL";

	private static final String CS_COLUMN = "CS_COL";

	private static final int COLUMN_SIZE = 100;

	private ConnectionPool _pool;

	private DBHelper _sqlDialect;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_sqlDialect = _pool.getSQLDialect();
	}

	@Override
	protected void tearDown() throws Exception {
		_sqlDialect = null;
		_pool = null;
		super.tearDown();
	}

	/**
	 * Tests that a non-binary string column matches independent of case, while a binary string
	 * column of the very same table only matches with the exact case that was stored.
	 */
	public void testNonBinaryColumnIsCaseInsensitive() throws SQLException {
		createTable();
		try {
			insertRow("Foo", "Foo");

			// Exact-case matching must work on every database.
			assertEquals("Non-binary column must match the exact stored value.", 1, count(CI_COLUMN, "Foo"));
			assertEquals("Binary column must match the exact stored value.", 1, count(CS_COLUMN, "Foo"));

			// Oracle and DB2 cannot make a column case-insensitive via its collation
			// (case-insensitivity there depends on the database/session configuration), so the
			// case-insensitive assertions only apply where the dialect supports column collation.
			if (_sqlDialect.supportsColumnCollation()) {
				assertEquals("Non-binary column must match a differently-cased value.", 1, count(CI_COLUMN, "foo"));
				assertEquals("Binary column must not match a differently-cased value.", 0, count(CS_COLUMN, "foo"));
			}
		} finally {
			dropTable();
		}
	}

	/**
	 * Creates the test table with one non-binary and one binary string column through the regular
	 * schema-creation path, so that the dialect's column collation is applied exactly as in
	 * production (and, on PostgreSQL, the {@code tl_ci} collation is created via
	 * {@link DBHelper#prepareDatabase(PooledConnection)}).
	 */
	private void createTable() throws SQLException {
		DBSchema schema = DBSchemaFactory.createDBSchema();
		DBTable table = DBSchemaFactory.createTable(TABLE_NAME);
		schema.getTables().add(table);
		table.getColumns().add(stringColumn(CI_COLUMN, false));
		table.getColumns().add(stringColumn(CS_COLUMN, true));

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			DBSchemaUtils.createTables(connection, schema, false);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Creates a non-mandatory string column of the configured {@link #COLUMN_SIZE size}.
	 */
	private DBColumn stringColumn(String name, boolean binary) {
		DBColumn column = DBSchemaFactory.createColumn(name);
		column.setType(DBType.STRING);
		column.setSize(COLUMN_SIZE);
		column.setBinary(binary);
		column.setMandatory(false);
		return column;
	}

	/**
	 * Inserts a single row storing the given values in the non-binary and binary column,
	 * respectively.
	 */
	private void insertRow(String ciValue, String csValue) throws SQLException {
		CompiledStatement insert = query(
			insert(
				table(TABLE_NAME),
				columnNames(CI_COLUMN, CS_COLUMN),
				expressions(literal(DBType.STRING, ciValue), literal(DBType.STRING, csValue))))
			.toSql(_sqlDialect);

		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			insert.executeUpdate(connection);
			connection.commit();
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Counts the rows whose given column equals the given value, relying on the database's own
	 * (case-sensitive or case-insensitive) comparison semantics for that column.
	 */
	private int count(String columnName, String value) throws SQLException {
		CompiledStatement select = query(
			select(
				columns(columnDef(columnName)),
				table(TABLE_NAME),
				eqSQL(column(columnName), literal(DBType.STRING, value))))
			.toSql(_sqlDialect);

		PooledConnection connection = _pool.borrowReadConnection();
		try {
			int rows = 0;
			try (ResultSet result = select.executeQuery(connection)) {
				while (result.next()) {
					rows++;
				}
			}
			return rows;
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Drops the test table, ignoring failures (e.g. if the table was never created).
	 */
	private void dropTable() {
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			try (Statement statement = connection.createStatement()) {
				statement.execute("DROP TABLE " + _sqlDialect.tableRef(TABLE_NAME));
			}
			connection.commit();
		} catch (SQLException ex) {
			// Ignore: table may not have been created.
		} finally {
			_pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Suite of tests running across all databases configured for the multi-database test setup.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(DatabaseTestSetup.getDBTest(TestCaseInsensitiveCollation.class));
	}

}
