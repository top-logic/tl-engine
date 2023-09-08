/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.dummy;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A {@link DataSource} for tests.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class DummyDataSource implements DataSource {

	/** The separator between a marking and the actual sql-string. */
	public static final String SEPARATOR = ":";

	/**
	 * The mark for a fake-sql-string that should result in an update count.
	 * <p>
	 * For internal use by the DummyFoo classes (and their descendants) only.
	 * </p>
	 */
	public static final String UPDATE_COUNT = "UpdateCount";

	/**
	 * The mark for a fake-sql-string that should result in a result set.
	 * <p>
	 * For internal use by the DummyFoo classes (and their descendants) only.
	 * </p>
	 */
	public static final String RESULT_SET = "ResultSet";

	/**
	 * The mark for a fake-sql-string that should result in nothing.
	 * <p>
	 * For internal use by the DummyFoo classes (and their descendants) only.
	 * </p>
	 */
	public static final String NO_RESULT = "NoResult";

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// Ignore
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// Ignore
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return new DummyConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}

	static int parseAffectedRows(String sql) {
		int divider = sql.indexOf(':');
		if (divider < 0) {
			return 0;
		}
		return Integer.parseInt(sql.substring(0, divider));
	}

	/**
	 * Marks a statement sent to {@link DummyStatement#execute(String)} or
	 * {@link DummyPreparedStatement#execute()} as resulting in a {@link ResultSet}.
	 */
	public static String markResultSet(String sql) {
		return RESULT_SET + SEPARATOR + sql;
	}

	/**
	 * Marks a statement sent to {@link DummyStatement#execute(String)} or
	 * {@link DummyPreparedStatement#execute()} as resulting in an update count.
	 */
	public static String markUpdateCount(String sql) {
		return UPDATE_COUNT + SEPARATOR + sql;
	}

	/**
	 * Marks a statement sent to {@link DummyStatement#execute(String)} or
	 * {@link DummyPreparedStatement#execute()} as resulting in nothing.
	 */
	public static String markNoResult(String sql) {
		return NO_RESULT + SEPARATOR + sql;
	}

	/**
	 * Removes the mark set with {@link #markResultSet(String)}.
	 * <p>
	 * For internal use by the DummyFoo classes (and their descendants) only.
	 * </p>
	 */
	public static String removeMarkResultSet(String sql) {
		assert sql.startsWith(RESULT_SET + SEPARATOR);
		return sql.substring(RESULT_SET.length() + SEPARATOR.length());
	}

	/**
	 * Removes the mark set with {@link #markUpdateCount(String)}.
	 * <p>
	 * For internal use by the DummyFoo classes (and their descendants) only.
	 * </p>
	 */
	public static String removeMarkUpdateCount(String sql) {
		assert sql.startsWith(UPDATE_COUNT + SEPARATOR);
		return sql.substring(UPDATE_COUNT.length() + SEPARATOR.length());
	}

	/**
	 * Removes the mark set with {@link #markNoResult(String)}.
	 * <p>
	 * For internal use by the DummyFoo classes (and their descendants) only.
	 * </p>
	 */
	public static String removeMarkNoResult(String sql) {
		assert sql.startsWith(NO_RESULT + SEPARATOR);
		return sql.substring(NO_RESULT.length() + SEPARATOR.length());
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("Dummy datasource.");
	}

}
