/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.top_logic.basic.sql.DBHelper;

/**
 * A SQL statement which knows the actual source to create and execute {@link Statement} with given
 * arguments.
 * 
 * @see SQLQuery#toSql(com.top_logic.basic.sql.DBHelper)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompiledStatement {

	/**
	 * Creates a string representation of the statement, assuming the given arguments would be given
	 */
	String toString(Object[] arguments);

	/**
	 * Executes this compiled statement with the given parameters
	 * 
	 * @param connection
	 *        the connection to the database
	 * @param arguments
	 *        The arguments for the statement, see
	 *        {@link SQLFactory#query(java.util.List, SQLStatement)}.
	 * 
	 * @return the result of the query
	 * 
	 * @throws SQLException
	 *         if a database access error occurs
	 * 
	 * @see Statement#executeQuery(String)
	 */
	ResultSet executeQuery(Connection connection, Object... arguments) throws SQLException;

	/**
	 * Executes this compiled statement with the given parameters
	 * 
	 * @param connection
	 *        the connection to the database
	 * @param arguments
	 *        The arguments for the statement, see
	 *        {@link SQLFactory#query(java.util.List, SQLStatement)}.
	 * 
	 * @return the result of the query
	 * 
	 * @throws SQLException
	 *         if a database access error occurs
	 * 
	 * @see Statement#executeUpdate(String)
	 */
	int executeUpdate(Connection connection, Object... arguments) throws SQLException;

	/**
	 * Creates a batch object to execute batch updates
	 * 
	 * @param connection
	 *        the connection to the database
	 * 
	 * @return a {@link Batch} object to execute batch updates
	 * 
	 * @throws SQLException
	 *         if a database access error occurs
	 */
	Batch createBatch(Connection connection) throws SQLException;
	
	/**
	 * Creates a {@link CompiledStatement} closure with the given arguments already bound.
	 * 
	 * @param environment
	 *        The first arguments to bind.
	 */
	CompiledStatement bind(Object... environment);
	
	/**
	 * Sets the type and concurrency of the {@link ResultSet} returned by this
	 * {@link CompiledStatement}.
	 * 
	 * @param resultSetType
	 *        One of {@link ResultSet#TYPE_FORWARD_ONLY}, {@link ResultSet#TYPE_SCROLL_INSENSITIVE},
	 *        or {@link ResultSet#TYPE_SCROLL_SENSITIVE}.
	 * @param resultSetConcurrency
	 *        One of {@link ResultSet#CONCUR_READ_ONLY} or {@link ResultSet#CONCUR_UPDATABLE}.
	 * 
	 * @see #executeQuery(Connection, Object...)
	 * @see Connection#createStatement(int, int)
	 * @see Connection#prepareStatement(String, int, int)
	 */
	void setResultSetConfiguration(int resultSetType, int resultSetConcurrency);

	/**
	 * Sets the given fetch size for statements.
	 * 
	 * <p>
	 * Setting the fetch size allows to read the statements result in streaming mode preventing to
	 * load all result data into memory.
	 * </p>
	 * 
	 * @param size
	 *        The chunk size.
	 */
	void setFetchSize(int size);

	/**
	 * The SQL dialect of the database for which this statement can be used.
	 * 
	 * @return May be <code>null</code>, which means that this {@link CompiledStatement} is
	 *         compatible to all databases.
	 */
	DBHelper getSQLDialect();

}
