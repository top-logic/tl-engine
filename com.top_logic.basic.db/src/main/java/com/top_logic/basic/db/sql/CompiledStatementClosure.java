/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.sql.DBHelper;

/**
 * {@link CompiledStatement} with some arguments already bound.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class CompiledStatementClosure implements CompiledStatement {

	/**
	 * Creates a {@link CompiledStatementClosure}.
	 * 
	 * <p>
	 * Provides the default implementation for {@link CompiledStatement#bind(Object...)}.
	 * </p>
	 * 
	 * @param impl
	 *        The delegate {@link CompiledStatement}.
	 * @param environment
	 *        The first arguments to bind.
	 * 
	 * @see CompiledStatement#bind(Object...)
	 */
	public static CompiledStatement bind(CompiledStatement impl, Object... environment) {
		return new CompiledStatementClosure(impl, environment);
	}

	private final CompiledStatement _impl;

	private Object[] _environment;

	/**
	 * @see #bind(CompiledStatement, Object...)
	 */
	private CompiledStatementClosure(CompiledStatement impl, Object... environment) {
		_impl = impl;
		_environment = environment;
	}

	@Override
	public String toString() {
		return _impl.toString(_environment);
	}

	@Override
	public String toString(Object[] arguments) {
		return _impl.toString(complete(arguments));
	}

	@Override
	public int executeUpdate(Connection connection, Object... arguments) throws SQLException {
		return _impl.executeUpdate(connection, complete(arguments));
	}

	@Override
	public ResultSet executeQuery(Connection connection, Object... arguments) throws SQLException {
		return _impl.executeQuery(connection, complete(arguments));
	}

	@Override
	public Batch createBatch(Connection connection) throws SQLException {
		return _impl.createBatch(connection);
	}

	@Override
	public void setResultSetConfiguration(int resultSetType, int resultSetConcurrency) {
		_impl.setResultSetConfiguration(resultSetType, resultSetConcurrency);
	}

	@Override
	public void setFetchSize(int size) {
		_impl.setFetchSize(size);
	}

	@Override
	public CompiledStatement bind(Object... environment) {
		_environment = complete(environment);
		return this;
	}

	private Object[] complete(Object... arguments) {
		return ArrayUtil.join(_environment, arguments);
	}

	@Override
	public DBHelper getSQLDialect() {
		return _impl.getSQLDialect();
	}

}