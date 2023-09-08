/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.EmptyResultSet;

/**
 * {@link CompiledStatement} that represents a query with empty result.
 * 
 * <p>
 * This {@link CompiledStatement} is an statement representing a read query, i.e. the methods
 * {@link #executeUpdate(Connection, Object...)} and {@link #createBatch(Connection)} are not
 * supported.
 * </p>
 * 
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyCompiledStatement implements CompiledStatement {

	/** Singleton {@link EmptyCompiledStatement} instance. */
	public static final EmptyCompiledStatement INSTANCE = new EmptyCompiledStatement();

	private EmptyCompiledStatement() {
		// singleton instance
	}

	@Override
	public String toString(Object[] arguments) {
		return "EMPTY statement.";
	}

	@Override
	public ResultSet executeQuery(Connection connection, Object... arguments) throws SQLException {
		return new EmptyResultSet();
	}

	@Override
	public int executeUpdate(Connection connection, Object... arguments) throws SQLException {
		throw new SQLException("Empty CompiledStatement must not be used to update some rows.");
	}

	@Override
	public Batch createBatch(Connection connection) throws SQLException {
		throw new SQLException("Empty CompiledStatement must not be used to create batch updates.");
	}

	@Override
	public CompiledStatement bind(Object... environment) {
		return this;
	}

	@Override
	public void setResultSetConfiguration(int resultSetType, int resultSetConcurrency) {
		// ignore
	}

	@Override
	public void setFetchSize(int size) {
		// ignore
	}

	@Override
	public DBHelper getSQLDialect() {
		// Not database specific.
		return null;
	}

}

