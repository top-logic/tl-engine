/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql.dummy;

import static test.com.top_logic.basic.sql.dummy.DummyDataSource.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ArrayUtil;

/**
 * A {@link Statement} for tests.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DummyStatement implements Statement {

	private List<Integer> _batchAffectedRows = new ArrayList<>();

	private int _updateCount;

	private ResultSet _resultSet;

	/**
	 * Sets the current update count to the given value and the current result set to null. Returns
	 * the new update count.
	 */
	protected int setUpdateCount(int newValue) {
		_updateCount = newValue;
		_resultSet = null;
		return _updateCount;
	}

	/**
	 * Sets the current result set to the given value and the update count set to -1. Returns the
	 * new result set.
	 */
	protected ResultSet setResultSet(ResultSet newValue) {
		_updateCount = -1;
		_resultSet = newValue;
		return _resultSet;
	}

	/** Sets the current update count to -1 and the current result set to null. */
	protected void clearResults() {
		_updateCount = -1;
		_resultSet = null;
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
	public ResultSet executeQuery(String sql) throws SQLException {
		return setResultSet(new DummyResultSet(DummyDataSource.parseAffectedRows(sql)));
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return setUpdateCount(DummyDataSource.parseAffectedRows(sql));
	}

	@Override
	public void close() throws SQLException {
		// Ignore
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxRows() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cancel() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		if (sql.startsWith(UPDATE_COUNT)) {
			setUpdateCount(parseAffectedRows(removeMarkUpdateCount(sql)));
			return false;
		} else if (sql.startsWith(RESULT_SET)) {
			setResultSet(new DummyResultSet(parseAffectedRows(removeMarkResultSet(sql))));
			return true;
		} else if (sql.startsWith(NO_RESULT)) {
			clearResults();
			return false;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return _resultSet;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return _updateCount;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getFetchSize() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getResultSetType() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		clearResults();
		_batchAffectedRows.add(DummyDataSource.parseAffectedRows(sql));
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		clearResults();
		return ArrayUtil.toIntArray(_batchAffectedRows);
	}

	@Override
	public Connection getConnection() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isClosed() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPoolable() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		throw new UnsupportedOperationException();
	}

}