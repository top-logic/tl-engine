/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Proxy implementation for the {@link ConnectionPool} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConnectionPoolAdapter implements ConnectionPool {

	/** The {@link ConnectionPool} this adapter delegates to. */
	private final ConnectionPool _impl;

	/**
	 * Creates a {@link ConnectionPoolAdapter}.
	 * 
	 * @param impl
	 *        The {@link ConnectionPool} to delegate to.
	 */
	public ConnectionPoolAdapter(ConnectionPool impl) {
		_impl = impl;
	}

	@Override
	public String getName() {
		return _impl.getName();
	}

	@Override
	public DataSource getDataSource() {
		return _impl.getDataSource();
	}

	@Override
	public DBHelper getSQLDialect() throws SQLException {
		return _impl.getSQLDialect();
	}

	@Override
	public PooledConnection borrowReadConnection() {
		return _impl.borrowReadConnection();
	}

	@Override
	public void releaseReadConnection(PooledConnection connection) {
		_impl.releaseReadConnection(connection);
	}

	@Override
	public void invalidateReadConnection(PooledConnection connection) {
		_impl.invalidateReadConnection(connection);
	}

	@Override
	public PooledConnection borrowWriteConnection() {
		return _impl.borrowWriteConnection();
	}

	@Override
	public void releaseWriteConnection(PooledConnection connection) {
		_impl.releaseWriteConnection(connection);
	}

	@Override
	public void invalidateWriteConnection(PooledConnection connection) {
		_impl.invalidateWriteConnection(connection);
	}

	@Override
	public void clear() {
		_impl.clear();
	}

	@Override
	public void close() {
		_impl.close();
	}

	@Override
	public boolean isDryRun() {
		return _impl.isDryRun();
	}

}
