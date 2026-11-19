/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import com.top_logic.basic.sql.H2DataSourceProxy;

/**
 * Test that a connection of the {@link H2DataSourceProxy} answers a repeated query with the
 * current content of the queried table after another connection has changed it, and test the
 * handling of the database URL by {@link H2DataSourceProxy}.
 */
@SuppressWarnings("javadoc")
public class TestH2ResultReuse extends TestCase {

	private static final String USER_PROPERTY = "user";

	private static final String PASSWORD_PROPERTY = "password";

	private static final String URL = "jdbc:h2:mem:TestH2ResultReuse;DB_CLOSE_DELAY=-1";

	private static final String CANARY_URL = "jdbc:h2:mem:TestH2ResultReuseCanary;DB_CLOSE_DELAY=-1";

	private static final String USER = "sa";

	private static final String PASSWORD = "";

	private static final String QUERY_CACHE_SIZE_QUERY = "SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS"
		+ " WHERE SETTING_NAME = '" + H2DataSourceProxy.QUERY_CACHE_SIZE_SETTING + "'";

	private static final String QUERY = "SELECT v FROM t WHERE id IN (1, 2) ORDER BY id";

	private H2DataSourceProxy _dataSource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dataSource = newDataSource(URL);

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

	public void testQueryCacheOff() throws SQLException {
		try (Connection connection = _dataSource.getConnection()) {
			assertEquals("0", queryCacheSize(connection));
		}
	}

	public void testExplicitQueryCacheSizeKept() throws Exception {
		H2DataSourceProxy dataSource =
			newDataSource("jdbc:h2:mem:TestH2ResultReuseExplicit;DB_CLOSE_DELAY=-1;query_cache_size=5");
		try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
			assertEquals("5", queryCacheSize(connection));
			statement.execute("SHUTDOWN");
		}
	}

	public void testDatabasePathWithSeveralSettings() throws Exception {
		H2DataSourceProxy dataSource = newDataSource("jdbc:h2:file:/x/db;A=1;B=2");
		assertEquals(Paths.get("/x/db.mv.db").toAbsolutePath(), dataSource.getDatabasePath());
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
		try (Connection reader = _dataSource.getConnection();
				Connection writer = _dataSource.getConnection()) {
			assertFalse("Deleted rows found.", inListWithLimitFindsDeletedRows(reader, writer));
		}
	}

	/**
	 * Canary for the H2 bug the statement cache switch of {@link H2DataSourceProxy} works around
	 * (h2database/h2database#4580, fixed by h2database/h2database#4581).
	 * 
	 * <p>
	 * Runs the scenario of {@link #testInListWithLimitAfterDelete()} on connections opened directly
	 * through the H2 JDBC driver with the default statement cache, and asserts that H2 still answers
	 * the re-executed query with the deleted rows. As soon as an H2 version with the fix is used,
	 * this test fails, so that the workaround is removed.
	 * </p>
	 */
	public void testH2StaleInListResultStillPresent() throws SQLException {
		try (Connection reader = DriverManager.getConnection(CANARY_URL, USER, PASSWORD);
				Connection writer = DriverManager.getConnection(CANARY_URL, USER, PASSWORD)) {
			boolean deletedRowsFound = inListWithLimitFindsDeletedRows(reader, writer);
			try (Statement statement = writer.createStatement()) {
				statement.execute("SHUTDOWN");
			}
			assertTrue("H2 no longer returns deleted rows for a re-executed IN query (h2database#4581)."
				+ " Remove " + H2DataSourceProxy.QUERY_CACHE_SIZE_SETTING
				+ "=0 from H2DataSourceProxy and this test.", deletedRowsFound);
		}
	}

	/**
	 * Inserts rows through the writer, deletes them again, and tells whether the reader still finds
	 * them with a re-executed query with an IN list and a row limit.
	 */
	private static boolean inListWithLimitFindsDeletedRows(Connection reader, Connection writer)
			throws SQLException {
		String q = "SELECT 1 FROM ss WHERE target IN (1, 2) AND role IN (10) LIMIT 1";
		writer.setAutoCommit(false);
		try (Statement st = writer.createStatement()) {
			st.execute("CREATE TABLE ss (target BIGINT NOT NULL, bo BIGINT NOT NULL, role BIGINT NOT NULL,"
				+ " reason SMALLINT NOT NULL, PRIMARY KEY (target, bo, role, reason))");
			st.execute("CREATE INDEX ss_borr ON ss (bo, role, reason)");
			writer.commit();
		}
		reader.setAutoCommit(true);
		assertFalse(exists(reader, q));

		batch(writer, "INSERT INTO ss VALUES (?, ?, ?, ?)", new long[][] { { 1, 100, 10, 3 }, { 1, 101, 10, 5 } });
		writer.commit();
		assertTrue(exists(reader, q));

		batch(writer, "DELETE FROM ss WHERE target=? AND bo=? AND role=? AND reason=?",
			new long[][] { { 1, 100, 10, 3 }, { 1, 101, 10, 5 } });
		writer.commit();
		return exists(reader, q);
	}

	private static H2DataSourceProxy newDataSource(String url) throws Exception {
		Properties config = new Properties();
		config.setProperty(H2DataSourceProxy.URL_OPTION, url);
		config.setProperty(USER_PROPERTY, USER);
		config.setProperty(PASSWORD_PROPERTY, PASSWORD);
		return new H2DataSourceProxy(config);
	}

	private static String queryCacheSize(Connection connection) throws SQLException {
		try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(QUERY_CACHE_SIZE_QUERY)) {
			assertTrue(rs.next());
			return rs.getString(1);
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
