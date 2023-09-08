/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base class for {@link QueryResult} implementations based on a
 * {@link ResultSet}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResultSetWrapper implements QueryResult {

	/**
	 * The (optional) {@link Statement} that produces the {@link #resultSet}.
	 */
	private final Statement statement;
	
	/**
	 * The wrapped {@link ResultSet}.
	 */
	protected final ResultSet resultSet;

	/**
	 * Creates a {@link ResultSetWrapper} for the given {@link ResultSet}.
	 * 
	 * <p>
	 * The given result set is closed, if this {@link ResultSetWrapper} is
	 * closed.
	 * </p>
	 * 
	 * @param resultSet
	 *        The {@link ResultSet} to wrap.
	 */
	public ResultSetWrapper(ResultSet resultSet) {
		this(null, resultSet);
	}

	/**
	 * Creates a {@link ResultSetWrapper} on the query result of the given
	 * statement.
	 * 
	 * <p>
	 * The given statement is closed, if this {@link ResultSetWrapper} is
	 * closed.
	 * </p>
	 * 
	 * @param statement
	 *        The statement to execute a query on.
	 */
	public ResultSetWrapper(PreparedStatement statement) throws SQLException {
		this(statement, statement.executeQuery());
	}

	/**
	 * Creates a {@link ResultSetWrapper}.
	 *
	 * @param statement
	 *        The statement the given query was executed on.
	 * @param resultSet
	 *        The {@link ResultSet} to wrap.
	 */
	public ResultSetWrapper(Statement statement, ResultSet resultSet) {
		this.statement = statement;
		this.resultSet = resultSet;
	}

	@Override
	public boolean next() throws SQLException {
		return resultSet.next();
	}
	
	@Override
	public void close() throws SQLException {
		try {
			resultSet.close();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
}
