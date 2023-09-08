/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.SQLH;

/**
 * {@link CompiledStatement} that bases on {@link PreparedStatement}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PrepStmtBasedCompiledStatement extends ConfiguredCompiledStatement {

	/**
	 * Creates a new {@link PrepStmtBasedCompiledStatement}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the used database.
	 */
	protected PrepStmtBasedCompiledStatement(DBHelper sqlDialect) {
		super(sqlDialect);
	}

	/**
	 * Fills the given arguments into the {@link PreparedStatement}
	 * 
	 * @param statement
	 *        the statement to fill
	 * @param arguments
	 *        the arguments to fill into the {@link PreparedStatement}
	 * 
	 * @throws SQLException
	 *         iff an error occurred.
	 */
	protected abstract void setArguments(PreparedStatement statement, Object[] arguments) throws SQLException;

	@Override
	public String toString() {
		return toString(ArrayUtil.EMPTY_ARRAY);
	}

	@Override
	public final ResultSet executeQuery(Connection connection, Object... arguments) throws SQLException {
		try {
			return tryExecuteQuery(connection, arguments);
		} catch (SQLException ex) {
			throw enhanceMessage(ex, arguments);
		}
	}

	private ResultSet tryExecuteQuery(Connection connection, Object... arguments) throws SQLException {
		boolean statementReturned = false;
		String sql = toString(arguments);
		final PreparedStatement stmt = prepareStatement(connection, sql);
		try {
			setArguments(stmt, arguments);
			final ResultSet resultSet = stmt.executeQuery();
			try {
				ResultSet finalResult = new StatementClosingResultSet(resultSet, stmt);
				statementReturned = true;
				return finalResult;
			} finally {
				if (!statementReturned) {
					resultSet.close();
				}
			}
		} finally {
			if (!statementReturned) {
				stmt.close();
			}
		}
	}

	@Override
	public final int executeUpdate(Connection connection, Object... arguments) throws SQLException {
		try {
			return tryExecuteUpdate(connection, arguments);
		} catch (SQLException ex) {
			throw enhanceMessage(ex, arguments);
		}
	}

	private int tryExecuteUpdate(Connection connection, Object... arguments) throws SQLException {
		PreparedStatement stmt = prepareStatement(connection, toString(arguments));
		try {
			setArguments(stmt, arguments);
			return stmt.executeUpdate();
		} finally {
			stmt.close();
		}
	}

	@Override
	public final Batch createBatch(Connection connection) throws SQLException {
		try {
			return tryCreateBatch(connection);
		} catch (SQLException ex) {
			throw enhanceMessage(ex, ArrayUtil.EMPTY_ARRAY);
		}
	}

	private Batch tryCreateBatch(Connection connection) throws SQLException {
		PreparedStatement stmt = prepareStatement(connection, toString(ArrayUtil.EMPTY_ARRAY));
		return new AbstractStatementBatch<PreparedStatement>(stmt) {

			@Override
			public void addBatch(Object... arguments) throws SQLException {
				try {
					setArguments(_statement, arguments);
					_statement.addBatch();
				} catch (SQLException ex) {
					throw enhanceMessage(ex, arguments);
				}
			}

			@Override
			public int[] executeBatch() throws SQLException {
				try {
					return super.executeBatch();
				} catch (SQLException ex) {
					throw enhanceMessage(ex, ArrayUtil.EMPTY_ARRAY);
				}
			}

		};
	}

	@Override
	public CompiledStatement bind(Object... environment) {
		return CompiledStatementClosure.bind(this, environment);
	}

	final SQLException enhanceMessage(SQLException ex, Object... arguments) {
		return SQLH.enhanceMessage(ex, toString(arguments));
	}

}

