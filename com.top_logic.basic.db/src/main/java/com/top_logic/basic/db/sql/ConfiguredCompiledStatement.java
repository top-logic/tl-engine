/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
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


/**
 * Abstract implementation of {@link CompiledStatement} handling the configuration API.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredCompiledStatement implements CompiledStatement {

	private static final int IGNORE_FETCH_SIZE = 0;

	/**
	 * The SQL dialect used to produce this {@link CompiledStatement}.
	 */
	protected final DBHelper _sqlDialect;

	private int _resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private int _resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

	private int _fetchSize = IGNORE_FETCH_SIZE;

	/**
	 * Creates a new {@link ConfiguredCompiledStatement}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect of the used database.
	 */
	protected ConfiguredCompiledStatement(DBHelper sqlDialect) {
		_sqlDialect = sqlDialect;
	}

	@Override
	public void setResultSetConfiguration(int resultSetType, int resultSetConcurrency) {
		_resultSetType = resultSetType;
		_resultSetConcurrency = resultSetConcurrency;
	}

	/**
	 * Creates a {@link PreparedStatement} according to the configuration.
	 * 
	 * @param connection
	 *        The {@link Connection} to prepare statement.
	 * @param sql
	 *        The statement string.
	 */
	protected PreparedStatement prepareStatement(Connection connection, String sql) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(sql, _resultSetType, _resultSetConcurrency);
		setFetchSize(stmt);
		return stmt;
	}

	/**
	 * Creates a {@link Statement} according to the configuration.
	 * 
	 * @param connection
	 *        The {@link Connection} to create statement.
	 */
	protected Statement createStatement(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement(_resultSetType, _resultSetConcurrency);
		setFetchSize(stmt);
		return stmt;
	}

	/**
	 * Sets the fetch size to the given {@link Statement}.
	 * 
	 * <p>
	 * If {@link #_fetchSize} is {@value #IGNORE_FETCH_SIZE} nothing is done. The reason is that
	 * {@link Statement#setFetchSize(int)} ignores that value but
	 * {@link DBHelper#setFetchSize(Statement, int)} does not.
	 * </p>
	 */
	private void setFetchSize(Statement stmt) throws SQLException {
		if (_fetchSize != IGNORE_FETCH_SIZE) {
			_sqlDialect.setFetchSize(stmt, _fetchSize);
		}
	}

	@Override
	public void setFetchSize(int size) {
		_fetchSize = size;
	}

	@Override
	public DBHelper getSQLDialect() {
		return _sqlDialect;
	}

}

