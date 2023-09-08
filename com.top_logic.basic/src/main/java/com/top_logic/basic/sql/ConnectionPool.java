/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Preliminary interface for a connection pool.
 * 
 * <p>
 * Connections are represented by {@link PooledConnection} wrappers.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConnectionPool {

	/**
	 * The name of this {@link ConnectionPool} used for registration in the
	 * {@link ConnectionPoolRegistry}.
	 */
	String getName();

	/**
	 * Borrow a connection for update from the pool.
	 * 
	 * <p>
	 * When the connection is no longer used (e.g. after commit), the connection
	 * must be released by calling
	 * {@link #releaseWriteConnection(PooledConnection)}.
	 * </p>
	 * 
	 * <p>
	 * Note: This method must be exclusively used with the following pattern:
	 * </p>
	 * 
	 * <pre>
	 * connection = pool.borrowWriteConnection();
	 * try {
	 *    // Perform DB updates using the connection.
	 * } finally {
	 *    pool.releaseWriteConnection(connection);
	 * }
	 * </pre>
	 * 
	 * @return A connection for performing updates.
	 */
	PooledConnection borrowWriteConnection();

	/**
	 * Borrow a read connection.
	 * 
	 * <p>
	 * Note: The returned connection is read-only and must only be used for
	 * non-modifying statements only.
	 * </p>
	 * 
	 * <p>
	 * Note: The same thread must only borrow a single read connection at a
	 * time. It is illegal to execute code that itself borrows a read
	 * connection, while still holding a read connection.
	 * </p>
	 * 
	 * <p>
	 * When the connection is no longer used (after the lookup has completed),
	 * the connection must be released by calling
	 * {@link #releaseReadConnection(PooledConnection)}.
	 * </p>
	 * 
	 * <p>
	 * Note: This method must be exclusively used with the following pattern:
	 * </p>
	 * 
	 * <pre>
	 * connection = pool.borrowReadConnection();
	 * try {
	 * 	// Perform DB lookup using the connection.
	 * } finally {
	 * 	pool.releaseReadConnection(connection);
	 * }
	 * </pre>
	 * 
	 * @return A connection for performing database lookups.
	 */
	PooledConnection borrowReadConnection();

	/**
	 * Releases a connection borrowed through {@link #borrowReadConnection()}.
	 * 
	 * @param connection
	 *        The connection object returned from
	 *        {@link #borrowReadConnection()}.
	 */
	void releaseReadConnection(PooledConnection connection);

	/**
	 * Releases a connection borrowed through {@link #borrowWriteConnection()}.
	 * 
	 * @param connection
	 *        The connection object returned from
	 *        {@link #borrowWriteConnection()}.
	 */
	void releaseWriteConnection(PooledConnection connection);

	/**
	 * Alternative to {@link #releaseWriteConnection(PooledConnection)}, if
	 * the caller has detected a potential connection problem.
	 * 
	 * <p>
	 * Either {@link #releaseWriteConnection(PooledConnection)} or
	 * {@link #invalidateWriteConnection(PooledConnection)} must be called,
	 * never both.
	 * </p>
	 * 
	 * @param connection
	 *        The connection returned from {@link #borrowWriteConnection()}.
	 */
	void invalidateWriteConnection(PooledConnection connection);

	/**
	 * Alternative to {@link #releaseReadConnection(PooledConnection)}, if
	 * the caller has detected a potential connection problem.
	 * 
	 * <p>
	 * Either {@link #releaseReadConnection(PooledConnection)} or
	 * {@link #invalidateReadConnection(PooledConnection)} must be called,
	 * never both.
	 * </p>
	 * 
	 * @param connection
	 *        The connection returned from {@link #borrowReadConnection()}.
	 */
	void invalidateReadConnection(PooledConnection connection);

	/**
	 * Closes this pool and releases all resources.
	 * 
	 * <p>
	 * After this method has been called, no further calls to any methods of
	 * this pool are allowed.
	 * </p>
	 */
	void close();

	/**
	 * Clears all cached resources.
	 * 
	 * <p>
	 * After this operation, connections requested from the pool are guaranteed to be newly
	 * allocated from the underlying data source.
	 * </p>
	 */
	void clear();

	/**
	 * The {@link DataSource}, this pool takes connections from.
	 */
	DataSource getDataSource();
	
	
	/**
	 * Will help you to cope with the different flavors of SQL.
	 */
	DBHelper getSQLDialect() throws SQLException;

	/**
	 * Whether the configured pool is in "dry run" mode, it can actually not be used to connect to
	 * the database, e.g. because it is permanently unreachable.
	 * 
	 * @return <code>true</code> if this pool can not be used to connect to the database.
	 */
	boolean isDryRun();

}
