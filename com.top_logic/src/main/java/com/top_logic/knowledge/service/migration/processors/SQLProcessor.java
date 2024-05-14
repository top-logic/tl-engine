/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.sql.PooledConnection;

/**
 * Utility for directly executing SQL generated with {@link SQLFactory}.
 */
public class SQLProcessor {

	private PooledConnection _connection;

	/**
	 * Creates a {@link SQLProcessor}.
	 */
	public SQLProcessor(PooledConnection connection) {
		_connection = connection;
	}

	/**
	 * Executes the given query and returns a single value.
	 * 
	 * <p>
	 * The query is expected to select only a single column. It is expected that exactly one row is
	 * returned.
	 * </p>
	 */
	public <T> T querySingleValue(SQLSelect select, Class<T> type, Object... arguments) throws SQLException {
		return CollectionUtil.getSingleValueFromCollection(queryValues(select, type, arguments));
	}

	/**
	 * Executes the given query and returns a list of single values.
	 * 
	 * <p>
	 * The query is expected to select only a single column.
	 * </p>
	 */
	public <T> List<T> queryValues(SQLSelect select, Class<T> type, Object... arguments) throws SQLException {
		List<T> result = new ArrayList<>();
		try (ResultSet resultSet = queryResultSet(select, arguments)) {
			while (resultSet.next()) {
				T value = resultSet.getObject(1, type);
				result.add(value);
			}
		}
		return result;
	}

	/**
	 * Executes the given query.
	 * 
	 * @return The {@link ResultSet} produced be the query.
	 */
	public ResultSet queryResultSet(SQLSelect select, Object... arguments) throws SQLException {
		CompiledStatement sql = compile(select);
		ResultSet resultSet = sql.executeQuery(_connection, arguments);
		return resultSet;
	}

	/**
	 * Executes the given statement.
	 * 
	 * @return The number of affected rows.
	 */
	public int execute(SQLStatement statement, Object... arguments) throws SQLException {
		CompiledStatement sql = compile(statement);
		return sql.executeUpdate(_connection, arguments);
	}

	private CompiledStatement compile(SQLStatement statement) throws SQLException {
		SQLQuery<SQLStatement> query = SQLFactory.query(statement);
		CompiledStatement sql = query.toSql(_connection.getSQLDialect());
		return sql;
	}

}
