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
import java.sql.Statement;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.SQLH;

/**
 * {@link CompiledStatement} which creates a statement directly without using a
 * {@link PreparedStatement}.
 * 
 * @see DirectStatementBuilder
 * @see DefaultCompiledStatementImpl compiled statement that creates prepared statements and inserts the
 *      argument that statement.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DirectCompiledStatement extends ConfiguredCompiledStatement {

	/**
	 * Creates a new {@link DirectCompiledStatement}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the used database.
	 */
	protected DirectCompiledStatement(DBHelper sqlDialect) {
		super(sqlDialect);
	}

	@Override
	public String toString() {
		return toString(new Object[] {});
	}

	@Override
	public ResultSet executeQuery(Connection connection, Object... arguments) throws SQLException {
		String source = toString(arguments);
		boolean statementReturned = false;
		final Statement stmt = createStatement(connection);
		try {
			final ResultSet resultSetImpl = stmt.executeQuery(source);
			try {
				ResultSet result = new StatementClosingResultSet(resultSetImpl, stmt);
				statementReturned = true;
				return result;
			} finally {
				if (!statementReturned) {
					resultSetImpl.close();
				}
			}
		} catch (SQLException ex) {
			throw enhanceMessage(ex, arguments);
		} finally {
			if (!statementReturned) {
				stmt.close();
			}
		}
	}

	@Override
	public int executeUpdate(Connection connection, Object... arguments) throws SQLException {
		String source = toString(arguments);
		try (Statement stmt = createStatement(connection)) {
			return stmt.executeUpdate(source);
		} catch (SQLException ex) {
			throw enhanceMessage(ex, arguments);
		}
	}

	@Override
	public Batch createBatch(Connection connection) throws SQLException {
		Statement stmt = createStatement(connection);
		return new AbstractStatementBatch<>(stmt) {

			@Override
			public void addBatch(Object... arguments) throws SQLException {
				String sql = DirectCompiledStatement.this.toString(arguments);
				try {
					_statement.addBatch(sql);
				} catch (SQLException ex) {
					throw enhanceMessage(ex, arguments);
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

