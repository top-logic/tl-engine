/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import com.top_logic.basic.sql.H2DataSourceProxy;

/**
 * Test that a connection of the {@link H2DataSourceProxy} answers a repeated query with the
 * current content of the queried table after another connection has changed it.
 */
@SuppressWarnings("javadoc")
public class TestH2ResultReuse extends TestCase {

	private static final String URL_PROPERTY = "url";

	private static final String USER_PROPERTY = "user";

	private static final String PASSWORD_PROPERTY = "password";

	private static final String URL = "jdbc:h2:mem:TestH2ResultReuse;DB_CLOSE_DELAY=-1";

	private static final String QUERY = "SELECT v FROM t WHERE id IN (1, 2) ORDER BY id";

	private H2DataSourceProxy _dataSource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Properties config = new Properties();
		config.setProperty(URL_PROPERTY, URL);
		config.setProperty(USER_PROPERTY, "sa");
		config.setProperty(PASSWORD_PROPERTY, "");
		_dataSource = new H2DataSourceProxy(config);

		try (Connection connection = _dataSource.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE t (id INT PRIMARY KEY, v INT)");
			statement.execute("INSERT INTO t VALUES (1, 10), (2, 20)");
			connection.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Connection connection = _dataSource.getConnection();
				Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
			statement.execute("SHUTDOWN");
		}
		_dataSource = null;
		super.tearDown();
	}

	public void testSeesChangeOfOtherConnection() throws SQLException {
		try (Connection reader = _dataSource.getConnection();
				Connection writer = _dataSource.getConnection();
				Statement readStatement = reader.createStatement();
				Statement writeStatement = writer.createStatement()) {
			reader.setAutoCommit(true);
			writer.setAutoCommit(false);

			assertEquals(10, firstValue(readStatement));

			writeStatement.executeUpdate("UPDATE t SET v = 11 WHERE id = 1");
			assertEquals("Uncommitted change is not visible.", 10, firstValue(readStatement));

			writer.commit();
			assertEquals("Committed change of the other connection is visible.", 11, firstValue(readStatement));
		}
	}

	/**
	 * A query with a list of several values for the first primary key column and a row limit,
	 * repeated on the same connection, sees the rows another connection has deleted and committed.
	 */
	public void testInListWithLimitAfterDelete() throws SQLException {
		String q = "SELECT 1 FROM ss WHERE target IN (1, 2) AND role IN (10) LIMIT 1";
		try (Connection setup = _dataSource.getConnection(); Statement st = setup.createStatement()) {
			st.execute("CREATE TABLE ss (target BIGINT NOT NULL, bo BIGINT NOT NULL, role BIGINT NOT NULL,"
				+ " reason SMALLINT NOT NULL, PRIMARY KEY (target, bo, role, reason))");
			st.execute("CREATE INDEX ss_borr ON ss (bo, role, reason)");
			setup.commit();
		}
		try (Connection reader = _dataSource.getConnection();
				Connection writer = _dataSource.getConnection()) {
			reader.setAutoCommit(true);
			writer.setAutoCommit(false);
			assertFalse(exists(reader, q));

			batch(writer, "INSERT INTO ss VALUES (?, ?, ?, ?)", new long[][] { { 1, 100, 10, 3 }, { 1, 101, 10, 5 } });
			writer.commit();
			assertTrue(exists(reader, q));

			batch(writer, "DELETE FROM ss WHERE target=? AND bo=? AND role=? AND reason=?",
				new long[][] { { 1, 100, 10, 3 }, { 1, 101, 10, 5 } });
			writer.commit();
			assertFalse("Deleted rows found.", exists(reader, q));
		}
	}

	private static boolean exists(Connection connection, String sql) throws SQLException {
		try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
			return rs.next();
		}
	}

	private static void batch(Connection connection, String sql, long[][] rows) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (long[] row : rows) {
				for (int i = 0; i < row.length; i++) {
					ps.setLong(i + 1, row[i]);
				}
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private static int firstValue(Statement statement) throws SQLException {
		try (ResultSet result = statement.executeQuery(QUERY)) {
			assertTrue(result.next());
			return result.getInt(1);
		}
	}

}
