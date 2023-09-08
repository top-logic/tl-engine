/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * {@link ResultSet} without any rows.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyResultSet extends ResultSetProxy {

	private boolean _closed;

	@Override
	protected ResultSet impl() {
		throw new IllegalStateException("This result set does not have any rows.");
	}

	@Override
	public boolean next() throws SQLException {
		return false;
	}

	@Override
	public void close() {
		_closed = true;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return _closed;
	}

	/**
	 * This result has no warnings.
	 * 
	 * @see com.top_logic.basic.sql.ResultSetProxy#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/**
	 * No-op because this {@link ResultSet} never has warnings.
	 * 
	 * @see com.top_logic.basic.sql.ResultSetProxy#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		// nothing to clear here
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return false;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return false;
	}

	@Override
	public boolean isFirst() throws SQLException {
		return false;
	}

	@Override
	public boolean isLast() throws SQLException {
		return false;
	}

	@Override
	public void beforeFirst() throws SQLException {
		// no op
	}

	@Override
	public void afterLast() throws SQLException {
		// no op
	}

	@Override
	public boolean first() throws SQLException {
		return false;
	}

	@Override
	public boolean last() throws SQLException {
		return false;
	}

	@Override
	public int getRow() throws SQLException {
		return 0;
	}

	@Override
	public boolean absolute(int a1) throws SQLException {
		return false;
	}

	@Override
	public boolean relative(int a1) throws SQLException {
		return false;
	}

}

