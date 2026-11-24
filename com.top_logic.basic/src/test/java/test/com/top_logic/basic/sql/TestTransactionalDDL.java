/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import com.top_logic.basic.sql.DB2Helper;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.MSAccessHelper;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.MSSQLHelper90;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.Oracle12Helper;
import com.top_logic.basic.sql.PostgreSQLHelper;

/**
 * Test of {@link DBHelper#supportsTransactionalDDL()}.
 *
 * <p>
 * Checks against an H2 database that its DDL is not transactional, as {@link H2Helper} reports,
 * and the value each dialect reports.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTransactionalDDL extends TestCase {

	private static final String URL = "jdbc:h2:mem:TestTransactionalDDL;DB_CLOSE_DELAY=-1";

	private static final String USER = "sa";

	private static final String PASSWORD = "";

	private Connection _connection;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_connection = DriverManager.getConnection(URL, USER, PASSWORD);
		_connection.setAutoCommit(false);
		try (Statement statement = _connection.createStatement()) {
			statement.execute("CREATE TABLE a (id INT)");
		}
		_connection.commit();
	}

	@Override
	protected void tearDown() throws Exception {
		try (Statement statement = _connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
			statement.execute("SHUTDOWN");
		}
		_connection.close();
		_connection = null;
		super.tearDown();
	}

	/**
	 * A rollback on H2 after DDL statements undoes neither the DDL nor the data change executed
	 * before it in the same transaction.
	 */
	public void testH2DDLNotTransactional() throws SQLException {
		try (Statement statement = _connection.createStatement()) {
			statement.execute("INSERT INTO a VALUES (1)");
			statement.execute("CREATE TABLE b (id INT)");
			statement.execute("ALTER TABLE a ADD COLUMN x INT");
			_connection.rollback();

			assertEquals("Created table survives the rollback.", 1,
				count(statement, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'B'"));
			assertEquals("Row inserted before the DDL survives the rollback.", 1,
				count(statement, "SELECT COUNT(*) FROM a"));
			assertEquals("Added column survives the rollback.", 1,
				count(statement, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS"
					+ " WHERE TABLE_NAME = 'A' AND COLUMN_NAME = 'X'"));
		}
		assertFalse(supportsTransactionalDDL(H2Helper.class));
	}

	public void testDialects() {
		assertTrue(supportsTransactionalDDL(PostgreSQLHelper.class));
		assertTrue(supportsTransactionalDDL(MSSQLHelper.class));
		assertTrue(supportsTransactionalDDL(MSSQLHelper90.class));
		assertTrue(supportsTransactionalDDL(DB2Helper.class));

		assertFalse(supportsTransactionalDDL(H2Helper.class));
		assertFalse(supportsTransactionalDDL(Oracle12Helper.class));
		assertFalse(supportsTransactionalDDL(MySQLHelper.class));
		assertFalse(supportsTransactionalDDL(MySQLHelper.MySQLWithBackslashEscape.class));
		assertFalse(supportsTransactionalDDL(MSAccessHelper.class));
	}

	private static boolean supportsTransactionalDDL(Class<? extends DBHelper> dialect) {
		return DBHelper.createDefaultInstance(dialect).supportsTransactionalDDL();
	}

	private static int count(Statement statement, String sql) throws SQLException {
		try (ResultSet result = statement.executeQuery(sql)) {
			assertTrue(result.next());
			return result.getInt(1);
		}
	}

}
