/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.top_logic.basic.sql.ResultSetProxy;

/**
 * {@link ResultSetProxy} that closes a given {@link Statement} after {@link #close()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StatementClosingResultSet extends ResultSetProxy {

	private final ResultSet _resultSetImpl;

	private final Statement _stmt;

	/**
	 * Creates a {@link StatementClosingResultSet}.
	 * 
	 * @param resultSetImpl
	 *        The actual wrapped {@link ResultSet}.
	 * @param stmt
	 *        A {@link Statement} to close when this {@link ResultSet} is closed.
	 */
	public StatementClosingResultSet(ResultSet resultSetImpl, Statement stmt) {
		_resultSetImpl = resultSetImpl;
		_stmt = stmt;
	}

	@Override
	protected ResultSet impl() {
		return _resultSetImpl;
	}

	@Override
	public void close() throws SQLException {
		try {
			super.close();
		} finally {
			_stmt.close();
		}
	}
}
