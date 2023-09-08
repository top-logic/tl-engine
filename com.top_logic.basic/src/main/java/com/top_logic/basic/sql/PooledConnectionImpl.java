/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPool.PooledConnectionFactory;

/**
 * the default implementation of {@link PooledConnection}.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
class PooledConnectionImpl extends ConnectionProxy implements PooledConnection {

	/**
	 * The {@link ConnectionPool} that created this {@link PooledConnection}.
	 * 
	 * <p>
	 * The {@link ConnectionPool#getDataSource()} is used to create actual {@link Connection}.
	 * </p>
	 */
	private final ConnectionPool _pool;

	/**
	 * The Connection currently used to created the PreparedStatements
	 */
	private Connection con;

	/**
	 * <b>true</b> if autoCommit should be activated, <b>false</b> if it
	 * shouldn't be activated.
	 */
	private final boolean autoCommit;

	/**
	 * Variable containing the transaction isolation level. Attention: the
	 * default value is <b>-1</b> and will be ignored.
	 */
	private final int transactionIsolationLevel;

	private final boolean readOnly;

	/**
	 * Whether this connection is currently owned by the application.
	 */
	private boolean _active;

	/**
	 * Create a {@link PooledConnectionImpl} for a given DataSource.
	 * 
	 * @param pool
	 *        The {@link ConnectionPool} that created this {@link PooledConnection}.
	 */
	public PooledConnectionImpl(ConnectionPool pool) {
		this(pool, -1);
	}

	/**
	 * Create a {@link PooledConnectionImpl} with a given TransactionIsolationLevel.
	 * 
	 * @param pool
	 *        The {@link ConnectionPool} that created this {@link PooledConnection}.
	 * @param aTransactionIsolationLevel
	 *        the desired transaction isolation level.
	 */
	public PooledConnectionImpl(ConnectionPool pool, int aTransactionIsolationLevel) {
		this(pool, aTransactionIsolationLevel, false);
	}

	/**
	 * Create a {@link PooledConnectionImpl} with a given TransactionIsolationLevel.
	 * 
	 * @param pool
	 *        The {@link ConnectionPool} that created this {@link PooledConnection}.
	 * @param aTransactionIsolationLevel
	 *        the desired transaction isolation level.
	 * @param autoCommit
	 *        if a commit shall be done after each statement
	 */
	public PooledConnectionImpl(ConnectionPool pool, int aTransactionIsolationLevel, boolean autoCommit) {
		this(pool, aTransactionIsolationLevel, autoCommit, false);
	}
	
	/**
	 * Create a {@link PooledConnectionImpl} with a given TransactionIsolationLevel.
	 * 
	 * @param pool
	 *        The {@link ConnectionPool} that created this {@link PooledConnection}.
	 * @param aTransactionIsolationLevel
	 *        the desired transaction isolation level.
	 * @param autoCommit
	 *        if a commit shall be done after each statement
	 */
	public PooledConnectionImpl(ConnectionPool pool, int aTransactionIsolationLevel, boolean autoCommit,
			boolean readOnly) {
		if (pool == null) {
			throw new NullPointerException("'pool' must not be 'null'.");
		}
		this._pool = pool;
		this.transactionIsolationLevel = aTransactionIsolationLevel;
		this.autoCommit = autoCommit;
		this.readOnly = readOnly;
	}

	/**
	 * Allow subclasses to ovverride (e.g. setAutocommit()) If the
	 * transactionIsolationLevel variable is set and the database implementation
	 * supports this level, the transaction isolation level is set for the
	 * returned connection
	 */
	private Connection createConnection() throws SQLException {
		Connection theCon = _pool.getDataSource().getConnection();
		theCon.setAutoCommit(autoCommit);
		theCon.setReadOnly(readOnly);

		// 27.04.04 changed by Alice Scheerer
		// If the transactionIsolationLevel variable is set
		// the transaction isolation level is set for the returned
		// connection
		if (transactionIsolationLevel > -1) {
			theCon.setTransactionIsolation(transactionIsolationLevel);
		}
		return theCon;
	}

	/**
	 * Makes this connection active for use by the application.
	 */
	void activate() {
		_active = true;
	}

	/**
	 * Deactivates this connection for use by the application.
	 */
	void deactivate() {
		_active = false;
	}

	/**
	 * Mark this connection being released to the pool.
	 */
	/* package protected */void cleanup() {
		deactivate();
		if (hasDBConnection()) {
			try {
				if (con.isClosed()) {
					con = null;
					return;
				}

				rollback(con);

				if (readOnly != con.isReadOnly()) {
					Logger.warn("Connection read-only state was changed illegaly.", PooledConnectionFactory.class);
					con.setReadOnly(readOnly);
				}

				if (autoCommit != con.getAutoCommit()) {
					Logger.warn("Connection auto-commit state was changed illegaly.", PooledConnectionFactory.class);
					con.setAutoCommit(autoCommit);
				}
			} catch (SQLException ex) {
				// Drop DB connection.
				con = null;

				Logger.warn("Connection cleanup failed.", ex, PooledConnectionFactory.class);
			}
		}
	}

	@Override
	public void closeConnection(Throwable aReason) {
		Connection dbConnection = null;

		synchronized (this) {
			if (this.con != null) {
				dbConnection = this.con;
				this.con = null;
			}
		}
		if (dbConnection == null) {
			return;
		}

		try {
			if (dbConnection.isClosed()) {
				return;
			}

			rollback(dbConnection);

			dbConnection.close();
		} // WAS may throw one of these ugly WorkRolledbackExceptions :-(
		catch (Exception allx) {
			Logger.info("Failed to closeConnection()", allx, PooledConnectionImpl.class);
		}
	}

	/**
	 * Safely roll-back the given {@link Connection}.
	 * 
	 * <p>
	 * Note: A {@link Connection} cannot be rolled back, if it is either read-only, or in auto
	 * commit mode.
	 * </p>
	 * 
	 * @param connection
	 *        The {@link Connection} to roll back.
	 * @throws SQLException
	 *         If access to the connection fails.
	 */
	private static void rollback(Connection connection) throws SQLException {
		if (!connection.isReadOnly() && !connection.getAutoCommit()) {
			// Note: E.g. Oracle treats a close() call on its connection as an
			// implicit commit. Therefore, the transaction must be rolled back
			// before the connection can be closed.
			connection.rollback();
		}
	}

	@Override
	protected Connection impl() throws SQLException {
		if (!_active) {
			throw new SQLException("Access to released connection.");
		}
		return internalGetConnection();
	}
	
	/**
	 * returns the current connection, or create a new in case there is none.
	 * 
	 * @throws SQLException
	 *         when creating connection fails
	 */
	private Connection internalGetConnection() throws SQLException {
		if (!hasDBConnection()) {
			try {
				con = createConnection();
			} catch (SQLException sqlx) {
				closeConnection(sqlx);
				throw sqlx;
			}
		}
		return con;
	}

	/* package protected */final boolean hasDBConnection() {
		return con != null;
	}

	@Override
	public ConnectionPool getPool() {
		return _pool;
	}

	@Override
	public DBHelper getSQLDialect() throws SQLException {
		return _pool.getSQLDialect();
	}

	@Override
	public void commit() throws SQLException {
		if (!hasDBConnection()) {
			return;
		}

		try {
			con.commit();
		} catch (SQLException ex) {
			SQLH.logWarnings(this);
			throw ex;
		}
	}

	@Override
	public void rollback() throws SQLException {
		if (!hasDBConnection()) {
			return;
		}

		try {
			con.rollback();
		} catch (SQLException ex) {
			SQLH.logWarnings(this);
			throw ex;
		}
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		if (hasDBConnection()) {
			return con.getAutoCommit();
		} else {
			return autoCommit;
		}
	}
	
	@Override
	public void setAutoCommit(boolean value) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isReadOnly() throws SQLException {
		if (hasDBConnection()) {
			return con.isReadOnly();
		} else {
			return readOnly;
		}
	}

	@Override
	public void setReadOnly(boolean value) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearWarnings() throws SQLException {
		if (hasDBConnection()) {
			con.clearWarnings();
		}
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		if (hasDBConnection()) {
			return con.getWarnings();
		} else {
			return null;
		}
	}

	@Override
	public void close() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		if (hasDBConnection()) {
			con.releaseSavepoint(savepoint);
		}
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		if (hasDBConnection()) {
			con.rollback(savepoint);
		}
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return impl().setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return impl().setSavepoint(name);
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

}
